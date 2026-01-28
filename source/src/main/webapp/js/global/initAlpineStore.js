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
