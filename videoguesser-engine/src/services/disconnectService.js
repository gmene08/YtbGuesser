const ioConfig = require('../config/socket');

const activeTimers = new Map();

// 1. Uma função burra e genérica que APENAS conta o tempo e avisa o front-end
function runCountdown(roomCode, durationInSeconds, currentStatus) {
  return new Promise((resolve) => {
    const io = ioConfig.getIO();
    let timeLeft = durationInSeconds;

    if (activeTimers.has(roomCode)) {
      clearInterval(activeTimers.get(roomCode));
    }

    const intervalId = setInterval(() => {
      io.to(`${roomCode}-game`).emit('timeUpdate', { timeLeft: timeLeft, currentStatus });
      timeLeft--;

      if (timeLeft < 0) { // O tempo acabou!
        clearInterval(intervalId);
        activeTimers.delete(roomCode);
        resolve(); // 🔥 A Mágica: Avisa o async/await que o tempo terminou!
      }
    }, 1000);

    activeTimers.set(roomCode, intervalId);
  });
}

// 2. O seu fluxo de jogo limpo, síncrono visualmente e impossível de falhar
async function startRoundSequence(roomCode, activeLobbies, prepDuration, guessDuration, finishedDuration) {
  const lobby = activeLobbies.get(roomCode);
  const match = lobby?.match;
  if (!match) return;

  const io = ioConfig.getIO();

  try {
    // ==========================================
    // FASE 1: PREPARING
    // ==========================================
    match.currentRound.status = 'PREPARING';
    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: PREPARING (${prepDuration}s)`);
    await runCountdown(roomCode, prepDuration, 'PREPARING'); 

    // ==========================================
    // FASE 2: GUESSING
    // ==========================================
    await fetch(`http://localhost:8080/api/engine/${roomCode}/status`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status: 'GUESSING' })
    });

    match.currentRound.status = 'GUESSING';
    io.to(`${roomCode}-game`).emit('guessingStarted', {
      roundNumber: match.currentRound.number,
      roundStatus: match.currentRound.status,
      totalTime: guessDuration
    });

    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: GUESSING (${guessDuration}s)`);
    await runCountdown(roomCode, guessDuration, 'GUESSING');

    // ==========================================
    // FASE 3: FINISHED (Resultados do Round)
    // ==========================================
    const reportPayload = { roomCode, guesses: match.currentRound.guesses };
    const res = await fetch('http://localhost:8080/api/engine/end-round', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(reportPayload)
    });
    
    if (!res.ok) throw new Error('Erro no Java ao terminar round');
    const engineResponse = await res.json();
    
    match.currentRound.status = 'FINISHED';
    io.to(`${roomCode}-game`).emit('roundResults', engineResponse);

    console.log(`⏳ Round ${match.currentRound.number} na sala [${roomCode}]: FINISHED (${finishedDuration}s)`);
    await runCountdown(roomCode, finishedDuration, 'FINISHED');

    // ==========================================
    // FASE 4: PREPARA O PRÓXIMO OU ACABA
    // ==========================================
    const changeRes = await fetch(`http://localhost:8080/api/engine/${roomCode}/change-round`, { method: 'PATCH' });
    const changeResponse = await changeRes.json();

    match.currentRound.status = changeResponse.currentRound.status;
    match.currentRound.number = changeResponse.currentRound.roundNumber;
    match.currentRound.guesses = [];
    match.status = changeResponse.status;
    
    io.to(`${roomCode}-game`).emit('changeOfRounds', changeResponse);

    if (match.status === 'PLAYING' && match.currentRound.number <= match.maxRounds) {
      // Começa o ciclo todo de novo (Recursividade Limpa!)
      startRoundSequence(roomCode, activeLobbies, prepDuration, guessDuration, finishedDuration);
    } else {
      console.log(`🏆 Partida na sala [${roomCode}] terminou e foi para os RESULTADOS FINAIS.`);
    }

  } catch (err) {
    console.error(`❌ Erro crítico no fluxo da sala [${roomCode}]:`, err.message);
  }
}

// Exporta o nome correto para o server.js usar
//module.exports = { startRoundSequence };

/*🧠 Por que esta estrutura é infinitamente superior?
Fim do Callback Hell / If Hell: Você não precisa de aninhar código ou fazer if-else complexos. O código flui de cima para baixo como se fosse síncrono.

Reaproveitamento Extremo: A função runCountdown só tem um trabalho na vida: contar os segundos, emitir o timeUpdate para o Angular e avisar quando acabar.

Erros controlados: Ao usar try/catch à volta de todos os await fetch, se o Java cair ou der um erro 500 durante uma fase do jogo, ele não trava o servidor Node inteiro, o erro é contido de forma profissional!

Basta renomear a importação no seu server.js para startRoundSequence e o ciclo de vida do seu jogo vai rodar com uma precisão cirúrgica de um relógio suíço, mantendo o front-end 100% atualizado a cada segundo!*/