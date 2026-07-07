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
document.addEventListener('alpine:init', () => {

    Alpine.store('sidebar', {
        expanded: JSON.parse(localStorage.getItem('sidebarShow')) ?? true,
        hover: false,
        showLogo: true,
        activeMenu: null,
        activeSubmenu: null,
        hoverTimeout: null,
        hoverMenu: {automate:false, maintain:false, execute:false, monitor:false, insights:false, settings: false, admin: false, developer: false, help: false},
        hoverSubMenu: {automate:false, maintain:false, execute:false, monitor:false, insights:false, settings: false, admin: false, developer: false, help: false},
        showSubMenu: {automate:false, maintain:false, execute:false, monitor:false, insights:false, settings: false, admin: false, developer: false, help: false},
        hoverPos: null,
        hidden: false,

        toggleSidebar() {
            this.expanded = !this.expanded;
            localStorage.setItem('sidebarShow', JSON.stringify(this.expanded));
        },

        toggleActiveMenu(menu){
            if (this.expanded) {
                this.activeMenu = this.activeMenu === menu ? null : menu
            }
        },

        enterMenu(rect, menu){
            this.hoverMenu[menu] = true;
            setTimeout(() => {
                if (!this.expanded && (this.hoverMenu || this.hoverSubMenu)) {
                    this.hoverPos = {top: rect.top + window.scrollY, left: rect.right + window.scrollX};
                    this.showSubMenu[menu] = true;
                }
            }, 100);
        },

        leaveMenu(menu){
            this.hoverMenu[menu] = false;
                setTimeout(() => {
                    if (!this.expanded && !this.hoverMenu[menu] && !this.hoverSubMenu[menu]) {
                        this.showSubMenu[menu] = false;
                    }
                }, 100);
        },

        enterSubMenu(menu){
            this.hoverSubMenu[menu] = true;
                setTimeout(() => {
                    if (!this.expanded && (this.hoverMenu[menu] || this.hoverSubMenu[menu])) {
                        this.showSubMenu[menu] = true;
                    }
                }, 100);
        },

        leaveSubMenu(menu){
            this.hoverSubMenu[menu] = false;
                setTimeout(() => {
                    if (!this.expanded && !this.hoverMenu[menu] && !this.hoverSubMenu[menu]) {
                        this.showSubMenu[menu] = false;
                    }
                }, 100);
        }
    });

    Alpine.store('rightPanel', {
        open: JSON.parse(localStorage.getItem('rightPanel.open') || 'false'),
        activeTab: localStorage.getItem('rightPanel.activeTab') || 'execution',

        defaultWidth: 420,
        minWidth: 320,
        maxWidth: 720,
        width: 420,
        isResizing: false,

        init() {
            const saved = Number(localStorage.getItem('rightPanel.width'));
            this.width = saved
                ? Math.min(this.maxWidth, Math.max(this.minWidth, saved))
                : this.defaultWidth;
        },

        openTab(tab) {
            if (this.open && this.activeTab === tab) {
                this.close();
                return;
            }
            this.activeTab = tab;
            this.open = true;
            localStorage.setItem('rightPanel.open', 'true');
            localStorage.setItem('rightPanel.activeTab', tab);

            Alpine.nextTick(() => {
                requestAnimationFrame(() => {
                    if (window.lucide && typeof lucide.createIcons === "function") {
                        lucide.createIcons();
                    }
                });
            });
        },

        close() {
            this.open = false;
            localStorage.setItem('rightPanel.open', 'false');
        },

        setWidth(width) {
            this.width = Math.min(this.maxWidth, Math.max(this.minWidth, width));
        },

        persistWidth() {
            localStorage.setItem('rightPanel.width', String(this.width));
        }
    });

    Alpine.store('branding', window.__CERBERUS_BRANDING__ || {
        appName: 'Cerberus',
        showAppName: true,
        logo: {
            expanded: 'images/Logo-cerberus_menu.png',
            collapsed: 'images/Logo-cerberus_menu.png'
        },
        size: {
            expanded: { width: 40, height: 40 },
            collapsed: { width: 32, height: 32 }
        }
    });

    Alpine.store('user', {
        data: JSON.parse(sessionStorage.getItem('user')) || {},
        language: (JSON.parse(sessionStorage.getItem('user')) || {}).language || 'en',
        theme: localStorage.getItem('theme') || 'light',
    });

    Alpine.store('labels', {
        language: (JSON.parse(sessionStorage.getItem('user')) || {}).language || 'en',
        // Fonction de traduction
        getLabel(objectName, key) {
            let doc;

            doc = window[objectName+"Label"];

            if (doc[key]) {
                return doc[key][this.language] || doc[key].en || key;
            }
            return key;
        }
    });

    Alpine.store('ws', {
        connected: false,
        _ws: null,
        _reconnectDelay: 1000,

        init() {
            const user = JSON.parse(sessionStorage.getItem('user') || '{}');
            if (user && user.login) {
                this._connect();
            } else {
                window.addEventListener('user-loaded', () => this._connect(), { once: true });
            }
        },

        _connect() {
            if (this._ws && (this._ws.readyState === WebSocket.OPEN || this._ws.readyState === WebSocket.CONNECTING)) return;

            const protocol = window.location.protocol === 'https:' ? 'wss://' : 'ws://';
            const url = `${protocol}${window.location.host}${getCerberusBasePath()}api/ws/CerberusWebSocket`;

            this._ws = new WebSocket(url);

            this._ws.onopen = () => {
                this.connected = true;
                this._reconnectDelay = 1000;
                document.dispatchEvent(new CustomEvent(CerberusWs.Event.CONNECTED));
            };

            this._ws.onclose = () => {
                this.connected = false;
                this._ws = null;
                document.dispatchEvent(new CustomEvent(CerberusWs.Event.DISCONNECTED));
                setTimeout(() => {
                    this._reconnectDelay = Math.min(this._reconnectDelay * 2, 30000);
                    this._connect();
                }, this._reconnectDelay);
            };

            this._ws.onerror = (err) => console.error('CerberusWS error:', err);

            this._ws.onmessage = (event) => {
                let message;

                try {
                    message = JSON.parse(event.data);
                } catch (e) {
                    console.warn('CerberusWS: invalid JSON message', event.data);
                    return;
                }

                const channel = message.channel || '';

                // Main routing by channel.
                if (channel) {
                    document.dispatchEvent(new CustomEvent(CerberusWs.Event.forChannel(channel), {
                        detail: message
                    }));
                }
            };
        },

        send(payload) {
            if (!this._ws || this._ws.readyState !== WebSocket.OPEN) {
                console.warn('CerberusWS: not connected, message dropped');
                return false;
            }
            this._ws.send(typeof payload === 'string' ? payload : JSON.stringify(payload));
            return true;
        },

        whenConnected() {
            return new Promise((resolve, reject) => {
                if (this.connected && this._ws?.readyState === WebSocket.OPEN) {
                    resolve();
                    return;
                }
                const timeout = setTimeout(() => {
                    document.removeEventListener(CerberusWs.Event.CONNECTED, onConnect);
                    reject(new Error('WebSocket connection timeout'));
                }, 5000);
                const onConnect = () => { clearTimeout(timeout); resolve(); };
                document.addEventListener(CerberusWs.Event.CONNECTED, onConnect, { once: true });
                this._connect();
            });
        }
    });

    Alpine.data('dropdown', ({ items = [], label = 'Select' }) => ({
        items,
        selected: [],
        label,
        open: false,

        toggleOpen() { this.open = !this.open },
        close() { this.open = false },
        toggle(item) {
            if (this.selected.includes(item)) {
                this.selected = this.selected.filter(i => i !== item);
            } else {
                this.selected.push(item);
            }
        },
        selectedLabel() {
            return this.selected.length > 0 ? this.selected.join(', ') : 'Select...';
        },
        selectAll() { this.selected = [...this.items] },
        selectNone() { this.selected = [] }
    }));

});

document.addEventListener('alpine:initialized', () => {
    lucide.createIcons();
});

document.addEventListener("alpine:mutated", () => {
    lucide.createIcons();
});

document.addEventListener('alpine:initialized', () => {
    document.body.classList.add('alpine-ready');
});

document.addEventListener('DOMContentLoaded', () => {
    const metaMenu = document.querySelector('meta[name="active-menu"]');
    const metaSubmenu = document.querySelector('meta[name="active-submenu"]');
    if (metaMenu) Alpine.store('sidebar').activeMenu = metaMenu.content;
    if (metaSubmenu) Alpine.store('sidebar').activeSubmenu = metaSubmenu.content;
});
