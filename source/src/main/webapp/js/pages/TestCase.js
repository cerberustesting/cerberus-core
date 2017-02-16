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

var listOfPropertyTypes;
/**
 * Loads the common functions from a global javascript file
 * @param {type} param1 - filename
 */
$.when($.getScript("js/pages/global/global.js")).then(function() {

    $(document).ready(function() {
        displayPageLabel();
        //Show save button when testcase page loaded
        $("input[name='divButtonSaveChange']").removeAttr("style");
    });


    /**
     * Document ready methods
     */
    $(function() {
        //clears the useStepItem in session storage
        sessionStorage.removeItem("usedStepBackup");
        /*
         * Loads the list of property types used to create the dropdown tha twill create the property
         * @param {type} data
         */
        /*getInvariantList("PROPERTYTYPE", function(data) {
            listOfPropertyTypes = data;
            /**
             * For each property adds the icon corresponding to its state
             */
        /*
            $("input.property_value").each(drawPropertySymbolHandler);

        });*/

        /**
         * Removes all rows when the modal window is hidden and clears the message
         */
        $('#myModal').on('hidden.bs.modal', function() {
            $('#editSubDataTableBody tr').remove();
            clearResponseMessage($('#myModal'));
        });

        /*****************************************************************************/
        /**
         * Handles the click on the entry button for properties getFromDataLib
         */
        $('button[id*="entryButton_"]').click(setEntrybuttonClickHandler);
        /*****************************************************************************/
        /**
         * Clears all rows inserted for subdata
         */
        $('#selectSubDataFromListModal').on('hidden.bs.modal', function() {
            $('#subDataTableBody tr').remove();
        });




        //temporary fix. TODO:FN refactoring in the next iteration -  should be refactored in the future after the update of the list of nature types
        /**
         * Changes the property nature when we change the property type. If we select the get from datalib, two options are hidden and therefore another should
         * be selected.
         * */
//        $("div[id*='propertyRow'] select[id*='properties_nature_']").each(function() {
//            var parents = $(this).parents("div[id*='propertyRow']");
//            var natureElement = parents.find("select[id*='properties_type_']");
//            var propertyType = $(natureElement).find("option:selected").prop("value");
//            if (propertyType === "getFromDataLib") {
////                $(this).find("option:first").prop("selected", true);
////                $(this).find("option[value='RANDOMNEW']").addClass("hideElement");
////                $(this).find("option[value='NOTINUSE']").addClass("hideElement");
//            } else {
//                $(this).find("option[value='RANDOMNEW']").removeClass("hideElement");
//                $(this).find("option[value='NOTINUSE']").removeClass("hideElement");
//            }
//        });


        //TODO: this needs to be redefined when the refactor of this page is performed
        $("div[id*='propertyRow'] select[id*='properties_type_']").change(function() {
            //check if the autocomplete was defined, //if it is not then define it
            var parents = $(this).parents("[id*='propertyRow']");
            var textArea1 = $(parents).find("textarea[id*='properties_value1']");

            //var textArea1 = $(parents).find("textarea[id*='properties_value1']");

            if ($(this).prop("value") === "getFromDataLib") {
                //gets the second textbox                

                //var textArea2 = $(parents).find("textarea[id*='properties_value2']");

                if ($(textArea1).hasClass("getFromDataLib")) {
                    if (!$(textArea1).hasClass("ui-autocomplete-input")) {
                        addAutoComplete($(textArea1), callbackAutoCompleteTestDataLibName, 1, true);
                    }
                }
                //gets the first textbox
            } else {
                if ($(textArea1).hasClass("ui-autocomplete-input")) {
                    $(textArea1).autocomplete("disable");
                }
            }
        });

        //adds auto complete for all text areas with the class "getFromDataLib" that are rendered in the page when the pages loads
        //setPropertyValuesAutoComplete($("textarea[id*='properties_value1'][class*='getFromDataLib']"), callbackAutoCompleteTestDataLibName);

        $("textarea[id*='properties_value1'][class*='getFromDataLib']").each(function() {
            setPropertyValuesAutoComplete($(this), callbackAutoCompleteTestDataLibName);
        });


        if ($("#editableContent").find("div[class='blockUI']").size() > 0) {
            //checks if the editable content has a loader component
            hideLoader($("#editableContent"));
        }

        //handlers for library and useStep checkboxes in order to avoid that both are selected at the same time
        $("input[id*=step_useStep_]").change(useStepChangeHandler);
        $("input[id*=step_inLibrary_]").change(stepInLibraryChangeHandler);

        //saves 
        //define the handler that should be invoked when the step changes
        $("select[id*='step_useStepStep_']").change(stepInUseStepChangeHandler);
        //handlers for the buttons that allow to load and reset the steps
        $("button[id*='load_step_inLibrary_']").click(loadStep);
        $("button[id*='reset_step_inLibrary_']").click(resetStepClickHandler);

        //saves the steps that useStep, to restore purposes
        saveUseStepInfo();
        //avoids automatica save when user presses enter
        $(window).keydown(function(event){
            if(event.keyCode === 13) {
              event.preventDefault();
              return false;
            }
        });
    });

});

/**
 * Function that stores the information about the steps that the useStep option. This allows us to restore a step after modifing it.
 * @returns {undefined}
 */
function saveUseStepInfo() {
    var usedStepBackup = sessionStorage.getItem("usedStepBackup");
    if (usedStepBackup === null) {
        usedStepBackup = {};
    } else {
        usedStepBackup = JSON.parse(usedStepBackup);
    }
    $("input[id*='step_useStep_'][value='Y']").each(function() {

        var test = "";
        var testCase = "";
        var testStep = "";
        var type = 0; //normal, 1 - imported

        var stepNumber = $(this).attr("data-step-number");

        if (Boolean($(this).attr("checked"))) {
            test = $("#step_useStepTest_" + stepNumber).val();
            testCase = $("#step_useStepTestCase_" + stepNumber).val();
            testStep = $("#step_useStepStep_" + stepNumber).val();
            type = 1;
        }

        var useStepKey = "useStep" + stepNumber;
        var testDescription = $("#step_description_" + stepNumber).attr("value");

        var currentStep = new ImportedStep(test, testCase, testStep, stepNumber, testDescription, type);
        usedStepBackup[useStepKey] = currentStep;
    });

    sessionStorage.setItem("usedStepBackup", JSON.stringify(usedStepBackup));
}

function useStepChangeHandlerNew() {

    var parentNode = $(this).parent("div[id*='StepUseStepDiv']");
    var stepNumber = $(this).attr("data-step-number");

    var idStepCopiedFromDiv = "StepCopiedFromDiv" + stepNumber;
    var idStepUseStepTestDiv = "StepUseStepTestDiv" + stepNumber;
    var idStepUseStepTestCaseDiv = "StepUseStepTestCaseDiv" + stepNumber;
    var idStepUseStepStepDiv = "StepUseStepStepDiv" + stepNumber;
    var idStepUseStepLinkDiv = "StepUseStepLinkDiv" + stepNumber;
    var idlinkEditUsedStep = "linkEditUsedStep" + stepNumber;

    var stepChanged = $("#step_useStepChanged_" + stepNumber).val();

    var inLibraryElement = $("#step_inLibrary_" + stepNumber);

    if ($(this).attr("checked")) {
        $(this).attr("value", "Y")
    } else {
        $(this).attr("value", "N")
    }

    if ($(this).val() === "Y") {
        //if exists then remove it and draw it again
        var htmlAppend = '';
        var idTestSelect = "step_useStepTest_" + stepNumber;
        //var idTestCaseSelect = "step_useStepTestCase_" + stepNumber;
        var idStepSelect = "step_useStepStep_" + stepNumber;

        //TODO:FN when page is refactored, please add the corresponding translations

        htmlAppend = generateHTMLStepDiv(stepNumber);

        //add divs
        $(parentNode).after(htmlAppend);

        //adds select with list of tests
        var testSelect = $("div[data-id='useStepForNewStep'] select[data-id='step_useStepTest_template']").clone();
        $(testSelect).removeAttr('data-id');

        $(testSelect).attr("id", idTestSelect);
        $(testSelect).attr("name", idTestSelect);
        //appends the test select
        $('#' + idStepUseStepTestDiv).append(testSelect);

        //add the events to the components

        //configures the select elements
        //sets the default event for the test select component
        $(testSelect).change(function() {
            findStepBySystemTest($(this), $("select[id='MySystem']").val(),
                    $('#step_useStepTestCase_' + stepNumber), $("#load_step_inLibrary_" + stepNumber), "", false);
        });
        $('#' + idStepUseStepTestDiv).append(testSelect);
        //add select with list of test cases and adds the event to the select
        //add steps
        $("#step_useStepTestCase_" + stepNumber).change(function() {
            findStepBySystemTestTestCase($('#step_useStepTest_' + stepNumber),
                    $(this),
                    $("select[id='MySystem']").val(),
                    $('#step_useStepStep_' + stepNumber),
                    $("#load_step_inLibrary_" + stepNumber),
                    "",
                    false);
        });

        //sets the step select handler for the event change
        $("#" + idStepSelect).change(stepInUseStepChangeHandler);
        //$("#step_useStep_" + stepNumber).change(useStepChangeHandler);
        //associates the events to the new elements
        $("#load_step_inLibrary_" + stepNumber).click(loadStep);
        //adds the handler for the reset button, when applicable
        $("#reset_step_inLibrary_" + stepNumber).click(resetStepClickHandler);
    } else {
        if (stepChanged === "Y") {
            //disconnet steps

            if (confirm("Do you want to break the link between steps? If you select yes, when you save this test case, \n\
                    all actions and controls will be copied to the current test case.")) {

                $(this).attr("value", "N");

                $(inLibraryElement).removeAttr("disabled");
                //hide the remaing elements
                $(parentNode).siblings("#" + idStepCopiedFromDiv).remove();
                $(parentNode).siblings("#" + idStepUseStepTestDiv).remove();
                $(parentNode).siblings("#" + idStepUseStepTestCaseDiv).remove();
                $(parentNode).siblings("#" + idStepUseStepStepDiv).remove();
                $(parentNode).siblings("#" + idStepUseStepLinkDiv).remove();
                $(parentNode).siblings("#" + idlinkEditUsedStep).remove();


                $("#isToCopySteps_" + stepNumber).attr("value", "Y");
                //updates the styles for 

                $("div[id*='StepListOfActionDiv" + stepNumber + "']").addClass("ActionOfUseStepTemp");
                $("div[id*='StepListOfControlDiv" + stepNumber + "']").addClass("ActionControlOfUseStepTemp");
            } else {
                //sets the useStep checked again because users selected cancel
                $(this).attr("checked", "checked");
                $(this).attr("value", "Y");
            }

        } else {
            //When no is selected in a no step, then removes the elements
            $(parentNode).siblings("#" + idStepCopiedFromDiv).remove();
            $(parentNode).siblings("#" + idStepUseStepTestDiv).remove();
            $(parentNode).siblings("#" + idStepUseStepTestCaseDiv).remove();
            $(parentNode).siblings("#" + idStepUseStepStepDiv).remove();
            $(parentNode).siblings("#" + idStepUseStepLinkDiv).remove();
            $(parentNode).siblings("#" + idlinkEditUsedStep).remove();
        }

    }
}

/**
 * Handler that manages the options available when the useStep checkbox is changed
 * @returns {undefined}
 */
