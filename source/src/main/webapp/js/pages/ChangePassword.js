/*
 * Cerberus  Copyright (C) 2013  vertigo17
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

//$.when($.getScript("js/pages/global/global.js")).then(function () {
    
$(document).ready(function () {   
    
    $("#changePassword").click(changePasswordClickHandler);   
    $("#changePasswordForm").submit(function(event) {
            var postData = $(this).serializeArray();
            var jqxhr =  $.ajax({
                type        : 'POST', 
                url         : 'ChangeUserPassword', 
                data        : postData,
                dataType    : 'json'

            });
            $.when(jqxhr).then(function (data) {     
                alert(data.message);
                if(data.messageType === "OK"){
                    var user = sessionStorage.getItem("user");
                    user = JSON.parse(user);
                    user.request = 'N';
                    sessionStorage.setItem("user", JSON.stringify(user));
                    $(location).attr("href", "Homepage.jsp");
                }
            });       
            event.preventDefault(); //STOP default action
            $("#changePasswordForm").trigger("reset");
        });  
});

function changePasswordClickHandler(){ 
    var emptyValues = $("input[type='password']").filter(function () {        
        return this.value === "";
    }).size();
            
            
    if(emptyValues === 0){
        $("#changePasswordForm").trigger("submit");
    }else{
        //TODO:FN needs to be refactored to new interface
        alert("All fields are mandatory");
    }
    
}