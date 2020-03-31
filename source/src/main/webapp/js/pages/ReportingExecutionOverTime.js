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
/* global handleErrorAjaxAfterTimeout */

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        initPage();

        bindToggleCollapse();

        var urlTest = GetURLParameter('Test');
        var urlTestCase = GetURLParameter('TestCase');

        //open Run navbar Menu
        openNavbarMenu("navMenuExecutionReporting");

        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'}
        );


    });
});

/*
 * Loading functions
 */

function initPage() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayPageLabel(doc);
    displayFooter(doc);

}

function displayPageLabel(doc) {
    $("#pageTitle").html(doc.getDocLabel("page_reportovertime", "title"));
    $("#title").html(doc.getDocOnline("page_reportovertime", "title"));
    $("#loadbutton").html(doc.getDocLabel("page_reportovertime", "button_load"));
    $("#reloadbutton").html(doc.getDocLabel("page_reportovertime", "button_reload"));
    $("#filters").html(doc.getDocOnline("page_reportovertime", "filters"));
}

function loadPerfGraph() {
    console.info("Load Graph.");
}