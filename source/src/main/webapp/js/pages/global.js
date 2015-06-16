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


/***
 * Returns a label depending on the type of entry
 * @param {type} type - type selected
 * @returns {String} - label associated with the type
 */
function getSubDataLabel(type){
    var labelEntry = "Entry";
    if (type === "STATIC") {
        labelEntry = "Value";        
    } else if (type === "SQL") {
        labelEntry = "Column";                    
    } else if (type === "SOAP") {
        labelEntry = "Parsing Answer";                    
    }
    return labelEntry;
}
/***********************************************Messages***************************************/
function Message(messageType, message){
    this.messageType = messageType;
    this.message = message;
} 


function clearResponseMessage(dialog){
    var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
    elementAlert.fadeOut();                    
}

function showMessage(obj, dialog){             
    if(obj.messageType !== "success" && dialog !== null){ 
            console.log("entrouaqui");
            //shows the error message in the current dialog    
            var elementAlert = dialog.find("div[id*='DialogMessagesAlert']");
            var elementAlertDescription = dialog.find("span[id*='DialogAlertDescription']");

            elementAlertDescription.html(obj.message); 
            elementAlert.addClass("alert-" + obj.messageType);            
            elementAlert.fadeIn();                    
    }else{
        //shows the message in the main page
        showMessageMainPage(obj.messageType, obj.message);        
    }
    
    if(dialog !== null && obj.messageType==="success"){
        jQuery(dialog).dialog('close');
    } 
   
} 

 /***
 * 
 * @param {type} type - type of message: successm, info, ...
 * @param {type} message - message to show
 * @returns {undefined}
 */
function showMessageMainPage(type, message){
    $("#mainAlert").addClass("alert-" + type);    
    $("#alertDescription").html(message); 
    $("#mainAlert").fadeIn();
}
