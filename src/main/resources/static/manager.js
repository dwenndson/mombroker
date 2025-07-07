document.addEventListener('DOMContentLoaded', () => {
    // --- Constantes da API ---
    const KAFKA_API_URL = '/api/kafka';
    const USERS_API_URL = '/api/users';

    // --- Elementos de Tópicos ---
    const createTopicBtn = document.getElementById('create-topic-btn');
    const topicNameInput = document.getElementById('topic-name-input');
    const topicsList = document.getElementById('topics-list');
    const topicFeedback = document.getElementById('topic-feedback');

    // --- Elementos de Usuários ---
    const registerUserBtn = document.getElementById('register-user-btn');
    const usernameInput = document.getElementById('username-input');
    const usersList = document.getElementById('users-list');
    const userFeedback = document.getElementById('user-feedback');

    /**
     * Busca a lista de tópicos da API e atualiza a interface.
     */
    async function fetchAndRenderTopics() {
        try {
            const response = await fetch(`${KAFKA_API_URL}/topics?_=${new Date().getTime()}`);
            if (!response.ok) throw new Error('Falha ao buscar tópicos.');
            const topics = await response.json();

            topicsList.innerHTML = '';
            const publicTopics = topics.filter(t => !t.startsWith('user-inbox-'));

            if (publicTopics.length === 0) {
                topicsList.innerHTML = '<li class="list-group-item">Nenhum tópico público encontrado.</li>';
                return;
            }

            publicTopics.forEach(topic => {
                const li = document.createElement('li');
                li.className = 'list-group-item';
                li.innerHTML = `<span>${topic}</span> <button class="btn btn-danger btn-sm" data-topic="${topic}">Remover</button>`;
                topicsList.appendChild(li);
            });
        } catch (error) {
            topicsList.innerHTML = `<li class="list-group-item text-danger">${error.message}</li>`;
        }
    }

    /**
     * Busca a lista de USUÁRIOS da API e atualiza a interface.
     */
    async function fetchAndRenderUsers() {
        try {
            const response = await fetch(`${USERS_API_URL}/?_=${new Date().getTime()}`);
            if (!response.ok) throw new Error('Falha ao buscar usuários.');
            const users = await response.json();

            usersList.innerHTML = '';
            if (users.length === 0) {
                usersList.innerHTML = '<li class="list-group-item">Nenhum usuário registrado.</li>';
                return;
            }

            users.forEach(user => {
                const li = document.createElement('li');
                li.className = 'list-group-item';
                li.textContent = user;
                usersList.appendChild(li);
            });
        } catch (error) {
            usersList.innerHTML = `<li class="list-group-item text-danger">${error.message}</li>`;
        }
    }

    /**
     * Envia uma requisição para registrar um novo usuário.
     */
    async function registerUser() {
        const username = usernameInput.value.trim();
        if (!username) {
            userFeedback.textContent = 'Por favor, insira um nome de usuário.';
            userFeedback.className = 'form-text text-danger';
            return;
        }

        try {
            const response = await fetch(`${USERS_API_URL}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: username }),
            });
            const responseText = await response.text();
            if (!response.ok) throw new Error(responseText);

            userFeedback.textContent = responseText;
            userFeedback.className = 'form-text text-success';
            usernameInput.value = '';

            // --- PONTO CRUCIAL DA LÓGICA ---
            // Após o sucesso do registro, chamamos a função para
            // buscar e renderizar a lista de usuários ATUALIZADA.
            fetchAndRenderUsers();

        } catch (error) {
            userFeedback.textContent = `Erro: ${error.message}`;
            userFeedback.className = 'form-text text-danger';
        }
    }

    // (O restante do código para criar e deletar tópicos permanece o mesmo)
    async function createTopic() {
        const topicName = topicNameInput.value.trim();
        if (!topicName) {
            topicFeedback.textContent = 'Por favor, insira um nome para o tópico.';
            topicFeedback.className = 'form-text text-danger';
            return;
        }
        try {
            const response = await fetch(`${KAFKA_API_URL}/topic`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: topicName }),
            });
            const responseText = await response.text();
            if (!response.ok) throw new Error(responseText);
            topicFeedback.textContent = responseText;
            topicFeedback.className = 'form-text text-success';
            topicNameInput.value = '';
            fetchAndRenderTopics();
        } catch (error) {
            topicFeedback.textContent = `Erro: ${error.message}`;
            topicFeedback.className = 'form-text text-danger';
        }
    }
    async function deleteTopic(topicName) {
        if (!confirm(`Tem certeza que deseja remover o tópico "${topicName}"?`)) return;
        try {
            const response = await fetch(`${KAFKA_API_URL}/topic/${topicName}`, { method: 'DELETE' });
            if (!response.ok) throw new Error(await response.text());
            alert(`Tópico "${topicName}" removido com sucesso.`);
            fetchAndRenderTopics();
        } catch (error) {
            alert(`Erro ao remover tópico: ${error.message}`);
        }
    }


    // --- Adicionar Event Listeners ---
    createTopicBtn.addEventListener('click', createTopic);
    topicNameInput.addEventListener('keydown', (e) => e.key === 'Enter' && createTopic());
    topicsList.addEventListener('click', (e) => {
        if (e.target && e.target.matches('button.btn-danger')) {
            deleteTopic(e.target.dataset.topic);
        }
    });
    registerUserBtn.addEventListener('click', registerUser);
    usernameInput.addEventListener('keydown', (e) => e.key === 'Enter' && registerUser());

    // --- Carregamento Inicial ---
    fetchAndRenderTopics();
    fetchAndRenderUsers();
});
