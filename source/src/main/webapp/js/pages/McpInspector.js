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

/**
 * Minimal MCP (streamable HTTP) client for the /mcp endpoint, driving an Alpine
 * "try it out" UI — the in-app equivalent of the MCP Inspector, scoped to Cerberus.
 *
 * /mcp sits behind its own stateless + httpBasic Spring Security chain
 * (WebSecurityLocalConfiguration#mcpSecurityFilterChain), so the browser's normal
 * session cookie does not authenticate it. Reuses the existing X-API-KEY fallback
 * (McpApiKeyAuthFilter) instead — the user pastes their own Cerberus API key once,
 * exactly like the "Authorize" box in a public Swagger UI.
 */
function mcpInspector() {
    return {
        apiKeyInput: '',
        apiKey: '',
        sessionId: null,
        nextId: 1,

        connecting: false,
        connected: false,
        error: '',

        activeTab: 'playground',

        tools: [],
        search: '',
        selectedTool: null,
        formValues: {},
        activeSystems: null,

        calling: false,
        result: null,
        history: [],

        init() {
            const saved = sessionStorage.getItem('mcpInspectorApiKey');
            if (saved) {
                this.apiKeyInput = saved;
                this.apiKey = saved;
                this.connect();
            }
        },

        get filteredTools() {
            if (!this.search.trim()) {
                return this.tools;
            }
            const q = this.search.toLowerCase();
            return this.tools.filter(t =>
                t.name.toLowerCase().includes(q) || (t.description || '').toLowerCase().includes(q));
        },

        get successRateLabel() {
            if (this.history.length === 0) {
                return '—';
            }
            const okCount = this.history.filter(h => h.ok).length;
            return (100 * okCount / this.history.length).toFixed(1) + ' %';
        },

        get activeSystemsLabel() {
            return this.activeSystems === null ? '—' : String(this.activeSystems);
        },

        connectionConfigPreview() {
            return JSON.stringify({
                mcpServers: {
                    cerberus: {
                        type: 'http',
                        url: this.mcpEndpointUrl(),
                        headers: {
                            'X-API-KEY': '<votre clé API>'
                        }
                    }
                }
            }, null, 2);
        },

        /**
         * First line of a tool description, for the compact list row.
         *
         * line-clamp-1 alone isn't reliable here: descriptions are multi-paragraph text
         * blocks, and this app layers Tailwind under Bootstrap's still-unlayered base
         * styles, which can win the cascade regardless of specificity. Truncating in JS
         * guarantees a short row no matter how that CSS fight resolves.
         */
        firstLine(description) {
            if (!description) {
                return '';
            }
            const line = description.split('\n')[0].trim();
            return line.length > 90 ? line.slice(0, 90) + '…' : line;
        },

        /**
         * Classifies a tool as read/create/update/delete for the list icon.
         *
         * Cerberus tool names follow a consistent _list/_get/_create/_update/_delete
         * suffix convention, which is more specific than the MCP annotations alone
         * (readOnlyHint/destructiveHint don't distinguish create from update). Falls
         * back to the annotations for the handful of tools that don't fit the pattern
         * (cerberus_tag_wait, cerberus_tag_pause, ...).
         */
        actionBadge(tool) {
            const name = tool.name || '';
            if (name.endsWith('_list') || name.endsWith('_get')) {
                return {label: 'Lecture', icon: 'eye', cls: 'text-blue-500'};
            }
            if (name.endsWith('_create')) {
                return {label: 'Création', icon: 'circle-plus', cls: 'text-emerald-500'};
            }
            if (name.endsWith('_update')) {
                return {label: 'Modification', icon: 'pencil', cls: 'text-slate-400'};
            }
            if (name.endsWith('_delete')) {
                return {label: 'Suppression', icon: 'trash-2', cls: 'text-red-500'};
            }
            if (tool.annotations && tool.annotations.readOnlyHint) {
                return {label: 'Lecture', icon: 'eye', cls: 'text-blue-500'};
            }
            if (tool.annotations && tool.annotations.destructiveHint) {
                return {label: 'Sensible', icon: 'triangle-alert', cls: 'text-amber-500'};
            }
            return {label: 'Action', icon: 'zap', cls: 'text-slate-400'};
        },

        mcpEndpointUrl() {
            return window.location.origin + getCerberusBasePath() + 'mcp';
        },

        copyText(text) {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(text);
            }
        },

        authorize() {
            if (!this.apiKeyInput || !this.apiKeyInput.trim()) {
                return;
            }
            this.apiKey = this.apiKeyInput.trim();
            sessionStorage.setItem('mcpInspectorApiKey', this.apiKey);
            this.connect();
        },

        logout() {
            this.apiKey = '';
            this.apiKeyInput = '';
            this.sessionId = null;
            this.connected = false;
            this.tools = [];
            this.selectedTool = null;
            this.result = null;
            this.activeSystems = null;
            sessionStorage.removeItem('mcpInspectorApiKey');
        },

        async connect() {
            this.connecting = true;
            this.error = '';
            this.sessionId = null;
            try {
                await this.mcpRequest('initialize', {
                    protocolVersion: '2024-11-05',
                    capabilities: {},
                    clientInfo: {name: 'cerberus-mcp-inspector', version: '1.0.0'}
                });
                await this.mcpRequest('notifications/initialized', {}, {notification: true});
                await this.refreshTools();
                this.connected = true;
                this.refreshActiveSystems();
            } catch (e) {
                this.error = 'Connection failed: ' + e.message;
                this.connected = false;
                sessionStorage.removeItem('mcpInspectorApiKey');
            } finally {
                this.connecting = false;
            }
        },

        /** tools/list is paginated per the MCP spec — follow nextCursor until it's absent. */
        async refreshTools() {
            const collected = [];
            let cursor;
            let pages = 0;
            do {
                const result = await this.mcpRequest('tools/list', cursor ? {cursor} : {});
                collected.push(...((result && result.tools) || []));
                cursor = result && result.nextCursor;
                pages++;
            } while (cursor && pages < 50);

            collected.sort((a, b) => a.name.localeCompare(b.name));
            this.tools = collected;
            console.debug('MCP Inspector: loaded', collected.length, 'tool(s) across', pages, 'page(s)');
        },

        /** Best-effort KPI: how many systems/workspaces are active in the caller's context. */
        async refreshActiveSystems() {
            try {
                const result = await this.mcpRequest('tools/call', {name: 'cerberus_context_system_list', arguments: {}});
                const parsed = this.parseToolResultJson(result);
                this.activeSystems = parsed && Array.isArray(parsed.activeSystems) ? parsed.activeSystems.length : null;
            } catch (e) {
                this.activeSystems = null;
            }
        },

        selectTool(tool) {
            this.selectedTool = tool;
            this.result = null;
            this.formValues = {};
            const properties = (tool.inputSchema && tool.inputSchema.properties) || {};
            for (const key in properties) {
                const schema = properties[key];
                if (schema.type === 'boolean') {
                    this.formValues[key] = false;
                } else if (schema.default !== undefined) {
                    this.formValues[key] = schema.default;
                } else {
                    this.formValues[key] = '';
                }
            }
        },

        isRequired(key) {
            return !!this.selectedTool
                && Array.isArray(this.selectedTool.inputSchema.required)
                && this.selectedTool.inputSchema.required.includes(key);
        },

        parameterCount() {
            if (!this.selectedTool) {
                return 0;
            }
            return Object.keys((this.selectedTool.inputSchema && this.selectedTool.inputSchema.properties) || {}).length;
        },

        /** Builds the tools/call `arguments` object from the current form, skipping empty fields. */
        buildArgs() {
            if (!this.selectedTool) {
                return {};
            }
            const properties = (this.selectedTool.inputSchema && this.selectedTool.inputSchema.properties) || {};
            const args = {};
            for (const key in properties) {
                const schema = properties[key];
                const raw = this.formValues[key];
                if (raw === '' || raw === null || raw === undefined) {
                    continue;
                }
                if (schema.type === 'array') {
                    args[key] = String(raw).split(',').map(s => s.trim()).filter(s => s.length > 0);
                } else if (schema.type === 'integer' || schema.type === 'number') {
                    args[key] = Number(raw);
                } else if (schema.type === 'boolean') {
                    args[key] = !!raw;
                } else {
                    args[key] = raw;
                }
            }
            return args;
        },

        requestPreview() {
            if (!this.selectedTool) {
                return '';
            }
            return JSON.stringify({tool: this.selectedTool.name, arguments: this.buildArgs()}, null, 2);
        },

        async callTool() {
            if (!this.selectedTool) {
                return;
            }
            this.calling = true;
            this.result = null;
            this.error = '';
            const toolName = this.selectedTool.name;
            const startedAt = performance.now();
            try {
                const result = await this.mcpRequest('tools/call', {name: toolName, arguments: this.buildArgs()});
                this.result = result;
                this.history.unshift({
                    name: toolName,
                    at: new Date(),
                    durationMs: Math.round(performance.now() - startedAt),
                    ok: !(result && result.isError)
                });
            } catch (e) {
                this.error = 'Call failed: ' + e.message;
                this.history.unshift({
                    name: toolName,
                    at: new Date(),
                    durationMs: Math.round(performance.now() - startedAt),
                    ok: false
                });
            } finally {
                this.history = this.history.slice(0, 50);
                this.calling = false;
            }
        },

        renderResultContent(result) {
            if (!result || !Array.isArray(result.content)) {
                return JSON.stringify(result, null, 2);
            }
            return result.content.map(c => {
                if (c.type !== 'text') {
                    return JSON.stringify(c, null, 2);
                }
                try {
                    return JSON.stringify(JSON.parse(c.text), null, 2);
                } catch (e) {
                    return c.text;
                }
            }).join('\n\n');
        },

        /** Parses a tool's first text content block as JSON, or returns null. */
        parseToolResultJson(result) {
            if (!result || !Array.isArray(result.content) || result.content.length === 0) {
                return null;
            }
            try {
                return JSON.parse(result.content[0].text);
            } catch (e) {
                return null;
            }
        },

        formatTimestamp(date) {
            return date.toLocaleTimeString();
        },

        /**
         * Sends one JSON-RPC message over the streamable HTTP transport.
         *
         * Handles the two wrinkles that make /mcp unlike a plain JSON REST call:
         * - the server hands back a Mcp-Session-Id response header on `initialize`
         *   that must be echoed on every following request;
         * - a synchronous response can come back as a bare JSON body or wrapped in a
         *   single `text/event-stream` "data:" frame, depending on server behaviour.
         */
        async mcpRequest(method, params, {notification = false} = {}) {
            const body = {jsonrpc: '2.0', method, params};
            if (!notification) {
                body.id = this.nextId++;
            }

            const headers = {
                'Content-Type': 'application/json',
                'Accept': 'application/json, text/event-stream'
            };
            if (this.apiKey) {
                headers['X-API-KEY'] = this.apiKey;
            }
            if (this.sessionId) {
                headers['Mcp-Session-Id'] = this.sessionId;
            }

            const response = await fetch(getCerberusBasePath() + 'mcp', {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(body)
            });

            const returnedSessionId = response.headers.get('Mcp-Session-Id');
            if (returnedSessionId) {
                this.sessionId = returnedSessionId;
            }

            if (!response.ok) {
                const text = await response.text();
                throw new Error('HTTP ' + response.status + (text ? ': ' + text : ''));
            }

            if (notification) {
                return null;
            }

            const text = await response.text();
            if (!text) {
                return null;
            }

            const contentType = response.headers.get('Content-Type') || '';
            const payload = contentType.includes('text/event-stream') ? this.parseSseJson(text) : JSON.parse(text);

            if (payload && payload.error) {
                throw new Error(payload.error.message || 'MCP error');
            }

            return payload ? payload.result : null;
        },

        parseSseJson(text) {
            for (const line of text.split('\n')) {
                if (line.startsWith('data:')) {
                    const jsonStr = line.slice(5).trim();
                    if (jsonStr) {
                        return JSON.parse(jsonStr);
                    }
                }
            }
            return null;
        }
    };
}