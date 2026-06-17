const { Server } = require('socket.io');
const cookie = require('cookie');
const jwt = require('jsonwebtoken');
require('dotenv').config();

const lobbyHandler = require('../handlers/lobbyHandler');
const gameHandler = require('../handlers/gameHandler');

let io;

function init(server) {
  io = new Server(server, {
    cors: {
      origin: process.env.FRONTEND_URL || 'http://localhost:4200',
      methods: ['GET', 'POST'],
      credentials: true
    }
  });

  // Middleware de Autenticação por Cookie
  io.use((socket, next) => {
    try {
      const cookies = cookie.parse(socket.handshake.headers.cookie || '');
      const token = cookies['auth_token'];

      if (!token) return next(new Error('Acesso negado: Sem Token'));

      const decoded = jwt.verify(token, process.env.JWT_SECRET);
      socket.userId = parseInt(decoded.sub);
      socket.nickname = decoded.nickname;
      next();
    } catch (err) {
      return next(new Error('Acesso negado: Token inválido'));
    }
  });

  // Ativa os ouvidores de eventos
  // Passamos o mapa global 'activeMatches' para os handlers manipularem
  const activeLobbies = server.activeLobbies;

  io.on('connection', (socket) => {
    gameHandler(io, socket, activeLobbies);
    lobbyHandler(io, socket, activeLobbies);
  });

  return io;
}

function getIO() {
  if (!io) throw new Error("Socket.io não foi inicializado!");
  return io;
}

module.exports = { init, getIO };