function useStepChangeHandler() {
    var stepNumber = $(this).attr("data-step-number");
    //disables the stepinlibrary

    //gets the useStep element 
    var inLibraryElement = $("#step_inLibrary_" + stepNumber);

    var parentNode = $(this).parent("div[id*='StepUseStepDiv']");

    var idStepCopiedFromDiv = "StepCopiedFromDiv" + stepNumber;
    var idStepUseStepTestDiv = "StepUseStepTestDiv" + stepNumber;
    var idStepUseStepTestCaseDiv = "StepUseStepTestCaseDiv" + stepNumber;
    var idStepUseStepStepDiv = "StepUseStepStepDiv" + stepNumber;
    var idStepUseStepLinkDiv = "StepUseStepLinkDiv" + stepNumber;
    var initUseStep = $("#initUseStep_" + stepNumber).attr("value");
    var idlinkEditUsedStep = "linkEditUsedStep" + stepNumber;

    if ($(this).attr("checked")) {
        //sets the value to Y
        $(this).attr("value", "Y");
        $("#isToCopySteps_" + stepNumber).attr("value", "N");//if is checked then is not to copy steps
        //disables the "inLibrary" checkbox and loads the comboboxes
        $(inLibraryElement).attr("disabled", "disabled");
        if ($("#" + idStepCopiedFromDiv).length === 0) {
            drawSpecificUseStepCompoments(parentNode, stepNumber);
        } else {
            $(parentNode).siblings("#" + idStepCopiedFromDiv).css("visibility", "visible");
            $(parentNode).siblings("#" + idStepUseStepTestDiv).css("visibility", "visible");
            $(parentNode).siblings("#" + idStepUseStepTestCaseDiv).css("visibility", "visible");
            $(parentNode).siblings("#" + idStepUseStepStepDiv).css("visibility", "visible");
            $(parentNode).siblings("#" + idStepUseStepLinkDiv).css("visibility", "visible");
            $(parentNode).siblings("#" + idlinkEditUsedStep).css("visibility", "visible");
            showLinkElementsPanel(stepNumber);
        }

    } else {
        $(this).attr("value", "N");
        var stepChanged = $("#step_useStepChanged_" + stepNumber).attr("value");
        var isToHidde = false;
        if (initUseStep === "Y" || stepChanged === "Y") {
            console.log("Step changed " + stepChanged);
            if (confirm("Do you want to break the link between steps? If you select yes, \n\
                when you save this test case, all actions and controls will be copied to the current test case.")) {
                //clicked ok
                console.log("OK");
                $(this).attr("value", "N");
                $(inLibraryElement).removeAttr("disabled");
                $("#isToCopySteps_" + stepNumber).attr("value", "Y");

                $("div[id*='StepListOfActionDiv" + stepNumber + "']").addClass("ActionOfUseStepTemp");
                $("div[id*='StepListOfControlDiv" + stepNumber + "']").addClass("ActionControlOfUseStepTemp");
                isToHidde = true;

            } else {
                //clicked cancel
                console.log("CANCEL");
                $(this).attr("checked", "checked");
                $(this).attr("value", "Y");
                $(inLibraryElement).removeAttr("disabled");

            }
        } else {
            //hide the remaing elements
            isToHidde = true;
        }
        if (isToHidde) {
            hideUseStepElementsPanel(stepNumber);
            /*$(parentNode).siblings("#" + idStepCopiedFromDiv).css("visibility", "hidden");
             $(parentNode).siblings("#" + idStepUseStepTestDiv).css("visibility", "hidden");
             $(parentNode).siblings("#" + idStepUseStepTestCaseDiv).css("visibility", "hidden");
             $(parentNode).siblings("#" + idStepUseStepStepDiv).css("visibility", "hidden");
             $(parentNode).siblings("#" + idStepUseStepLinkDiv).css("visibility", "hidden");
             $(parentNode).siblings("#" + idlinkEditUsedStep).css("visibility", "hidden");*/
        }
    }
}

function dragAndDropdrawSpecificUseStepCompoments(parentNode, stepNumber, selectedTest, selectedTestCase, selectedStep) {
    var idTestSelect = "step_useStepTest_" + stepNumber;
    var idStepUseStepTestDiv = "StepUseStepTestDiv" + stepNumber;

    var htmlAppend = generateHTMLStepDiv(stepNumber);
    //add divs
    $(parentNode).after(htmlAppend);

    //adds select with list of tests
    var testSelect = $("div[data-id='useStepForNewStep'] select[data-id='step_useStepTest_template']").clone();
    $(testSelect).removeAttr('data-id');

    $(testSelect).attr("id", idTestSelect);
    $(testSelect).attr("name", idTestSelect);
    //appends the test select

    $('#' + idStepUseStepTestDiv).append(testSelect);

    if (selectedTest !== null && selectedTestCase !== null && selectedStep !== null) {
        $("#step_useStepTest_" + stepNumber).find("option[value='']").remove();
        $("#step_useStepTest_" + stepNumber).find("option[value='" + selectedTest + "']").attr("selected", "selected")

        //add event to test element
        $("#step_useStepTest_" + stepNumber).change(function() {
            findStepBySystemTest($(this), $("select[id='MySystem']").val(),
                    $('#step_useStepTestCase_' + stepNumber),
                    $("#load_step_inLibrary_" + stepNumber),
                    "");
        });
        //add event to test case element
        $("#step_useStepTestCase_" + stepNumber).change(function() {
            findStepBySystemTestTestCase($('#step_useStepTest_' + stepNumber),
                    $(this),
                    $("select[id='MySystem']").val(),
                    $('#step_useStepStep_' + stepNumber),
                    $("#load_step_inLibrary_" + stepNumber),
                    "",
                    false);
        });
        //add event to step element
        $("#step_useStepStep_" + stepNumber).change(stepInUseStepChangeHandler);

        //associates the events to the new elements
        $("#load_step_inLibrary_" + stepNumber).click(loadStep);
        //adds the handler for the reset button, when applicable
        $("#reset_step_inLibrary_" + stepNumber).click(resetStepClickHandler);

        //Selects the step by default, which means that all components will be load and the steps will be added into the page
        dragAndDroploadTestCaseAndStepComponents(
                $("#step_useStepTestCase_" + stepNumber),
                $('#step_useStepStep_' + stepNumber),
                $("select[id='MySystem']").val(),
                $("#load_step_inLibrary_" + stepNumber),
                selectedTest, selectedTestCase, selectedStep);
    }
}


function generateHTMLStepDiv(stepNumber) {
    var doc = new Doc();
    var idStepCopiedFromDiv = "StepCopiedFromDiv" + stepNumber;
    var idStepUseStepTestCaseDiv = "StepUseStepTestCaseDiv" + stepNumber;
    var idStepUseStepStepDiv = "StepUseStepStepDiv" + stepNumber;
    var idStepUseStepLinkDiv = "StepUseStepLinkDiv" + stepNumber;
    var idlinkEditUsedStep = "linkEditUsedStep" + stepNumber;
    var idStepUseStepTestDiv = "StepUseStepTestDiv" + stepNumber;
    var idTestCaseSelect = "step_useStepTestCase_" + stepNumber;
    var idStepSelect = "step_useStepStep_" + stepNumber;

    var htmlAppend = '';
    //TODO:FN when page is refactored, please add the corresponding translations
    htmlAppend += '<div id="' + idStepCopiedFromDiv + '" style="float:left"><p style="margin-top:15px;">' + doc.getDocLabel("page_testcase", "lbl_copied_from") + '</p></div>';
    //div for select with list of tests
    htmlAppend += '<div id="' + idStepUseStepTestDiv + '"  style="float:left">' + "</div>";

    //div for select with list of test cases
    htmlAppend += '<div id="' + idStepUseStepTestCaseDiv + '"  style="float:left">';
    htmlAppend += '<select name="' + idTestCaseSelect + '" id="' + idTestCaseSelect + '" style="width: 150px;margin-top:12.5px;font-weight: bold;">';
    htmlAppend += '<option style="width: 400px" value="">---</option>';
    htmlAppend += "</select></div>";
    //div for select with list of test steps
    htmlAppend += '<div id="' + idStepUseStepStepDiv + '"  style="float:left">';

    htmlAppend += '<select data-step-number="' + stepNumber + '" id="' + idStepSelect + '" name="' + idStepSelect + '" style="width: 150px;margin-top:12.5px;font-weight: bold;">';
    htmlAppend += '<option style="width: 400px" value="0">---</option>';
    htmlAppend += '</select>';
    htmlAppend += "</div>";

    //appends the link that allows you to edit the test case
    htmlAppend += '<div id="' + idStepUseStepLinkDiv + '" class="StepUseStepLinkDiv">';

    htmlAppend += '<a id="' + idlinkEditUsedStep + '" href="#" title="' + doc.getDocLabel("page_testcase", "link_edit_step") + '">\n\
        <span class="glyphicon glyphicon-new-window"></span></a>';
    htmlAppend += '<button id="load_step_inLibrary_' + stepNumber + '" class="btn btn-xs" disabled="disabled" data-step-number="' + stepNumber + '" type="button">';
    htmlAppend += '<span class="glyphicon glyphicon-refresh"></span></button>';

    //adds the reset button
    htmlAppend += '<button id="reset_step_inLibrary_' + stepNumber + '" class="btn btn-xs" disabled="disabled" data-step-number="' + stepNumber + '" type="button">';
    htmlAppend += '<span class="glyphicon glyphicon-remove"></span></button>';

    htmlAppend += '</div>';
    htmlAppend += "</div>";

    return htmlAppend;

}

function dragAndDroploadTestCaseAndStepComponents(testCaseElement, testStepElement, system, refreshButton,
        testSelectedOption, testCaseSelectedOption, testStepSelectedOption) {

    var url = 'GetStepInLibrary?test=' + testSelectedOption;
    $.get(url, function(data) {
        $(testCaseElement).empty();

        var testFromLib = "";

        for (var i = 0; i < data.testCaseStepList.length; i++) {
            if (data.testCaseStepList[i].testCase !== testFromLib) {
                $(testCaseElement).append($("<option></option>")
                        .attr('value', data.testCaseStepList[i].testCase)
                        .attr('style', 'width:400px;')
                        .text(data.testCaseStepList[i].testCase + " : " + data.testCaseStepList[i].tcdesc));
                testFromLib = data.testCaseStepList[i].testCase;
            }
        }

        //sets the value for the test case
        $(testCaseElement).find("option[value='" + testCaseSelectedOption + "']").attr("selected", "selected");


        var url;
        url = 'GetStepInLibrary?test=' + testSelectedOption + '&testCase=' + testCaseSelectedOption;
        $.get(url, function(data) {
            $(testStepElement).empty();
            $(testStepElement).append($("<option></option>")
                    .attr('value', '')
                    .attr('style', 'width:400px;')
                    .text('Choose Step'));
            for (var i = 0; i < data.testCaseStepList.length; i++) {
                $(testStepElement).append($("<option></option>")
                        .attr('value', data.testCaseStepList[i].step)
                        .attr('style', 'width:400px;')
                        .text(data.testCaseStepList[i].sort + ':' + data.testCaseStepList[i].description));
            }

            $(testStepElement).find("option[value='" + testStepSelectedOption + "']").attr("selected", "selected");
            $(refreshButton).removeAttr("disabled");
            $(refreshButton).trigger("click");

            var stepNumber = $(testStepElement).attr("data-step-number");
            setNewHrefForEditStep(stepNumber, testSelectedOption, testCaseSelectedOption, testStepSelectedOption);

        });
    });
}

function drawSpecificUseStepCompoments(parentNode, stepNumber) {
    var idStepUseStepTestDiv = "StepUseStepTestDiv" + stepNumber;

    var idTestSelect = "step_useStepTest_" + stepNumber;

    var idStepSelect = "step_useStepStep_" + stepNumber;

    //TODO:FN when page is refactored, please add the corresponding translations
    var htmlAppend = generateHTMLStepDiv(stepNumber);
    //add divs
    $(parentNode).after(htmlAppend);


    //adds select with list of tests
    var testSelect = $("div[data-id='useStepForNewStep'] select[data-id='step_useStepTest_template']").clone();
    $(testSelect).removeAttr('data-id');

    $(testSelect).attr("id", idTestSelect);
    $(testSelect).attr("name", idTestSelect);
    //appends the test select
    $('#' + idStepUseStepTestDiv).append(testSelect);


    //configures the select elements
    //sets the default event for the test select component
    $(testSelect).change(function() {
        findStepBySystemTest($(this), $("select[id='MySystem']").val(), $('#step_useStepTestCase_' + stepNumber), $("#load_step_inLibrary_" + stepNumber), "");
    });
    $('#' + idStepUseStepTestDiv).append(testSelect);
    //add select with list of test cases and adds the event to the select
    //add steps
    $("#step_useStepTestCase_" + stepNumber).change(function() {
        findStepBySystemTestTestCase($('#step_useStepTest_' + stepNumber), $(this),
                $("select[id='MySystem']").val(), $('#step_useStepStep_' + stepNumber), $("#load_step_inLibrary_" + stepNumber), "");
    });

    //sets the step select handler for the event change
    $("#" + idStepSelect).change(stepInUseStepChangeHandler);
    //$("#step_useStep_" + stepNumber).change(useStepChangeHandler);


    //associates the events to the new elements
    $("#load_step_inLibrary_" + stepNumber).click(loadStep);
    //adds the handler for the reset button, when applicable
    $("#reset_step_inLibrary_" + stepNumber).click(resetStepClickHandler);
}

/**
 * Handler for the button that resets the step
 * @returns {undefined}
 */
