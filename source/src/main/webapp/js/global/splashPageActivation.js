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


window.onload = function () {
    var user = getUser();
    var cerberusInformation = getCerberusInformation();
    if ((cerberusInformation.isGlobalSplashPageActive || cerberusInformation.isInstanceSplashPageActive) && !user.isAdmin) {
        if (getPageName(window.location.pathname) !== "SplashPage.jsp") {
            window.location.replace("SplashPage.jsp");
        }
    }
};

$(document).ready(function () {
    if (getPageName(window.location.pathname) === "SplashPage.jsp") {
        var adminEmail = getParameter("cerberus_support_email", "", true);
        $('#adminMailLink').attr("href", ('mailto:' + adminEmail.value)).text(adminEmail.value);
    }
});

function getPageName(url) {
    var index = url.lastIndexOf("/") + 1;
    return url.substr(index);
}