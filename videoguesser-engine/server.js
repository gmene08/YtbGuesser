const express = require('express');
const http = require('http');
const cors = require('cors');
const socketConfig = require('./src/config/socket');
const { startRoundTimer, startRoundSequence } = require('./src/services/timerService');
const { connectRedis } = require('./src/config/redis');
const { clearActivityLogs } = require('./src/services/logService');



const app = express();
const server = http.createServer(app);



const activeLobbies = new Map();
server.activeLobbies = activeLobbies;
server.keepAliveTimeout = 60000; // 60 segundos para evitar timeouts prematuros
server.headersTimeout = 61000;

const JAVA_BACKEND_URL = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';
const FRONTEND_URL = process.env.FRONTEND_URL || 'http://localhost:4200';

// Inicializa o Socket.io passando o servidor HTTP
socketConfig.init(server);

app.use(cors({ origin: FRONTEND_URL, credentials: true }));
app.use(express.json());

// Endpoint Rest que o Java chama quando o Dono clica em Start Game
app.post('/api/engine/lobby/:roomCode/match', (req, res) => {
  const { maxRounds, players, prepDurationSeconds, guessingDurationSeconds, finishedDurationSeconds } = req.body;
  const roomCode = req.params.roomCode;

  if (!roomCode || !players) {
    return res.status(400).json({ error: 'Dados insuficientes.' });
  }

  const lobby = activeLobbies.get(roomCode)
  if(lobby){
    lobby.match = {
      maxRounds,
      currentRound: {
        number:1,
        status:'PREPARING',
        guesses: []
      },
      status: 'PLAYING',
    }
  }

  console.log(`🎮 Partida criada na Engine para a sala [${roomCode}]`);

  startRoundSequence(roomCode, activeLobbies, prepDurationSeconds, guessingDurationSeconds, finishedDurationSeconds);
   
  return res.status(200).json({ message: 'Engine assumiu controle.' });
});

app.delete('/api/engine/lobby/:roomCode/match', async (req, res) => {
  const roomCode = req.params.roomCode;

  const lobby = activeLobbies.get(roomCode);
  if (lobby) {
    lobby.match = null;
    console.log(`🧹 Partida da sala [${roomCode}] foi DELETADA da memória RAM.`);
  }

  await clearActivityLogs(roomCode);
  const io =socketConfig.getIO();
  io.to(`${roomCode}-game`).emit('gameEnded');
  return res.status(200).json({ message: 'Partida removida com sucesso do motor.' });
})

app.put('/api/engine/lobby/:roomCode', (req,res)=>{
  const {code, status, maxPlayers, ownerId, players} = req.body;

  if(!code){
    return res.status(400).json({ error: 'Código da sala é obrigatório.' });
  }
  
  const currentMatchState = activeLobbies.get(code) ? activeLobbies.get(code).match : null
  const currentPlayersWhoDisconnected = activeLobbies.get(code) ? activeLobbies.get(code).playersWhoDisconnected : [];

 const updatedLobby = {
    code: code,

    status: status,
    maxPlayers,
    players: players || [],
    playersWhoDisconnected: currentPlayersWhoDisconnected,
    ownerId,
    currentPlayers: players ? players.length : 0,

    match: currentMatchState
 }

  activeLobbies.set(code, updatedLobby);

  const io =socketConfig.getIO();
  io.to(`${code}-lobby`).emit('lobbyUpdate', updatedLobby);

  console.log(`📢 Lobby [${code}] atualizado via REST do Java e transmitido aos sockets.`);
  return res.status(200).json({ message: 'Lobby atualizado com sucesso.' });

})

app.delete('/api/engine/lobby/:code', async (req, res) => {
  const roomCode = req.params.code;

  if (activeLobbies.has(roomCode)) {
    activeLobbies.delete(roomCode);
    console.log(`🧼 Faxina REST: Sala [${roomCode}] foi explicitamente DELETADA da memória RAM.`);
  }

  await clearActivityLogs(roomCode);
  return res.status(200).json({ message: 'Sala removida com sucesso do motor.' });
});

app.get('/health', (req, res) => res.send('Motor rodando liso e fatorado!'));

connectRedis().then(() => {
  const PORT = 3000;
  server.listen(PORT, () => console.log(`🚀 Engine rodando na porta ${PORT}`));
});