function resetStepClickHandler() {
    var stepNumber = $(this).attr("data-step-number");
    var backupFromSessionStorage = JSON.parse(sessionStorage.getItem("usedStepBackup"));
    var useStepKey = "useStep" + stepNumber;

    var stepToRestore = backupFromSessionStorage[useStepKey];
    //the current button is set to disabled as well as the load button
    $(this).attr("disabled", "disabled");

    //restores the description value
    $("#step_description_" + stepNumber).attr("value", stepToRestore.description);

    //need to reset the select components
    //the defferred option $.when(). done() does not seem to work with trigger calls trigger("change"), therefore 
    //the method that is invoked in the change event is called here    
    if (stepToRestore.type === 0) {
        $("#step_useStep_" + stepNumber).removeAttr("checked");
        hideUseStepElementsPanel(stepNumber);
    } else {
        loadTestCaseAndStepComponents($("#step_useStepTest_" + stepNumber),
                $("#step_useStepTestCase_" + stepNumber), $('#step_useStepStep_' + stepNumber),
                $("select[id='MySystem']").val(), $("#load_step_inLibrary_" + stepNumber),
                stepToRestore.useTest, stepToRestore.useTestCase, stepToRestore.useStep, true);
        //resets the edit step url        
        setNewHrefForEditStep(stepNumber, stepToRestore.useTest, stepToRestore.useTestCase, stepToRestore.useStep);
    }
    //sets the step as it was not modified
    $("#step_useStepChanged_" + stepNumber).attr("value", "N");
    $("#load_step_inLibrary_" + stepNumber).attr("disabled", "disabled");
    //removes all current actions and controls drawn 
    removeActionsAndControls(stepNumber);
    drawStep($("#BeforeFirstAction" + stepNumber), stepNumber, stepToRestore.actionList, stepToRestore.type, false);
}

/**
 * Auxiliary method that hides all elements related to the useStep option
 * @param {type} stepNumber
 * @returns {undefined}
 */
function hideUseStepElementsPanel(stepNumber) {

    //hide the elements associated with the useStep
    $("#StepCopiedFromDiv" + stepNumber).css("visibility", "hidden");
    $("#StepUseStepTestDiv" + stepNumber).css("visibility", "hidden");
    $("#StepUseStepTestCaseDiv" + stepNumber).css("visibility", "hidden");
    $("#StepUseStepStepDiv" + stepNumber).css("visibility", "hidden");
    $("#StepUseStepLinkDiv" + stepNumber).css("visibility", "hidden");
    hideLinkElementsPanel(stepNumber);
}
/**
 * Auxiliary function that updates the link href attribute that allows the edition of the step that is being imported
 * @param {type} stepNumber
 * @param {type} test
 * @param {type} testcase
 * @param {type} step
 * @returns {undefined}
 */
function setNewHrefForEditStep(stepNumber, test, testcase, step) {
    var urlElement = $("#linkEditUsedStep" + stepNumber);
    var url = 'TestCaseScript.jsp?test=' + test + '&testcase=' + testcase + '&step=' + step;
    $(urlElement).attr("href", url);
    $(urlElement).attr("target", "_blank");
}
/**
 * Handler that manages the options available when the inLibrary checkbox is changed
 * @returns {undefined}
 */
function stepInLibraryChangeHandler() {
    var stepNumber = $(this).attr("data-step-number");
    //gets the useStep element 
    var useStepElement = $("#step_useStep_" + stepNumber);

    if ($(this).attr("checked")) {
        //disables the "useStep" checkbox if the test is marked as inLibrary
        $(useStepElement).attr("disabled", "disabled");
        $(this).attr("value", "Y");
        $("input[name='" + $(this).attr("name") + "'][type='hidden']").attr("value", "Y");
    } else {
        $(useStepElement).removeAttr("disabled");
        $(this).attr("value", "N");
        $("input[name='" + $(this).attr("name") + "'][type='hidden']").attr("value", "N");
    }
}

/**
 * Auxiliary function that loads a step into the GUI
 * @returns {undefined}
 */
function loadStep() {

    var stepNumber = $(this).attr("data-step-number");

    var parent = "#StepsBorderDiv" + stepNumber + " #Action" + stepNumber;
    var useStepKey = "useStep" + stepNumber;
    //gets the current information that is in storage
    var usedStepBackup = JSON.parse(sessionStorage.getItem("usedStepBackup"));

    $("#step_useStep_" + stepNumber).attr("value", "Y");
    $("#step_useStepChanged_" + stepNumber).attr("value", "Y");

    if (!usedStepBackup.hasOwnProperty(useStepKey) || !usedStepBackup[useStepKey].actionsLoaded) {
        var step = $.extend(new ImportedStep(), usedStepBackup[useStepKey]);

        if (!usedStepBackup.hasOwnProperty(useStepKey)) {
            step.type = 0;
            step.description = "";
            this.useTest = "";
            this.useTestCase = "";
            this.useStep = "";
            this.step = "";
        }

        var actionList = {};

        $.each($(parent).children("div"), function(idx, obj) {
            var id = $(obj).attr("id");
            //check if there are itens inside the div
            if (id.indexOf("StepListOfActionDiv") >= 0) {

                var sequence = $(obj).find('input[data-field="sequence"]').attr("value");
                var description = $(obj).find('input[id="action_description_' + stepNumber + '_' + sequence + '"]').attr("value");
                var actionType = $(obj).find('#action_action_' + stepNumber + '_' + sequence + '').attr("value");
                var actionObject = $(obj).find('input[id="action_object_' + stepNumber + '_' + sequence + '"]').attr("value");

                var actionPropertyElement = $(obj).find('input[id="action_property_' + stepNumber + '_' + sequence + '"]');

                var actionProperty = $(actionPropertyElement).attr("value");

                var fileName = $(obj).find('div[id="AttachPictureDiv_' + stepNumber + '_' + sequence + '"] img').attr("src");


                var action = new Action(id, sequence, description, actionType, actionObject, actionProperty, fileName);
                //check if the property has some button associated
                var type = 0;
                var dataTest = "";
                var dataTestCase = "";
                var buttonElement = "";
                //check if the property has some button associated
                if ($(actionPropertyElement).prev("div[class='dropdown']").length === 1) {
                    buttonElement = $(actionPropertyElement).prev("div[class='dropdown']").find("button")[0];
                    if ($(buttonElement).hasClass("property_missing")) {
                        type = 1;
                    }

                } else if ($(actionPropertyElement).prev("button").length === 1) {
                    buttonElement = $(actionPropertyElement).prev("button");
                    if ($(buttonElement).hasClass("property_tooverride")) {
                        type = 2;
                        dataTest = $(actionPropertyElement).attr("data-usestep-test");
                        dataTestCase = $(actionPropertyElement).attr("data-usestep-testcase");
                    } else if ($(buttonElement).hasClass("property_overriden")) {
                        dataTest = $(actionPropertyElement).attr("data-usestep-test");
                        dataTestCase = $(actionPropertyElement).attr("data-usestep-testcase");
                        type = 3;

                    }
                }
                if (type !== 0) {
                    //get the values that indicate the source of the property, e.g., data-usestep-testcase="0001A" data-usestep-test="_IMPORTED_STEPS
                    var propertyBtnAction = new PropertyButtonAction(type, $(buttonElement).attr("title"), dataTest, dataTestCase);
                    action.setPropertyButtonAction(propertyBtnAction);
                }
                step.addAction(action);
                actionList [sequence] = action;
            }
            if (id.indexOf("StepListOfControlDiv") >= 0) {
                //this is a control

                //retrieve all data

                var actionSequence = $(obj).find('input[data-field="sequence"]').attr("value");
                var currentAction = actionList[actionSequence];

                var controlNR = $(obj).find('input[data-field="control"]').attr("value");
                var controlDescription = $(obj).find('input[id="control_description_' + stepNumber + '_' + actionSequence + '_' + controlNR + '"]').attr("value");
                var controlType = $('#control_type_' + stepNumber + '_' + actionSequence + '_' + controlNR).attr("value");
                var controlProperty = $(obj).find('input[id="control_property_' + stepNumber + '_' + actionSequence + '_' + controlNR + '"]').attr("value");
                var controlValue = $(obj).find('input[id="control_value_' + stepNumber + '_' + actionSequence + '_' + controlNR + '"]').attr("value");
                var controlFatal = $('#control_fatal_' + stepNumber + '_' + actionSequence + '_' + controlNR).attr("value");
                var controlFileName = $(obj).find('div[id="AttachPictureDiv_' + stepNumber + '_' + actionSequence + '_' + controlNR + '"] img').attr("src");


                var control = new Control(id, actionSequence, controlNR, controlDescription, controlType, controlProperty, controlValue, controlFatal, controlFileName);
                //adds the control to the list
                currentAction.addControl(control);
                //updates the action
                actionList[actionSequence] = currentAction;
            }
        });


        //associates the actions to the step
        step.setActionList(actionList);
        step.actionsLoaded = true;

        usedStepBackup[useStepKey] = step;
        sessionStorage.setItem("usedStepBackup", JSON.stringify(usedStepBackup));
    }

    //remove all actions and controls from the interface
    removeActionsAndControls(stepNumber);

    //Retrieves the selected values for test, testcase and step, in order to load the test step
    var selectedTest = $("#step_useStepTest_" + stepNumber).val();
    var selectedTestCase = $("#step_useStepTestCase_" + stepNumber).val();
    var selectedStep = $("#step_useStepStep_" + stepNumber).val();

    var parentNode = $(parent).find("#BeforeFirstAction" + stepNumber);

    //Activates the loader, which is hidden after the readfromdatabase completes
    showLoader($("#BeforeFirstAction" + stepNumber));
    readStepFromDatabase(selectedTest, selectedTestCase, selectedStep, stepNumber, parentNode);

    //Activates the reset button 
    $("#reset_step_inLibrary_" + stepNumber).removeAttr("disabled");

}

/**
 * Auxiliary function that removes the list of actions and controls for a selected step
 * @param {type} stepNumber step where items are removed
 * @returns {undefined}
 */
function removeActionsAndControls(stepNumber) {
    $("div[id*='StepListOfActionDiv" + stepNumber + "']").remove();
    $("div[id*='DivActionEndOfAction" + stepNumber + "']").remove();
    $("div[id*='StepListOfControlDiv" + stepNumber + "']").remove();
}
function stepInUseStepChangeHandler() {
    //gets the step number that is being defined
    var stepNumber = $(this).attr("data-step-number"); //current step number


    if ($(this).val() === "0") {
        hideLinkElementsPanel(stepNumber);
        $("#step_useStepChanged_" + stepNumber).attr("value", "");
        $("#linkEditUsedStep" + stepNumber).attr("href", "#");
    } else {
        $("#step_useStepChanged_" + stepNumber).attr("value", "N");
        //shows the options that should be displayed when the step is selected
        showLinkElementsPanel(stepNumber);
        //updates the url of the step
        setNewHrefForEditStep(stepNumber, $("#step_useStepTest_" + stepNumber).val(), $("#step_useStepTestCase_" + stepNumber).val(), $(this).val());
        //removes the empty option
        $(this).find("option[value='']").remove();
    }
}

function showLinkElementsPanel(stepNumber, reset) {
    var urlElement = $("#linkEditUsedStep" + stepNumber);
    var btnRefresh = $("#load_step_inLibrary_" + stepNumber);
    var btnReset = $("#reset_step_inLibrary_" + stepNumber);

    $(urlElement).css("visibility", "visible");
    $(btnRefresh).css("visibility", "visible");
    $(btnReset).css("visibility", "visible");
    if (Boolean(reset) && reset) {
        $(btnRefresh).attr("disabled", "disabled");
        $(btnReset).attr("disabled", "disabled");
        console.log("reset");
    } else {
        $(btnRefresh).removeAttr("disabled");
        $(btnRefresh).trigger("click");
        console.log("NAO reset");
    }

}
function hideLinkElementsPanel(stepNumber) {
    var urlElement = $("#linkEditUsedStep" + stepNumber);
    var btnRefresh = $("#load_step_inLibrary_" + stepNumber);
    var btnReset = $("#reset_step_inLibrary_" + stepNumber);

    if ($("#step_useStepStep_" + stepNumber).val() === "0" || $("#step_useStepStep_" + stepNumber).val() === "") {
        $(btnRefresh).attr("disabled", "disabled");
    }
    $(urlElement).css("visibility", "hidden");
    $(btnRefresh).css("visibility", "hidden");
    $(btnReset).css("visibility", "hidden");
}



/**
 * Gets an imported step from the database
 * @param {type} test
 * @param {type} testcase
 * @param {type} step
 * @param {type} stepNumber 
 * @param {type} parentNode  
 * @returns {undefined}
 */
