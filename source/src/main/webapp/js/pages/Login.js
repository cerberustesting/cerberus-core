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
    });
});

/**
 * 
 * @returns {undefined}
 */
function initPage() {
    if ($("#error").text() === "1") {
        showUnexpectedError(null, "Error", "Login or Password incorrect !");
    }

    // We trim username.
    $('#username').change(function () {
        var usr = $('#username').val();
        $('#username').val(usr.trim());
    });


    $("#forgotpassword-box").submit(function () {
        return false;
    });
}

function forgotPassword() {

    var login = $("#loginForgotPassword").val();
    $("body").block({message: "processing"});
    var jqxhr = $.ajax({
        type: "GET",
        contentType: "application/text",
        url: "ForgotPassword",
        // This is the type what you are waiting back from the server
        dataType: "text",
        async: false,
        data: {
            login: login
        },
        success: function (data, textStatus, xhr) {
            var obj = jQuery.parseJSON(data);
            showUnexpectedError(data, obj.messageType, obj.message);
            $("body").unblock();
        },
        error: function (jqXHR, textStatus, errorThrown) {
            showUnexpectedError(jqXHR, textStatus, errorThrown.message);
            $("body").unblock();
        }
    });

    return false;
}

function showForgotPasswordFormulary() {
    $("#login-box").hide();
    $("#alertMessageLogin").hide();
    $("#alertMessageForgotPassword").hide();
    $("#forgot-password-box").show();

}

function showLoginBoxFormulary() {
    $("#login-box").show();
    $("#alertMessageLogin").hide();
    $("#alertMessageForgotPassword").hide();
    $("#forgot-password-box").hide();
}