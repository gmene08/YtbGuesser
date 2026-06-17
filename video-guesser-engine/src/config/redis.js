const redis = require('redis');
require('dotenv').config(); //


const redisClient = redis.createClient({
  url: process.env.REDIS_URL || 'redis://127.0.0.1:6379'
});

redisClient.on('error', (err) => console.error('❌ Erro no Cliente Redis:', err.message));

async function connectRedis() {
  if (!redisClient.isOpen) {
    await redisClient.connect();
    console.log('🔌 Motor conectado ao banco Redis com sucesso!');
  }
}

module.exports = { redisClient, connectRedis };