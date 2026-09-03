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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html class="h-full">
<head>
    <meta name="active-menu" content="admin">
    <meta name="active-submenu" content="LogEvent.jsp">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript" src="dependencies/Chart.js-2.9.3/Chart.min.js"></script>
    <%@ include file="include/global/dependenciesInclusions.html" %>

    <style>
        /* Markdown rendering for AI messages in the detail table (js/pages/Usage.js#loadAIUsageDetailTable). */
        .crb-msg-markdown {
            max-width: 420px;
            overflow-wrap: break-word;
            word-break: break-word;
        }
        .crb-msg-markdown p { margin: 0 0 0.5em; }
        .crb-msg-markdown p:last-child { margin-bottom: 0; }
        .crb-msg-markdown ul, .crb-msg-markdown ol { margin: 0 0 0.5em 1.25em; padding: 0; }
        .crb-msg-markdown h1, .crb-msg-markdown h2, .crb-msg-markdown h3,
        .crb-msg-markdown h4, .crb-msg-markdown h5, .crb-msg-markdown h6 {
            font-size: 1em;
            font-weight: 600;
            margin: 0.5em 0 0.25em;
        }
        .crb-msg-markdown blockquote {
            border-left: 3px solid #94a3b8;
            margin: 0 0 0.5em;
            padding-left: 0.75em;
            color: #64748b;
        }
        .crb-msg-markdown pre {
            background: rgba(0, 0, 0, 0.06);
            border-radius: 6px;
            padding: 0.5em 0.75em;
            overflow-x: auto;
            max-width: 100%;
            font-size: 0.85em;
        }
        .crb-msg-markdown code {
            background: rgba(0, 0, 0, 0.06);
            border-radius: 4px;
            padding: 0.1em 0.3em;
            font-size: 0.85em;
        }
        .crb-msg-markdown pre code { background: none; padding: 0; }
        .crb-msg-markdown table { border-collapse: collapse; font-size: 0.85em; }
        .crb-msg-markdown th, .crb-msg-markdown td { border: 1px solid #cbd5e1; padding: 2px 6px; }
        html.dark .crb-msg-markdown pre,
        html.dark .crb-msg-markdown code { background: rgba(255, 255, 255, 0.1); }
        html.dark .crb-msg-markdown blockquote { border-left-color: #64748b; color: #94a3b8; }
        html.dark .crb-msg-markdown th, html.dark .crb-msg-markdown td { border-color: #475569; }
    </style>

    <!-- Définition de aiUsagePage avant que le DOM ne l'utilise -->
    <script>
        function aiUsagePage() {
            return {
                // Filters
                filters: {
                    user: 'ALL',
                    aggregation: 'Month',
                    month: new Date().toISOString().slice(0,7)
                },

                // Users combobox
                users: [],

                // Stats
                totalInput: 0,
                totalOutput: 0,
                estimatedCost: 0.0,

                // Month picker
                currentMonth: new Date(),
                displayMonth: '',

                init() {
                    this.loadUsers();
                    this.updateDisplayMonth();
                    this.loadStats();
                    this.loadChart();
                },

                // Fetch users from API
                loadUsers() {
                    fetch('./api/usage/readDistinctValueOfColumn?columnName=UsrCreated')
                        .then(res => res.json())
                        .then(data => {
                            if(data?.distinctValues) {
                                this.users = data.distinctValues;
                            }
                        })
                        .catch(err => console.error("Erreur fetch distinct users:", err));
                },

                updateDisplayMonth() {
                    const options = { month: 'long', year: 'numeric' };
                    this.displayMonth = this.currentMonth.toLocaleDateString('fr-FR', options);
                    this.filters.month = this.currentMonth.getFullYear() + '-' + String(this.currentMonth.getMonth() + 1).padStart(2,'0');
                },

                prevMonth() {
                    this.currentMonth.setMonth(this.currentMonth.getMonth() - 1);
                    this.updateDisplayMonth();
                    this.loadStats();
                    this.loadChart();
                    loadAIUsageTable();
                },

                nextMonth() {
                    this.currentMonth.setMonth(this.currentMonth.getMonth() + 1);
                    this.updateDisplayMonth();
                    this.loadStats();
                    this.loadChart();
                    loadAIUsageTable();
                },

                formatDate(date) {
                    return date.getFullYear() + '-' +
                        String(date.getMonth()+1).padStart(2,'0') + '-' +
                        String(date.getDate()).padStart(2,'0') + 'T' +
                        String(date.getHours()).padStart(2,'0') + ':' +
                        String(date.getMinutes()).padStart(2,'0') + ':' +
                        String(date.getSeconds()).padStart(2,'0');
                },

                loadStats() {
                    const [year, month] = this.filters.month.split('-');
                    const startDate = this.formatDate(new Date(year, month-1, 1, 0,0,0));
                    const endDate   = this.formatDate(new Date(year, month, 0, 23,59,59));
                    const user = this.filters.user;

                    fetch('./api/usage/stats?startDate=' + encodeURIComponent(startDate)
                        + '&endDate=' + encodeURIComponent(endDate)
                        + '&user=' + encodeURIComponent(user))
                        .then(res => res.json())
                        .then(data => {
                            this.totalInput = data.totalInputTokens;
                            this.totalOutput = data.totalOutputTokens;
                            this.estimatedCost = new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(data.totalCost);
                        })
                        .catch(err => console.error("Erreur fetch stats AI Usage:", err));
                },

                loadChart() {
                    const [year, month] = this.filters.month.split('-');
                    const start = new Date(year, month-1, 1);
                    const end = new Date(year, month, 0);

                    const startDate = this.formatDate(new Date(year, month-1, 1, 0,0,0));
                    const endDate   = this.formatDate(new Date(year, month, 0, 23,59,59));

                    const user = this.filters.user;

                    fetch('./api/usage/usageByDay?startDate=' + encodeURIComponent(startDate)
                        + '&endDate=' + encodeURIComponent(endDate)
                        + '&user=' + encodeURIComponent(user))
                        .then(res => res.json())
                        .then(data => {


                            const labels = [];
                            const inputTokens = [];
                            const outputTokens = [];

                            for(let d = new Date(start); d <= end; d.setDate(d.getDate() + 1)) {
                                const dayStr = d.toISOString().slice(0,10); // format YYYY-MM-DD
                                labels.push(dayStr);

                                //
                                const dayData = data.find(item => item.fromDate === dayStr);

                                //
                                inputTokens.push(dayData ? dayData.totalInputTokens : 0);
                                outputTokens.push(dayData ? dayData.totalOutputTokens : 0);
                            }

                            const ctx = document.getElementById('usageChart').getContext('2d');

                            if (this.usageChart) {
                                this.usageChart.data.labels = labels;
                                this.usageChart.data.datasets[0].data = inputTokens;
                                this.usageChart.data.datasets[1].data = outputTokens;
                                this.usageChart.update();
                            } else {
                                this.usageChart = new Chart(ctx, {
                                    type: 'bar', // ✅ Bar chart
                                    data: {
                                        labels: labels,
                                        datasets: [
                                            {
                                                label: 'Input Tokens',
                                                data: inputTokens,
                                                backgroundColor: 'rgba(54, 162, 235, 0.7)',
                                                borderColor: 'rgba(54, 162, 235, 0.7)',
                                                borderWidth: 1,
                                                stack: 'tokens'
                                            },
                                            {
                                                label: 'Output Tokens',
                                                data: outputTokens,
                                                backgroundColor: 'rgba(255, 99, 132, 0.7)',
                                                borderColor: 'rgba(255, 99, 132, 0.7)',
                                                borderWidth: 1,
                                                stack: 'tokens'
                                            }
                                        ]
                                    },
                                    options: {
                                        responsive: true,
                                        plugins: {
                                            legend: { position: 'top' },
                                            tooltip: { mode: 'index', intersect: false }
                                        },
                                        interaction: { mode: 'nearest', axis: 'x', intersect: false },
                                        scales: {
                                            x: {
                                                stacked: true,
                                                title: { display: true, text: 'Date' }
                                            },
                                            y: {
                                                stacked: true,
                                                beginAtZero: true,
                                                title: { display: true, text: 'Tokens' },
                                                ticks: {
                                                    callback: value => value.toLocaleString()
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        })
                        .catch(err => console.error("Erreur fetch chart AI Usage:", err));
                }
            }
        }
    </script>
    <script type="text/javascript" src="js/pages/Usage.js?v=${appVersion}" defer></script>

    <title id="pageTitle">Usage</title>
</head>
<body x-data x-cloak class="crb_body">
    <jsp:include page="include/global/header2.html"/>
    <jsp:include page="include/global/modalInclusions.jsp"/>
    <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">

        <h1 class="page-title-line" id="title">AI Usage</h1>

        <!-- Conteneur principal Alpine.js -->
        <div x-data="aiUsagePage()" x-init="init()" class="w-full">

            <!-- Content Public -->
            <div class="space-y-6">

                <!-- Filters -->
                <div class="crb_card p-4 grid grid-cols-3 gap-4">
                    <!-- Users -->
                    <div>
                        <label class="block text-sm font-medium mb-1">Users</label>
                        <select x-model="filters.user" @change="loadStats();loadChart();loadAIUsageTable(filters.user)" class="w-full border rounded p-2 h-10">
                            <option value="ALL">ALL</option>
                            <template x-for="user in users" :key="user">
                                <option :value="user" x-text="user"></option>
                            </template>
                        </select>
                    </div>

                    <!-- Aggregation -->
                    <div>
                        <label class="block text-sm font-medium mb-1">Aggregate</label>
                        <select x-model="filters.aggregation" class="w-full border rounded p-2 h-10">
                            <option selected>Month</option>
                        </select>
                    </div>

                    <!-- Month Picker -->
                    <div>
                        <label class="block text-sm font-medium mb-1">Period</label>
                        <div class="flex items-center gap-2 border rounded-lg p-2 h-10">
                            <button @click="prevMonth()" type="button">◀</button>
                            <span class="flex-1 text-center" x-text="displayMonth"></span>
                            <button @click="nextMonth()" type="button">▶</button>
                        </div>
                    </div>
                </div>

                <!-- Totals -->
                <div class="grid grid-cols-3 gap-4">
                    <div class="crb_card">
                        <p class="text-md font-medium mb-2">Total Input Tokens</p>
                        <div class="text-2xl font-bold" x-text="totalInput"></div>
                    </div>
                    <div class="crb_card">
                        <p class="text-md font-medium mb-2">Total Output Tokens</p>
                        <div class="text-2xl font-bold" x-text="totalOutput"></div>
                    </div>
                    <div class="crb_card">
                        <p class="text-md font-medium mb-2">Estimated Cost</p>
                        <div class="text-2xl font-bold" x-text="estimatedCost"></div>
                    </div>
                </div>

                <!-- Chart -->
                <div class="crb_card">
                    <h3 class="text-lg font-medium mb-4">Mensual Trend (Tokens / day)</h3>
                    <canvas id="usageChart" height="100"></canvas>
                </div>

                <!-- Table: Calls list -->
                <div id="aiUsageListSection">
                    <div class="flex items-center gap-3 mb-4">
                        <span class="flex items-center font-medium text-lg">
                            <i data-lucide="list" class="w-4 h-4 mr-2 text-blue-600 flex-shrink-0"></i>
                            <span>AI Sessions</span>
                        </span>
                    </div>
                    <div id="aiUsage" class="crb_card" style="padding: 0; overflow: hidden"></div>
                </div>

                <!-- Table: Call detail (individual messages), hidden until a call is opened -->
                <div id="aiUsageDetailSection" class="hidden">
                    <div class="flex items-center gap-3 mb-4">
                        <button type="button" onclick="closeAIUsageDetail()"
                                class="flex items-center gap-1 px-3 py-1 rounded-lg border border-slate-300 dark:border-slate-600 hover:bg-slate-100 dark:hover:bg-slate-700 text-sm">
                            <i data-lucide="arrow-left" class="w-4 h-4"></i>Back
                        </button>
                        <span class="flex items-center font-medium text-lg">
                            <i data-lucide="messages-square" class="w-4 h-4 mr-2 text-blue-600 flex-shrink-0"></i>
                            <span id="aiUsageDetailTitle">Messages</span>
                        </span>
                    </div>
                    <div id="aiUsageDetail" class="crb_card" style="padding: 0; overflow: hidden"></div>
                </div>

            </div>

        </div>

        <footer class="footer">
            <div class="container-fluid" id="footer"></div>
        </footer>

    </main>
</body>
</html>
