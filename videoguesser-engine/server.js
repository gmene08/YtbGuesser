const express = require('express');
const http = require('http');
const cors = require('cors');
const socketConfig = require('./src/config/socket');
const { startRoundTimer } = require('./src/services/timerService');

const app = express();
const server = http.createServer(app);


const activeLobbies = new Map();
server.activeLobbies = activeLobbies;

// Inicializa o Socket.io passando o servidor HTTP
socketConfig.init(server);

app.use(cors({ origin: 'http://localhost:4200', credentials: true }));
app.use(express.json());

// Endpoint Rest que o Java chama quando o Dono clica em Start Game
app.post('/api/engine/start-match', (req, res) => {
  const { roomCode, players, maxRounds } = req.body;

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

  setTimeout(() => {
    const match = activeLobbies.get(roomCode)?.match;
    if (match && match.currentRound.status === 'PREPARING') {
      startRoundTimer(roomCode, activeLobbies, 5);
    }
  }, 5000);
  
  return res.status(200).json({ message: 'Engine assumiu controle.' });
});

app.post('/api/engine/lobby/update', (req,res)=>{
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

app.delete('/api/engine/lobby/:code', (req, res) => {
  const roomCode = req.params.code;

  if (activeLobbies.has(roomCode)) {
    activeLobbies.delete(roomCode);
    console.log(`🧼 Faxina REST: Sala [${roomCode}] foi explicitamente DELETADA da memória RAM.`);
  }

  return res.status(200).json({ message: 'Sala removida com sucesso do motor.' });
});

app.get('/health', (req, res) => res.send('Motor rodando liso e fatorado!'));

const PORT = 3000;
server.listen(PORT, () => console.log(`🚀 Engine rodando na porta ${PORT}`));