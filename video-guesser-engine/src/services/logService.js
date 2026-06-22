const {redisClient} = require('../config/redis');

async function addActivityLog(roomCode, io, type, logMessage) {
    const logKey = `room:${roomCode}:logs`;
    
    const timestamp = new Date().toISOString(); 
    
    const logEntry = { timestamp, type, message: logMessage };
    try{
        await redisClient.lPush(logKey, JSON.stringify(logEntry));

        await redisClient.expire(logKey, 7200); 

        io.to(`${roomCode}-game`).emit('newLogEntry', logEntry);
    }
    catch(err){
        console.error(`❌ Erro ao adicionar log da sala [${roomCode}] no Redis:`, err.message);
    }
}

async function getActivityLogs(roomCode) {
    const logKey = `room:${roomCode}:logs`;
    try {
        const logs = await redisClient.lRange(logKey, 0, -1);
        return logs.map(log => JSON.parse(log)).reverse(); // Retorna do mais antigo para o mais recente
    } catch (err) {
        console.error(`❌ Erro ao buscar logs da sala [${roomCode}] no Redis:`, err.message);
        return [];
    }
}

async function clearActivityLogs(roomCode) {
    const logKey = `room:${roomCode}:logs`;
    try {
        await redisClient.del(logKey);
    } catch (err) {
        console.error(`❌ Erro ao limpar logs da sala [${roomCode}] no Redis:`, err.message);
    }
}

module.exports = { addActivityLog, getActivityLogs, clearActivityLogs };