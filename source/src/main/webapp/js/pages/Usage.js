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
    displayPageLabel();
    loadAIUsageTable();
}

function loadAIUsageTable(filter) {
    var existing = crbTableInstance('aiUsageTable');
    if (existing) {
        existing.search = (filter && filter !== "ALL") ? filter : "";
        existing.onSearchInput();
        return;
    }

    createCerberusTable({
        id: 'aiUsageTable',
        mount: '#aiUsage',
        embedded: true,
        endpoint: 'api/usage/aiCallList',
        rowKey: 'id',
        defaultSort: {field: 'id', dir: 'desc'},
        searchPlaceholder: 'Search AI usage calls...',
        emptyMessage: 'No AI usage call found',
        initialSearch: filter && filter !== "ALL" ? filter : "",
        columns: aoColumnsFuncAIUsage(),
        actions: [
            {
                key: 'view', icon: 'eye', gate: 'always', title: 'View Details',
                onClick: function (row) { viewAIUsage(row); }
            }
        ]
    });
}

/**
 * Opens the message-detail table for one AI usage call, in place of the calls list.
 */
function viewAIUsage(row) {
    $('#aiUsageListSection').addClass('hidden');
    $('#aiUsageDetailSection').removeClass('hidden');
    $('#aiUsageDetailTitle').text(
        'Messages — Session ' + row.sessionID + (row.login ? ' (' + row.login + ')' : '')
    );
    loadAIUsageDetailTable(row.sessionID);
}

function closeAIUsageDetail() {
    $('#aiUsageDetailSection').addClass('hidden');
    $('#aiUsageListSection').removeClass('hidden');
}

function loadAIUsageDetailTable(sessionID) {
    fetch('./api/usage/messagesFromPrompt/' + encodeURIComponent(sessionID))
        .then(function (res) { return res.json(); })
        .then(function (rows) {
            if (crbTableInstance('aiUsageMessagesTable')) {
                crbTableSetRows('aiUsageMessagesTable', rows);
                return;
            }

            createCerberusTable({
                id: 'aiUsageMessagesTable',
                mount: '#aiUsageDetail',
                embedded: true,
                clientRows: rows,
                rowKey: 'id',
                defaultSort: {field: 'id', dir: 'asc'},
                pageLength: 20,
                searchPlaceholder: 'Search messages...',
                emptyMessage: 'No message found for this session',
                columns: [
                    {field: 'role', title: 'Role', width: '90px'},
                    {
                        field: 'message', title: 'Message', width: '420px',
                        render: function (row) {
                            if (!row.message) { return ""; }
                            var html = DOMPurify.sanitize(marked.parse(row.message));
                            return '<div class="crb-msg-markdown">' + html + '</div>';
                        }
                    },
                    {field: 'tokens', title: 'Tokens', width: '80px'},
                    {
                        field: 'cost', title: 'Cost ($)', width: '80px',
                        render: function (row) { return row.cost ? row.cost.toFixed(4) : "0.0000"; }
                    },
                    {
                        field: 'dateCreated', title: 'Date', width: '130px',
                        render: function (row) { return row.dateCreated ? getDate(row.dateCreated) : ""; }
                    }
                ],
                actions: []
            });
        })
        .catch(function (err) { console.error("Erreur fetch messages AI Usage:", err); });
}

function displayPageLabel() {
    var doc = new Doc();
    displayFooter(doc);
    displayGlobalLabel(doc);
}

function aoColumnsFuncAIUsage() {
    return [
        {field: 'id', title: 'ID', width: '40px'},
        {field: 'login', title: 'User', width: '90px'},
        {field: 'sessionID', title: 'Session ID', width: '90px'},
        {field: 'iaModel', title: 'Model', width: '90px'},
        {field: 'iaMaxTokens', title: 'Max Tokens', width: '80px'},
        {field: 'type', title: 'Type', width: '80px'},
        {
            field: 'title', title: 'Title', width: '250px', like: true,
            render: function (row) {
                return row.title ? crbTableEscape(row.title.substring(0, 80)) + "..." : "";
            }
        },
        {field: 'totalCalls', title: 'Total Calls', width: '80px'},
        {field: 'totalInputTokens', title: 'Total Input Tokens', width: '80px'},
        {field: 'totalOutputTokens', title: 'Total Output Tokens', width: '80px'},
        {
            field: 'totalCost', title: 'Cost ($)', width: '60px',
            render: function (row) { return row.totalCost ? row.totalCost.toFixed(2) : "0.00"; }
        },
        {field: 'usrCreated', title: 'User', width: '90px'},
        {
            field: 'dateCreated', title: 'Created', width: '130px',
            render: function (row) { return row.dateCreated ? getDate(row.dateCreated) : ""; }
        }
    ];
}
