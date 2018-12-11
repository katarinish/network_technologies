window.addEventListener("DOMContentLoaded", function() {
    const elements = {
        loginInput: document.querySelector('.login__field'),
        loginBtn: document.querySelector('.login__btn'),
        loginForm: document.querySelector('.login'),
        messageContainer: document.querySelector('.message'),
        messageList: document.querySelector('.message__list'),
        senderInput: document.querySelector('.sender__field'),
        senderForm: document.querySelector('.sender'),
        logoutBtn: document.querySelector('.logout__btn'),
        activeList: document.querySelector('.active__list'),
    }

    const requestsInfo = {
        login: 'http://127.0.0.1:8888/login',
        logout: 'http://127.0.0.1:8888/logout',
        users: 'http://127.0.0.1:8888/users',
        messages: 'http://127.0.0.1:8888/messages',
    }

    const appState = {
        isReady: false,
        login: '',
        authToken: '',
        lastMsgId: 0,
    }

    const removeUser = () => {
        appState.isReady = false;
        appState.login = '';
        appState.authToken = '';
        appState.lastMsgId = 0;

        elements.loginInput.value = '';
        elements.messageList.innerHTML = '';
        elements.activeList.innerHTML = '';


        elements.messageContainer.classList.remove('shown');
        elements.messageContainer.classList.add('hidden');
    }

    const setNewUser = data => {
        appState.isReady = true;
        appState.login = data.login;
        appState.authToken = data.authToken;

        console.log(appState.authToken, appState.login);

        elements.messageContainer.classList.add('shown');
        elements.messageContainer.classList.remove('hidden');
    }

    const renderMsgs = (msgs) => {
        if (!msgs) return;
        
        const messages = Array.from(msgs);
        appState.lastMsgId = messages[messages.length - 1].id + 1;

        for(const msg of messages) {
            let item = `
                <li class="message__item">
                    <div class="message__author">${msg.author}</div>
                    <div class="message__text">${msg.message}</div>
                </li>
            `;

            elements.messageList.insertAdjacentHTML('beforeend', item);
        }
    }

    const renderActiveUsers = (users) => {
        elements.activeList.innerHTML = '';

        if (!users) return;

        const usersArr = Array.from(users);

        for(const user of usersArr) {
            let item = `
                <li class="message__item">
                    <div class="message__author">${user.login}</div>
                </li>
            `;

            elements.activeList.insertAdjacentHTML('beforeend', item);
        }
    }

    const messagesRequestListener = xhr => {
        if (xhr.readyState != 4) return;

        if (xhr.status === 200) {
            appState.messages = xhr.response.messages;
            renderMsgs(appState.messages);
            controlActive();
            setTimeout(getMsgsHistory, 1000);
        } else {
            alert(xhr.status + ': ' + xhr.statusText + ' cannot get message!');
        }
    }

    const getMsgsHistory = async () => {
        await sendRequest({
            method: "GET",
            url: `${requestsInfo.messages}?offset=${appState.lastMsgId}`,
            stateChangeListener: messagesRequestListener,
        });
    }

    const createWebSocketConnection = () => {
        var socket = new WebSocket("ws://localhost:8080/untitled1_war_exploded/ws");
        appState.socket = socket;

        socket.onopen = function() {
            console.log("Соединение установлено.");
        };
            
        socket.onclose = function(event) {
            if (event.wasClean) {
                console.log('Соединение закрыто чисто');
            } else {
                console.log('Обрыв соединения');
            }

            console.log('Код: ' + event.code + ' причина: ' + event.reason);
        };
            
        socket.onerror = function(error) {
            console.log("Ошибка " + error.message);
        };

        socket.onmessage = function (event) {
            const msg = event.data;
            console.log(event.data);

            let item = `
                    <li class="message__item">
                        <div class="message__author">${msg.author}</div>
                        <div class="message__text">${msg.message}</div>
                    </li>
                `;
    
            elements.messageList.insertAdjacentHTML('beforeend', item);
        }
    }

    const loginRequestListener = xhr => {
        if (xhr.readyState != 4) return;

        if (xhr.status === 200) {
            alert("Пользователь успешно авторизован!");
            setNewUser(xhr.response);

            createWebSocketConnection();
            // getMsgsHistory();
        } else {
            appState.isReady = false;
            elements.loginInput.value = ''

            alert(xhr.status + ': ' + xhr.statusText);
        }
    }

    const logoutRequestListener = xhr => {
        if (xhr.readyState != 4) return;

        if (xhr.status === 200) {
            removeUser();
        } else {
            alert(xhr.status + ': ' + xhr.statusText);
        }
    }

    const senderRequestListener = xhr => {
        if (xhr.readyState != 4) return;

        if (xhr.status !== 200) {
            alert(xhr.status + ': ' + xhr.statusText);
        }
    }

    const activeRequestListener = xhr => {
        if (xhr.readyState != 4) return;

        if (xhr.status === 200) {
            renderActiveUsers(xhr.response.users);
            setTimeout(controlActive, 2000);
        }
    }

    const sendRequest = ({method, url, data = {}, stateChangeListener}) => {
        let xhr = new XMLHttpRequest();

        xhr.onreadystatechange = e => stateChangeListener(xhr);

        xhr.open(method, url, true);
        xhr.responseType = 'json';
        xhr.setRequestHeader('Content-Type', 'application/json');
        xhr.setRequestHeader('Authorization', `${appState.authToken}`);

        data ? xhr.send(data) : xhr.send;
    }

    const controlLogin = () => {
        const loginInput = elements.loginInput.value;

        sendRequest({
            method: "POST",
            url: requestsInfo.login,
            data: `{
                "username": "${loginInput}"
            }`,
            stateChangeListener: loginRequestListener,
        });

    }

    const controlSender = () => {
        const senderInput = elements.senderInput.value;
        elements.senderInput.value = '';

        const data = `{
            "message": "${senderInput}",
            "author": "${appState.login}"
        }`;

        appState.socket.send(data);

        // sendRequest({
        //     method: "POST",
        //     url: requestsInfo.messages,
        //     data: `{
        //         "message": "${senderInput}",
        //         "username": "${appState.login}"
        //     }`,
        //     stateChangeListener: senderRequestListener,
        // });

    }

    const controlLogout = () => {
        sendRequest({
            method: "POST",
            url: requestsInfo.logout,
            stateChangeListener: logoutRequestListener,
        });
    }

    const controlActive = () => {
        sendRequest({
            method: "GET",
            url: requestsInfo.users,
            stateChangeListener: activeRequestListener,
        });
    }

    
    elements.loginForm.addEventListener('submit', e => {
        e.preventDefault();
        controlLogin();
    });

    elements.senderForm.addEventListener('submit', e => {
        e.preventDefault();
        controlSender();
    });

    elements.logoutBtn.addEventListener('click', e => {
        e.preventDefault();
        controlLogout();
    });

    // createWebSocketConnection();
	
});