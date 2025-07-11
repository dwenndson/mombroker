document.addEventListener('DOMContentLoaded', () => {
    // --- Elementos da UI ---
    const loginView = document.getElementById('login-view');
    const mainView = document.getElementById('main-view');
    const loginBtn = document.getElementById('login-btn');
    const usernameInput = document.getElementById('username-input');
    const loginError = document.getElementById('login-error');
    const currentUserSpan = document.getElementById('current-user');
    const topicsListDiv = document.getElementById('topics-list');
    const usersListDiv = document.getElementById('users-list');
    const messagesArea = document.getElementById('messages-area');
    const recipientSelect = document.getElementById('recipient-select');
    const messageInput = document.getElementById('message-input');
    const sendBtn = document.getElementById('send-btn');

    // --- Estado da Aplicação ---
    let stompClient = null;
    let username = null;
    const subscriptions = new Map(); // Armazena as inscrições de tópicos

    // --- Lógica de Login ---
    loginBtn.addEventListener('click', async () => {
        const enteredUsername = usernameInput.value.trim();
        if (!enteredUsername) {
            loginError.textContent = 'Por favor, insira um nome de usuário.';
            return;
        }

        // Valida se o usuário existe no backend
        try {
            const response = await fetch(`/api/users/?_=${new Date().getTime()}`);
            const users = await response.json();
            if (users.includes(enteredUsername)) {
                username = enteredUsername;
                loginView.style.display = 'none';
                mainView.style.display = 'block';
                currentUserSpan.textContent = username;
                await initializeChat();
            } else {
                loginError.textContent = 'Usuário não encontrado. Verifique o nome ou registre-se na aplicação do Gerenciador.';
            }
        } catch (error) {
            loginError.textContent = 'Erro ao conectar com o servidor.';
        }
    });

    /**
     * Inicializa o chat após o login bem-sucedido.
     */
    async function initializeChat() {
        await loadRecipients();
        connectWebSocket();
    }

    /**
     * Carrega a lista de usuários e tópicos para o seletor de destinatário.
     */
    async function loadRecipients() {
        try {
            // Carrega usuários
            const usersResponse = await fetch(`/api/users/?_=${new Date().getTime()}`);
            const users = await usersResponse.json();
            usersListDiv.innerHTML = '';
            recipientSelect.innerHTML = '<option selected>Selecione um destino...</option>';

            users.forEach(user => {
                if (user !== username) { // Não mostra o próprio usuário na lista para envio
                    const userItem = document.createElement('a');
                    userItem.className = 'list-group-item list-group-item-action';
                    userItem.href = '#';
                    userItem.textContent = `👤 ${user}`;
                    usersListDiv.appendChild(userItem);

                    const option = document.createElement('option');
                    option.value = `user:${user}`;
                    option.textContent = `Usuário: ${user}`;
                    recipientSelect.appendChild(option);
                }
            });

            // Carrega tópicos
            const topicsResponse = await fetch(`/api/kafka/topics?_=${new Date().getTime()}`);
            const topics = await topicsResponse.json();
            topicsListDiv.innerHTML = '';
            const publicTopics = topics.filter(t => !t.startsWith('user-inbox-'));

            publicTopics.forEach(topic => {
                const topicItem = document.createElement('a');
                topicItem.id = `topic-${topic}`;
                topicItem.className = 'list-group-item list-group-item-action';
                topicItem.href = '#';
                topicItem.textContent = ` # ${topic}`;
                topicItem.onclick = () => toggleTopicSubscription(topic);
                topicsListDiv.appendChild(topicItem);

                const option = document.createElement('option');
                option.value = `topic:${topic}`;
                option.textContent = `Tópico: ${topic}`;
                recipientSelect.appendChild(option);
            });

        } catch (error) {
            console.error("Erro ao carregar destinatários:", error);
        }
    }

    /**
     * Conecta ao WebSocket e se inscreve nos canais necessários.
     */
    function connectWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.debug = (str) => {
            console.log('STOMP DEBUG: ' + str);
        }
        stompClient.connect({ login: username, passcode: 'password' },
            (frame) => {
                console.log('Conectado ao WebSocket: ' + frame);

                const userDestination = `/user/${username}/queue/messages`;
                console.log(`Inscrevendo-se no destinmo pessoal: ${userDestination}`);

                // Inscrição na fila pessoal para mensagens diretas
                stompClient.subscribe(userDestination, (message) => {
                    console.log("!!! MENSAGEM DIRETA RECEBIDA PELO CLIENTE !!!", message);
                    const msg = JSON.parse(message.body);
                    displayMessage(msg, false);
                });

                displaySystemMessage(`Conectado como ${username}. Pronto para enviar e receber mensagens.`);
            },
            (error) => {
                console.error('Erro na conexão WebSocket:', error);
                displaySystemMessage('Erro de conexão. Tente recarregar a página.');
            }
        );
    }

    /**
     * Inscreve ou desinscreve de um tópico público.
     */
    function toggleTopicSubscription(topic) {
        const topicItem = document.getElementById(`topic-${topic}`);
        if (subscriptions.has(topic)) {
            // Desinscrever
            const subscription = subscriptions.get(topic);
            console.log("--- MENSAGEM DE TÓPICO RECEBIDA ---", message);
            subscription.unsubscribe();
            subscriptions.delete(topic);
            topicItem.classList.remove('active');
            displaySystemMessage(`Você saiu do tópico #${topic}.`);
        } else {
            // Inscrever
            const subscription = stompClient.subscribe(`/topic/${topic}`, (message) => {
                const msg = JSON.parse(message.body);
                displayMessage(msg, false);
            });
            subscriptions.set(topic, subscription);
            topicItem.classList.add('active');
            displaySystemMessage(`Inscrito no tópico #${topic}!`);
        }
    }

    /**
     * Envia uma mensagem através da API REST.
     */
    sendBtn.addEventListener('click', async () => {
        const content = messageInput.value.trim();
        const recipientValue = recipientSelect.value;

        if (!content || recipientValue === 'Selecione um destino...') {
            alert('Por favor, selecione um destinatário e digite uma mensagem.');
            return;
        }

        const [type, recipient] = recipientValue.split(':');

        const message = {
            sender: username,
            recipient: recipient,
            content: content,
            type: type.toUpperCase() === 'USER' ? 'DIRECT' : 'TOPIC'
        };

        try {
            const response = await fetch('/api/messages/send', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(message)
            });

            if (response.ok) {
                // Se a mensagem foi para mim mesmo (em um tópico), não mostro duas vezes.
                // O backend envia de volta via WebSocket.
                if (message.type !== 'TOPIC' || message.recipient !== username) {
                     displayMessage(message, true); // Mostra a mensagem enviada na tela
                }
                messageInput.value = '';
            } else {
                alert('Erro ao enviar mensagem.');
            }
        } catch (error) {
            console.error('Erro ao enviar mensagem:', error);
            alert('Erro de conexão ao enviar mensagem.');
        }
    });

    messageInput.addEventListener('keypress', (e) => {
        if(e.key === "Enter") {
            sendBtn.click();
        }
    });

    /**
     * Exibe uma mensagem na área de chat.
     */
    function displayMessage(msg, isSent) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${isSent ? 'sent' : 'received'}`;

        let header = '';
        if (msg.type === 'TOPIC') {
            header = `<strong>${msg.sender}</strong> para o tópico <strong>#${msg.recipient}</strong>`;
        } else {
            header = `<strong>${msg.sender}</strong> para <strong>você</strong>`;
        }

        if (isSent) {
             header = `<strong>Você</strong> para <strong>${msg.recipient}</strong> (${msg.type.toLowerCase()})`;
        }

        messageDiv.innerHTML = `<div class="sender">${header}</div><div>${msg.content}</div>`;
        messagesArea.appendChild(messageDiv);
        messagesArea.scrollTop = messagesArea.scrollHeight;
    }

    function displaySystemMessage(text) {
        const messageDiv = document.createElement('div');
        messageDiv.className = 'text-center text-muted fst-italic my-2';
        messageDiv.textContent = text;
        messagesArea.appendChild(messageDiv);
        messagesArea.scrollTop = messagesArea.scrollHeight;
    }
});