function readStepFromDatabase(test, testcase, step, stepNumber, parentNode) {
    //var jqxhr = $.getJSON("ReadTestCaseStep", {test: test, testcase:testcase, step:step});
    var jqxhr = $.post("ReadTestCaseStep", {test: test, testcase: testcase, step: step}, "json");
    $.when(jqxhr).then(function(data) {

        var actionList = [];
        //iterates the actions set
        $.each(data.tcsActionList, function(idx, obj) {
            var action = obj
            var actionObj = $.extend(new Action(), action);
            //sets the new action
            actionList[actionObj.sequence] = actionObj;
        });
        //iterates the controls set and associates each one with the corresponding action
        $.each(data.tcsActionControlList, function(idx, obj) {
            //var obj2 = $.parseJSON(obj);
            var control = obj
            //console.log("aa >>   " + action.test);
            var controlObj = $.extend(new Control(), control);

            //gets the action, and updates its control list
            var action = actionList[controlObj.sequence];
            action.addControl(controlObj);
            actionList[controlObj.sequence] = action;
        });

        //updates the description
        $("#step_description_" + stepNumber).attr("value", data.step.description);

        //draws the actions and controls
        drawStep(parentNode, stepNumber, actionList, data.step.type, true);
        //hides loader, meaning that the process has finished
        hideLoader($("#BeforeFirstAction" + stepNumber));
    });

}
/**
 * Auxiliary method that draws the actions that belong to a step. 
 * @param {type} parentNode
 * @param {type} stepNumber
 * @param {type} actionList
 * @param {type} stepType
 * @param {type} temporary - if temporary = true than means that the current step is being edited.
 * @returns {undefined}
 */
function drawStep(parentNode, stepNumber, actionList, stepType, temporary) {
    var htmlToAppend = "";
    var doc = new Doc();
    var classActionOfUSe = 'ActionOfNormalStep '; //normal step
    var readonly = "";
    if (stepType === 1) {
        classActionOfUSe += 'ActionOfUseStep '; //imported step
        readonly = "readonly ";
    }
    if (temporary) {
        classActionOfUSe += 'ActionOfUseStepTemp ';
        readonly = "readonly ";
    }


    //TODO:FN draw step needs to be refactored when the page is converted to the new standards (new css classes should be defined)
    for (var index in actionList) {
        var action = actionList[index];

        //starts action element
        htmlToAppend += '<div style="margin-top:0px;display:block;height:50px;width:100%;border-style: solid; border-width:thin ; border-color:#CCCCCC;" \n\
        class="RowActionDiv ';
        htmlToAppend += classActionOfUSe;
        htmlToAppend += '" id="StepListOfActionDiv' + stepNumber + action.sequence + '">';
        htmlToAppend += '<div style="background-color:blue; width:8px;height:100%;display:inline-block;float:left" name="actionRow_color_' + stepNumber + '">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="display:inline-block;float:left;width:2%;height:100%;text-align:center">';
        //if is a normal step, then we need to add the options to delete 
        if (stepType === 0) {
            htmlToAppend += '<img id="img_delete_' + stepNumber + "_" + action.sequence + '" onclick="checkDeleteBox(\'img_delete_' + stepNumber + "_" + action.sequence + '\', ';
            htmlToAppend += '\'action_delete_' + stepNumber + "_" + action.sequence + '\', \'StepListOfActionDiv' + stepNumber + action.sequence + '\', \'RowActionDiv\')" src="images/bin.png" style="margin-top:12px">';
            htmlToAppend += '<input id="action_delete_' + stepNumber + "_" + action.sequence + '" class="wob" type="checkbox" value="' + stepNumber + "_" + action.sequence + '" style="display:none;';
            htmlToAppend += 'margin-top:20px; background-color: transparent" name="action_delete_' + stepNumber + "_" + action.sequence + '" data-action="delete_action">';
        }
        htmlToAppend += '<input type="hidden" value="' + action.sequence + '" name="action_increment_' + stepNumber + '">';
        htmlToAppend += '<input type="hidden" value="' + stepNumber + '" data-fieldtype="stepNumber" name="action_step_' + stepNumber + '_' + action.sequence + '">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:3%;float:left;display:inline-block">';

        //if is a normal step, then we need to add the options to add action and control
        if (stepType === 0) {
            htmlToAppend += '<div style="margin-top: 5px;height:50%;width:100%;clear:both;display:inline-block">';
            htmlToAppend += '<img onclick="addTCSANew(\'DivActionEndOfAction' + stepNumber + action.sequence + '\', \'' + stepNumber + '\', this)" data-fieldtype="addActionButton" title="' + doc.getDocLabel("page_testcase", "tooltip_addAction") + '" style="width:15px;height:15px" src="images/addAction.png">';
            htmlToAppend += '</div>';
            htmlToAppend += '<div style="margin-top:-15px;height:50%;width:100%;clear:both;display:inline-block">';
            htmlToAppend += '<img onclick="addTCSACNew(\'StepListOfActionDiv' + stepNumber + action.sequence + '\', \'' + stepNumber + '\', \'' + action.sequence + '\', this)" data-fieldtype="addControlButton" title="Add Control" style="width:15px;height:15px" src="images/addControl.png">';
            htmlToAppend += '</div>';
        }

        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:4%;display:inline-block;float:left">';
        htmlToAppend += '<input id="action_sequence_' + stepNumber + '_' + action.sequence + '" name="action_sequence_' + stepNumber + '_' + action.sequence + '" \n\
        data-field="sequence" data-fieldtype="action_' + stepNumber + '" value="' + action.sequence + '" class="wob';
        if (stepType === 1 || temporary) { //imported step
            htmlToAppend += ' readonlyaction_seq ';
        } else {
            htmlToAppend += ' marginTop20 ';
        }
        htmlToAppend += '" ' + readonly + ' /></div>';
        htmlToAppend += '<div style="height:100%;width:80%;float:left; display:inline-block">';
        htmlToAppend += '<div style="height:20px;display:inline-block;clear:both;width:100%; background-color: transparent" class="functional_description">';
        htmlToAppend += '<div style="float:left; width:80%">';
        htmlToAppend += '<div style="float:left;width:80px; ">\n\
        <p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionDescription">Description <a onclick="stopPropagation(event)" \n\
        href="javascript:popup(&quot;Documentation.jsp?DocTable=testcasestepaction&amp;DocField=description&amp;Lang=en&quot;)" \n\
        class="docOnline"><span class="glyphicon glyphicon-question-sign"></span></a></p>';
        htmlToAppend += '</div>';
        htmlToAppend += '<input ' + readonly + ' id="action_description_' + stepNumber + '_' + action.sequence + '" name="action_description_' + stepNumber + '_' + action.sequence +
                '" placeholder="Description" value="' + action.description + '" style="border-style:groove;border-width:thin;border-color:white;border: 1px solid white; \n\
        color:#333333; width: 80%; font-weight:bold;font-size:12px ;font-family: Trebuchet MS; " data-fieldtype="Description" class="wob">';
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="display:inline-block;clear:both; height:15px;width:100%;background-color:transparent">';
        htmlToAppend += '<div style="width: 20%; float:left; background-color: transparent" class="technical_part">';
        htmlToAppend += '<div style="float:left;width:80px; "><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionAction">Action \n\
        <a onclick="stopPropagation(event)" href="javascript:popup(&quot;Documentation.jsp?DocTable=testcasestepaction&amp;DocField=Action&amp;Lang=en&quot;)" class="docOnline">\n\
        <span class="glyphicon glyphicon-question-sign"></span></a></p>';
        htmlToAppend += '</div>';
        //if is a normal step, then we need to add the options to add action and control
        if (stepType === 0) {
            var actionSelect = $("#action_action_template").clone();
            $(actionSelect).attr("id", 'action_action_' + stepNumber + '_' + action.sequence);
            $(actionSelect).attr("name", 'action_action_' + stepNumber + '_' + action.sequence);
            $(actionSelect).find("option[value='" + action.action + "']").attr("selected", "selected");
            htmlToAppend += $(actionSelect).prop("outerHTML");
        } else {
            htmlToAppend += '<input id="action_action_' + stepNumber + '_' + action.sequence + '" value="' + action.action + '" name="action_action_' + stepNumber + '_' +
                    action.sequence + '"  readonly style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; color:#999999"/>';
        }
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width: 25%; float:left; background-color: transparent" class="technical_part">';
        htmlToAppend += '<div style="float:left;"><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionObject1">Val1</p>';
        htmlToAppend += '</div>';
        htmlToAppend += '<input ' + readonly + ' name="action_object_' + stepNumber + '_' + action.sequence + '" id="action_object_' + stepNumber + '_' + action.sequence + '" \n\
        value="' + action.object + '" style="float:left;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; height:100%;width:75%; color:#999999">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width: 20%; float:left; background-color:transparent" class="technical_part">';
        htmlToAppend += '<div style="float:left;"><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionProperty">\n\
        Property <a onclick="stopPropagation(event)" href="javascript:popup(&quot;Documentation.jsp?DocTable=testcasestepaction&amp;DocField=Property&amp;Lang=en&quot;)" \n\
        class="docOnline"><span class="glyphicon glyphicon-question-sign"></span></a></p>';
        htmlToAppend += '</div>';

        //draws the button if it exists
        if (action.propertyButtonAction !== null) {
            htmlToAppend += drawPropertyActionButton(action.property, action.propertyButtonAction, 'id="action_property_' + stepNumber + '_' + action.sequence + '"');
        }

        htmlToAppend += '<input ' + readonly + ' name="action_property_' + stepNumber + '_' + action.sequence + '" id="action_property_' + stepNumber + '_' + action.sequence + '" \n\
        value="' + action.property + '"';
        if (action.propertyButtonAction !== null && (action.propertyButtonAction.type === 2 || action.propertyButtonAction.type === 3)) {
            htmlToAppend += ' data-usestep-test="' + action.propertyButtonAction.usestep_test + '" data-usestep-testcase="' + action.propertyButtonAction.usestep_testcase + '" ';
        }
        htmlToAppend += ' style="width:75%;border-style:groove;border-width:thin;border-color:white;border: 1px solid white; \n\
        color:#888888" class="wob property_value"/>';
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="background-color:blue; width:3px;height:100%;display:inline-block;float:right">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:5%;display:inline-block;float:right">';
        htmlToAppend += '<div id="AttachPictureDiv_' + stepNumber + '_' + action.sequence + '">';

        var onclickEvent = "";
        var imgStyle = "";

        if (stepType === 0 && !temporary) {//normal step that was reset
            if (action.screenshotFilename === "" || action.screenshotFilename === "./images/th.jpg") {
                imgStyle = ' style="margin-top:15px; margin-left:15px"  width="15" height="15" class="AttachPictureClass"';
                onclickEvent = ' onclick="showModalAddPicture(' + stepNumber + ',' + action.sequence + ', null)" ';
            } else {
                imgStyle = ' width="45" height="35" class="wob" ';
                onclickEvent = ' onclick="showPicture(\'' + action.screenshotFilename + '\', ' + stepNumber + ',' + action.sequence + ', null)" ';
            }
        } else {
            if (action.screenshotFilename !== "" && action.screenshotFilename !== "./images/th.jpg") {
                imgStyle = ' width="45" height="35" class="wob" ';
                onclickEvent = '';
            }
        }


        htmlToAppend += '<img ' + imgStyle + ' ' + onclickEvent + ' id="displayedPicture_' + stepNumber + '_' + action.sequence + '" width="45" height="35" src="' + action.screenshotFilename + '" class="wob">';
        htmlToAppend += '</div>';
        if (stepType === 0 && !temporary) {//normal step
            htmlToAppend += '<input id="action_screenshot_' + stepNumber + '_' + action.sequence + '" value="' + action.screenshotFilename + '" \n\
            onchange="showChangedRow(this.parentNode.parentNode)" name="action_screenshot_' + stepNumber + '_' + action.sequence + '" style="display:none">';
        }
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';

        htmlToAppend += drawControlList(stepNumber, action.controlList, temporary, stepType);

        htmlToAppend += '<div id="DivActionEndOfAction' + stepNumber + action.sequence + '" class="endOfAction"></div>';

    }

    //insert the html after the node that indicates the place were action should be listed
    $(parentNode).after(htmlToAppend);
}
/**
 * Auxiliary method that draws a button associated with the property value: to override, overriden and missing.
 * @param {type} propertyValue
 * @param {type} propertyAction
 * @param {type} inputID
 * @returns {String}
 */
