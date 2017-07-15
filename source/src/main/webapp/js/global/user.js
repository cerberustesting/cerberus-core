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
/**
 * Load the User from the database in sessionStorage
 * @returns {void}
 */
function readUserFromDatabase() {
    var user;
    var jqxhr = $.ajax({url: "ReadMyUser",
        async: false,
        dataType: 'json',
        success: function (data) {
            user = data;
            sessionStorage.setItem("user", JSON.stringify(user));
            loadUserPreferences(data);
            return user;
        }
    });
    $.when(jqxhr).then(function (data) {
        return data;
    });
}
/**
 * Get the User from sessionStorage
 * @returns {JSONObject} User Object from sessionStorage
 */
function getUser() {
    var user;
    if (sessionStorage.getItem("user") === null) {
        $.when(readUserFromDatabase()).then(function (data) {
            user = data;
        });
    }
    user = sessionStorage.getItem("user");
    user = JSON.parse(user);
    if (user !== null) {
        if (user.request === 'Y') {
            //user needs to change password
            $(location).attr("href", "ChangePassword.jsp");
        }
    }
    return user;
}


function loadUserPreferences(user) {
    localStorage = user.userPreferences;
}