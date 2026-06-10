const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cors = require('cors');

const app = express();
const activeMatches = new Map();

app.use(cors({ origin: '*' }));

app.use(express.json());

const server = http.createServer(app);


const io = new Server(server, {
  cors: {
    origin: '*',
    methods: ['GET', 'POST']
  }
});


io.on('connection', (socket) => {
  console.log(`🔌 Novo cliente conectado: ${socket.id}`);

  // Quando o Angular pede para entrar na sala do jogo
  socket.on('joinGameRoom', ({ roomCode, userId, nickname }) => {
    
    // 1. Coloca o WebSocket do cara na sala específica
    socket.join(roomCode);
    
    console.log(`👤 Jogador ${nickname} (ID: ${userId}) entrou no jogo da sala [${roomCode}]`);

    // (Opcional) Podemos avisar a sala que ele entrou
    // io.to(roomCode).emit('playerJoined', { userId, nickname });
  });

  // Quando o jogador envia um palpite (Guess)
  socket.on('submitGuess', ({ roomCode, userId, guessValue }) => {
    console.log(`🎯 Palpite recebido! Sala: ${roomCode} | Jogador: ${userId} | Chutou: ${guessValue}`);

    const match = activeMatches.get(roomCode);
    if(match && match.status === 'GUESSING'){

        const alreadyGuessed = match.guesses.some(g => g.userId === userId);
        if(!alreadyGuessed){
            match.guesses.push({userId, guessValue});
            console.log(`🎯 Palpite salvo! Sala: [${roomCode}] | Jogador: ${userId} | Chute: ${guessValue}`);

            io.to(roomCode).emit('playerGuessed', userId)
        }
    }
    
    // Aqui no futuro vamos pegar a variável activeMatches, validar o tempo, calcular os pontos etc.
  });

  socket.on('disconnect', () => {
    console.log(`❌ Cliente desconectado: ${socket.id}`);
  });
});


app.get('/health', (req, res) => {
  res.send('O Motor do Jogo está rodando em JavaScript puro!');
});

// 🚪 Rota para o Java avisar que uma partida começou
app.post('/api/engine/start-match', (req, res) => {
  const { roomCode, players, maxRounds } = req.body;

  if (!roomCode || !players) {
    return res.status(400).json({ error: 'Faltam dados obrigatórios para iniciar a partida.' });
  }

  // Cria o "Estado da Partida" (Match State) na memória
  activeMatches.set(roomCode, {
    roomCode: roomCode,
    players: players, // Lista de quem está jogando
    maxRounds: maxRounds,
    currentRound: 1,
    status: 'PREPARING',
    guesses: [] // Aqui vamos guardar os palpites depois!
  });

  console.log(`🎮 Partida iniciada na sala [${roomCode}] com ${players.length} jogadores!`);

  setTimeout(() => {
    const match = activeMatches.get(roomCode);
    if (match && match.status === 'PREPARING') {
      startRoundTimer(roomCode, 5); // A função que muda pra GUESSING e avisa o Java
    }
  }, 5000);
  
  // Responde ao Java que deu tudo certo
  return res.status(200).json({ message: `Engine pronta para a sala ${roomCode}` });
});

// ⏰ Função que controla o relógio de cada sala
function startRoundTimer(roomCode, durationInSeconds) {
  const match = activeMatches.get(roomCode);
  if (!match) return;

  // 1. Atualiza o status na memória do Node.js
  match.status = 'GUESSING';
  let timeLeft = durationInSeconds;
  
  // 2. Avisa o Angular: "O Round começou, liberem a interface!"
  io.to(roomCode).emit('roundStarted', { currentRound: match.currentRound, totalTime: durationInSeconds });
  console.log(`⏳ Round ${match.currentRound} da sala [${roomCode}] mudou para GUESSING e começou!`);

  // 3. Dispara a requisição HTTP para o Java atualizar o status no banco de dados
  // (Nota: No próximo passo, nós vamos criar esse endpoint exato no Spring Boot)
  fetch(`http://localhost:8080/api/engine/${roomCode}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ status: 'GUESSING' })
  }).then(async res => {
     if (!res.ok) console.error(`❌ Erro no PATCH status: HTTP ${res.status} -> ${await res.text()}`);
  })
  .catch(err => console.error(`❌ Falha de rede ao tentar contato com o Java:`, err.message));

  //guardar os guesses aqui??

  // 4. Inicia a contagem regressiva
  const intervalId = setInterval(() => {
    timeLeft--;
    io.to(roomCode).emit('timeUpdate', { timeLeft });

    // Se o tempo zerar
    if (timeLeft <= 0) {
      clearInterval(intervalId); 
      console.log(`🛑 Tempo esgotado na sala [${roomCode}]! Enviando dados ao Java...`);
      
      // 1. Avisa o Angular que acabou a fase de palpites
      io.to(roomCode).emit('roundEnded', { reason: 'timeout' });

      // 2. Monta o pacote com a sala e os palpites que estavam na memória
      const reportPayload = {
        roomCode: roomCode,
        guesses: match.guesses
      };

      // 3. Dispara o relatório para o Java processar a matemática
      fetch('http://localhost:8080/api/engine/end-round', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(reportPayload)
      })
      .then(async res => {
        if (!res.ok) {
          // 🔥 A MÁGICA ACONTECE AQUI: Lemos o corpo do erro que o Java mandou!
          const errorText = await res.text();
          throw new Error(`Java recusou com HTTP ${res.status} -> Detalhes: ${errorText}`);
        }
        return res.json(); // Esperamos que o Java devolva os dados do placar atualizado
      })
      .then(engineResponse => {
        console.log(`🏆 Resultados recebidos do Java para a sala [${roomCode}]!`);
        
        // engineResponse tem { matchData: {...}, roundResult: {...} }
        // Repassamos esse pacote combinado intacto para o Front-end
        io.to(roomCode).emit('roundResults', engineResponse);

        // Limpa a memória do Node para o próximo round
        setTimeout(()=>{
            match.currentRound++;
            match.guesses = [];
            match.status = 'PREPARING';

            // 1. Avisa o Java: "Muda pro próximo round no banco!"
            fetch(`http://localhost:8080/api/engine/${roomCode}/change-round`, {
                method: 'PATCH',
                headers: {'Content-Type': 'application/json'},
            })
            .then( res =>{
                if (!res.ok) throw new Error('Erro ao processar fim do round no Java');
                return res.json(); 
            })
            .then(response =>{
                // 2. Avisa o Angular: "Voltem pra tela de vídeo!" (io.to.emit('nextRoundStarted'))
                io.to(roomCode).emit('changeOfRounds', response);
                // 3. E chama aquele setTimeout de 5 segundos pra iniciar o timer do novo round!
                if (match.currentRound <= match.maxRounds){
                    setTimeout(()=>{
                        startRoundTimer(roomCode, durationInSeconds);
                    }, 5000)
                }
            })
           
        }, 10000)
      })
      .catch(err => console.error(`❌ Erro ao enviar relatório ao Java:`, err.message));
    }
  }, 1000);

  match.currentTimer = intervalId; 
}

const PORT = 3000;
server.listen(PORT, () => {
  console.log(`🚀 Videoguesser Engine rodando na porta ${PORT}`);
});