function drawPropertyActionButton(propertyValue, propertyAction, inputID) {
    //(propertyValue, toolTipMessage, type, listOfPropertyTypes, inputName)
    return createCommandList(propertyValue, propertyAction.title, propertyAction.type, listOfPropertyTypes, inputID);
}
/**
 * Auxiliary method that draws the controls that belong to an action. 
 * @param {type} stepNumber
 * @param {type} controlList
 * @param {type} temporary - if temporary = true than means that the current step is being edited.
 * @param {type} stepType - normal or imported
 * @returns {String}
 */
function drawControlList(stepNumber, controlList, temporary, stepType) {
    var htmlToAppend = '';
    var doc = new Doc();
    var readonly = "";
    var classActionOfStep = "ActionOfNormalStep ";//normal step

    if (stepType === 1) {
        classActionOfStep = 'ActionOfUseStep '; //imported step
        readonly = "readonly";
    }
    if (temporary) {
        classActionOfStep += ' ActionControlOfUseStepTemp ';
        readonly = "readonly";
    }



    for (var index in controlList) {
        var control = controlList[index];
        htmlToAppend += '<div style="width:100%;height:50px;clear:both;display:block;border-style: solid; border-width:thin ; border-color:#CCCCCC;" \n\
        class="RowActionDiv ';

        htmlToAppend += classActionOfStep;

        htmlToAppend += '" data-associatedaction="StepListOfActionDiv' + stepNumber + control.sequence + '" id="StepListOfControlDiv' +
                stepNumber + control.sequence + control.control + '">';
        htmlToAppend += '<div style="background-color:#33CC33; width:8px;height:100%;display:inline-block;float:left">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width: 2%;float:left; text-align: center;">';

        if (stepType === 0) { //normal step adds or restores the delete option
            htmlToAppend += '<img id="img_delete_' + stepNumber + '_' + control.sequence + '_' + control.control + '" onclick="checkDeleteBox(\'img_delete_' + stepNumber + '_' + control.sequence + '_' + control.control + '\', \'control_delete_' + stepNumber + '_' + control.sequence + '_' + control.control + '\', \'StepListOfControlDiv' + stepNumber + control.sequence + control.control + '\', \'RowActionDiv\')" src="images/bin.png" style="margin-top:12px">';
            htmlToAppend += '<input id="control_delete_' + stepNumber + '_' + control.sequence + '_' + control.control + '" class="wob" type="checkbox" value="' + control.sequence + control.control + '" style="display:none; margin-top:20px; background-color: transparent" name="control_delete_' + stepNumber + '_' + control.sequence + '_' + control.control + '" data-associatedaction="action_delete_' + stepNumber + '_' + control.sequence + '_' + '">';
        }

        htmlToAppend += '<input type="hidden" name="control_increment_' + stepNumber + '_' + control.sequence + '" value="' + control.control + '" />';
        htmlToAppend += '<input type="hidden" data-fieldtype="stepNumber" name="control_step_' + stepNumber + '_' + control.sequence + '_' + control.control + '" value="' + stepNumber + '" />';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:3%;float:left;display:inline-block">';
        //if is to draw a normal step than the buttons to add control and add action should be included
        if (stepType === 0) {
            htmlToAppend += '<div style="margin-top:5px;height:50%;width:100%;clear:both;display:inline-block">';
            htmlToAppend += '<img onclick="addTCSANew(\'DivActionEndOfAction' + stepNumber + control.sequence + '\', \'' + stepNumber + '\', this); enableField(\'submitButtonAction\');" data-fieldtype="addActionButton" title="Add Action" \n\
                style="width:15px;height:15px" src="images/addAction.png">';
            htmlToAppend += '</div>';
            htmlToAppend += '<div style="margin-top:-10px;height:50%;width:100%;clear:both;display:inline-block">';
            htmlToAppend += '<img onclick="addTCSACNew(\'StepListOfControlDiv' + stepNumber + control.sequence + control.control + '\', \'' + stepNumber + '\', \'' + control.sequence + '\', this); enableField(\'submitButtonChanges\');" \n\
                data-fieldtype="addControlButton" title="' + doc.getDocLabel("page_testcase", "tooltip_addControl") + '" style="width:15px;height:15px" src="images/addControl.png">';
            htmlToAppend += '</div>';
        }
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width:2%;float:left;height:100%;display:inline-block">';
        htmlToAppend += '<input name="control_sequence_' + stepNumber + '_' + control.sequence + '_' + control.control + '" value="' + control.sequence + '" class="wob ';
        if (stepType === 1 || temporary) {
            htmlToAppend += ' readonlyaction_seq ';
        } else {
            htmlToAppend += ' marginTop20 ';
        }
        htmlToAppend += '" data-field="sequence" data-fieldtype="ctrlseq_' + stepNumber + '" ' + readonly + ' />';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width:2%;float:left;height:100%;display:inline-block">';
        htmlToAppend += '<input name="control_control_' + stepNumber + '_' + control.sequence + '_' + control.control + '" value="' + control.control + '" \n\
            data-fieldtype="control_' + stepNumber + '_' + control.sequence + '" data-field="control" class="wob ';
        if (stepType === 1 || temporary) {
            htmlToAppend += ' readonlyactioncontrol_seq ';
        } else {
            htmlToAppend += ' marginTop20 ';
        }
        htmlToAppend += '" ' + readonly + ' />';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:80%;float:left;display:inline-block">';
        htmlToAppend += '<div style="clear:both;width:100%;height:20px" class="functional_description">';
        htmlToAppend += '<div style="float:left; width:80%">';
        htmlToAppend += '<div style="float:left;width:80px; "><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionControlDescription">Description</p>';
        htmlToAppend += '</div>';
        htmlToAppend += '<input ' + readonly + ' name="control_description_' + stepNumber + '_' + control.sequence + '_' + control.control + '" id="control_description_' + stepNumber + '_' + control.sequence + '_' + control.control + '" \n\
            value="' + control.description + '" data-fieldtype="Description" \n\
        style="border-style:groove;border-width:thin;border-color:white;border: 2px solid white; color:#333333; width: 80%; font-weight:bold;font-size:12px \n\
        ;font-family: Trebuchet MS; " placeholder="Description" class="wob" />';
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="clear:both;display:inline-block; width:100%; height:15px">';
        htmlToAppend += '<div style="width:30%; float:left;">';
        htmlToAppend += '<div style="float:left;width:80px; "><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionControlType">Type</p>';
        htmlToAppend += '</div>';
        //if is a normal step, then we need to add the options to add action and control
        if (stepType === 0) {
            var controlSelect = $("#control_type_template").clone();
            $(controlSelect).attr("id", 'control_type_' + stepNumber + '_' + control.sequence + '_' + control.control);
            $(controlSelect).attr("name", 'control_type_' + stepNumber + '_' + control.sequence + '_' + control.control);
            $(controlSelect).find("option[value='" + control.type + "']").attr("selected", "selected");
            htmlToAppend += $(controlSelect).prop("outerHTML");
        } else {
            htmlToAppend += '<input value="' + control.type + '" name="control_type_' + stepNumber + '_' + control.sequence + '_' + control.control + '" style="width:50%;font-size:10px ;border: 1px solid white;color:grey" class="technical_part" \n\
            id="control_type_' + stepNumber + '_' + control.sequence + '_' + control.control + '" readonly />';
        }

        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width:30%;float:left;" class="technical_part">';
        htmlToAppend += '<div style="float:left;"><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionControlProperty">Property</p>';
        htmlToAppend += '</div>';
        htmlToAppend += '<input value="' + control.controlProperty + '" name="control_property_' + stepNumber + '_' + control.sequence + '_' + control.control + '" \n\
            id="control_property_' + stepNumber + '_' + control.sequence + '_' + control.control + '" style="width: 70%;border: 1px solid white;  \n\
            color:grey" class="wob" ' + readonly + ' />';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width:30%;float:left; " class="technical_part">';
        htmlToAppend += '<div style="float:left;"><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionControlValue">Value</p>';
        htmlToAppend += '</div>';
        htmlToAppend += '<input value="' + control.controlValue + '" name="control_value_' + stepNumber + '_' + control.sequence + '_' + control.control + '" \n\
        id="control_value_' + stepNumber + '_' + control.sequence + '_' + control.control + '" value="" style="width: 70%;border: 1px solid white; color:grey" class="wob"  ' + readonly + '/>';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="width:8%;float:left; " class="technical_part">';
        htmlToAppend += '<div style="float:left;"><p link="white" style="float:right;font-weight:bold;" name="labelTestCaseStepActionControlFatal">Fatal</p>';
        htmlToAppend += '</div>';
        if (stepType === 0) {
            //select for fatal option
            var fatalSelect = $("#control_fatal_template").clone();
            $(fatalSelect).attr("id", 'control_fatal_' + stepNumber + '_' + control.sequence + '_' + control.control);
            $(fatalSelect).attr("name", 'control_fatal_' + stepNumber + '_' + control.sequence + '_' + control.control);
            $(fatalSelect).find("option[value='" + control.type + "']").attr("selected", "selected");
            htmlToAppend += $(fatalSelect).prop("outerHTML");
        } else {
            htmlToAppend += '<input name="control_fatal_' + stepNumber + '_' + control.sequence + '_' + control.control + '" style="width: 40%;border: 1px solid white;color:grey" \n\
            class="wob" id="control_fatal_' + stepNumber + '_' + control.sequence + '_' + control.control + '" value="' + control.fatal + '" readonly />';
        }
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="background-color:#33CC33; width:3px;height:100%;display:inline-block;float:right">';
        htmlToAppend += '</div>';
        htmlToAppend += '<div style="height:100%;width:5%;display:inline-block;float:right">';
        var onclickEvent = "";

        var imgStyle = "";

        if (stepType === 0 && !temporary) {//normal step that was reset
            if (control.screenshotFilename === "" || control.screenshotFilename === "./images/th.jpg") {
                imgStyle = ' style="margin-top:15px; margin-left:15px"  width="15" height="15" class="AttachPictureClass"';
                onclickEvent = ' onclick="showModalAddPicture(' + stepNumber + ',' + control.sequence + ',' + control.control + ')" ';
            } else {
                imgStyle = ' width="45" height="35" class="wob" ';
                onclickEvent = ' onclick="showPicture(\'' + control.screenshotFilename + '\', ' + stepNumber + ',' + control.sequence + ',' + control.control + ')" ';
            }
        } else {
            if (control.screenshotFilename !== "" && control.screenshotFilename !== "./images/th.jpg") {
                imgStyle = ' width="45" height="35" class="wob" ';
                onclickEvent = '';
            }
        }
        htmlToAppend += '<div id="AttachPictureDiv_' + stepNumber + '_' + control.sequence + '_' + control.control + '">';
        htmlToAppend += '<img ' + imgStyle + ' ' + onclickEvent + ' id="displayedPicture_' + stepNumber + '_' + control.sequence + '_' + control.control + '"  src="' + control.screenshotFilename + '" />';
        htmlToAppend += '</div>';
        if (stepType === 0 && !temporary) {//normal step
            htmlToAppend += '<input id="control_screenshot_' + stepNumber + '_' + control.sequence + '" value="' + control.screenshotFilename + '" \n\
            onchange="showChangedRow(this.parentNode.parentNode)" name="control_screenshot_' + stepNumber + '_' + control.sequence + '_' + control.control + '" style="display:none">';
        }
        htmlToAppend += '</div>';
        htmlToAppend += '</div>';
    }
    return htmlToAppend;
}


/**
 * Method that overrides a property from the click on the button
 * @param {type} element - element clicked by tge yser
 * @returns {undefined}
 */
function overrideProperty(element) {
    var property = $(element).next("input.property_value");//gets the input tag next to the img tag
    var propertyName = cleanPropertyName($(property).attr("value")); //gets the name of the property
    if (propertyName)
    var testID = $("#hiddenInformationTest").attr("value");
    var testCaseID = $("#hiddenInformationTestCase").attr("value");

    if (property.data("usestep-step") !== null && property.data("usestep-step") !== "") {
        var useTest = property.data("usestep-test");
        var useTestcase = property.data("usestep-testcase");
        $.get("./ImportPropertyOfATestCaseToAnOtherTestCase", {"fromtest": useTest, "fromtestcase": useTestcase,
            "totest": testID, "totestcase": testCaseID,
            "property": propertyName}
        , function(data) {
            if (getAlertType(data.messageType) === 'success') {
                $("#selectTestCase").submit();
            } else {
                //TODO:FN refactor this when the page is converted to the new GUI standards
                //showMessageMainPage()  
                alert(data.message);
            }
        }
        );
    }
}

