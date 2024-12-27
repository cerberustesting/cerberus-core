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
//$.when($.getScript("js/global/global.js")).then(function () {

$(document).ready(function () {
//First check if page is called from pressing forgot Password link
    var login = GetURLParameter("login");
    var token = GetURLParameter("confirmationToken");
    $.when(checkResetPasswordFromEmailLink(login, token)).then(function (data) {
        console.log(data);
        if(data === true){
        //If parameters, hide the current password field and feed token hidden field
        $("#currentPasswordDiv").hide();
        $("#currentPasswordLabel").hide();
        $("#currentPassword").val(token);
        $("#resetPasswordToken").val(token);
    }
    });
    //event.preventDefault(); //STOP default action
    
    
    $("#changePassword").click(changePasswordClickHandler);
    $("#changePasswordForm").submit(function (event) {
        var postData = $(this).serializeArray();
        var jqxhr = $.ajax({
            type: 'POST',
            url: 'ChangeUserPassword',
            data: postData,
            dataType: 'json'

        });
        $.when(jqxhr).then(function (data) {
            if (data.messageType === "OK") {
                sessionStorage.clear();
                $(location).attr("href", "Homepage.jsp");
            }
        });
        event.preventDefault(); //STOP default action
        $("#changePasswordForm").trigger("reset");
    });
});

function changePasswordClickHandler() {
    var emptyValues = $("input[type='password']").filter(function () {
        return this.value === "";
    }).size();
    if (emptyValues === 0) {
        $("#changePasswordForm").trigger("submit");
    } else {
        //TODO:FN needs to be refactored to new interface
        alert("All fields are mandatory");
    }

}

function checkResetPasswordFromEmailLink(login, token) {
    var result = false;
//  If login and token defined, call forget Password
    if (login !== null && token !== null) {
        $.ajax({
            type: "POST",
            contentType: "application/text",
            url: "ForgotPasswordEmailConfirmation",
            dataType: "text",
            async: false,
            data: {
                login: login,
                confirmationToken: token
            },
            success: function (data, textStatus, xhr) {
                var obj = jQuery.parseJSON(data);
                showUnexpectedError(data, obj.messageType, obj.message);
                if (textStatus === "success") {
                    result = true;
                }
            },
            error: function (jqXHR, textStatus, errorThrown) {
                showUnexpectedError(jqXHR, textStatus, errorThrown.message);
                result = false;
            }
        });
    }
    return result;
}