/*
 * Cerberus Copyright (C) 2013 - 2017 cerberustesting
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
/*
 * Ajax overlay 1.0
 * Author: Simon Ilett @ aplusdesign.com.au
 * Descrip: Creates and inserts an ajax loader for ajax calls / timed events
 * Date: 03/08/2011
 */
function ajaxLoader(el, options) {
    // Becomes this.options
    var defaults = {
        bgColor: '#fff',
        duration: 000,
        opacity: 0.7,
        classOveride: false
    }
    this.options = jQuery.extend(defaults, options);
    this.container = $(el);

    this.init = function () {
        var container = this.container;
        // Delete any other loaders
        this.remove();
        // Create the overlay
        var overlay = $('<div></div>').css({
            'background-color': this.options.bgColor,
            'opacity': this.options.opacity,
            'width': container.width(),
            'height': container.height(),
            'position': 'absolute',
            'top': '0px',

            'z-index': 99999
        }).addClass('ajax_overlay');
        // add an overiding class name to set new loader style
        if (this.options.classOveride) {
            overlay.addClass(this.options.classOveride);
        }
        // insert overlay and loader into DOM
        container.append(
            overlay.append(
                $('<div></div>').addClass('ajax_loader')
            ).fadeIn(this.options.duration)
        );
    };

    this.remove = function () {
        var overlay = this.container.children(".ajax_overlay");
        if (overlay.length) {
            overlay.fadeOut(this.options.classOveride, function () {
                overlay.remove();
            });
        }
    }

    this.init();
}	
	