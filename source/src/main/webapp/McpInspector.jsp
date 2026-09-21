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
        <meta name="active-menu" content="developer">
        <meta name="active-submenu" content="McpInspector.jsp">
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="js/pages/McpInspector.js?v=${appVersion}"></script>

        <title id="pageTitle">MCP Inspector</title>
    </head>
    <body x-data x-cloak class="crb_body">
        <jsp:include page="include/global/header2.html"/>
        <jsp:include page="include/global/modalInclusions.jsp"/>
        <main class="crb_main" :class="$store.sidebar.expanded ? 'crb_main_sidebar-expanded' : 'crb_main_sidebar-collapsed'">
            <%@ include file="include/global/messagesArea.html"%>

            <div x-data="mcpInspector()" x-init="init()">

                <div class="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <h1 class="page-title-line" id="title">MCP Inspector</h1>
                        <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">Testez les tools MCP exposés par ce serveur et suivez vos appels.</p>
                    </div>
                    <div class="inline-flex items-center gap-2 rounded-xl border border-slate-200 dark:border-slate-700 px-3 py-2">
                        <span class="size-2 rounded-full" :class="connected ? 'bg-emerald-500' : 'bg-slate-300 dark:bg-slate-600'"></span>
                        <span class="crb_code" x-text="mcpEndpointUrl()"></span>
                        <button @click="copyText(mcpEndpointUrl())" title="Copier l'URL" class="crb_table_iconbtn">
                            <i data-lucide="copy" class="w-3.5 h-3.5"></i>
                        </button>
                    </div>
                </div>

                <!-- KPIs (inspired by the Homepage's card-with-tabs: icon badge + title header, big value) -->
                <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4 mt-6">
                    <div class="crb_card !mb-0">
                        <div class="flex items-center space-x-3 mb-3">
                            <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-blue-500">
                                <i data-lucide="wrench" class="w-5 h-5 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold text-slate-700 dark:text-slate-200">Tools</span>
                        </div>
                        <div class="text-3xl font-bold text-slate-900 dark:text-slate-100" x-text="tools.length"></div>
                        <div class="text-slate-500 dark:text-slate-400 text-sm mt-1">Disponibles sur ce serveur</div>
                    </div>
                    <div class="crb_card !mb-0">
                        <div class="flex items-center space-x-3 mb-3">
                            <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-indigo-500">
                                <i data-lucide="layers" class="w-5 h-5 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold text-slate-700 dark:text-slate-200">Workspaces actifs</span>
                        </div>
                        <div class="text-3xl font-bold text-slate-900 dark:text-slate-100" x-text="activeSystemsLabel"></div>
                        <div class="text-slate-500 dark:text-slate-400 text-sm mt-1">Contexte système courant</div>
                    </div>
                    <div class="crb_card !mb-0">
                        <div class="flex items-center space-x-3 mb-3">
                            <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-emerald-500">
                                <i data-lucide="activity" class="w-5 h-5 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold text-slate-700 dark:text-slate-200">Appels</span>
                        </div>
                        <div class="text-3xl font-bold text-slate-900 dark:text-slate-100" x-text="history.length"></div>
                        <div class="text-slate-500 dark:text-slate-400 text-sm mt-1">Depuis l'ouverture de cette page</div>
                    </div>
                    <div class="crb_card !mb-0">
                        <div class="flex items-center space-x-3 mb-3">
                            <div class="w-10 h-10 rounded-xl flex items-center justify-center bg-amber-500">
                                <i data-lucide="shield-check" class="w-5 h-5 text-white"></i>
                            </div>
                            <span class="text-sm font-semibold text-slate-700 dark:text-slate-200">Taux de succès</span>
                        </div>
                        <div class="text-3xl font-bold text-slate-900 dark:text-slate-100" x-text="successRateLabel"></div>
                        <div class="text-slate-500 dark:text-slate-400 text-sm mt-1" x-text="history.length + ' appel(s) sur cette session'"></div>
                    </div>
                </div>

                <div class="crb_card mt-4">

                    <!-- Authorize bar -->
                    <template x-if="!connected">
                        <div class="flex flex-wrap items-center gap-2">
                            <input type="password" x-model="apiKeyInput" placeholder="Clé API Cerberus (X-API-KEY)"
                                   class="crb_input rounded-md px-3 py-2 text-sm w-72"
                                   @keydown.enter="authorize()">
                            <button @click="authorize()" :disabled="connecting"
                                    class="bg-blue-600 dark:bg-blue-500 !text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50">
                                <span x-show="!connecting">Authorize</span>
                                <span x-show="connecting">Connexion…</span>
                            </button>
                            <span x-show="error" x-text="error" class="text-sm text-red-600 dark:text-red-400"></span>
                        </div>
                    </template>

                    <template x-if="connected">
                        <div>
                            <div class="flex flex-wrap items-center justify-between gap-2 mb-4">
                                <div class="crb_tabs !mb-0">
                                    <button type="button" class="crb_tab" :class="activeTab === 'playground' ? 'crb_tab--active' : ''" @click="activeTab = 'playground'">
                                        <i data-lucide="terminal" class="w-4 h-4"></i> Playground
                                    </button>
                                    <button type="button" class="crb_tab" :class="activeTab === 'history' ? 'crb_tab--active' : ''" @click="activeTab = 'history'">
                                        <i data-lucide="history" class="w-4 h-4"></i> Historique
                                        <span class="crb_tab_badge" x-show="history.length > 0" x-text="history.length"></span>
                                    </button>
                                    <button type="button" class="crb_tab" :class="activeTab === 'connection' ? 'crb_tab--active' : ''" @click="activeTab = 'connection'">
                                        <i data-lucide="plug" class="w-4 h-4"></i> Connexion
                                    </button>
                                </div>
                                <div class="flex items-center gap-2">
                                    <button @click="refreshTools()" class="crb_table_iconbtn">
                                        <i data-lucide="refresh-cw" class="w-3.5 h-3.5"></i> Rafraîchir
                                    </button>
                                    <button @click="logout()" class="crb_table_iconbtn">
                                        <i data-lucide="key-round" class="w-3.5 h-3.5"></i> Changer de clé
                                    </button>
                                </div>
                            </div>

                            <!-- Playground -->
                            <div x-show="activeTab === 'playground'" class="grid gap-4 lg:grid-cols-[300px_1fr]">

                                <!-- Tool list -->
                                <aside class="rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 p-3 flex flex-col w-full">
                                    <div class="relative w-full">
                                        <i data-lucide="search" class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400"></i>
                                        <input type="text" x-model="search" placeholder="Rechercher un tool…"
                                               class="crb_input rounded-md pl-9 pr-3 py-1.5 text-sm w-full block">
                                    </div>
                                    <p class="mt-2 px-1 text-xs text-slate-400 w-full" x-text="filteredTools.length + ' / ' + tools.length + ' tool(s)'"></p>
                                    <ul class="mt-3 w-full px-1 space-y-1 max-h-[65vh] overflow-y-auto no-scrollbar"
                                        x-effect="filteredTools.length; $nextTick(() => window.lucide && lucide.createIcons())">
                                        <template x-for="tool in filteredTools" :key="tool.name">
                                            <li>
                                                <button @click="selectTool(tool)"
                                                        class="w-full rounded-xl px-3 py-2 text-left transition-colors"
                                                        :class="selectedTool && selectedTool.name === tool.name ? 'bg-blue-50 dark:bg-blue-500/10 ring-1 ring-blue-200 dark:ring-blue-500/25' : 'hover:bg-slate-50 dark:hover:bg-slate-800'">
                                                    <div class="flex items-center justify-between gap-2">
                                                        <span class="font-mono text-[13px] font-semibold text-slate-900 dark:text-slate-100 truncate" x-text="tool.name"></span>
                                                        <i :data-lucide="actionBadge(tool).icon" :class="actionBadge(tool).cls" :title="actionBadge(tool).label" class="w-4 h-4 shrink-0"></i>
                                                    </div>
                                                    <p class="mt-0.5 text-xs text-slate-500 dark:text-slate-400 truncate" x-text="firstLine(tool.description)"></p>
                                                </button>
                                            </li>
                                        </template>
                                        <li x-show="filteredTools.length === 0" class="p-3 text-sm text-slate-400">Aucun tool ne correspond.</li>
                                    </ul>
                                </aside>

                                <!-- Selected tool -->
                                <section>
                                    <template x-if="!selectedTool">
                                        <div class="rounded-2xl border border-dashed border-slate-200 dark:border-slate-700 p-8 text-center text-sm text-slate-400">
                                            Sélectionnez un tool à gauche pour le tester.
                                        </div>
                                    </template>

                                    <template x-if="selectedTool">
                                        <div class="rounded-2xl border border-slate-200 dark:border-slate-700 p-6">
                                            <div class="flex flex-wrap items-start justify-between gap-4">
                                                <div>
                                                    <h3 class="font-mono text-lg font-bold text-slate-900 dark:text-slate-100" x-text="selectedTool.name"></h3>
                                                    <p class="mt-1 max-w-xl text-sm text-slate-500 dark:text-slate-400" x-text="selectedTool.description"></p>
                                                    <div class="mt-3 flex flex-wrap gap-2">
                                                        <span class="crb_tc_chip" :class="selectedTool.annotations && selectedTool.annotations.readOnlyHint ? 'crb_tc_chip--info' : 'crb_tc_chip--neutral'"
                                                              x-text="selectedTool.annotations && selectedTool.annotations.readOnlyHint ? 'Lecture seule' : 'Écriture'"></span>
                                                        <span class="crb_tc_chip crb_tc_chip--warn" x-show="selectedTool.annotations && selectedTool.annotations.destructiveHint">Destructif</span>
                                                        <span class="crb_tc_chip crb_tc_chip--neutral" x-text="parameterCount() + ' paramètre(s)'"></span>
                                                    </div>
                                                </div>
                                                <button @click="callTool()" :disabled="calling"
                                                        class="inline-flex items-center gap-2 bg-blue-600 dark:bg-blue-500 !text-white text-sm font-medium px-4 py-2 rounded-md hover:bg-blue-700 disabled:opacity-50">
                                                    <i data-lucide="play" class="w-4 h-4"></i>
                                                    <span x-show="!calling">Exécuter</span>
                                                    <span x-show="calling">Appel…</span>
                                                </button>
                                            </div>

                                            <div class="mt-6 grid gap-5 xl:grid-cols-2">
                                                <!-- Parameters form -->
                                                <div class="space-y-3">
                                                    <h4 class="crb_table_sectiontitle">Paramètres</h4>
                                                    <template x-if="parameterCount() === 0">
                                                        <p class="text-xs text-slate-400">Ce tool ne prend aucun paramètre.</p>
                                                    </template>
                                                    <template x-for="key in Object.keys((selectedTool.inputSchema && selectedTool.inputSchema.properties) || {})" :key="key">
                                                        <div class="space-y-1">
                                                            <label class="font-mono text-xs font-medium text-slate-700 dark:text-slate-200">
                                                                <span x-text="key"></span>
                                                                <span x-show="isRequired(key)" class="text-red-500">*</span>
                                                                <span class="ml-1 font-sans font-normal text-slate-400" x-text="'(' + (selectedTool.inputSchema.properties[key].type || 'any') + ')'"></span>
                                                            </label>

                                                            <template x-if="selectedTool.inputSchema.properties[key].type === 'boolean'">
                                                                <input type="checkbox" x-model="formValues[key]" class="h-4 w-4 rounded border-slate-300">
                                                            </template>

                                                            <template x-if="selectedTool.inputSchema.properties[key].enum">
                                                                <select x-model="formValues[key]" class="crb_input rounded-md px-2 py-1.5 text-sm w-full">
                                                                    <option value=""></option>
                                                                    <template x-for="option in selectedTool.inputSchema.properties[key].enum" :key="option">
                                                                        <option :value="option" x-text="option"></option>
                                                                    </template>
                                                                </select>
                                                            </template>

                                                            <template x-if="!selectedTool.inputSchema.properties[key].enum && selectedTool.inputSchema.properties[key].type !== 'boolean'">
                                                                <input type="text" x-model="formValues[key]"
                                                                       class="crb_input rounded-md px-2 py-1.5 text-sm w-full"
                                                                       :placeholder="selectedTool.inputSchema.properties[key].type === 'array' ? 'valeur1,valeur2' : ''">
                                                            </template>

                                                            <p class="text-xs text-slate-400" x-text="selectedTool.inputSchema.properties[key].description"></p>
                                                        </div>
                                                    </template>
                                                </div>

                                                <!-- Request / response preview -->
                                                <div class="space-y-3">
                                                    <div class="flex items-center justify-between">
                                                        <h4 class="crb_table_sectiontitle">Requête</h4>
                                                        <button @click="copyText(requestPreview())" class="crb_table_iconbtn">
                                                            <i data-lucide="copy" class="w-3.5 h-3.5"></i> Copier
                                                        </button>
                                                    </div>
                                                    <pre class="max-h-52 overflow-auto rounded-xl bg-slate-900 dark:bg-slate-950 p-4 font-mono text-xs leading-relaxed text-slate-100" x-text="requestPreview()"></pre>

                                                    <h4 class="crb_table_sectiontitle">Réponse</h4>
                                                    <template x-if="!result">
                                                        <pre class="max-h-72 min-h-24 overflow-auto rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800/50 p-4 text-xs text-slate-400">Lancez le tool pour voir la réponse.</pre>
                                                    </template>
                                                    <template x-if="result">
                                                        <div>
                                                            <span class="crb_tc_chip mb-2 inline-block" :class="result.isError ? 'crb_tc_chip--warn' : 'crb_tc_chip--on'"
                                                                  x-text="result.isError ? 'Erreur' : 'Succès'"></span>
                                                            <pre class="max-h-72 overflow-auto rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 p-4 font-mono text-xs leading-relaxed text-slate-800 dark:text-slate-100" x-text="renderResultContent(result)"></pre>
                                                        </div>
                                                    </template>
                                                </div>
                                            </div>
                                        </div>
                                    </template>
                                </section>
                            </div>

                            <!-- History -->
                            <div x-show="activeTab === 'history'" class="rounded-2xl border border-slate-200 dark:border-slate-700 overflow-hidden">
                                <table class="crb_table w-full">
                                    <thead>
                                        <tr>
                                            <th class="crb_table_th">Heure</th>
                                            <th class="crb_table_th">Tool</th>
                                            <th class="crb_table_th">Statut</th>
                                            <th class="crb_table_th">Durée</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <template x-for="(entry, index) in history" :key="index">
                                            <tr class="crb_table_tr">
                                                <td class="crb_table_td text-xs text-slate-500" x-text="formatTimestamp(entry.at)"></td>
                                                <td class="crb_table_td"><span class="crb_code" x-text="entry.name"></span></td>
                                                <td class="crb_table_td">
                                                    <span class="crb_tc_chip" :class="entry.ok ? 'crb_tc_chip--on' : 'crb_tc_chip--warn'" x-text="entry.ok ? 'OK' : 'Erreur'"></span>
                                                </td>
                                                <td class="crb_table_td text-xs text-slate-500" x-text="entry.durationMs + ' ms'"></td>
                                            </tr>
                                        </template>
                                    </tbody>
                                </table>
                                <p x-show="history.length === 0" class="p-4 text-sm text-slate-400">Aucun appel effectué sur cette session.</p>
                            </div>

                            <!-- Connection -->
                            <div x-show="activeTab === 'connection'" class="grid gap-4 lg:grid-cols-2">
                                <div class="rounded-2xl border border-slate-200 dark:border-slate-700 p-6">
                                    <h3 class="text-base font-semibold text-slate-900 dark:text-slate-100">Connecter un assistant</h3>
                                    <p class="mt-1 text-sm text-slate-500 dark:text-slate-400">
                                        Ajoutez cette configuration dans Claude Desktop, Claude Code ou un autre client MCP.
                                        Les tools s'exécutent avec les droits de la clé API fournie, restreints à son contexte
                                        système actif (voir <span class="crb_code">cerberus_context_system_list</span>).
                                    </p>
                                    <pre class="mt-4 overflow-auto rounded-xl bg-slate-900 dark:bg-slate-950 p-4 font-mono text-xs leading-relaxed text-slate-100" x-text="connectionConfigPreview()"></pre>
                                    <button @click="copyText(connectionConfigPreview())"
                                            class="inline-flex items-center gap-2 mt-4 rounded-md border border-slate-200 dark:border-slate-700 px-4 py-2 text-sm hover:bg-slate-50 dark:hover:bg-slate-800">
                                        <i data-lucide="copy" class="w-4 h-4"></i> Copier la configuration
                                    </button>
                                </div>

                                <div class="rounded-2xl border border-slate-200 dark:border-slate-700 p-6">
                                    <h3 class="text-base font-semibold text-slate-900 dark:text-slate-100">Détails de connexion</h3>
                                    <dl class="mt-4 space-y-3 text-sm">
                                        <div class="flex items-center justify-between gap-3">
                                            <dt class="text-slate-500 dark:text-slate-400">Endpoint</dt>
                                            <dd class="crb_code truncate" x-text="mcpEndpointUrl()"></dd>
                                        </div>
                                        <div class="flex items-center justify-between gap-3">
                                            <dt class="text-slate-500 dark:text-slate-400">Authentification</dt>
                                            <dd class="crb_code">X-API-KEY</dd>
                                        </div>
                                        <div class="flex items-center justify-between gap-3">
                                            <dt class="text-slate-500 dark:text-slate-400">Protocole MCP</dt>
                                            <dd class="crb_code">2024-11-05</dd>
                                        </div>
                                        <div class="flex items-center justify-between gap-3">
                                            <dt class="text-slate-500 dark:text-slate-400">Session</dt>
                                            <dd class="crb_code truncate" x-text="sessionId || '—'"></dd>
                                        </div>
                                        <div class="flex items-center justify-between gap-3">
                                            <dt class="text-slate-500 dark:text-slate-400">Tools exposés</dt>
                                            <dd class="crb_code" x-text="tools.length"></dd>
                                        </div>
                                    </dl>
                                </div>
                            </div>
                        </div>
                    </template>
                </div>
            </div>

            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </main>
    </body>
</html>