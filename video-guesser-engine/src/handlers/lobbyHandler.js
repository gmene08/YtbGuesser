// Memória temporária para guardar quem está em qual lobby antes do jogo começar
const activeLobbies = new Map();
const disconnectTimeouts = new Map();

const JAVA_API = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';
const JAVA_BACKEND_URL = process.env.JAVA_BACKEND_URL || 'http://localhost:8080';

module.exports = (io, socket, activeLobbies) => {

    socket.on('joinLobbyRoom', ({roomCode})=>{
        const lobbyConnectionName = `${roomCode}-lobby`;
        socket.join(lobbyConnectionName);
        socket.roomCode = roomCode;

        const lobby = activeLobbies.get(roomCode);
        if (lobby){
            if(disconnectTimeouts.has(socket.userId)){
                clearTimeout(disconnectTimeouts.get(socket.userId));
                disconnectTimeouts.delete(socket.userId);

                lobby.playersWhoDisconnected = lobby.playersWhoDisconnected.filter(id => id !== socket.userId);
                io.to(`${roomCode}-lobby`).emit('lobbyUpdate', activeLobbies.get(roomCode));
                
                console.log(`🔌 ${socket.nickname} reconectou-se a tempo na sala [${roomCode}]!`);
            }
        
            console.log(`🔌 ${socket.nickname} conectou-se na sala [${roomCode}]!`);
        } 
    }); 
 
    // 2. Entrar na sala quando o Jogo já está rodando
    // TODO: see if it's better to replicate the joinLobby pattern (send data to front end) and change join
    
    socket.on('disconnect', ()=>{
        const lobby = activeLobbies.get(socket.roomCode);
        if (lobby && lobby.players.some(p => p.userId === socket.userId)){

            if(!lobby.playersWhoDisconnected.includes(socket.userId)){
                lobby.playersWhoDisconnected.push(socket.userId);
            }
            
            console.log(`⚠️ ${socket.nickname} caiu! 30 segundos para voltar...`);
            io.to(`${socket.roomCode}-lobby`).emit('lobbyUpdate', lobby);
            const timeoutId = setTimeout(()=>{
                disconnectTimeouts.delete(socket.userId);
                
                if(lobby){
                        lobby.playersWhoDisconnected = lobby.playersWhoDisconnected.filter(id =>  id !== socket.userId);
                        console.log(`⚠️ ${socket.nickname} não voltou a tempo... tirando da sala`);
                } 

                fetch(`${JAVA_BACKEND_URL}/api/engine/disconnect?userId=${socket.userId}`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' }
                })
                .catch(err => console.error(`❌ Erro no DELETE status:`, err.message));
            }, 20000) // 20 segundos para o jogador voltar antes de ser removido da sala
            
            disconnectTimeouts.set(socket.userId, timeoutId);

        }
    })



};