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


/*
 * When the document is ready if the user is on the splashPage, we fill that page we some information and function
 * 
 * On every page we check if a message information needs to be displayed for the user
 */
$(document).ready(function () {

    if (document.title !== "Login") {
        var user = getUser();
        var cerberusInformation = getCerberusInformation();

        if (cerberusInformation != null) {
            if ((cerberusInformation.isGlobalSplashPageActive || cerberusInformation.isInstanceSplashPageActive)) {
                if (!user.isAdmin) {
                    redirectionToSplashPage();
                } else {

                    showMessageMainPage("warning", "Cerberus maintenance is in progress !! Splash page activated for non admin users.", false, 10000);
                }
            }

            if (getPageName(window.location.pathname) === "SplashPage.jsp") {
                displayAdminEmailOnSplashPage();
                addRedictionSplashPageButton();
                var doc = new Doc();
                displayFooter(doc)
            }

            if (cerberusInformation.isMessageInformationEnabled) {
                displayMessageInfoforAllUsers(cerberusInformation.messageInformation);
            }
        }
    }
});

function redirectionToSplashPage() {
    if (getPageName(window.location.pathname) !== "SplashPage.jsp") {
        window.location.replace("SplashPage.jsp");
    }
}

function displayAdminEmailOnSplashPage() {
    var adminEmail = getParameter("cerberus_support_email", "", true);
    $('#adminMailLink').attr("href", ('mailto:' + adminEmail.value)).text(adminEmail.value);
}

function addRedictionSplashPageButton() {
    $('#reloadButton').click(function () {
        var cerberusInformation = getCerberusInformation();
        if (!cerberusInformation.isGlobalSplashPageActive && !cerberusInformation.isInstanceSplashPageActive) {
            window.location.replace("Homepage.jsp");
        } else {
            showMessageMainPage("info", "Cerberus maintenance is still in progress", false, 10000);
        }
    });
}

function getPageName(url) {
    var index = url.lastIndexOf("/") + 1;
    return url.substr(index);
}

function displayMessageInfoforAllUsers(messageInfo) {
    showMessageMainPage("info", messageInfo, false, 10000);
}