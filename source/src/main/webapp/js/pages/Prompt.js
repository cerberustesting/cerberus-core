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
    const socket = new WebSocket("ws://localhost:8080/cerberus_core_war/api/ws/chatai"); // Adjust to your backend port/path

    var username = getUser().name;
    // Append incoming streamed data
    var textContent = "";
    socket.onmessage = function(event) {
        const botMsg = document.getElementById('last-message');
        botMsg.className = 'message';
        botMsg.classList.remove('typing');
        textContent += event.data;
        //botMsg.innerHTML = marked.parse(textContent);
        botMsg.innerHTML = DOMPurify.sanitize(textContent);

        const chatWindow = document.getElementById('chatWindow');
        chatWindow.scrollTop = chatWindow.scrollHeight;
        const input = document.getElementById('userInput');
        input.value = '';
        input.focus();
    };



    // Handle open connection
    socket.onopen = function() {
        console.log("Connected to WebSocket!");
        const msg = {
            sender: username,
            content: "Hello, I'm "+username+". Could you give me some example of things that I can ask you related to Cerberus-Testing?"
        };
        socket.send(JSON.stringify(msg));
    };

    // Handle errors
    socket.onerror = function(error) {
        console.error("WebSocket error:", error);
    };

    socket.onclose = function(event) {
        console.log("WebSocket closed:", event);
    };

    $('#sendMessageButton').click(function () {
        const input = document.getElementById('userInput');
        const text = input.value.trim();
        const msg = {
            sender: "Benoit",
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

}


function displayPageLabel(doc) {

}