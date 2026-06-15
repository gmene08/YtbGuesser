const { addActivityLog, getActivityLogs } = require('../services/logService');

module.exports = (io, socket, activeLobbies) => {
  // Quando o jogador envia um palpite (Guess)
  socket.on('joinGameRoom', async ({ roomCode }) => {
        const gameConnectionName = `${roomCode}-game`
        socket.join(gameConnectionName);
        socket.roomCode = roomCode;
        console.log(`👤 Jogador ${socket.nickname} entrou na PARTIDA ativa [${roomCode}]`);
        
        const historyLogs = await getActivityLogs(roomCode);
        io.to(`${roomCode}-game`).emit('logHistory', historyLogs); // Envia os logs para o jogador que acabou de entrar
  });

  socket.on('leaveGameRoom', ({ roomCode }) => {
      const gameConnectionName = `${roomCode}-game`;
      socket.leave(gameConnectionName); // Sai apenas do canal de jogo
      console.log(`🚪 Jogador ${socket.nickname} saiu dos eventos da PARTIDA [${roomCode}]`);
  });
  
  socket.on('submitGuess', async ({ roomCode, guessValue }) => {
    console.log(`🎯 Palpite recebido! Sala: ${roomCode} | Jogador: ${socket.userId} | Chutou: ${guessValue}`);

    const lobby = activeLobbies.get(roomCode);
    const isPlayerInLobby = lobby?.players.some(p => p.userId === socket.userId);
    if(isPlayerInLobby){
      const match = lobby.match;
      if (match && match.currentRound.status === 'GUESSING') {
        const alreadyGuessed = match.currentRound.guesses.some(g => g.userId === socket.userId);
        if (!alreadyGuessed) {
          match.currentRound.guesses.push({ userId: socket.userId, guessValue });
          io.to(`${roomCode}-game`).emit('playerGuessed', { userId: socket.userId });
          
          await addActivityLog(roomCode, io, 'GUESS', `Player ${socket.nickname} guessed!`);
        }
      }
    } else{
      console.warn(`🚨 Tentativa de palpite rejeitada! O jogador ${socket.userId} não pertence à sala [${roomCode}].`);
    }
  });
};