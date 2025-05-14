/*
 * Cerberus Copyright (C) 2013 - 2025 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {
        initPage();

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );
    });
});

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);

    // Connect to your WebSocket endpoint
    var parser = document.createElement('a');
    parser.href = window.location.href;
    var protocol = "ws:";
    if (parser.protocol === "https:") {
        protocol = "wss:";
    }
    var path = parser.pathname.split("Prompt")[0];
    var new_uri = protocol + parser.host + path + "api/ws/chatai";
    console.info("Open Socket to : " + new_uri);
    var socket = new WebSocket(new_uri);

    var username = getUser().name;
    // Append incoming streamed data
    var textContent = "";
    socket.onmessage = function (event) {
        let msg;
        try {
            msg = JSON.parse(event.data);
        } catch (e) {
            console.error("Invalid JSON received:", event.data);
            return;
        }

        if (msg.type === 'title') {
            const div = document.createElement('div');
            div.className = 'history-entry';
            div.textContent = msg.title?.replace(/(^")|("$)/g, '') || 'Untitled';
            div.dataset.sessionId = msg.sessionID;
            div.onclick = () => {
                loadSession(msg.sessionID);
            };
            document.getElementById("Today").after(div);
        }

        if (msg.type === 'chat') {

            const botMsg = document.getElementById('last-message');
            botMsg.className = 'message';
            botMsg.classList.remove('typing');
            textContent += msg.data;
            //botMsg.innerHTML = marked.parse(textContent);
            botMsg.innerHTML = DOMPurify.sanitize(textContent);

            const chatWindow = document.getElementById('chatWindow');
            chatWindow.scrollTop = chatWindow.scrollHeight;
            const input = document.getElementById('userInput');
            input.value = '';
            input.focus();
        }
    };


    // Handle open connection
    socket.onopen = function () {
        console.log("Connected to WebSocket!");
        const msg = {
            sender: getUser().login,
            sessionID: "",
            content: "Hello, I'm " + username + ". Could you give me some example of things that I can ask you related to Cerberus-Testing?"
        };
        socket.send(JSON.stringify(msg));
    };

    // Handle errors
    socket.onerror = function (error) {
        console.error("WebSocket error:", error);
    };

    socket.onclose = function (event) {
        console.log("WebSocket closed:", event);
    };

    $('#sendMessageButton').click(function () {
        const input = document.getElementById('userInput');
        const text = input.value.trim();
        input.value = '';

        const msg = {
            sender: getUser().login,
            sessionID: $('#userInput').attr( 'data-sessionID')==undefined?"":$('#userInput').attr( 'data-sessionID'),
            content: text
        };
        socket.send(JSON.stringify(msg));

        const chatWindow = document.getElementById('chatWindow');

        // Add user message
        const userMsg = document.createElement('div');
        userMsg.className = 'message user';
        userMsg.textContent = text;
        chatWindow.appendChild(userMsg);

        // Remove last Response tag
        document.getElementById('last-message').removeAttribute('id');
        textContent = "";

        // Add response container
        const botMsg = document.createElement('div');
        botMsg.className = 'message bot typing';
        botMsg.id = 'last-message';
        botMsg.textContent = "Thinking";
        chatWindow.appendChild(botMsg);

    });

    document.getElementById('userInput').addEventListener('keydown', function (event) {
        if (event.key === 'Enter' && !event.shiftKey) {
            event.preventDefault(); // Prevent newline
            document.getElementById('sendMessageButton').click(); // Trigger the existing click handler
        }
    });

    fetchAndDisplayHistory();

}

function toggleSidebar() {
    document.getElementById('historySidebar').classList.toggle('open');
    document.getElementById('mainContainer').classList.toggle('sidebar-open');
}

function fetchAndDisplayHistory() {
    var parser = document.createElement('a');
    parser.href = window.location.href;
    var path = parser.pathname.split("Prompt")[0];
    fetch(parser.protocol +"//"+ parser.host + path + "/api/ai/prompts")
        .then(response => response.json())
        .then(data => {
            const grouped = groupPromptsByDate(data.prompts);
            renderGroupedPrompts(grouped);
        })
        .catch(err => console.error("Failed to fetch prompt history:", err));
}

function groupPromptsByDate(prompts) {
    const now = new Date();
    const today = [], last7 = [], last30 = [], older = {};

    prompts.forEach(p => {
        if (p.title !== "") {

            const date = new Date(p.dateCreated);
            const diffDays = (now - date) / (1000 * 60 * 60 * 24);
            const monthKey = `${date.toLocaleString('default', {month: 'long'})} ${date.getFullYear()}`;

            if (diffDays < 1) {
                today.push(p);
            } else if (diffDays <= 7) {
                last7.push(p);
            } else if (diffDays <= 30) {
                last30.push(p);
            } else {
                if (!older[monthKey]) older[monthKey] = [];
                older[monthKey].push(p);
            }
        }
    });

    return { today, last7, last30, older };
}

function renderGroupedPrompts(grouped) {
    const container = document.getElementById('historyContent');
    container.innerHTML = '';

    const addGroup = (label, items) => {
        if (items.length === 0) return;
        const group = document.createElement('div');
        group.className = 'history-group';
        group.innerHTML = `<h3 id="${label}">${label}</h3>`;
        items.forEach(p => {
            const div = document.createElement('div');
            div.className = 'history-entry';
            div.textContent = p.title?.replace(/(^")|("$)/g, '') || 'Untitled';
            div.dataset.sessionId = p.sessionID;
            div.onclick = () => {
                loadSession(p.sessionID);
            };
            group.appendChild(div);
        });
        container.appendChild(group);
    };

    addGroup('Today', grouped.today);
    addGroup('Previous 7 Days', grouped.last7);
    addGroup('Previous 30 Days', grouped.last30);
    for (const [month, items] of Object.entries(grouped.older)) {
        addGroup(month, items);
    }
}

function loadSession(sessionID) {
    var parser = document.createElement('a');
    parser.href = window.location.href;
    var path = parser.pathname.split("Prompt")[0];
    fetch(parser.protocol +"//"+ parser.host + path + "api/ai/messagesFromPrompt/"+ sessionID)
        .then(res => res.json())
        .then(data => {
            const chatWindow = document.getElementById("chatWindow");
            chatWindow.innerHTML = "";
            data.messages.forEach((msg, index, array) => {
                const msgDiv = document.createElement("div");
                msgDiv.classList.add("message");
                if (msg.role === "user") {
                    msgDiv.classList.add("user");
                }
                if (index === array.length - 1) {
                    msgDiv.id = "last-message";
                }
                msgDiv.innerHTML = DOMPurify.sanitize(msg.message);
                chatWindow.appendChild(msgDiv);
            });
            chatWindow.scrollTop = chatWindow.scrollHeight;
        })
        .catch(err => console.error("Failed to load session:", err));

    $('#userInput').attr( 'data-sessionID', sessionID );
}

function displayPageLabel(doc) {

}