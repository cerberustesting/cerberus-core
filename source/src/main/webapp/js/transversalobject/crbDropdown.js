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
function crbDropdown(config) {
    return {
        id: config.id || 'dropdown',
        hiddenInput: config.hiddenInput || config.id,
        isOpen: false,
        allItems: [],
        items: [],
        selectedValue: '',
        selectedLabel: '',
        search: '',
        pos: { top: 0, left: 0, width: 0 },

        init: function() {
            var self = this;
            if (config.loader) {
                config.loader(function(items) { self.allItems = items; self.items = items; });
            }
            if (config.preselectEvent) {
                window.addEventListener(config.preselectEvent, function(e) {
                    self.preselect(e.detail);
                });
            }
            // Filter event: receives array of allowed values
            if (config.filterEvent) {
                window.addEventListener(config.filterEvent, function(e) {
                    var allowed = e.detail; // array of allowed values
                    if (allowed && Array.isArray(allowed)) {
                        self.items = self.allItems.filter(function(item) {
                            return allowed.indexOf(item.value) !== -1;
                        });
                    } else {
                        self.items = self.allItems;
                    }
                });
            }
            window.addEventListener('crb-dropdown-close-all', function(e) {
                if (e.detail !== self.id) self.isOpen = false;
            });
            document.addEventListener('click', function(e) {
                if (!self.isOpen) return;
                var btn = document.getElementById(self.id + '-btn');
                if (btn && btn.contains(e.target)) return;
                if (e.target.closest && e.target.closest('[x-show="isOpen"]')) return;
                self.isOpen = false;
            }, true);
        },

        preselect: function(value) {
            this.selectedValue = value || '';
            var match = null;
            var searchList = this.allItems && this.allItems.length > 0 ? this.allItems : this.items;
            for (var i = 0; i < searchList.length; i++) {
                if (searchList[i].value === value) { match = searchList[i]; break; }
            }
            this.selectedLabel = match ? match.label : (value || '');
            var form = this.$el.closest('form');
            var input = form ? form.querySelector('[name="' + this.hiddenInput + '"]') : document.getElementById(this.hiddenInput);
            if (input) input.value = value || '';
        },

        toggle: function() {
            var self = this;
            if (this.isOpen) {
                this.isOpen = false;
                return;
            }
            window.dispatchEvent(new CustomEvent('crb-dropdown-close-all', { detail: this.id }));
            this.isOpen = true;
            this.$nextTick(function() { self.updatePosition(); });
        },

        updatePosition: function() {
            var btn = document.getElementById(this.id + '-btn');
            if (!btn) return;
            var rect = btn.getBoundingClientRect();
            this.pos = { top: rect.bottom + 4, left: rect.left, width: Math.max(rect.width, 200) };
        },

        filtered: function() {
            var q = this.search.toLowerCase();
            if (!q) return this.items;
            return this.items.filter(function(i) { return i.label.toLowerCase().indexOf(q) !== -1; });
        },

        select: function(item) {
            this.selectedValue = item.value;
            this.selectedLabel = item.label;
            this.isOpen = false;
            this.search = '';
            var form = this.$el.closest('form');
            var input = form ? form.querySelector('[name="' + this.hiddenInput + '"]') : document.getElementById(this.hiddenInput);
            if (input) {
                input.value = item.value;
                $(input).trigger('change');
            }
            if (config.onSelect) {
                config.onSelect(item);
            }
        }
    };
}

window.crbLoaders = {
    // Load test folders
    tests: function(callback) {
        fetch('ReadTest?iSortCol_0=0&sSortDir_0=asc&sColumns=test&iDisplayLength=100')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                var items = data.contentTable.map(function(obj) {
                    return { value: obj.test, label: obj.test };
                }).sort(function(a, b) { return a.label.localeCompare(b.label); });
                callback(items);
            })
            .catch(function(err) { console.error("Failed to load tests:", err); callback([]); });
    },

    // Load invariant lists (priority, status, type, etc.)
    invariant: function(idName) {
        return function(callback) {
            var cacheKey = 'INVARIANT_' + idName;
            var cached = sessionStorage.getItem(cacheKey);
            if (cached) {
                var list = JSON.parse(cached);
                callback(list.map(function(el) {
                    var lbl = el.value;
                    if (el.description) lbl += ' - ' + el.description;
                    return { value: el.value, label: lbl };
                }));
                return;
            }
            $.ajax({
                url: 'FindInvariantByID',
                data: { idName: idName },
                success: function(data) {
                    sessionStorage.setItem(cacheKey, JSON.stringify(data));
                    callback(data.map(function(el) {
                        var lbl = el.value;
                        if (el.description) lbl += ' - ' + el.description;
                        return { value: el.value, label: lbl };
                    }));
                },
                error: function() { callback([]); }
            });
        };
    },

    // Load applications
    applications: function(callback) {
        $.getJSON('ReadApplication', 'q=1' + (typeof getUser === 'function' ? getUser().systemQuery : ''))
            .then(function(data) {
                var items = data.contentTable.map(function(app) {
                    return { value: app.application, label: app.application + ' [' + app.type + ']' };
                });
                callback(items);
            })
            .fail(function() { callback([]); });
    }
};