/**
 * Checks if there are undefined properties. If the false value is returned, then the user will be able to run 
 * the test case. 
 * @returns {Boolean} 
 */
function checkUndefinedProperties() {
    var doc = getDoc();
    if ($("div.dropdown button.property_missing span[class*='glyphicon-warning-sign']").size()) {
        alert(doc.page_testcase.undefined_error_message.docLabel);
        return false;
    }
    return true;
}

/**
 * Clean the property name by removing any % character
 *
 * @param propertyName the property name to clean
 */
function cleanPropertyName(propertyName) {
    return propertyName === undefined || propertyName === '' ? propertyName : propertyName.replace(/%/g, '');
}

/**
 * Auxiliary function that draws the icon near to the property name based on whether it is defined, imported or overridden.
 * @returns {undefined}
 */
function drawPropertySymbolHandler() {
    var doc = new Doc();

    var element = this;
    var propertyValue = cleanPropertyName(element.value);


    //type:
    //0 - none
    //1 - create
    //2 - override
    //3 - overidden
    var type = 0;
    if (propertyValue && propertyValue !== "") {
        //var jinput = $(this);
        $(element).css({"width": "60%"});
        var toolTipMessage = "";
        var testDesc = $(element).attr('data-usestep-test');

        if (!Boolean(testDesc) && $("input.property_name[value='" + propertyValue + "']").length === 0) {
            //check if is an access to a subdata entry
            var isToCreate = true;
            var isSubDataAccess = propertyValue.match("^[_A-Za-z0-9]+\\([_A-Za-z0-9]+\\)$");
            //is a format of the subdataaccess
            if (isSubDataAccess !== null) {
                //check if the property from getdatalibrary exists
                //get the name for the property
                var name = propertyValue.split(new RegExp("\\s+|\\(\\s*|\\)"));
                if (($("input.property_name[value='" + name[0] + "'] ").length !== 0)) {
                    //only adds the button to create if the type is not getFromDataLib
                    //gets the type of the property
                    var idNumber = $("input.property_name[value='" + name[0] + "'] ").attr("id").replace("properties_property_", "");
                    var propertyType = $("#properties_type_" + idNumber).val();

                    if (propertyType === "getFromDataLib") {
                        //"You are using the syntax to acces a GetFromDataLIB " + propertyValue +" is missing! Create the corresponding property! ";                    
                        propertyValue = name[0];
                        isToCreate = false;
                    }
                }
            }
            //Missing - property is not defined anywhere
            if (isToCreate) {
                toolTipMessage = doc.getDocLabel("page_testcase", "tooltip_clicktocreate").replace("%P%", propertyValue);
                type = 1;
            }

        } else if (Boolean(testDesc)) { //verify if it is defined
            var testCaseDesc = $(element).attr('data-usestep-testcase');
            var testStepDesc = $(element).attr('data-usestep-step');

            if ($("input.property_name[value='" + propertyValue + "']").length !== 0) {
                //Overridden - the property was defined in the imported step and redefined in the current test case                            
                toolTipMessage = doc.getDocLabel("page_testcase", "tooltip_infooverriden").replace("%P%", propertyValue).
                        replace("%T%", testDesc).replace("%TC%", testCaseDesc).replace("%S%", testStepDesc);
                type = 3;
            } else {
                ////Imported - the property is only defined in the import test step

                toolTipMessage = doc.getDocLabel("page_testcase", "tooltip_clicktooverride").replace("%P%", propertyValue).
                        replace("%T%", testDesc).replace("%TC%", testCaseDesc).replace("%S%", testStepDesc);
                type = 2;
            }
        }

        //if the property is not related to an imported step and if there is an image defined     
        //then the image is added into the page
        //the default scenario does not add any image to the property definition
        if (!Boolean($(element).attr('data-imported-property')) && (type > 0)) {
            /*$(element).before("<img " + classForImage + " data-property-name='" + propertyValue + 
             "' src='" + imageUrl + "' title='" + toolTipMessage +"' style='float:left;display:inline;' width='16px' height='16px' />");                          */
            $(element).before(createCommandList(propertyValue, toolTipMessage, type, listOfPropertyTypes, $(element).attr('name')));
        }
    }

}
/**
 * Method that creates a new property from the click on the item of the command list
 * @param {type} propertyName - name of the property
 * @param {type} propertyType - type of the property
 * @returns {undefined}
 */
function createNewPropertyFromCommandList(propertyName, propertyType) {
    //var propertyName = propertyValue;//(this).attr("data-property-name"); //gets the name of the property    
    var testID = $("#hiddenInformationTest").attr("value");
    var testCaseID = $("#hiddenInformationTestCase").attr("value");

    var user = getUser();

    /*checks if is a getFromDataLib syntax and if it is then send all the property name (i.e., excludes the subdata value)*/
    var isSubDataAccess = propertyName.match("^[_A-Za-z0-9]+\\([_A-Za-z0-9]+\\)$");
    //is a format of the subdataaccess
    if (isSubDataAccess !== null && propertyType === "getFromDataLib") {
        //check if the property from getdatalibrary exists
        //get the name for the property
        var name = propertyName.split(new RegExp("\\s+|\\(\\s*|\\)"));
        //"You are using the syntax to acces a GetFromDataLIB " 
        propertyName = name[0];
    }

    $.get("./CreateNotDefinedProperty", {"totest": testID, "totestcase": testCaseID,
        "property": propertyName, "propertyType": propertyType, "userLanguage": user.language}
    , function(data) {
        if (getAlertType(data.messageType) === 'success') {
            $("#selectTestCase").submit();
        } else {
            //TODO:FN refactor this when the page is converted to the new GUI standards
            //showMessageMainPage()                
            alert(data.message);
        }
    });
}
function createCommandList(propertyValue, toolTipMessage, type, listOfPropertyTypes, inputName) {
    var htmlButton = '';
    var additionalContent = '';
    if (type !== 0) {
        switch (type) {
            case 1: //click to create
                htmlButton = '<div class="dropdown"><button title="' + toolTipMessage +
                        '" class="btn btn-xs dropdown-toggle property_missing" type="button" id="dropDownMenu' + inputName +
                        '" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">\n\
                             <span class="glyphicon glyphicon-warning-sign colorRed"></span><span class="caret colorRed"></button>';

                //adds all types of properties
                additionalContent = '<ul class="dropdown-menu typesMenu" aria-labelledby="dropDownMenu' + inputName + '">';
                $.each(listOfPropertyTypes, function(index) {
                    additionalContent += '<li><a href="#" onclick="createNewPropertyFromCommandList(\'' + propertyValue + '\', \'' +
                            listOfPropertyTypes[index] + '\');return false;" name="command_' + listOfPropertyTypes[index] + '">' +
                            listOfPropertyTypes[index] + '</a></li>';
                })
                additionalContent += '</ul>';
                htmlButton += additionalContent + '</div>';
                break;
            case 2: //click to override
                htmlButton = '<button title="' + toolTipMessage + '" class="btn btn-xs property_tooverride" type="button" \n\
                id="tooverride' + inputName + '" onclick="overrideProperty(this); return false;"><span class="glyphicon glyphicon-pencil colorDarkBlue"></span></button>';
                break;
            case 3: //overridden
                htmlButton = '<button title="' + toolTipMessage + '" class="btn btn-xs property_overriden" type="button" id="overriden' + inputName + '">\n\
                            <span class="glyphicon glyphicon-import colorDarkYellow"></span></button>';
                break;
        }

    }
    return htmlButton;
}

/**
 * Method that translates the content of the pages with base on the user language.
 */
function displayPageLabel() {
    var doc = new Doc();

    displayHeaderLabel(doc);
    displayGlobalLabel(doc);
    //tooltips of the buttons for the property type getFromDataLib
    $("button[id='entryButton']").prop("title", doc.getDocLabel('page_testcase', 'tooltip_select_entry'));
    $("button[data-id='entryButton_template']").prop("title", doc.getDocLabel('page_testcase', 'tooltip_select_entry'));
    $("*[name='labelTest']").html(doc.getDocOnline("test", "Test"));
    $("*[name='labelTestCase']").html(doc.getDocOnline("testcase", "TestCase"));
    $("*[name='labelTestCaseStepActionDescription']").html(doc.getDocOnline("testcasestepaction", "description"));
    $("*[name='labelTestCaseStepActionAction']").html(doc.getDocOnline("testcasestepaction", "Action"));
    $("*[name='labelTestCaseStepActionObject']").html(doc.getDocOnline("testcasestepaction", "Value1"));
    $("*[name='labelTestCaseStepActionProperty']").html(doc.getDocOnline("testcasestepaction", "Value2"));
    $("*[name='labelTestCaseStepActionForce']").html(doc.getDocOnline("testcasestepaction", "ForceExeStatus"));
    $("*[name='labelTestCaseStepActionControlType']").html(doc.getDocOnline("testcasestepactioncontrol", "Control"));

}
/**
 * Applies the translations for the get list of test cases modal.
 * @param {type} doc object that contains Cerberus' documentation 
 */
function displayListTestDataLib(doc) {
    //title
    $("#selectEntryFromListModalLabel").html(doc.page_testcase_m_listtestdatalib.title.docLabel);
    //close button
    $("#closeTCListTestDataLib").text(doc.page_global.buttonClose.docLabel);
}

/*****************************************************************************/
/**
 * Set the entry name selected in the textarea "Value1"
 * @param {type} button -- button selected 
 * @returns {undefined}
 */
function selectEntry(button) {
    //select the entry name in order to put it into the Value1 textarea, and uses it to get the subdata list entries
    var text = $(button).parents("td").siblings(":first").text();
    var propertyValue = $('#selectEntryFromListModal').find("#propertyValueID").prop("value");

    //hides de modal dialog
    $('#selectEntryFromListModal').modal("hide");


    //updates the data stored in the testcase.jsp page
    //triggers the change event if the current value is different from the new value

    if ($("#properties_value1_" + propertyValue).prop("value") !== text) {
        $("#properties_value1_" + propertyValue).prop("value", text);
        //we need to trigger the change event in order to handle the required actions
        $("#properties_value1_" + propertyValue).trigger("change");
        //we need to invoke the blur() method to activate the changes on the autocomplete plugin
        $("#properties_value1_" + propertyValue).blur();
    }


}


/**
 * Callback function that handles the click on the button that allows the user to select a entry.
 */
function setEntrybuttonClickHandler() {
    var id = $(this).prop("id");
    var propertyValue = id.replace("entryButton_", "");
    createEntriesTable(propertyValue);
    $('#selectEntryFromListModal').find("#propertyValueID").prop("value", propertyValue);
    $('#selectEntryFromListModal').modal("show");
}
/**
 * Auxiliary function that configures the autocomplete component
 * @param {type} selector
 * @param {type} source
 * @returns {undefined}
 */
function setPropertyValuesAutoComplete(selector, source) {
    var taValue1Config = {};

    taValue1Config["source"] = source;

    //does not display the summary text
    taValue1Config["messages"] = {
        noResults: '',
        results: function() {
        }
    };
    taValue1Config["delay"] = 500;
    //for each textarea for value1 that has the property type getFromDataLib we will set the autocomplete
    $(selector).autocomplete(taValue1Config);

}
/**
 * Callback function used to perform autocomplete while user is typing
 * @param {type} request 
 * @param {type} response
 * @returns {undefined}
 */
function callbackAutoCompleteTestDataLibName(request, response) {
    $.ajax({
        url: "ReadTestDataLib?name=" + request.term + "&limit=10",
        dataType: "json",
        success: function(data) {
            response(data["data"]);
        }
    });
}

/**
 * Creates a table that shows the list of entries
 * @returns {undefined}
 */
function createEntriesTable() {

    var oTable = $('#listOfTestDataLib').hasClass('dataTable');

    if (oTable === false) {

        //var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "GetTestDataLib", "TestDataLib", aoColumnsFunc(propertyValue));
        var configurations = new TableConfigurationsServerSide("listOfTestDataLib", "ReadTestDataLib", "contentTable", aoColumnsFunc());
        configurations.scrollY = "460px";
        showLoaderInModal('#selectEntryFromListModal');
        $.when(createDataTableWithPermissions(configurations)).then(function() {
            hideLoaderInModal('#selectEntryFromListModal');
        });

    }
}
/***
 * Auxiliary function used to customise the datatable aesthetics and translations
 * @returns {aoColumnsFunc.aoColumns|Array}
 */
