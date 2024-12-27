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

    var lang = $("#MyLang").val();
    var windowsHeight = $(window).height() + 'px';
    
    var hash = window.location.hash.substr(1);
    $("#documentationApiFrame").attr("src", "./api/swagger-ui.html#" + hash);
    $('#documentationApiFrame').css('height', windowsHeight);
    $('#documentationApiFrame').css('height', windowsHeight);

    $('#documentationApiFrame').load(function () {
        $('#documentationApiFrame').contents().find('#content').css('height', windowsHeight).css('overflow', 'auto');
       // document.getElementById("documentationApiFrame").style.height = document.getElementById("documentationApiFrame").contentWindow.document.body.scrollHeight + "px";
    });


}

function resizeIframe(obj) {
    obj.style.height = obj.contentWindow.document.body.scrollHeight + 'px';
}
