const {redisClient} = require('../config/redis');
const {addActivityLog} = require('../services/logService');

const activeTimers = new Map();

const JAVA_BACKEND_URL = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';

function runCountdown(roomCode, durationInSeconds, currentStatus) {
  return new Promise((resolve) => {
    const ioConfig = require('../config/socket'); // Importa aqui para evitar dependência circular  
    const io = ioConfig.getIO();

    let timeLeft = durationInSeconds;

    if (activeTimers.has(roomCode)) {
      clearInterval(activeTimers.get(roomCode).intervalId);
    }

    io.to(`${roomCode}-game`).emit('timeUpdate', { timeLeft: timeLeft, currentStatus });

    const intervalId = setInterval(() => {
      timeLeft--;
      if (timeLeft < 0) {
        clearInterval(intervalId);
        activeTimers.delete(roomCode);
        resolve();
      } else {
        io.to(`${roomCode}-game`).emit('timeUpdate', { timeLeft: timeLeft, currentStatus });
      }
    }, 1000);

    activeTimers.set(roomCode, { intervalId, resolve });
  })
}

function forceEndCountdown(roomCode) {
  const timerData = activeTimers.get(roomCode);
  if(timerData){
    clearInterval(timerData.intervalId);
    activeTimers.delete(roomCode);
    timerData.resolve();
  }
}

async function startRoundSequence(roomCode, activeLobbies, prepDurationSeconds, guessingDurationSeconds, finishedDurationSeconds) {
  const ioConfig = require('../config/socket');
  
  const lobby = activeLobbies.get(roomCode);
  const match = lobby?.match;
  const io = ioConfig.getIO();

  if (!match) return;

  try {
    // === TIMER DE PREPARAÇÃO ===
    match.currentRound.status = 'PREPARING';
    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: PREPARING (${prepDurationSeconds}s)`);
    await addActivityLog(roomCode, io, 'SYSTEM', `Round ${match.currentRound.number} is about to start.`);
    await runCountdown(roomCode, prepDurationSeconds, 'PREPARING');

    // === MUDANÇA PARA GUESSING ===
    await fetch(`${JAVA_BACKEND_URL}/api/engine/${roomCode}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'GUESSING' })
    });
    match.currentRound.status = 'GUESSING';

    io.to(`${roomCode}-game`).emit('guessingStarted', {
      roundNumber: match.currentRound.number,
      roundStatus: match.currentRound.status,
      totalTime: guessingDurationSeconds
    
    });

    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: GUESSING (${guessingDurationSeconds}s)`);
    await addActivityLog(roomCode, io, 'SYSTEM', `Round ${match.currentRound.number} has started! Players can now submit their guesses.`);
    // === TIMER DE GUESSING ===
    await runCountdown(roomCode, guessingDurationSeconds, 'GUESSING');

    // === MUDANÇA PARA FINISHED ===
    const reportPayload = { guesses: match.currentRound.guesses };
    const res = await fetch(`${JAVA_BACKEND_URL}/api/engine/${roomCode}/end-round`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reportPayload)
    });
    if (!res.ok) throw new Error('Erro no Java ao terminar round');
    const engineResponse = await res.json();
    match.currentRound.status = 'FINISHED';

    io.to(`${roomCode}-game`).emit('roundResults', engineResponse);
    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: FINISHED (${finishedDurationSeconds}s)`);

    await addActivityLog(roomCode, io, 'SYSTEM', `Round ${match.currentRound.number} has finished.`);

    const roundDetails = engineResponse.currentRound?.roundDetails || {};
    const playersScore = roundDetails.playersScore || [];

    for (const result of playersScore) {
      const logMessage = `${result.nickname} Guessed ${result.lastGuess} views and scored ${result.pointsScored} points!`;
      
      await addActivityLog(roomCode, io, 'RESULT', logMessage);
    }

    // === TIMER DE FINISHED ===
    await runCountdown(roomCode, finishedDurationSeconds, 'FINISHED');

    // === MUDANÇA PARA PRÓXIMO ROUND OU FIM DE JOGO ===
    const changeRes = await fetch(`${JAVA_BACKEND_URL}/api/engine/${roomCode}/change-round`, { method: 'PATCH' });
    if (!changeRes.ok) throw new Error('Erro no Java ao mudar round');
    const changeResponse = await changeRes.json();

    match.currentRound.status = changeResponse.currentRound.status;
    match.currentRound.number = changeResponse.currentRound.roundNumber;
    match.currentRound.guesses = [];
    match.status = changeResponse.status;

    io.to(`${roomCode}-game`).emit('changeOfRounds', changeResponse);

    if (match.status === 'PLAYING') {
      console.log(`⏳ Sala [${roomCode}] mudou para round ${match.currentRound.number}!`);

      if (match.currentRound.number <= match.maxRounds) {
        startRoundSequence(roomCode, activeLobbies, prepDurationSeconds, guessingDurationSeconds, finishedDurationSeconds);
      }
    } else {
      await addActivityLog(roomCode, io, 'SYSTEM', `Game has ended after round ${match.currentRound.number}.`);
      console.log(`🏁 Jogo na sala [${roomCode}] terminou!`);
    }
  }
  catch (err) {
    console.error(`❌ Erro ao iniciar round:`, err.message);
    return;
  }

}

module.exports = { startRoundSequence, forceEndCountdown };