function aoColumnsFunc() {
    var doc = new Doc();
    //TODO:FN rename translations
    //var docTestDataLib = doc.testdatalib;
    //var docModal = doc.page_testcase_m_listtestdatalib;
    var aoColumns = [];
    $("#listOfTestDataLib th").each(function(i) {
        switch (i) {
            case 0:
                aoColumns.push({
                    className: "width150  center",
                    "data": "testDataLibID",
                    "sName": "tdl.TestDataLibID",
                    "bSortable": false,
                    "title": doc.getDocLabel("page_testcase_m_listtestdatalib", "actions"),
                    "mRender": function(data, type, oObj) {
                        var selectElement = '<button id="selectEntry' + data + '"  onclick="selectEntry(this);" \n\
                                                class="selectEntry btn btn-default btn-xs margin-right5" \n\
                                            name="editTestDataLib" title="' + doc.getDocLabel("page_testcase_m_listtestdatalib", "tooltip_choose_entry") + '" type="button">\n\
                                            <span class="glyphicon glyphicon-hand-up"></span></button>';
                        return '<div class="btn-group center">' + selectElement + '</div>';
                    }});
                break;

            case 1 :
                aoColumns.push({className: "width250", "sName": "tdl.Name", "data": "name", "title": doc.getDocLabel("testdatalib", "name")});
                break;
            case 2 :
                aoColumns.push({className: "width80", "sName": "tdl.System", "data": "system", "title": doc.getDocLabel("testdatalib", "system")});
                break;
            case 3 :
                aoColumns.push({className: "width100", "sName": "tdl.Environment", "data": "environment", "title": doc.getDocLabel("testdatalib", "environment")});
                break;
            case 4 :
                aoColumns.push({className: "width80", "sName": "tdl.Country", "data": "country", "title": doc.getDocLabel("testdatalib", "country")});
                break;
            case 5 :
                aoColumns.push({className: "width100", "sName": "tdl.Group", "data": "group", "title": doc.getDocLabel("testdatalib", "group")});
                break;
            case 6 :
                aoColumns.push({className: "width80", "sName": "tdl.Type", "data": "type", "title": doc.getDocLabel("testdatalib", "type")});
                break;
            case 7 :
                aoColumns.push({className: "width100", "sName": "tdl.Database", "data": "database", "title": doc.getDocLabel("testdatalib", "database")});
                break;
            case 8 :
                aoColumns.push({className: "width500", "sName": "tdl.Script", "data": "script", "title": doc.getDocLabel("testdatalib", "script")});
                break;
            case 9 :
                aoColumns.push({className: "width250", "sName": "tdl.ServicePath", "data": "servicePath", "title": doc.getDocLabel("testdatalib", "servicepath"),
                    "mRender": function(data, type, oObj) {
                        return drawURL(data);//TODO:FN check the special characters that may be encapsulated                        
                    }});
                break;
            case 10 :
                aoColumns.push({className: "width250", "sName": "tdl.Method", "data": "method", "title": doc.getDocLabel("testdatalib", "method")});
                break;
            case 11 :
                aoColumns.push({className: "width500", "sName": "tdl.Envelope", "data": "envelope", "title": doc.getDocLabel("testdatalib", "envelope")});
                break;
            case 12:
                aoColumns.push({className: "width150", "sName": "tdl.Description", "data": "description", "title": doc.getDocLabel("testdatalib", "description")});
                break;

            default :
                aoColumns.push({"sWidth": "100px"});
                break;
        }
    });
    return aoColumns;


}
/******************* Used Step functions ******************************/
function findTestcaseByTest(test, system, field) {
    var url;
    if (system === "") {
        url = 'GetTestCaseList?test=' + test;
    } else {
        url = 'GetTestCaseForTest?system=' + system + '&test=' + test;
    }
    $.get(url, function(data) {
        $(document.getElementById(field)).empty();
        $('#' + field).append($("<option></option>")
                .attr('value', '')
                .attr('style', 'width:400px;')
                .text('Choose TestCase'));
        if (system !== "") {
            for (var i = 0; i < data.testCaseList.length; i++) {
                $('#' + field).append($("<option></option>")
                        .attr('value', data.testCaseList[i].testCase)
                        .attr('style', 'width:400px;')
                        .text(data.testCaseList[i].description));
            }
        } else {
            for (var i = 0; i < data.testcasesList.length; i++) {
                $('#' + field).append($("<option></option>")
                        .attr('value', data.testcasesList[i])
                        .attr('style', 'width:400px;')
                        .text(data.testcasesList[i]));
            }
        }
    });
}

/**
 * Auxiliary method that performs the calls in chain that are necessary to load the select components for testcase and teststep. This is necessary because we want to load the step select
 * only after the testcase select is changed. 
 * @param {type} testElement
 * @param {type} testCaseElement
 * @param {type} testStepElement
 * @param {type} system
 * @param {type} refreshButton
 * @param {type} testSelectedOption
 * @param {type} testCaseSelectedOption
 * @param {type} testStepSelectedOption
 * @param {type} reset
 * @returns {undefined}
 */
function loadTestCaseAndStepComponents(testElement, testCaseElement, testStepElement, system,
        refreshButton, testSelectedOption, testCaseSelectedOption, testStepSelectedOption, reset) {
    //disables the button that performs the reload
    $(refreshButton).attr("disabled", "disabled");
    var stepNumber = $(testElement).attr("data-step-number");
    $("#linkEditUsedStep" + stepNumber).attr("href", "#");

    //sets the test value
    $(testElement).attr("value", testSelectedOption);

    var url = 'GetStepInLibrary?test=' + testSelectedOption;
    $.get(url, function(data) {
        $(testCaseElement).empty();
        $(testCaseElement).append($("<option></option>")
                .attr('value', '')
                .attr('style', 'width:400px;')
                .text('Choose TestCase'));
        var testFromLib = "";

        for (var i = 0; i < data.testCaseStepList.length; i++) {
            if (data.testCaseStepList[i].testCase !== testFromLib) {
                $(testCaseElement).append($("<option></option>")
                        .attr('value', data.testCaseStepList[i].testCase)
                        .attr('style', 'width:400px;')
                        .text(data.testCaseStepList[i].testCase + " : " + data.testCaseStepList[i].tcdesc));
                testFromLib = data.testCaseStepList[i].testCase;
            }
        }

        //sets the value for the test case
        $(testCaseElement).find("option[value='" + testCaseSelectedOption + "']").attr("selected", "selected");

        //loads the test case information
        findStepBySystemTestTestCase(testElement, testCaseElement, system, testStepElement, refreshButton, testStepSelectedOption, reset);
    });
}
function findStepBySystemTest(testElement, system, testCaseElement, refreshElement, testCaseSelectedOption) {
    var url;
    var test = $(testElement).val();
    if (test !== '') {
        $(testElement).find("option[value='']").remove();
    }
    $(refreshElement).attr("disabled", "disabled");


    url = 'GetStepInLibrary?test=' + test;
    $.get(url, function(data) {
        $(testCaseElement).empty();
        $(testCaseElement).append($("<option></option>")
                .attr('value', '')
                .attr('style', 'width:400px;')
                .text('Choose TestCase'));
        var testFromLib = "";

        //clears the step dropdown
        var elementParent = $(testCaseElement).parents("div[id*=StepFirstLineDiv]");
        var selectSteps = $(elementParent).find("div[id*='StepUseStepStepDiv'] select");
        $(selectSteps).find("option").remove();
        $(selectSteps).append($("<option></option>").attr('value', '').attr('style', 'width:300px;').text('---'));
        var stepNumber = $(selectSteps).attr("data-step-number");

        $("#linkEditUsedStep" + stepNumber).attr("href", "#");
        $("#linkEditUsedStep" + stepNumber).attr("target", "_self");

        for (var i = 0; i < data.testCaseStepList.length; i++) {
            if (data.testCaseStepList[i].testCase !== testFromLib) {
                $(testCaseElement).append($("<option></option>")
                        .attr('value', data.testCaseStepList[i].testCase)
                        .attr('style', 'width:400px;')
                        .text(data.testCaseStepList[i].testCase + " [" + data.testCaseStepList[i].tcapp + "] : " + data.testCaseStepList[i].tcdesc));
                testFromLib = data.testCaseStepList[i].testCase;
            }
        }
        if (testCaseSelectedOption !== null) {
            $(testCaseElement).find("option[value='" + testCaseSelectedOption + "']").attr("selected", "selected");
        }
    });
}

function findStepBySystemTestTestCase(testElement, testCaseElement, system, testStepElement, refreshButton, testStepSelectedOption, reset) {

    var test = $(testElement).val();
    var testCase = $(testCaseElement).val();

    //removes the choose test option
    if (testCase !== "") {
        $(testCaseElement).find("option[value='']").remove();
    }
    //disables the button to load the actions and controls
    $(refreshButton).attr("disabled", "disabled");
    var url;
    url = 'GetStepInLibrary?test=' + test + '&testCase=' + testCase;
    $.get(url, function(data) {
        $(testStepElement).empty();
        $(testStepElement).append($("<option></option>")
                .attr('value', '')
                .attr('style', 'width:400px;')
                .text('Choose Step'));
        for (var i = 0; i < data.testCaseStepList.length; i++) {
            $(testStepElement).append($("<option></option>")
                    .attr('value', data.testCaseStepList[i].step)
                    .attr('style', 'width:400px;')
                    .text(data.testCaseStepList[i].sort + ':' + data.testCaseStepList[i].description));
        }

        $(testStepElement).find("option[value='" + testStepSelectedOption + "']").attr("selected", "selected");
        var stepNumber = $(testStepElement).attr("data-step-number");

        if (testStepSelectedOption !== "" && testStepSelectedOption !== 0) {
            //resets the edit step url
            setNewHrefForEditStep(stepNumber, test, testCase, testStepSelectedOption);
            //show the options to load and reset
            showLinkElementsPanel(stepNumber, reset);
            $(testStepElement).find("option[value='']").remove();
        } else {
            $("#linkEditUsedStep" + stepNumber).attr("href", "#");
            $("#linkEditUsedStep" + stepNumber).attr("target", "_self");
        }
    });
}

function findStepByTestCase(test, testcase, field) {
    $.get('GetTestCase?testcase=' + testcase + '&test=' + test, function(data) {
        $('#' + field).empty();
        $('#' + field).append($("<option></option>")
                .attr('value', '')
                .attr('style', 'width:400px;')
                .text('Choose Step'));
        for (var i = 0; i < data.list.length; i++) {
            $('#' + field).append($("<option></option>")
                    .attr('value', data.list[i].number)
                    .attr('style', 'width:400px;')
                    .text(data.list[i].number + ':' + data.list[i].name));
        }
    });
}
/**********************************modal features *************************/
/**
 * Shows the modal dialog that allows the user to associate an url to an action or control
 * @param {type} step  
 * @param {type} action
 * @param {type} control
 * @returns {undefined}
 */
function showModalAddPicture(step, action, control) {
    clearResponseMessage($('#addPictureModal'));
    //clears the input
    $('#attachNewScreenshot').attr("value", "");

    //add the translations
    $('#addPictureModal').modal('show');
    var doc = new Doc();
    //update the labels according the current documentation
    $('#addPictureModalTitle').text(doc.getDocLabel("page_testcase_m_addPicture", "title"));
    $('#lblFeedUrl').text(doc.getDocLabel("page_testcase_m_addPicture", "lbl_feedurl"));
    $('#closeAddPictureButton').text(doc.getDocLabel("page_global", "buttonClose"));
    $('#addAddPictureButton').text(doc.getDocLabel("page_global", "btn_add"));

    $('#addPictureModal #step').attr("value", step);
    $('#addPictureModal #action').attr("value", action);
    if (control !== null) {
        $('#addPictureModal #control').attr("value", control);
    }

    //include the handler for the ok option
    $('#addPictureModal #addAddPictureButton').click(addPictureClickHandler);
}
/**
 * Auxiliary function that opens the modal that allows user to view/remove the picture associated with an action/control.
 * @param {type} pictureUrl
 * @param {type} step
 * @param {type} action
 * @param {type} control
 * @returns {undefined}
 */
function showPicture(pictureUrl, step, action, control) {
    var doc = new Doc();
    $('#attachNewScreenshot').attr("pictureUrl", "");
    $('#showPictureModalTitle').text(doc.getDocLabel("page_testcase_m_showPicture", "title"));
    $('#removePictureButton').text(doc.getDocLabel("page_testcase_m_showPicture", "btn_remove"));
    $('#closeShowPictureButton').text(doc.getDocLabel("page_global", "buttonClose"));

    //set the translations
    $('#selectedPicture').attr("src", pictureUrl);
    $('#showPictureModal #step').attr("value", step);
    $('#showPictureModal #action').attr("value", action);
    if (control !== null) {
        $('#showPictureModal #control').attr("value", control);
    }
    $('#showPictureModal').modal('show');
    $('#removePictureButton').click(removePictureClickHandler);
}
/**
 * Auxiliary method that removes the image from the action or control when the user clicks "Remove" in the show picture modal
 * @returns {undefined}
 */
