// 1. Importar as ferramentas necessárias
const express = require('express');
const path = require('path');

// 2. Inicializar o servidor Express
const app = express();
const PORT = 3000; // A porta onde o site vai correr (ex: http://localhost:3000)

// 3. Configurações do Servidor

// Define EJS como o nosso motor de "templates"
app.set('view engine', 'ejs');
// Diz ao EJS para procurar as "views" na pasta /views
app.set('views', path.join(__dirname, 'views'));

// Diz ao Express para servir ficheiros estáticos (css, imagens) da pasta /public
app.use(express.static(path.join(__dirname, 'public')));

// Permite ao servidor ler dados enviados por formulários HTML
app.use(express.urlencoded({ extended: true }));

// --- 4. Definição das Rotas (Os nossos links) ---

/**
 * Rota: GET /login
 * Objetivo: Mostrar a página de login.
 */
app.get('/login', (req, res) => {
  // O "error: null" é para o caso de querermos mostrar uma mensagem de erro
  res.render('login', { title: 'Login - Gestor de Estoque', error: null });
});

/**
 * Rota: POST /login
 * Objetivo: Simular o processo de login.
 * (Por agora, vamos apenas redirecionar para o dashboard)
 */
app.post('/login', (req, res) => {
  const { email, password } = req.body;
  
  // Lógica de login (simulada)
  // No futuro, aqui irias verificar na base de dados
  if (email && password) {
    console.log(`Tentativa de login com email: ${email}`);
    // Se o login for bem-sucedido:
    res.redirect('/'); // Redireciona para o dashboard
  } else {
    // Se falhar:
    res.render('login', { 
      title: 'Login - Gestor de Estoque', 
      error: 'Email e senha são obrigatórios.' 
    });
  }
});

/**
 * Rota: GET /
 * Objetivo: Mostrar o dashboard principal (Menu).
 */
app.get('/', (req, res) => {
  // (Numa app real, esta rota devia ser protegida por sessão)
  res.render('dashboard', { 
    title: 'Dashboard - Gestor de Estoque',
    userName: 'Utilizador' // Podes passar o nome do user vindo do login
  });
});

/**
 * Rota: GET /operacao
 * Objetivo: Mostrar a página de Entrada ou Saída.
 * Usamos um query parameter (?tipo=...)
 */
app.get('/operacao', (req, res) => {
  const { tipo } = req.query; // 'entrada' ou 'saida'
  
  // Define o título com base no tipo
  const pageTitle = tipo === 'entrada' 
    ? 'Registar Entrada' 
    : 'Registar Saída';

  res.render('operacao', { 
    title: pageTitle,
    tipo: tipo // Passamos o tipo para a view saber o que mostrar
  });
});

/**
 * Rota: GET /logout
 * Objetivo: Simular o logout.
 */
app.get('/logout', (req, res) => {
  // (Numa app real, aqui irias limpar a sessão do utilizador)
  res.redirect('/login');
});

// --- 5. Iniciar o Servidor ---
app.listen(PORT, () => {
  console.log(`Servidor a correr em http://localhost:${PORT}`);
});