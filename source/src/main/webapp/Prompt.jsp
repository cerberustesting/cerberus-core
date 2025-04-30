<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%-- 
    Document   : Test2
    Created on : 22 sept. 2015, 10:54:19
    Author     : cerberus
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/Prompt.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/dompurify@3.0.6/dist/purify.min.js"></script>
        <title id="pageTitle">Prompt</title>
        <style>
            body, html {
                margin: 0;
                padding: 0;
                font-family: Arial, sans-serif;
                background-color: #f9f9f9;
            }

            .chat-container {
                display: flex;
                flex-direction: column;
                height: 80vh;
            }

            .chat-window {
                flex-grow: 1;
                padding: 1rem;
                overflow-y: auto;
                display: flex;
                flex-direction: column;
                gap: 1rem;
            }

            .message {
                max-width: 80%;
                padding: 0.75rem 1rem;
                border-radius: 12px;
                background-color: #e0e0e0;
                align-self: flex-start;
            }

            .message.user {
                background-color: #d1e7ff;
                align-self: flex-end;
            }

            .input-bar {
                padding: 1rem;
                background-color: white;
                display: flex;
                border-top: 1px solid #ccc;
            }

            .input-bar input {
                flex-grow: 1;
                padding: 0.75rem;
                border: 1px solid #ccc;
                border-radius: 20px;
                outline: none;
            }

            .input-bar button {
                margin-left: 0.5rem;
                padding: 0.75rem 1rem;
                border: none;
                border-radius: 20px;
                background-color: #007bff;
                color: white;
                cursor: pointer;
            }

            .input-bar button:hover {
                background-color: #0056b3;
            }

            .typing {
                display: inline-block;
                font-size: 1rem;
                color: #666;
                font-style: italic;
            }

            .typing::after {
                content: '⏳';
                animation: blink 1s infinite;
                margin-left: 5px;
            }

            @keyframes blink {
                0%, 100% { opacity: 0.2; }
                50% { opacity: 1; }
            }
        </style>
    </head>

    <body>
        <%@ include file="include/global/header.html"%>
        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html"%>
            <%@ include file="include/utils/modal-confirmation.html"%>

            <h1 class="page-title-line" id="title">Ask Cerberus</h1>
            <div class="panel panel-default">
                <div class="chat-container">
                    <div class="chat-window" id="chatWindow">
                        <div id="last-message"></div><!-- Messages will appear here -->
                    </div>
                    <div class="input-bar">
                        <input type="text" id="userInput" placeholder="Type your message..." />
                        <button id="sendMessageButton">Send</button>
                    </div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>

    </body>
</html>