function removePictureClickHandler() {
    var pictureUrl = "";
    var step = $('#showPictureModal #step').attr("value");
    var action = $('#showPictureModal #action').attr("value");
    var control = $('#showPictureModal #control').attr("value");
    updatePicture(pictureUrl, step, action, control);


    $('#showPictureModal').modal('hide');

}
/**
 * Funtion that updates the picture url in an action and control
 * @param {type} pictureUrl
 * @param {type} step
 * @param {type} action
 * @param {type} control
 * @returns {undefined}
 */
function updatePicture(pictureUrl, step, action, control) {
    var attachPictureDivID = "#AttachPictureDiv_" + step + "_" + action;
    //updates the image in the test case page
    if (control !== "") {
        attachPictureDivID += "_" + control;
    }

    var imgID = "displayedPicture_" + step + "_" + action;
    var screenShotValue = 'action_screenshot_' + step + '_' + action;
    var attachPictureDivID = "#AttachPictureDiv_" + step + "_" + action;

    //updates the image in the test case page
    if (control !== "") {
        imgID += "_" + control;
        screenShotValue = 'control_screenshot_' + step + '_' + action + "_" + control;
        attachPictureDivID += "_" + control;
    }

    var element = '<img id="' + imgID + '"class="wob" width="45" height="35" src="' + pictureUrl + '" ';
    element += 'onclick="showPicture(\'' + pictureUrl + '\', \'' + screenShotValue + '\', \'AttachPictureDiv_' + step + '_' + action + '\')" />';
    element += '<input id="' + screenShotValue + '" value="' + pictureUrl + '" onchange="showChangedRow(this.parentNode.parentNode)" name="' + screenShotValue + '" style="display:none">';
    $(attachPictureDivID).html(element);

    if (control !== "") {
        $('#StepListOfControlDiv' + step + action + control).addClass("ActionControlOfUseStepTemp");
    } else {
        $('#StepListOfActionDiv' + step + action).addClass("ActionOfUseStepTemp");
    }

}

/**
 * Auxiliary method that saves the image when the user clicks in "OK" in the add picture modal
 * @returns {undefined}
 */
function addPictureClickHandler() {
    var pictureURL = $('#attachNewScreenshot').attr("value");

    if (pictureURL === "") {//user needs to enter a URL different of empty
        var doc = new Doc();
        var localMessage = new Message("danger", doc.getDocLabel("page_testcase_m_addPicture", "error_message_empty"));
        showMessage(localMessage, $('#addPictureModal'));
    } else {
        var step = $('#addPictureModal #step').attr("value");
        var action = $('#addPictureModal #action').attr("value");
        var control = $('#addPictureModal #control').attr("value");

        updatePicture(pictureURL, step, action, control);

        $('#addPictureModal').modal('hide');
    }

}

/**
 * Auxiliary function that verifies if the 
 * @returns {undefined}
 */
function saveAsClickHandler(){
    var test = $("#inputAddTestInSelectTest").attr("value"); 
    var testCase = $("#inputAddTestCaseInSelectTestCase").attr("value").trim();
    if(test !=='' && testCase === ''){
        //TODO: translations and new GUI standars (alert messages)
        alert("'Save as' operation: Test case parameter is mandatory!");
        return false;        
    }else if(test === '' && testCase !== ''){
        //if test is empty then we are creating a test case in the same test, we need to check if it was not
        //already created
        var foundTestCase = $("#informationTestCase option[value='" + testCase + "']").length > 1;
        if(foundTestCase){
            alert("'Save as' operation: Test case you are trying to insert already exists in the current test!");
            return false;
        }

    }
    
    return $('#UpdateTestCase').submit();
}
/**
 * 
 * @param {type} img
 * @param {type} checkbox
 * @param {type} row
 * @param {type} initClassName
 * @returns {undefined}
 */
function checkDeleteBox(img, checkbox, row, initClassName) {
    console.log(document.getElementById(checkbox).checked);
    if (document.getElementById(checkbox).checked === false) {
        document.getElementById(checkbox).checked = true;
        document.getElementById(row).className = 'RowToDelete';
        document.getElementById(img).src = 'images/ko.png';
        $("div[data-associatedaction='" + row + "']").each(function(index, field) {
            $(field).attr('class', 'RowToDelete');
            $(field).find("img[src='images/bin.png']").attr('src', 'images/ko.png');
        });
    } else {
        document.getElementById(checkbox).checked = false;
        document.getElementById(row).className = initClassName;
        document.getElementById(img).src = 'images/bin.png';
        $("div[data-associatedaction='" + row + "']").each(function(index, field) {
            $(field).attr('class', initClassName);
            $(field).find("img[src='images/ko.png']").attr('src', 'images/bin.png');
        });
    }
}
/**
 * Show modal listing the test cases that use the step
 * @param {type} stepNumber
 * @returns {undefined}
 */
function showTestCaseUsingThisStep(stepNumber) {
    $("#showTestCasesModal #listofTC").empty();
    $("#showTestCasesModal #listofTC").html($("#listOfTestCaseUsingStep" + stepNumber).html());
    $("#showTestCasesModal").modal("show");
}

function openViewPropertyPopin(propertyID, test, testcase) {
    $("#showPropertyModal #showPropertyModalContent").empty();
    var prop = $(document.getElementById("properties_property_" + propertyID)).val();
    var value = $(document.getElementById("properties_value1_" + propertyID)).val();
    var db = $('select#properties_dtb_' + propertyID + '[name=\'properties_dtb_' + propertyID + '\']').val();
    var type = $(document.getElementById('properties_type_' + propertyID)).val();


    $("#showPropertyModal #showPropertyModalContent").load('ViewProperty.jsp?type=' + encodeURI(type) + '&db=' + encodeURI(db) + '&test=' + encodeURI(test) + '&testcase=' + encodeURI(testcase) + '&property=' + encodeURI(value));
    $("#showPropertyModal").modal("show");

}

function setSQLValue(value, type, valueField, propertyTypeField) {
    $("#" + valueField).attr("value", value);
    $("#" + propertyTypeField).find("option[value='" + type + "']").attr("selected", "selected");
    $("#showSQLLibraryModal").modal("hide");
}


function openSqlLibraryPopin(valueId, typeId) {
    loadSqlLibraryPopin(valueId, typeId);
    $("#showSQLLibraryModal").modal("show");
}

function loadSqlLibraryPopin(valueId, typeId) {
    $("#showSQLLibraryModal #showSQLLibraryModalContent").empty();
    $('#showSQLLibraryModal #showSQLLibraryModalContent').load('SqlLib.jsp?valueid=' + valueId + "&typeid=" + typeId);

}

function showEntireValue(valueId, nbline, buttonOneId, buttonTwoId) {
    //TODO:FN this is not implemented yet. previous implementation does not seem to work
    /*document.getElementById(valueId).rows = nbline;
     document.getElementById(buttonOneId).style.display = "none";
     document.getElementById(buttonTwoId).style.display = "inline";*/
}

function showLessValue(valueId, buttonOneId, buttonTwoId) {
    //TODO:FN this is not implemented yet. previous implementation does not seem to work
    /*document.getElementById(valueId).rows = "2";
     document.getElementById(buttonOneId).style.display = "inline";
     document.getElementById(buttonTwoId).style.display = "none";*/
}

function showSqlInstruction(valueId, buttonOneId, buttonTwoId) {
    //TODO:FN this is not implemented yet. previous implementation does not seem to work
    /*document.getElementById(valueId).style.display = "inline";
     document.getElementById(buttonOneId).style.display = "none";
     document.getElementById(buttonTwoId).style.display = "inline";*/
}
function hideSqlInstruction(valueId, buttonOneId, buttonTwoId) {
    //TODO:FN this is not implemented yet. previous implementation didn't work
    /*document.getElementById(valueId).style.display = "none";
     document.getElementById(buttonOneId).style.display = "inline";
     document.getElementById(buttonTwoId).style.display = "none";*/
}


function openTestData(value) {
    var win = window.open('TestData.jsp?Search=' + value, '_blank');
    if (win) {
        win.focus();
    } else {
        alert('Please allow popups for Cerberus');
    }
}
/*********************drag and drop functions ******************************/
function insertTCS(event, incStep) {
    event.preventDefault();
}
function drag(ev, th) {

    ev.dataTransfer.setData("text/html", ev.target.id);
    ev.dataTransfer.setData("step", ev.target.dataset.step);
    ev.dataTransfer.setData("test", ev.target.dataset.test);
    ev.dataTransfer.setData("testcase", ev.target.dataset.testcase);

    console.log(th);
}

function drop(ev, incStep) {
    ev.preventDefault();
    var step = ev.dataTransfer.getData("step");
    var test = ev.dataTransfer.getData("test");
    var testcase = ev.dataTransfer.getData("testcase");

    if (incStep === null) {
        //parentNodeID = 'StepNumberDiv0';
        addTCSCNew('StepNumberDiv0', null);
    } else {
        addTCSCNew('StepsEndDiv' + incStep, document.getElementById('addStepButton' + incStep));
    }


    var newIncStep = document.getElementsByName('step_increment').length;
    var parentNodeID = '#StepFirstLineDiv' + newIncStep;
    $("#step_useStep_" + newIncStep).prop('checked', true);
    //create here the select components
    dragAndDropdrawSpecificUseStepCompoments($(parentNodeID).children("div:first"), newIncStep, test, testcase, step);

}

/*************Javascript objects that allow the storage of the steps*****************/
/**
 * 
 * @param {type} useTest
 * @param {type} useTestCase
 * @param {type} useStep
 * @param {type} step
 * @param {type} description
 * @param {type} type
 * @returns {ImportedStep}
 */
function ImportedStep(useTest, useTestCase, useStep, step, description, type) {
    this.useTest = useTest;
    this.useTestCase = useTestCase;
    this.useStep = useStep;
    this.step = step;
    this.description = description;
    this.type = type;
    this.actionList = [];
    this.actionsLoaded = false;
}
/**
 * 
 * @param {type} id
 * @param {type} sequence
 * @param {type} description
 * @param {type} action
 * @param {type} object
 * @param {type} property
 * @param {type} screenshotFilename
 * @returns {Action}
 */
function Action(id, sequence, description, action, object, property, screenshotFilename) {
    this.id = id;
    this.sequence = sequence;
    this.description = description;
    this.action = action,
            this.object = object;
    this.property = property;
    this.screenshotFilename = screenshotFilename;
    this.controlList = [];
    this.propertyButtonAction = null;
}
/**
 * 
 * @param {type} id
 * @param {type} sequence
 * @param {type} control
 * @param {type} description
 * @param {type} type
 * @param {type} controlProperty
 * @param {type} controlValue
 * @param {type} fatal
 * @param {type} screenshotFilename
 * @returns {Control}
 */
function Control(id, sequence, control, description, type, controlProperty, controlValue, fatal, screenshotFilename) {
    this.id = id;
    this.sequence = sequence;
    this.control = control;
    this.description = description;
    this.type = type;
    this.controlProperty = controlProperty;
    this.controlValue = controlValue;
    this.fatal = fatal;
    this.screenshotFilename = screenshotFilename;
}
/**
 * 
 * @param {type} type
 * @param {type} title
 * @param {type} usestep_test
 * @param {type} usestep_testcase
 * @returns {PropertyButtonAction}
 */
function PropertyButtonAction(type, title, usestep_test, usestep_testcase) {
    this.type = type;
    this.title = title;
    this.usestep_test = usestep_test;
    this.usestep_testcase = usestep_testcase;
}
/**
 * 
 * @param {type} actionList
 * @returns {undefined}
 */
ImportedStep.prototype.setActionList = function(actionList) {
    this.actionList = actionList;
};
/**
 * 
 * @param {type} action
 * @returns {undefined}
 */
ImportedStep.prototype.addAction = function(action) {
    this.actionList.push(action);
};
/**
 * 
 * @param {type} control
 * @returns {undefined}
 */
Action.prototype.addControl = function(control) {
    this.controlList.push(control);
};
/**
 * 
 * @param {type} propertyButtonAction
 * @returns {undefined}
 */

Action.prototype.setPropertyButtonAction = function(propertyButtonAction) {
    this.propertyButtonAction = propertyButtonAction;
};
