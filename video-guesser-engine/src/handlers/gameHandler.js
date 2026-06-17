const { addActivityLog, getActivityLogs } = require('../services/logService');
const { forceEndCountdown } = require('../services/timerService');

module.exports = (io, socket, activeLobbies) => {
  
  socket.on('joinGameRoom', async ({ roomCode }) => {
        const gameConnectionName = `${roomCode}-game`
        socket.join(gameConnectionName);
        socket.roomCode = roomCode;
        console.log(`👤 Jogador ${socket.nickname} entrou na PARTIDA ativa [${roomCode}]`);
        
        const historyLogs = await getActivityLogs(roomCode);
        io.to(`${roomCode}-game`).emit('logHistory', historyLogs); // Envia os logs para o jogador que acabou de entrar

        const lobby = activeLobbies.get(roomCode);
        if(lobby && lobby.match){
          // Ao entrar na sala de jogo, o jogador recebe o estado atual da partida, incluindo quais jogadores já chutaram naquele round. Isso é importante porque essa informacao nao e guardada no Java(backend), entao precisamos reconstruir essa info a partir dos palpites registrados no match da lobby.
          const syncData = {
            ...lobby.match,
            currentRound: {
              ...lobby.match.currentRound,
              playersWhoGuessed: lobby.match.currentRound.guesses.map(g => g.userId)
            }
          };
          socket.emit('syncMatchState', syncData);
        }
  });

  socket.on('leaveGameRoom', ({ roomCode }) => {
      const gameConnectionName = `${roomCode}-game`;
      socket.leave(gameConnectionName); // Sai apenas do canal de jogo
      console.log(`🚪 Jogador ${socket.nickname} saiu dos eventos da PARTIDA [${roomCode}]`);

      // Como apenas o lobby contem os jogadores que estao na sala, e o front end mostra a leaderboard que e um campo de match, o lobbyUpdate nao atualiza a leaderboard, entao precisamos emitir um evento específico para atualizar a leaderboard quando um jogador sai durante a partida. O ideal seria refatorar isso futuramente para ter uma fonte de verdade única, mas por enquanto vamos manter a estrutura atual e emitir esse evento específico.
      io.to(gameConnectionName).emit('playerLeftGame', { 
        userId: socket.userId, 
        nickname: socket.nickname 
      });
  });

  // Quando o jogador envia um palpite (Guess)
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
        
        // Se todos os jogadores (considerando os desconectados) já chutaram, podemos finalizar o round antecipadamente
        if(match.currentRound.guesses.length >= lobby.players.length - lobby.playersWhoDisconnected.length){
          console.log(`⏱️ Todos os jogadores chutaram na sala [${roomCode}]! Finalizando o round...`);
          forceEndCountdown(roomCode);
        }
      }
    } else{
      console.warn(`🚨 Tentativa de palpite rejeitada! O jogador ${socket.userId} não pertence à sala [${roomCode}].`);
    }
  });
};