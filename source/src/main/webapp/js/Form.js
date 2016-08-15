//var prop_line = 1000;

function selectAll(selectBox, selectAll) {
    // have we been passed an ID
    if (typeof selectBox == "string") {
        selectBox = document.getElementsByName("Country");
    }

    // is the select box a multiple select box?
    if (selectBox.type == "select-multiple") {
        for (var i = 0; i < selectBox.options.length; i++) {
            selectBox.options[i].selected = selectAll;
        }
    } else {
        for (var i = 0; i < selectBox.length; i++) {
            selectBox[i].checked = selectAll;
        }
    }
}
function loadReporting(reportingFavorite) {
    document.location = reportingFavorite;
}

function popup(mylink) {
    window.open(mylink, 'popup',
            'width=500,height=400,scrollbars=yes,menubar=false,location=false');
}
function popupNT(mylink) {
    window.open(mylink, 'popup',
            'width=1200,height=600,scrollbars=yes,menubar=false,location=false');
}
function popupGraph(mylink) {
    window.open(mylink, 'popup',
            'width=500,height=270,scrollbars=yes,menubar=false,location=false');
}
function popupHisto(mylink) {
    window.open(mylink, 'popup',
            'width=1000,height=400,scrollbars=yes,menubar=false,location=false');
}
function importer(mylink) {
    window.open(mylink, 'popup',
            'width=800,height=400,scrollbars=yes,menubar=false,location=1');
}

function enableField(element) {
    document.getElementById(element).disabled = false;
}

function disableField(element) {
    document.getElementById(element).disabled = true;
}

function startUatCheck(arg) {
    /*
     * If not (Production or PreProd) URL, set html background color different
     */
    var prod_url = "/Cerberus/";
    var preprod_url = "/Cerberus-PreProd/";
    if ((location.toString().indexOf(prod_url, 0) == -1) && (location.toString().indexOf(preprod_url, 0) == -1)) {
        document.body.style.background = "#FFFFCC";
    }
}

function EnvTuning(arg) {
    /*
     * If not Production URL, set html background color different
     */
    if ((arg.toString().indexOf("PROD", 0) == -1) &&
            (arg.toString().indexOf("PREPROD", 0) == -1) &&
            (arg.toString().indexOf("prd", 0) == -1)) {
        document.body.style.background = "#FFFFCC";
    }
}


function menuColoring(arg) {
    /*
     * Put actual page button more visible
     */
    var menuCollection = document.getElementsByName('menu');

    var unicityMenu = 0;
    for (var cpt_menu = menuCollection.length - 1; cpt_menu >= 0; cpt_menu--) {

        if (location.toString().indexOf(menuCollection[cpt_menu]) != -1) {
            if (unicityMenu == 0) {
                //menuCollection[cpt_menu].style.background = "#00FF00";
                menuCollection[cpt_menu].style.color = "#00FF00";
                //menuCollection[cpt_menu].parentNode.parentNode.parentNode.firstChild.style.background = "#00FF00";
                menuCollection[cpt_menu].parentNode.parentNode.parentNode.firstChild.style.color = "#00FF00";
                unicityMenu = 1;
            }

        }

    }
    // to manage the coloring of the homepage when entering to the application
    if (location.toString().indexOf("jsp") == -1) {
        document.getElementById("Homepage").style.background = "#00FF00";
    }
}

function subSelect(elemName, inId) {
    var nameElms = [];
    var topItem = document.getElementById(inId);
    var childElms = document.getElementsByName(elemName);
    for (var i = 0; i < childElms.length; i++) {
        if (childElms[i].parentNode == topItem) {
            nameElms[nameElms.length] = childElms[i];
        }
    }
    return nameElms;
}
/*
 * Gestion des clefs, Mandatory, KEY
 */
function keyOnFocus(arg) {
    if (arg.value == 'Mandatory, KEY') {
        arg.value = '';
        arg.style.color = '#000000';
        arg.style.fontStyle = 'normal';
    }
}

function keyOnBlur(arg) {
    if (arg.value == '') {
        arg.value = 'Mandatory, KEY';
        arg.style.color = '#FF0000';
        arg.style.fontStyle = 'italic';
    }
}

function resetReportFilter() {
    document.getElementById('build').value = 'All';
    document.getElementById('revision').value = 'All';
    document.getElementById('env').value = 'All';
    document.getElementById('app').value = 'All';
    document.getElementById('ip').value = 'All';

    document.getElementById('ReportFilters').submit();
}

/*
 * Functions used for dynamic tables
 */
function addTestCaseRow(tableau, max_tc_desc, max_tc_behavior, max_tc_status,
        max_tc_group) {
    TR = document.createElement('tr');

    /* Delete */
    var form0 = document.createElement('input');
    form0.setAttribute('name', 'testcase_delete');
    form0.setAttribute('id', 'testcase_delete');
    form0.setAttribute('type', 'checkbox');
    var TD0 = document.createElement('td');
    TD0.appendChild(form0);
    TR.appendChild(TD0);

    /* Test Case */
    var form = document.createElement('input'); /* Create form */
    form.setAttribute('name', 'testcase_testcase');
    form.setAttribute('id', 'testcase_testcase');
    form
            .setAttribute('style',
                    'font-weight: bold;width: 50px;font-style: italic; color: #FF0000;');
    form.setAttribute('onfocus', 'keyOnFocus(this)');
    form.setAttribute('onblur', 'keyOnBlur(this)');
    form.setAttribute('value', 'Mandatory, KEY');
    var TD = document.createElement('td'); /* Create column */
    TD.appendChild(form); /* Add form to column */
    TR.appendChild(TD); /* Add column to row */

    /* Application */
    var form8 = document.createElement('select');
    if (document.getElementById("testcase_application_")) {
        form8.setAttribute('name', 'testcase_application');
        form8.setAttribute('id', 'testcase_application');
        form8
                .setAttribute('style',
                        'font-weight: bold;width: 80px;font-style: italic; color: #FF0000;');
        form8.innerHTML = (form8.innerHTML + document
                .getElementById('testcase_application_').innerHTML);
    }
    var TD8 = document.createElement('td');
    TD8.appendChild(form8);
    TR.appendChild(TD8);

    /* Project */
    var form9 = document.createElement('input');
    form9.setAttribute('name', 'testcase_project');
    form9.setAttribute('id', 'testcase_project');
    form9
            .setAttribute('style',
                    'font-weight: bold;width: 25px;font-style: italic; color: #FF0000;');
    var TD9 = document.createElement('td');
    TD9.appendChild(form9);
    TR.appendChild(TD9);

    /* Description */
    var form1 = document.createElement('textarea');
    form1.setAttribute('name', 'testcase_description');
    form1.setAttribute('id', 'testcase_description');
    form1.setAttribute('style', 'width: 165px');
    form1.setAttribute('maxlength', max_tc_desc);
    var TD1 = document.createElement('td');
    TD1.appendChild(form1);
    TR.appendChild(TD1);

    /* Value expected */
    var form2 = document.createElement('textarea');
    form2.setAttribute('name', 'testcase_valueexpec');
    form2.setAttribute('id', 'testcase_valueexpec');
    form2.setAttribute('style', 'width: 425px');
    form2.setAttribute('maxlength', max_tc_behavior);
    var TD2 = document.createElement('td');
    TD2.appendChild(form2);
    TR.appendChild(TD2);

    /* Read Only */
    var form3 = document.createElement('select');
    if (document.getElementById("testcase_readonly_")) {
        form3.setAttribute('name', 'testcase_readonly');
        form3.setAttribute('id', 'testcase_readonly');
        form3.setAttribute('style', 'width: 40px');
        form3.innerHTML = (form3.innerHTML + document
                .getElementById('testcase_readonly_').innerHTML);
    }
    var TD3 = document.createElement('td');
    TD3.appendChild(form3);
    TR.appendChild(TD3);

    /* Countries */
    var form5 = document.createElement('input');
    form5.setAttribute('name', 'testcase_countries');
    form5.setAttribute('id', 'testcase_countries');
    form5.setAttribute('style', 'width: 130px');
    var form5b = document.createElement('input');
    form5b.setAttribute('name', 'testcase_countries_hidden');
    form5b.setAttribute('id', 'testcase_countries_hidden');
    form5b.setAttribute('type', 'hidden');
    var TD5 = document.createElement('td');
    TD5.appendChild(form5);
    TD5.appendChild(form5b);
    TR.appendChild(TD5);

    /* Priority */
    var form6 = document.createElement('select');
    if (document.getElementById("testcase_priority_")) {
        form6.setAttribute('name', 'testcase_priority');
        form6.setAttribute('id', 'testcase_priority');
        form6.setAttribute('style', 'width: 40px');
        form6.innerHTML = (form6.innerHTML + document
                .getElementById('testcase_priority_').innerHTML);
    }
    var TD6 = document.createElement('td');
    TD6.appendChild(form6);
    TR.appendChild(TD6);

    /* Status */
    var form7 = document.createElement('select');
    if (document.getElementById("testcase_status_")) {
        form7.setAttribute('name', 'testcase_status');
        form7.setAttribute('id', 'testcase_status');
        form7.setAttribute('style', 'width: 70px');
        form7.innerHTML = (form7.innerHTML + document
                .getElementById('testcase_status_').innerHTML);
        form7.setAttribute('maxlength', max_tc_status);
    }
    var TD7 = document.createElement('td');
    TD7.appendChild(form7);
    TR.appendChild(TD7);

    /* Group */
    var form19 = document.createElement('select');
    if (document.getElementById("testcase_group_")) {
        form19.setAttribute('name', 'testcase_group');
        form19.setAttribute('id', 'testcase_group');
        form19.setAttribute('style', 'width: 50px');
        form19.setAttribute('maxlength', max_tc_group);
        form19.innerHTML = (form19.innerHTML + document
                .getElementById('testcase_group_').innerHTML);
    }
    var TD19 = document.createElement('td');
    TD19.appendChild(form19);
    TR.appendChild(TD19);

    /* tcActive */
    var form10 = document.createElement('select');
    if (document.getElementById("testcase_tcActive_")) {
        form10.setAttribute('name', 'testcase_tcActive');
        form10.setAttribute('id', 'testcase_tcActive');
        form10.setAttribute('style', 'width: 30px');
        form10.setAttribute('maxlength', max_tc_group);
        form10.innerHTML = (form10.innerHTML + document
                .getElementById('testcase_tcActive_').innerHTML);
    }
    var TD10 = document.createElement('td');
    TD10.appendChild(form10);
    TR.appendChild(TD10);

    document.getElementById(tableau).appendChild(TR);
}

function EnableAddTestButton(id1, id2, newValue, initValue) {
    if (newValue != initValue) {
        document.getElementById(id1).disabled = false;
        document.getElementById(id2).disabled = true;
    } else {
        document.getElementById(id1).disabled = true;
        document.getElementById(id2).disabled = false;
    }
}

function redirectionTestCase(target, test) { // Redirection testcase, un form
    // et deux submits, si save
    // changes 0, si add test 1
    if (target == 0) {
        document.DeleteTest.action = "UpdateTest";
        document.DeleteTest.submit();
    }
//	if (target == 1) {
//		document.updateTest.action = "AddTest";
//	}
    if (target == 2) {
        var oRows = document.getElementById('testcasetable')
                .getElementsByTagName('tr');
        var iRowCount = oRows.length;

        if (iRowCount == 1) {
            if (confirm('Do you really want to delete this Test ?')) {
                document.DeleteTest.action = "DeleteTest";
                document.DeleteTest.submit();
            }
        } else {
            alert('You have to delete all TestCases before delete the Test');
        }
    }
}

function addTestCaseProperties(tableau, max_tcp_country,
        max_tcp_property, max_tcp_value, max_tcp_length, max_tcp_rowlimit,
        row_number, size, size2) {

    TR = document.createElement('tr');

    /* Delete box */
    // prop_line++;
    var form1 = document.createElement('input');
    form1.setAttribute('type', 'checkbox');
    form1.setAttribute('name', 'properties_delete');
    form1.setAttribute('style', '');
    form1.setAttribute('value', row_number + 1);
    var form11 = document.createElement('input');
    form11.setAttribute('name', 'property_hidden');
    form11.setAttribute('type', 'hidden');
    form11.setAttribute('value', row_number + 1);
    var TD1 = document.createElement('td');
    TD1.setAttribute('style', 'background-color:white; text-align: center');
    TD1.appendChild(form1);
    TD1.appendChild(form11);
    TR.appendChild(TD1);

    /* Property */
    var form2 = document.createElement('input');
    form2.setAttribute('name', 'properties_property');
    form2.setAttribute('class', 'wob properties_id_' + eval(row_number + 1));
    form2.setAttribute('size', '130%');
    form2
            .setAttribute('style',
                    'width: 130px; font-weight: bold;font-style: italic; color: #FF0000;');
    form2.setAttribute('placeholder', 'Feed Property name');
    form2.setAttribute('maxlength', max_tcp_property);
    var TD2 = document.createElement('td');
    TD2.setAttribute('style', 'background-color:white');
    TD2.appendChild(form2);
    TR.appendChild(TD2);

    /*
     * Country
     */
    /* Parse values from hidden containing all countries */

    var TD3 = document.createElement('td'); /* Create column */
    if (document.getElementById("toto")) {
        TD3.setAttribute('style', 'font-size : x-small ; width: ' + size
                + 'px;');
        TD3.setAttribute('style', 'background-color: white;');

        var jToto = $('#toto');
        jToto.find('input[name="properties_country"]').addClass('properties_id_' + eval(row_number + 1));
        TD3.innerHTML = (TD3.innerHTML + jToto.html());

        // if (document.getElementById("checkbox-AT")) {
        // tata=TD3.getElementById("checkbox-AT")
        // tata.setAttribute('value', prop_line+' - AT');
        // tata.setAttribute('name', property_country);
        // }

    }
    TR.appendChild(TD3);

    /* Type */
    var form4 = document.createElement('select');
    if (document.getElementById("new_properties_type_new_properties_value")) {
        form4.setAttribute('name', 'properties_type');
        form4.setAttribute('id', 'typenew_properties_value');
        form4.setAttribute('style', 'width: 120px');
        form4.setAttribute('onchange', 'activateDatabaseBox(this.value, \'properties_dtb_typeID\' , \'properties_dtb_type_ID\'); activateValue2(this.value, \'tdValue2_new\', \'new_properties_value\',\'new_properties_value2\',\'' + size2 + '\')');
        form4.setAttribute('class', 'wob');
        form4.innerHTML = (form4.innerHTML + document
                .getElementById('new_properties_type_new_properties_value').innerHTML);
//        var form44 = document.createElement('option');
//        form44.setAttribute('Value', 'executeSql');
//        form44.setAttribute('selected', 'selected');
//        form4.appendChild(form44);
    }
    var TD4 = document.createElement('td');
    TD4.setAttribute('style', 'background-color:white');
    TD4.appendChild(form4);
    TR.appendChild(TD4);

    /* Database */
    var form41 = document.createElement('select');
    if (document.getElementById("properties_dtb_")) {
        form41.setAttribute('name', 'properties_dtb');
        form41.setAttribute('style', 'width: 40px');
        form41.setAttribute('style', 'display: inline');
        form41.setAttribute('class', 'wob');
        form41.innerHTML = (form41.innerHTML + document
                .getElementById('properties_dtb_').innerHTML);
        form41.setAttribute('id', 'properties_dtb_typeID');
    }

    var form42 = document.createElement('input');
    form42.setAttribute('style', 'display:none; width: 39px; background-color: white; text-align: center;');
    form42.setAttribute('id', 'properties_dtb_type_ID');
    form42.setAttribute('class', 'wob');
    form42.setAttribute('value', '---');
    var TD41 = document.createElement('td');
    TD41.setAttribute('style', 'background-color:white');
    TD41.appendChild(form41);
    TD41.appendChild(form42);
    TR.appendChild(TD41);

    /* Value */
    var form5 = document.createElement('textarea');
    form5.setAttribute('name', 'properties_value');
    form5.setAttribute('class', 'wob');
    form5.setAttribute('id', 'new_properties_value');
    form5.setAttribute('style', 'width: ' + size2 + 'px');
    form5.setAttribute('maxlength', max_tcp_value);
    var TD5 = document.createElement('td');
    var TB51 = document.createElement('table');
    var TR51 = document.createElement('tr');
    var TD51 = document.createElement('td');
    TD51.setAttribute('style', 'background-color:white');
    TD51.appendChild(form5);
    TD51.setAttribute('class', 'wob');
    TR51.appendChild(TD51);

    var form54 = document.createElement('textarea');
    form54.setAttribute('name', 'properties_value2');
    form54.setAttribute('placeholder', 'Attribute');
    form54.setAttribute('class', 'wob');
    form54.setAttribute('id', 'new_properties_value2');
    form54.setAttribute('maxlength', max_tcp_value);
    var TD52 = document.createElement('td');
    TD52.setAttribute('style', 'background-color:white; display:none');
    TD52.setAttribute('id', 'tdValue2_new');
    TD52.appendChild(form54);
    TD52.setAttribute('class', 'wob');
    TR51.appendChild(TD52);

    var form52 = document.createElement('input');
    form52.setAttribute('style', 'display:inline; height:20px; width:20px; background-color: white; color:blue; font-weight:bolder');
    form52.setAttribute('title', 'Open SQL Library');
    form52.setAttribute('class', 'smallbutton');
    form52.setAttribute('type', 'button');
    form52.setAttribute('value', 'L');
    form52.setAttribute('onclick', 'openSqlLibraryPopin(\'new_properties_value\')');

    var TD52 = document.createElement('td');
    TD52.setAttribute('style', 'background-color:white');
    TD52.setAttribute('class', 'wob');
    TD52.appendChild(form52);
    TR51.appendChild(TD52);

    TB51.appendChild(TR51);
    TD5.appendChild(TB51);
    TD5.setAttribute('style', 'background-color: white');
    TR.appendChild(TD5);



    /* Length */
    var form6 = document.createElement('input');
    form6.setAttribute('name', 'properties_length');
    form6.setAttribute('value', 0);
    form6.setAttribute('class', 'wob');
    form6.setAttribute('style', 'width: 40px');
    form6.setAttribute('maxlength', max_tcp_length);
    var TD6 = document.createElement('td');
    TD6.setAttribute('style', 'background-color:white');
    TD6.appendChild(form6);
    TR.appendChild(TD6);

    /* Row Limit */
    var form7 = document.createElement('input');
    form7.setAttribute('name', 'properties_rowlimit');
    form7.setAttribute('value', 0);
    form7.setAttribute('style', 'width: 40px');
    form7.setAttribute('class', 'wob');
    form7.setAttribute('maxlength', max_tcp_rowlimit);
    var TD7 = document.createElement('td');
    TD7.setAttribute('style', 'background-color:white');
    TD7.appendChild(form7);
    TR.appendChild(TD7);

    /* Nature */
    var form8 = document.createElement('select');
    if (document.getElementById("properties_nature_")) {
        form8.setAttribute('name', 'properties_nature');
        form8.setAttribute('class', 'wob');
        form8.setAttribute('style', 'width: 80px');
        form8.innerHTML = (form8.innerHTML + document
                .getElementById('properties_nature_').innerHTML);
    }
    var TD8 = document.createElement('td');
    TD8.setAttribute('style', 'background-color:white');
    TD8.appendChild(form8);
    TR.appendChild(TD8);

    document.getElementById(tableau).appendChild(TR);

}

function addTestCaseAction(table, id, max_tcsa_seq, max_tcsa_action, max_tcsa_obj,
        max_tcsa_pro, max_tcsa_desc) {

    TR = document.createElement('tr');

    /* Delete box */
    var form1 = document.createElement('input'); /* Create form */
    form1.setAttribute('type', 'checkbox');
    form1.setAttribute('style', 'height : 20px');
    form1.setAttribute('class', 'wob');
    form1.setAttribute('name', 'actions_delete');
    var TD1 = document.createElement('td'); /* Create column */
    TD1.setAttribute('style', 'text-align:center');
    TD1.setAttribute('style', 'background-color:white; text-align: center');
    TD1.appendChild(form1); /* Add form to column */

    var form = document.createElement('input');
    form.setAttribute('type', 'hidden');
    form.setAttribute('name', 'stepnumber_hidden');
    form.setAttribute('value', id);

    TD1.appendChild(form);
    TR.appendChild(TD1);

    /* Sequence */

    var value = getMaxValueForParentElementIdAndElementName(table, 'actions_sequence');

    if (value && parseInt(value) > 0) {
        value = parseInt(value) + 10;
    } else {
        value = 10;
    }

    var form2 = document.createElement('input');
    form2.setAttribute('name', 'actions_sequence');
    form2.setAttribute('size', '6%');
    form2
            .setAttribute('style',
                    'width: 60px ;font-weight: bold;font-style: italic; color: #FF0000;');
    form2.setAttribute('onfocus', 'keyOnFocus(this)');
    form2.setAttribute('class', 'wob');
    form2.setAttribute('onblur', 'keyOnBlur(this)');
    form2.setAttribute('value', value);
    form2.setAttribute('maxlength', max_tcsa_seq);
    var TD2 = document.createElement('td');
    TD2.setAttribute('style', 'background-color:white; text-align: center');
    TD2.appendChild(form2);
    TR.appendChild(TD2);

    /* Action */
    var form3 = document.createElement('select');
    if (document.getElementById("actions_action_")) {
        form3.setAttribute('name', 'actions_action');
        form3.setAttribute('style', 'width: 100%');
        form3.setAttribute('class', 'wob');
        form3.innerHTML = (form3.innerHTML + document
                .getElementById('actions_action_').innerHTML);
    }
    var TD3 = document.createElement('td');
    TD3.setAttribute('style', 'background-color:white; text-align: center');
    TD3.appendChild(form3);
    TR.appendChild(TD3);

    /* Object */
    var form4 = document.createElement('input');
    form4.setAttribute('name', 'actions_object');
    form4.setAttribute('size', '100%');
    form4.setAttribute('class', 'wob');
    form4.setAttribute('style', 'width: 350px');
    form4.setAttribute('maxlength', max_tcsa_obj);
    var TD4 = document.createElement('td');
    TD4.setAttribute('style', 'background-color:white; text-align: center');
    if (displayOnlyFunctional) {
        TD4.setAttribute('class', 'technical_part only_functional');
    } else {
        TD4.setAttribute('class', 'technical_part');
    }
    TD4.appendChild(form4);
    TR.appendChild(TD4);

    /* Property */
    var form5 = document.createElement('input');
    form5.setAttribute('name', 'actions_property');
    form5.setAttribute('size', '100%');
    form5.setAttribute('class', 'wob');
    form5.setAttribute('style', 'width: 210px');
    form5.setAttribute('maxlength', max_tcsa_pro);
    var TD5 = document.createElement('td');
    TD5.setAttribute('style', 'background-color:white; text-align: center');
    if (displayOnlyFunctional) {
        TD5.setAttribute('class', 'technical_part only_functional');
    } else {
        TD5.setAttribute('class', 'technical_part');
    }
    TD5.appendChild(form5);
    TR.appendChild(TD5);

    /* Description */
    var form6 = document.createElement('input');
    form6.setAttribute('name', 'actions_description');
    form6.setAttribute('size', '100%');
    if (displayOnlyFunctional) {
        form6.setAttribute('class', 'wob functional_description only_functional_description_size');
    } else {
        form6.setAttribute('class', 'wob functional_description');
    }
    form6.setAttribute('style', 'width: 100%');
    form6.setAttribute('maxlength', max_tcsa_desc);
    var TD6 = document.createElement('td');
    TD6.setAttribute('style', 'background-color:white;');
    if (displayOnlyFunctional) {
        TD6.setAttribute('class', 'functional_description only_functional_description_size');
    } else {
        TD6.setAttribute('class', 'functional_description');
    }
    TD6.appendChild(form6);
    TR.appendChild(TD6);

    document.getElementById(table).appendChild(TR);
}

var numberOfCall = 0;
function addStep(div, max_tcs_desc) {

    var table = document.createElement('div');
    table.setAttribute('id', 'table');

    TR = document.createElement('tr');
    table.appendChild(TR);

    TR.appendChild(document.createTextNode("  Step ID  :"));
    var input1 = document.createElement('input');
    input1.setAttribute('style',
            'font-weight: bold;font-style: italic; width:20px');
    input1.setAttribute('name', 'step_number_add');
    input1.setAttribute('class', 'wob');
    input1.setAttribute('title', 'Step ID');
    input1.setAttribute('maxlength', 10);
    input1.setAttribute('onfocus', 'keyOnFocus(this)');
    input1.setAttribute('onblur', 'keyOnBlur(this)');

    var value = getMaxValueForParentElementIdAndElementName(null, 'testcasestep_hidden');
    if (value && parseInt(value) > 0) {
        value = parseInt(value) + 10;
    } else {
        value = 10;
    }

    input1.setAttribute('value', value);
    var TD1 = document.createElement('td'); /* Create column */
    TD1.setAttribute('style', 'background-color:#e1e7f3; text-align: center; valign:center');
    TD1.setAttribute('class', 'wob');
    TD1.appendChild(input1); /* Add form to column */
    TR.appendChild(TD1);

    TR.appendChild(document.createTextNode("  Step Description  :"));
    var input2 = document.createElement('input');
    input2.setAttribute('size', '100%');
    input2.setAttribute('class', 'wob');
    input2.setAttribute('style', 'width : 500px');
    input2.setAttribute('name', 'step_description_add');
    input2.setAttribute('maxlength', max_tcs_desc);
    var TD2 = document.createElement('td'); /* Create column */
    TD2.setAttribute('style', 'background-color:#e1e7f3; text-align: center');
    TD2.setAttribute('class', 'wob');
    TD2.appendChild(input2); /* Add form to column */
    TR.appendChild(TD2);

    var TD7 = document.createElement('td');
    TD7.setAttribute('class', 'wob');
    TR.appendChild(TD7);

    TR2 = document.createElement('tr');

    var TD13 = document.createElement('td');
    TD13.setAttribute('style', 'width:10px');
    TD13.setAttribute('class', 'wob');
    TR2.appendChild(TD13);

    var input4 = document.createElement('input');
    input4.setAttribute('type', 'submit');
    input4.setAttribute('value', 'Save Changes');

    table.appendChild(input4);


    table.appendChild(document.createElement('br'));
    table.appendChild(document.createElement('br'));

    document.getElementById(div).appendChild(table);

    numberOfCall++;
}

function addTestCaseControl(table, step_id, max_tcc_step, max_tcc_sequence,
        max_tcc_control, max_tcc_type, max_tcc_value, max_tcc_property,
        max_tcc_description, tab_type) {
    TR = document.createElement('tr');

    /* Delete box */
    var form1 = document.createElement('input'); /* Create form */
    form1.setAttribute('type', 'checkbox');
    form1.setAttribute('style', 'width:30px');
    form1.setAttribute('class', 'wob');
    form1.setAttribute('name', 'controls_delete');


    /* Step */
    var form2 = document.createElement('input');
    form2.setAttribute('name', 'controls_step');
    form2.setAttribute('type', 'hidden');
    form2.setAttribute('value', step_id);

    var TD1 = document.createElement('td'); /* Create column */
    TD1.setAttribute('style', 'background-color:white; text-align: center');
    TD1.appendChild(form1); /* Add form to column */
    TD1.appendChild(form2); /* Add form to column */
    TR.appendChild(TD1);

    /* Sequence */
    var value_actions = getMaxValueForParentElementIdAndElementName('Action' + step_id, 'actions_sequence');
    var form3 = document.createElement('input');
    form3.setAttribute('name', 'controls_sequence');
    form3
            .setAttribute('style',
                    'width: 60px; font-weight: bold;font-style: italic; color: #FF0000;');
    form3.setAttribute('maxlength', max_tcc_sequence);
    form3.setAttribute('onfocus', 'keyOnFocus(this)');
    form3.setAttribute('onblur', 'keyOnBlur(this)');
    form3.setAttribute('class', 'wob');
    form3.setAttribute('value', value_actions);
    var TD3 = document.createElement('td');
    TD3.setAttribute('style', 'background-color:white');
    TD3.appendChild(form3);
    TR.appendChild(TD3);

    /* Control */
    var value_control = getMaxValueForParentElementIdAndElementName('control_table' + step_id, 'controls_control');
    if (value_control && parseInt(value_control) > 0) {
        value_control = parseInt(value_control) + 10;
    } else {
        value_control = 10;
    }


    var form4 = document.createElement('input');
    form4.setAttribute('name', 'controls_control');
    form4
            .setAttribute('style',
                    'width: 60px; font-weight: bold;font-style: italic; color: #FF0000;');
    form4.setAttribute('maxlength', max_tcc_control);
    form4.setAttribute('onfocus', 'keyOnFocus(this)');
    form4.setAttribute('class', 'wob');
    form4.setAttribute('onblur', 'keyOnBlur(this)');
    form4.setAttribute('value', value_control);
    var TD4 = document.createElement('td');
    TD4.setAttribute('style', 'background-color:white');
    TD4.appendChild(form4);
    TR.appendChild(TD4);

    /* Type */
    var form5 = document.createElement('select');
    if (document.getElementById("controls_type_")) {
        form5.setAttribute('name', 'controls_type');
        form5.setAttribute('class', 'wob');
        form5.setAttribute('style', 'width: 100%');
        form5.innerHTML = (form5.innerHTML + document
                .getElementById('controls_type_').innerHTML);
    }
    var TD5 = document.createElement('td');
    TD5.setAttribute('style', 'background-color:white');
    TD5.appendChild(form5);
    TR.appendChild(TD5);

    /* Property */
    var form7 = document.createElement('input');
    form7.setAttribute('name', 'controls_controlproperty');
    form7.setAttribute('style', 'width: 260px');
    form7.setAttribute('class', 'wob');
    form7.setAttribute('maxlength', max_tcc_property);
    var TD7 = document.createElement('td');
    TD7.setAttribute('style', 'background-color:white');
    if (displayOnlyFunctional) {
        TD7.setAttribute('class', 'technical_part only_functional');
    } else {
        TD7.setAttribute('class', 'technical_part');
    }
    TD7.appendChild(form7);
    TR.appendChild(TD7);

    /* Value */
    var form6 = document.createElement('input');
    form6.setAttribute('name', 'controls_controlvalue');
    form6.setAttribute('class', 'wob');
    form6.setAttribute('style', 'width: 180px');
    form6.setAttribute('maxlength', max_tcc_value);
    var TD6 = document.createElement('td');
    TD6.setAttribute('style', 'background-color:white');
    if (displayOnlyFunctional) {
        TD6.setAttribute('class', 'technical_part only_functional');
    } else {
        TD6.setAttribute('class', 'technical_part');
    }
    TD6.appendChild(form6);
    TR.appendChild(TD6);

    /* Fatal */
    var form7 = document.createElement('select');
    if (document.getElementById("controls_fatal_")) {
        form7.setAttribute('name', 'controls_fatal');
        form7.setAttribute('class', 'wob');
        form7.setAttribute('style', 'width: 40px');
        form7.innerHTML = (form7.innerHTML + document
                .getElementById('controls_fatal_').innerHTML);
    }
    var TD7 = document.createElement('td');
    TD7.setAttribute('style', 'background-color:white');
    if (displayOnlyFunctional) {
        TD7.setAttribute('class', 'technical_part only_functional');
    } else {
        TD7.setAttribute('class', 'technical_part');
    }

    TD7.appendChild(form7);
    TR.appendChild(TD7);

    /* Description */
    var form8 = document.createElement('input');
    form8.setAttribute('name', 'controls_controldescription');
    if (displayOnlyFunctional) {
        form8.setAttribute('class', 'wob functional_description_control only_functional_description_control_size');
    } else {
        form8.setAttribute('class', 'wob functional_description_control');
    }
    form8.setAttribute('style', 'width: 100%');
    form8.setAttribute('maxlength', max_tcc_description);
    var TD8 = document.createElement('td');
    TD8.setAttribute('style', 'background-color:white');
    if (displayOnlyFunctional) {
        TD8.setAttribute('class', 'functional_description_control only_functional_description_control_size');
    } else {
        TD8.setAttribute('class', 'functional_description_control');
    }
    TD8.appendChild(form8);
    TR.appendChild(TD8);

    document.getElementById(table).appendChild(TR);
}

function switchDivVisibleInvisible(visible, invisible) {
    document.getElementById(visible).style.display = "inline-block";
    document.getElementById(invisible).style.display = "none";
}
function switchTableVisibleInvisible(visible, invisible) {
    document.getElementById(visible).style.display = "inline";
    document.getElementById(invisible).style.display = "none";
}
function enableElement(element) {
    document.getElementById(element).disabled = false;
}
function setVisible() {
    document.getElementById('generalparameter').style.display = "table";
    document.getElementById('parametergeneral').style.display = "none";

}
function setInvisible() {
    document.getElementById('generalparameter').style.display = "none";
    document.getElementById('parametergeneral').style.display = "table";
}

function setVisibleP() {
    document.getElementById('propertytable').style.display = "table";
    document.getElementById('tableproperty').style.display = "none";
}
function setInvisibleP() {
    document.getElementById('propertytable').style.display = "none";
    document.getElementById('tableproperty').style.display = "table";
}
function setVisibleRep() {
    document.getElementById('reportingExec').style.display = "table";
    document.getElementById('statusReporting').style.display = "none";
    document.getElementById('groupReporting').style.display = "none";
    document.getElementById('execReporting').style.display = "none";
    document.getElementById('ShowS').style.display = "table";
    document.getElementById('ShowD').style.display = "none";
}
function setInvisibleRep() {
    document.getElementById('reportingExec').style.display = "none";
    document.getElementById('statusReporting').style.display = "table";
    document.getElementById('groupReporting').style.display = "table";
    document.getElementById('execReporting').style.display = "table";
    document.getElementById('ShowS').style.display = "none";
    document.getElementById('ShowD').style.display = "table";
}

function setNewtestVisible() {
    document.getElementById('reportingExec').style.display = "none";
    document.getElementById('reportingExec').style.display = "none";
}

function setTestVisible() {
    document.getElementById('filters').style.display = "table";
    document.getElementById('generalparameters').style.display = "table";
}

function setVisibleContent1() {
    document.getElementById('buildContent1').style.display = "table";
    document.getElementById('button11').style.display = "inline";
    document.getElementById('button21').style.display = "none";

}

function setVisibleContent2() {
    document.getElementById('buildContent2').style.display = "table";
    document.getElementById('button12').style.display = "inline";
    document.getElementById('button22').style.display = "none";
}

function setVisibleContent3() {
    document.getElementById('buildContent3').style.display = "table";
    document.getElementById('button13').style.display = "inline";
    document.getElementById('button23').style.display = "none";
}

function setVisibleContent4() {
    document.getElementById('buildContent4').style.display = "table";
    document.getElementById('button14').style.display = "inline";
    document.getElementById('button24').style.display = "none";
}

function setVisibleContent5() {
    document.getElementById('buildContent5').style.display = "table";
    document.getElementById('button15').style.display = "inline";
    document.getElementById('button25').style.display = "none";
}
function setVisibleContent6() {
    document.getElementById('buildContent6').style.display = "table";
    document.getElementById('button16').style.display = "inline";
    document.getElementById('button26').style.display = "none";
}

function setVisibleContent7() {
    document.getElementById('buildContent7').style.display = "table";
    document.getElementById('button17').style.display = "inline";
    document.getElementById('button27').style.display = "none";
}

function setVisibleContent8() {
    document.getElementById('buildContent8').style.display = "table";
    document.getElementById('button18').style.display = "inline";
    document.getElementById('button28').style.display = "none";
}

function setVisibleContent9() {
    document.getElementById('buildContent9').style.display = "table";
    document.getElementById('button19').style.display = "inline";
    document.getElementById('button29').style.display = "none";
}
function setVisibleContent10() {
    document.getElementById('buildContent10').style.display = "table";
    document.getElementById('button110').style.display = "inline";
    document.getElementById('button210').style.display = "none";
}

function setInvisibleContent1() {
    document.getElementById('buildContent1').style.display = "none";
    document.getElementById('button11').style.display = "none";
    document.getElementById('button21').style.display = "inline";

}

function setInvisibleContent2() {
    document.getElementById('buildContent2').style.display = "none";
    document.getElementById('button12').style.display = "none";
    document.getElementById('button22').style.display = "inline";
}

function setInvisibleContent3() {
    document.getElementById('buildContent3').style.display = "none";
    document.getElementById('button13').style.display = "none";
    document.getElementById('button23').style.display = "inline";
}

function setInvisibleContent4() {
    document.getElementById('buildContent4').style.display = "none";
    document.getElementById('button14').style.display = "none";
    document.getElementById('button24').style.display = "inline";
}

function setInvisibleContent5() {
    document.getElementById('buildContent5').style.display = "none";
    document.getElementById('button15').style.display = "none";
    document.getElementById('button25').style.display = "inline";
}
function setInvisibleContent6() {
    document.getElementById('buildContent6').style.display = "none";
    document.getElementById('button16').style.display = "none";
    document.getElementById('button26').style.display = "inline";
}

function setInvisibleContent7() {
    document.getElementById('buildContent7').style.display = "none";
    document.getElementById('button17').style.display = "none";
    document.getElementById('button27').style.display = "inline";
}

function setInvisibleContent8() {
    document.getElementById('buildContent8').style.display = "none";
    document.getElementById('button18').style.display = "none";
    document.getElementById('button28').style.display = "inline";
}

function setInvisibleContent9() {
    document.getElementById('buildContent9').style.display = "none";
    document.getElementById('button19').style.display = "none";
    document.getElementById('button29').style.display = "inline";
}
function setInvisibleContent10() {
    document.getElementById('buildContent10').style.display = "none";
    document.getElementById('button110').style.display = "none";
    document.getElementById('button210').style.display = "inline";
}


function activateDatabaseBox(value, fieldOneId, fieldTwoId) {
    if (value === "executeSql" || value === "executeSqlFromLib" || value === "executeSoapFromLib") {
        $("#" + fieldOneId).empty().append($('#' + fieldTwoId).html());
    } else
    {
        $("#" + fieldOneId).empty().append($('<option></option>').text("---").val("---"));
    }
}

function activateValue2(value, fieldOneId, fieldTwoId, fieldThreeId, size2) {
    if (value === "getAttributeFromHtml" || value === "getFromXml" || value === "getFromCookie" ||
            value === "getFromJson" || value === "getDifferencesFromXml") {
        var size3 = 1 * size2 / 3;
        var size4 = (2 * size2 / 3) - 5;
        document.getElementById(fieldOneId).style.display = "inline";
        document.getElementById(fieldThreeId).style.width = size3 + "px";
        document.getElementById(fieldTwoId).style.width = size4 + "px";
    } else
    {
        document.getElementById(fieldOneId).style.display = "none";
        document.getElementById(fieldTwoId).style.width = size2 + "px";
    }
}
//End of function for SQL Library
function setGraphInvisible(chart, button, button2) {
    document.getElementById(chart).style.display = "none";
    document.getElementById(button).style.display = "none";
    document.getElementById(button2).style.display = "inline";
}
function setGraphVisible(chart, button, button2) {
    document.getElementById(chart).style.display = "block";
    document.getElementById(button).style.display = "inline";
    document.getElementById(button2).style.display = "none";
}

//Functions for Run Page
function setEnvManual() {
    document.getElementById("myloginrelativeurl").disabled = false;
    document.getElementById("myhost").disabled = false;
    document.getElementById("mycontextroot").disabled = false;
    document.getElementById("myenvdata").disabled = false;
    document.getElementById("environment").disabled = true;
}
function setEnvAutomatic() {
    document.getElementById("myloginrelativeurl").disabled = true;
    document.getElementById("myhost").disabled = true;
    document.getElementById("mycontextroot").disabled = true;
    document.getElementById("myenvdata").disabled = true;
    document.getElementById("environment").disabled = false;
}
function setRobotManual() {
    document.getElementById("robot").disabled = true;
    document.getElementById("ss_ip").disabled = false;
    document.getElementById("ss_p").disabled = false;
    document.getElementById("platform").disabled = false;
    document.getElementById("browser").disabled = false;
    document.getElementById("version").disabled = false;
    document.getElementById("os").disabled = false;
}
function setRobotAutomatic() {
    document.getElementById("robot").disabled = false;
    document.getElementById("ss_ip").disabled = true;
    document.getElementById("ss_p").disabled = true;
    document.getElementById("platform").disabled = true;
    document.getElementById("browser").disabled = true;
    document.getElementById("version").disabled = true;
    document.getElementById("os").disabled = true;
}

/*
 * Functions used for dynamic tables
 */
function addBuildContent(tableau) {
    TR = document.createElement('tr');

    /* Delete */
    var form0 = document.createElement('input');
    form0.setAttribute('name', 'ubcDelete');
    form0.setAttribute('type', 'checkbox');
    form0.setAttribute('style', 'width:10px');
    var TD0 = document.createElement('td');
    TD0.setAttribute('style', 'border-color:gainsboro');
    TD0.setAttribute('border', '1px');
    TD0.appendChild(form0);
    TD0.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD0);


    /* Build */
    var form1 = document.createElement('select');
    if (document.getElementById("buildcontent_build_")) {
        form1.setAttribute('name', 'ubcBuild');
        form1.setAttribute('style', 'width:60px; font-size:x-small');
        form1.setAttribute('class', 'wob');
        form1.innerHTML = (form1.innerHTML + document.getElementById('buildcontent_build_').innerHTML);
    }
    var TD1 = document.createElement('td');
    TD1.appendChild(form1);
    TD1.setAttribute('border-left', '1px');
    TD1.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD1);

    /* Revision */
    var form2 = document.createElement('select');
    if (document.getElementById("buildcontent_revision_")) {
        form2.setAttribute('name', 'ubcRevision');
        form2.setAttribute('style', 'width:40px; font-size:x-small');
        form2.setAttribute('class', 'wob');
        form2.innerHTML = (form2.innerHTML + document.getElementById('buildcontent_revision_').innerHTML);
    }
    var TD2 = document.createElement('td');
    TD2.appendChild(form2);
    TD2.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD2);

    /* Application */
    var form3 = document.createElement('select');
    if (document.getElementById("buildcontent_application_")) {
        form3.setAttribute('name', 'ubcApplication');
        form3.setAttribute('class', 'wob');
        form3.setAttribute('style', 'font-weight: bold;width: 170px;font-style: italic; font-size:x-small');
        form3.innerHTML = (form3.innerHTML + document.getElementById('buildcontent_application_').innerHTML);
    }
    var form31 = document.createElement('input');
    form31.setAttribute('style', 'display:none');
    form31.setAttribute('name', 'ubcReleaseID');
    var TD3 = document.createElement('td');
    TD3.appendChild(form3);
    TD3.appendChild(form31);
    TD3.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD3);

    /* Release */
    var form4 = document.createElement('input');
    form4.setAttribute('style', 'width:100px; font-size:x-small');
    form4.setAttribute('class', 'wob');
    form4.setAttribute('name', 'ubcRelease');
    var TD4 = document.createElement('td');
    TD4.appendChild(form4);
    TD4.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD4);

    /* Project */
    var form8 = document.createElement('select');
    if (document.getElementById("ubcProject_")) {
        form8.setAttribute('name', 'ubcProject');
        form8.setAttribute('class', 'wob');
        form8.setAttribute('style', 'font-weight: bold;width: 50px;font-style: italic; font-size:x-small');
        form8.innerHTML = (form8.innerHTML + document.getElementById('ubcProject_').innerHTML);
    }
    var TD8 = document.createElement('td');
    TD8.appendChild(form8);
    TD8.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD8);


    /* Ticket */
    var form81 = document.createElement('input');
    form81.setAttribute('style', 'width:50px; font-size:x-small');
    form81.setAttribute('class', 'wob');
    form81.setAttribute('name', 'ubcTicketIDFixed');
    var TD81 = document.createElement('td');
    TD81.appendChild(form81);
    TD81.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD81);

    /* Bug */
    var form82 = document.createElement('input');
    form82.setAttribute('style', 'width:50px; font-size:x-small');
    form82.setAttribute('class', 'wob');
    form82.setAttribute('name', 'ubcBugIDFixed');
    var TD82 = document.createElement('td');
    TD82.appendChild(form82);
    TD82.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD82);


    /* Subject */
    var form9 = document.createElement('input');
    form9.setAttribute('style', 'width:300px');
    form9.setAttribute('class', 'wob');
    form9.setAttribute('name', 'ubcSubject');
    var TD9 = document.createElement('td');
    TD9.appendChild(form9);
    TD9.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD9);

    /* ReleaseOwner */
    var form7 = document.createElement('select');
    if (document.getElementById("ubcReleaseOwner_")) {
        form7.setAttribute('name', 'ubcReleaseOwner');
        form7.setAttribute('class', 'wob');
        form7.setAttribute('style', 'font-weight: bold;width: 100px;font-style: italic; font-size:x-small');
        form7.innerHTML = (form7.innerHTML + document.getElementById('ubcReleaseOwner_').innerHTML);
    }
    var TD7 = document.createElement('td');
    TD7.appendChild(form7);
    TD7.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD7);


    /* Link */
    var form6 = document.createElement('input');
    form6.setAttribute('style', 'width:250px');
    form6.setAttribute('class', 'wob');
    form6.setAttribute('name', 'ubcLink');
    var TD6 = document.createElement('td');
    TD6.appendChild(form6);
    TD6.setAttribute('colspan', '2');
    TD6.setAttribute('style', 'background-color:lightgrey');
    TR.appendChild(TD6);




    document.getElementById(tableau).appendChild(TR);
}


function hidebutton(buttonID) {
    document.getElementById(buttonID).style.display = "none";
}

function getMaxValueForParentElementIdAndElementName(parentElementId, elementName) {
    var elements = null;
    var value = 0;

    if (parentElementId) {
        elements = $('#' + parentElementId + ' [name="' + elementName + '"]');
    } else {
        elements = document.getElementsByName(elementName);
    }

    if (elements && elements.length > 0) {
        for (i = 0; i <= elements.length; i++) {
            if (elements[i] && elements[i].value && value < parseInt(elements[i].value)) {
                value = parseInt(elements[i].value);
            }
        }
    }
    return value;
}


function displayImportStep(attribute) {
    var value = getMaxValueForParentElementIdAndElementName(null, 'testcasestep_hidden');
    if (value && parseInt(value) > 0) {
        value = parseInt(value) + 10;
    } else {
        value = 10;
    }

    document.getElementById('import_step').value = value;

    var testSelected = GetCookie("ImportUseTest");
    if (testSelected) {
        $("#fromTest option[value='" + testSelected + "']").attr("selected", "selected");
        getTestCasesForImportStep();
    }

    $('#ImportStepButton').hide();
    $('#ImportStepTable').fadeIn(1000);
    $('#importbutton').removeAttr('onclick').attr('onclick', attribute);
    if (attribute === "useStep()") {
        document.getElementById('import_description').style.display = "inline";
    }
}

function getTestCasesForImportStep() {
    var selectTest = document.getElementById('fromTest');
    var testSelected = selectTest.options[selectTest.selectedIndex].value;

    var URL = './ImportTestCase.jsp?Test=' + encodeURI(testSelected);

    SetCookie("ImportUseTest", testSelected);
    var getImportUseTestCase = GetCookie("ImportUseTestCase");

    var selectTestCase = document.getElementById('fromTestCase');
    if (selectTestCase !== null && selectTestCase.selectedIndex >= 0) {
        var testCaseSelected = selectTestCase.options[selectTestCase.selectedIndex].value;
        URL += '&TestCase=' + encodeURI(testCaseSelected);
        SetCookie("ImportUseTestCase", testCaseSelected);
    } else if (getImportUseTestCase && getImportUseTestCase != "") {
        URL += '&TestCase=' + encodeURI(getImportUseTestCase);
    }

    $('#trImportTestCase').load(URL, function () {
        $('#trImportTestCase').fadeIn(250);
    });
}

function importStep() {


    var selectTest = document.getElementById('filtertest');
    var test = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('filtertestcase');
    var testCase = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromTest');
    var fromTest = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromTestCase');
    var fromTestCase = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromStep');
    var fromStep = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('ImportProperty');
    var importProperty;
    if (selectTest.checked) {
        importProperty = selectTest.value;
    } else {
        importProperty = "N";
    }


    var importStep = document.getElementById('import_step').value;

    var urlImportStep = './ImportTestCaseStep?';
    urlImportStep += 'Test=' + encodeURI(test);
    urlImportStep += '&TestCase=' + encodeURI(testCase);
    urlImportStep += '&Step=' + encodeURI(importStep);
    urlImportStep += '&FromTest=' + encodeURI(fromTest);
    urlImportStep += '&FromTestCase=' + encodeURI(fromTestCase);
    urlImportStep += '&FromStep=' + encodeURI(fromStep);
    urlImportStep += '&ImportProperty=' + encodeURI(importProperty);

    location.href = urlImportStep;
}

function useStep() {


    var selectTest = document.getElementById('filtertest');
    var test = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('filtertestcase');
    var testCase = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromTest');
    var fromTest = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromTestCase');
    var fromTestCase = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('fromStep');
    var fromStep = selectTest.options[selectTest.selectedIndex].value;

    selectTest = document.getElementById('ImportProperty');
    var importProperty;
    if (selectTest.checked) {
        importProperty = selectTest.value;
    } else {
        importProperty = "N";
    }


    var importStep = document.getElementById('import_step').value;
    var importDescription = document.getElementById('import_description').value;

    var urlImportStep = './UseTestCaseStep?';
    urlImportStep += 'Test=' + encodeURI(test);
    urlImportStep += '&TestCase=' + encodeURI(testCase);
    urlImportStep += '&Step=' + encodeURI(importStep);
    urlImportStep += '&FromTest=' + encodeURI(fromTest);
    urlImportStep += '&FromTestCase=' + encodeURI(fromTestCase);
    urlImportStep += '&FromStep=' + encodeURI(fromStep);
    urlImportStep += '&ImportProperty=' + encodeURI(importProperty);
    urlImportStep += '&Description=' + encodeURI(importDescription);

    location.href = urlImportStep + "#useStep";
}

function submitTestCaseModification(anchor) {
    var form = $("#UpdateTestCaseDetail");
    var allControlsToDelete = 0;

    var actionsToDelete = $("input[name='actions_delete']:checked");
    if (actionsToDelete.length > 0) {
        for (var i = 0; i < actionsToDelete.length; i++) {
            allControlsToDelete = eval(allControlsToDelete + $("td.controls_" + $(actionsToDelete[i]).val()).length);
        }
    }


    var execute = true;
    if (allControlsToDelete > 0) {
        execute = confirm("Your action will delete " + actionsToDelete.length + " action(s) and " +
                allControlsToDelete + " control(s).\nDo you want to continue ?");
    }

    if (execute) {
        form.attr("action", form.attr("action") + "#" + anchor);
        form.submit();
    }

    return false;
}


function getCountrySelectBox() {
    $.get('GetCountryForTestCase', {test: $('#test').val(), testCase: $('#testCase').val()}, function (data) {
        $('#country').empty().append($("<option></option>"));
        for (var i = 0; i < data.countriesList.length; i++) {
            $('#country').append($("<option></option>")
                    .attr("value", data.countriesList[i])
                    .text(data.countriesList[i]));
        }
    });
}
;

function getEnvironmentSelectBox() {
    $.get('GetEnvironmentAvailable', {test: $('#test').val(), testCase: $('#testCase').val(), country: $('#country').val()}, function (data) {
        $('#environment').empty().append($("<option></option>"));
        for (var i = 0; i < data.envList.length; i++) {
            $('#environment').append($("<option></option>")
                    .attr("value", data.envList[i].environment)
                    .text(data.envList[i].description));
        }
    });
}
;

function calculateProperty() {
    var query = {test: $("#test").val(),
        testCase: $("#testCase").val(),
        property: $("#property").val(),
        type: $("#type").val()
    };

    if (query.type !== "executeSoapFromLib" && query.type !== "getFromTestData") {
        query.country = $("#country").val();
        query.environment = $("#environment").val();
        query.database = $("#db").val();
    }

    $.get('CalculatePropertyForTestCase', query, function (data) {

        if (data !== null && data.resultList !== null) {
            $("#result").empty().text("Value: '" + data.resultList + "'");
            $("#propdesc").empty().text("Description: '" + data.description + "'");
        } else {
            $("#result").empty().append("<b>Unable to retrieve property in database !</b>");
        }
    });
}
;

function deleteTestCase(test, testcase, page) {
    if (confirm('Do you really want to delete this TestCase ? This operation cannot be reverted')) {
        $("#deleteTCDiv").append('<img src="images/loading.gif">');
        //var xhttp = new XMLHttpRequest();
        //xhttp.open("GET", "DeleteTestCase?test=" + test + "&testcase=" + testcase + "&fromPage=" + page, false);
        //xhttp.send();
        //var xmlDoc = xhttp.responseText;
        //location.href = (page + "?Test=" + test);
        window.open("DeleteTestCase?test=" + test + "&testcase=" + testcase + "&fromPage=" + page);
    }

}

function exportTestCase(test, testcase, page) {
    location.href = ("ExportTestCase?test=" + test + "&testcase=" + testcase);
}
//TODO:FN refactor after the improvement of the TestCase.jsp page
function alertOnMissingPropertyValues(total) {
    alert("You have " + total + " properties from type 'getFromDataLib' that are poorly defined. Please check if Value1 is defined!");
    return false;
}
function alertOnProperties() {
    return confirm("At least one property has empty name or no country selected, would you like to save, these properties will be deleted !");
}
function checkForm() {

    // Check all properties to be sure PK of each is OK
    var numberOfProperties = $("input[name='property_increment']").length;
    for (var index = 1; index <= numberOfProperties; index++) {

        // Check if name of property is not empty
        if ($("input[name='properties_property_" + index + "']").val() === "") {
            return alertOnProperties();
        } else {
            if ($("input[name='properties_country_" + index + "']:checked").length <= 0) {
                return alertOnProperties();
            }
        }

    }
    var emptyGetFromDataLib = $("textarea[class*='getFromDataLib']:empty").filter(function () {
        return this.value.length === 0;
    }).length;
    if (emptyGetFromDataLib > 0) {
        // some are empty
        return alertOnMissingPropertyValues(emptyGetFromDataLib);
    }
     
    return true;
}

function incrementRows(DIV, element, incrementAction) {
    var listAct = DIV.find(element);
    for (var a = 0; a < listAct.length; a++) {
        if (listAct[a].value > incrementAction) {
            listAct[a].value = parseInt(listAct[a].value) + 1;
        }
    }
}

var numberOfCall = 0;

function addStepNew(div) {

    var incStep = document.getElementsByName('step_increment').length;
    var inc = incStep;
    incStep++;

    var table = document.createElement('div');
    table.setAttribute('id', 'table');
    table.setAttribute('class', 'StepHeaderDiv');

    TR = document.createElement('tr');
    table.appendChild(TR);

    TR.appendChild(document.createTextNode("  Step ID  :"));
    var input1 = document.createElement('input');
    input1.setAttribute('style',
            'font-weight: bold;font-style: italic; width:20px');
    input1.setAttribute('name', 'step_number_' + incStep);
    input1.setAttribute('class', 'wob');
    input1.setAttribute('title', 'Step ID');

    var value = getMaxValueForParentElementIdAndElementName(null, 'step_number_' + inc);
    if (value && parseInt(value) > 0) {
        value = parseInt(value) + 10;
    } else {
        value = 10;
    }

    input1.setAttribute('value', value);

    var input2 = document.createElement('input');
    input2.setAttribute('style', 'display:none');
    input2.setAttribute('name', 'step_increment');
    input2.setAttribute('value', incStep);


    var TD1 = document.createElement('td'); /* Create column */
    TD1.setAttribute('style', 'background-color:#e1e7f3; text-align: center; valign:center');
    TD1.setAttribute('class', 'wob');
    TD1.appendChild(input1); /* Add form to column */
    TD1.appendChild(input2);
    TR.appendChild(TD1);

    TR.appendChild(document.createTextNode("  Step Description  :"));
    var input2 = document.createElement('input');
    input2.setAttribute('size', '100%');
    input2.setAttribute('class', 'wob');
    input2.setAttribute('style', 'width : 500px');
    input2.setAttribute('name', 'step_description_' + incStep);
    var TD2 = document.createElement('td'); /* Create column */
    TD2.setAttribute('style', 'background-color:#e1e7f3; text-align: center');
    TD2.setAttribute('class', 'wob');
    TD2.appendChild(input2); /* Add form to column */
    TR.appendChild(TD2);

    var TD7 = document.createElement('td');
    TD7.setAttribute('class', 'wob');
    TR.appendChild(TD7);

    TR2 = document.createElement('tr');

    var TD13 = document.createElement('td');
    TD13.setAttribute('style', 'width:10px');
    TD13.setAttribute('class', 'wob');
    TR2.appendChild(TD13);


    table.appendChild(document.createElement('br'));
    table.appendChild(document.createElement('br'));

    var form5 = document.createElement('input');
    form5.setAttribute('type', 'button');
    form5.setAttribute('value', 'Add Step');
    form5.setAttribute('class', 'buttonAddStep');
    form5.setAttribute('onclick', 'addStepNew(\'StepsEndDiv' + incStep + '\')');
    var DIV5 = document.createElement('div');
    DIV5.setAttribute('style', 'float:left;margin-top:25px');
    DIV5.appendChild(form5);
    var form6 = document.createElement('input');
    form6.setAttribute('type', 'button');
    form6.setAttribute('value', 'Save Changes');
    form6.setAttribute('class', 'buttonSaveChanges');
    form6.setAttribute('onclick', 'submitTestCaseModificationNew(\'stepAnchor_' + incStep + '\')');
    var DIV6 = document.createElement('div');
    DIV6.setAttribute('style', 'float:left;margin-top:25px');
    DIV6.appendChild(form6);
    var DIV7 = document.createElement('div');
    DIV7.setAttribute('style', 'display:inline-block; width:100%');
    DIV7.setAttribute('id', 'StepsEndDiv' + incStep);

    table.appendChild(DIV5);
    table.appendChild(DIV6);
    table.appendChild(DIV7);

    var referenceNode = document.getElementById(div);
    referenceNode.parentNode.insertBefore(table, referenceNode.nextSibling);
    document.getElementById('incrementStepNumber').value = incStep;

}

function addTestCasePropertiesNew(tableau, row_number, size, size2) {

    var incProp = document.getElementsByName('property_increment').length;
    var inc = incProp;
    incProp++;

    TR = document.createElement('tr');

    /* Delete box */
    // prop_line++;
    var form1 = document.createElement('input');
    form1.setAttribute('type', 'checkbox');
    form1.setAttribute('name', 'properties_delete_' + incProp);
    form1.setAttribute('style', '');
    form1.setAttribute('value', row_number + 1);
    var form11 = document.createElement('input');
    form11.setAttribute('name', 'property_increment');
    form11.setAttribute('value', incProp);
    form11.setAttribute('hidden', 'hidden');
    var TD1 = document.createElement('td');
    TD1.setAttribute('style', 'background-color:white; text-align: center');
    TD1.appendChild(form1);
    TD1.appendChild(form11);
    TR.appendChild(TD1);

    /* Property */
    var form2 = document.createElement('input');
    form2.setAttribute('name', 'properties_property_' + incProp);
    form2.setAttribute('class', 'wob properties_id_' + eval(row_number + 1));
    form2.setAttribute('size', '130%');
    form2
            .setAttribute('style',
                    'width: 130px; font-weight: bold;font-style: italic; color: #FF0000;');
    form2.setAttribute('placeholder', 'Feed Property name');
    var TD2 = document.createElement('td');
    TD2.setAttribute('style', 'background-color:white');
    TD2.appendChild(form2);
    TR.appendChild(TD2);

    /*
     * Country
     */
    /* Parse values from hidden containing all countries */

    var TD3 = document.createElement('td'); /* Create column */
    if (document.getElementById("hiddenProperty")) {
        TD3.setAttribute('style', 'font-size : x-small ; width: ' + size
                + 'px;');
        TD3.setAttribute('style', 'background-color: white;');

        var jToto = $('#hiddenProperty');
        jToto.find('input[data-country="ctr"]').addClass('properties_id_' + eval(row_number + 1));
        jToto.find('input[data-country="ctr"]').attr('name', 'properties_country_' + incProp);
        TD3.innerHTML = (TD3.innerHTML + jToto.html());

    }
    TR.appendChild(TD3);

    /* Type */
    var form4 = document.createElement('select');
    if (document.getElementById("new_properties_type_new_properties_value")) {
        form4.setAttribute('name', 'properties_type_' + incProp);
        form4.setAttribute('id', 'typenew_properties_value');
        form4.setAttribute('style', 'width: 120px');
        form4.setAttribute('onchange', 'activateDatabaseBox(this.value, \'properties_nodtb_' + incProp + '\' , \'properties_dtb_' + incProp + '\'); activateValue2(this.value, \'tdValue2_new\', \'new_properties_value\',\'new_properties_value2\',\'' + size2 + '\')');
        form4.setAttribute('class', 'wob');
        form4.innerHTML = (form4.innerHTML + document
                .getElementById('new_properties_type_new_properties_value').innerHTML);
//        var form44 = document.createElement('option');
//        form44.setAttribute('Value', 'executeSql');
//        form44.setAttribute('selected', 'selected');
//        form4.appendChild(form44);
    }
    var TD4 = document.createElement('td');
    TD4.setAttribute('style', 'background-color:white');
    TD4.appendChild(form4);
    TR.appendChild(TD4);

    /* Database */
    var form41 = document.createElement('select');
    if (document.getElementById("properties_dtb_")) {
        form41.setAttribute('name', 'properties_dtb_' + incProp);
        form41.setAttribute('style', 'width: 40px;display: inline');
        form41.setAttribute('class', 'wob');
        form41.innerHTML = (form41.innerHTML + document
                .getElementById('properties_dtb_').innerHTML);
        form41.setAttribute('id', 'properties_dtb_' + incProp);
    }

    var form42 = document.createElement('input');
    form42.setAttribute('style', 'display: none; width: 39px; background-color: white; text-align: center;');
    form42.setAttribute('id', 'properties_nodtb_' + incProp);
    form42.setAttribute('class', 'wob');
    form42.setAttribute('value', '---');
    var TD41 = document.createElement('td');
    TD41.setAttribute('style', 'background-color:white');
    TD41.appendChild(form41);
    TD41.appendChild(form42);
    TR.appendChild(TD41);

    /* Value */
    var form5 = document.createElement('textarea');
    form5.setAttribute('name', 'properties_value1_' + incProp);
    form5.setAttribute('class', 'wob');
    form5.setAttribute('id', 'new_properties_value');
    form5.setAttribute('style', 'width: ' + size2 + 'px');
    var TD5 = document.createElement('td');
    var TB51 = document.createElement('table');
    var TR51 = document.createElement('tr');
    var TD51 = document.createElement('td');
    TD51.setAttribute('style', 'background-color:white');
    TD51.appendChild(form5);
    TD51.setAttribute('class', 'wob');
    TR51.appendChild(TD51);

    var form54 = document.createElement('textarea');
    form54.setAttribute('name', 'properties_value2_' + incProp);
    form54.setAttribute('placeholder', 'Attribute');
    form54.setAttribute('class', 'wob');
    form54.setAttribute('id', 'new_properties_value2');
    var TD52 = document.createElement('td');
    TD52.setAttribute('style', 'background-color:white; display:none');
    TD52.setAttribute('id', 'tdValue2_new');
    TD52.appendChild(form54);
    TD52.setAttribute('class', 'wob');
    TR51.appendChild(TD52);

    var form52 = document.createElement('input');
    form52.setAttribute('style', 'display:inline; height:20px; width:20px; background-color: white; color:blue; font-weight:bolder');
    form52.setAttribute('title', 'Open SQL Library');
    form52.setAttribute('class', 'smallbutton');
    form52.setAttribute('type', 'button');
    form52.setAttribute('value', 'L');
    form52.setAttribute('onclick', 'openSqlLibraryPopin(\'new_properties_value\')');

    var TD52 = document.createElement('td');
    TD52.setAttribute('style', 'background-color:white');
    TD52.setAttribute('class', 'wob');
    TD52.appendChild(form52);
    TR51.appendChild(TD52);

    TB51.appendChild(TR51);
    TD5.appendChild(TB51);
    TD5.setAttribute('style', 'background-color: white');
    TR.appendChild(TD5);



    /* Length */
    var form6 = document.createElement('input');
    form6.setAttribute('name', 'properties_length_' + incProp);
    form6.setAttribute('value', 0);
    form6.setAttribute('class', 'wob');
    form6.setAttribute('style', 'width: 40px');
    var TD6 = document.createElement('td');
    TD6.setAttribute('style', 'background-color:white');
    TD6.appendChild(form6);
    TR.appendChild(TD6);

    /* Row Limit */
    var form7 = document.createElement('input');
    form7.setAttribute('name', 'properties_rowlimit_' + incProp);
    form7.setAttribute('value', 0);
    form7.setAttribute('style', 'width: 40px');
    form7.setAttribute('class', 'wob');
    var TD7 = document.createElement('td');
    TD7.setAttribute('style', 'background-color:white');
    TD7.appendChild(form7);
    TR.appendChild(TD7);

    /* Nature */
    var form8 = document.createElement('select');
    if (document.getElementById("properties_nature_")) {
        form8.setAttribute('name', 'properties_nature_' + incProp);
        form8.setAttribute('class', 'wob');
        form8.setAttribute('style', 'width: 80px');
        form8.innerHTML = (form8.innerHTML + document
                .getElementById('properties_nature_').innerHTML);
    }
    var TD8 = document.createElement('td');
    TD8.setAttribute('style', 'background-color:white');
    TD8.appendChild(form8);
    TR.appendChild(TD8);

    document.getElementById(tableau).appendChild(TR);

}
/**
 * Function that validates that there are not steps with the same number, upon saving.
 * @returns {Boolean}
 */
function validateStepNumbers(){
    var arrayValues = [];
    //check the step numbers are not repeated and if they are not marked to be delete
    var containsRepeated = 0;
    $.each($("div[id='StepNumberDiv']"), function(idx, obj){
        if(!$(this).parent("div[id*='StepFirstLineDiv']").hasClass("RowToDelete")){  //if the step step is not marked to be removed, then we can check if has a repeated number
            var inputElement = $(this).find("input[name*='step_number_'][data-fieldtype='stepNumber']");
            if(inputElement.length> 0 )    
                if(arrayValues.indexOf($(inputElement).prop("value")) < 0){
                    arrayValues.push($(inputElement).prop("value"));
                }else{
                    containsRepeated++;
                }                
            }
    });
    return containsRepeated === 0;
}
function submitTestCaseModificationNew(anchor) {
       
    var form = $("#UpdateTestCase");
    var allControlsToDelete = 0;

    /*temporary workaround for issue #611*/
    var stepNumbersValid = validateStepNumbers();

    if(!stepNumbersValid){
        //TODO: add translation in future refactoring
        alert("You have steps with the same number. Please check the step numbers before proceed!!");
        return false;
    }

    var actionsToDelete = $("input[data-action='delete_action']:checked");
    if (actionsToDelete.length > 0) {
        for (var i = 0; i < actionsToDelete.length; i++) {
            console.log($(actionsToDelete[i]).attr('name'));
            allControlsToDelete = eval(allControlsToDelete + $("input[data-associatedaction='" + $(actionsToDelete[i]).attr('name') + "']").size());
        }
    }


    var execute = true;
    if (allControlsToDelete > 0) {
        execute = confirm("Your action will delete " + actionsToDelete.length + " action(s) and " +
                allControlsToDelete + " control(s).\nDo you want to continue ?");
    }

    if (execute) {        
        execute = checkForm();
    }

    if (execute) {
        form.attr("action", form.attr("action") + "#" + anchor);
        form.submit();
    }

    return false;
}

function SetCookie(name, value) {
    var argv = SetCookie.arguments;
    var argc = SetCookie.arguments.length;
    var expires = (argc > 2) ? argv[2] : null;
    var path = (argc > 3) ? argv[3] : null;
    var domain = (argc > 4) ? argv[4] : null;
    var secure = (argc > 5) ? argv[5] : false;
    document.cookie = name + "=" + escape(value) +
            ((expires == null) ? "" : ("; expires=" + expires.toGMTString())) +
            ((path == null) ? "" : ("; path=" + path)) +
            ((domain == null) ? "" : ("; domain=" + domain)) +
            ((secure == true) ? "; secure" : "");
}
;


function insertAfter(newNode, referenceNode) {
    referenceNode.parentNode.insertBefore(newNode, referenceNode.nextSibling);
}
;

function getCookieVal(offset) {
    var endstr = document.cookie.indexOf(";", offset);
    if (endstr == -1)
        endstr = document.cookie.length;
    return unescape(document.cookie.substring(offset, endstr));
}
;

function GetCookie(name) {
    var arg = name + "=";
    var alen = arg.length;
    var clen = document.cookie.length;
    var i = 0;
    while (i < clen) {
        var j = i + alen;
        if (document.cookie.substring(i, j) == arg)
            return getCookieVal(j);
        i = document.cookie.indexOf(" ", i) + 1;
        if (i == 0)
            break;
    }
    return null;
}
;

function addTCSANew(rowID, step, obj) {
    /**
     * rowID : Name of the Row on which we'll add a line bellow
     * incrementStep : number of the set increment
     * obj : Object from where we call this function (button A+)
     */

    /* Increment All fields bellow the current*/
    var incremAction
    if (obj === null) {
        incremAction = 0;
    } else {
        incremAction = obj.parentNode.parentNode.parentNode.querySelector('[data-field="sequence"]').value;
    }
    var incrementAction = parseInt(incremAction);
    var nextIncAction = incrementAction + 1;

    incrementRows($('#StepsBorderDiv' + step), 'input[data-fieldtype="action_' + step + '"]', incrementAction);
    incrementRows($('#StepsBorderDiv' + step), 'input[data-fieldtype="ctrlseq_' + step + '"]', incrementAction);

    /* Get the number of Action on the table and increment it*/
    var numberOfAction = document.getElementsByName('action_increment_' + step).length;
    var newNumberOfAction = parseInt(numberOfAction) + 1;
    var StepNum = document.getElementById('initial_step_number_' + step).value;


    /* Create the new div and insert it in the page*/
    var DIV = document.createElement('div');
    if (document.getElementById("StepActionTemplateDiv")) {
        DIV.innerHTML = (DIV.innerHTML + document
                .getElementById('StepActionTemplateDiv').innerHTML);
    }
    DIV.setAttribute('style', 'display:block;height:50px; width:100%;background-color:#C4EBFF;border-style: solid; border-width:thin ; border-color:#CCCCCC;');
    DIV.setAttribute('id', 'StepListOfActionDiv' + step + newNumberOfAction);
    DIV.setAttribute('class', 'RowActionDiv');
    var DIV2 = document.createElement('div');
    DIV2.setAttribute('class', 'endOfAction');
    DIV2.setAttribute('id', 'DivActionEndOfAction' + step + newNumberOfAction);
    var referenceNode = document.getElementById(rowID);
    referenceNode.parentNode.insertBefore(DIV2, referenceNode.nextSibling);
    referenceNode.parentNode.insertBefore(DIV, referenceNode.nextSibling);


    /* Search fields and add new name and value*/
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_color_id"]')
            .attr('style', 'background-color:blue; width:8px;height:100%;display:inline-block;float:left');
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_delete_template"]')
            .attr('name', 'action_delete_' + step + '_' + newNumberOfAction)
            .attr('id', 'action_delete_' + step + '_' + newNumberOfAction)
            .attr('style', 'display:none');
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_increment_template"]')
            .attr('name', 'action_increment_' + step).val(newNumberOfAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_step_template"]')
            .attr('name', 'action_step_' + step + '_' + newNumberOfAction).val(StepNum);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('img[data-id="action_img_delete"]')
            .attr('id', 'img_delete_' + step + '_' + newNumberOfAction)
            .attr('onclick', 'checkDeleteBox(\'img_delete_' + step + '_' + newNumberOfAction + '\', \'action_delete_' + step + '_' + newNumberOfAction + '\',\'StepListOfActionDiv' + step + newNumberOfAction + '\',\'RowActionDiv\')');
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('img[data-id="actionAddActionButton_template"]')
            .attr('onclick', 'addTCSANew(\'DivActionEndOfAction' + step + newNumberOfAction + '\', \'' + step + '\', this)');
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('img[data-id="actionAddControlButton_template"]')
            .attr('onclick', 'addTCSACNew(\'StepListOfActionDiv' + step + newNumberOfAction + '\', \'' + step + '\', \'' + newNumberOfAction + '\', this)');
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_sequence_template"]')
            .attr('name', 'action_sequence_' + step + '_' + newNumberOfAction).attr('data-fieldtype', 'action_' + step).val(nextIncAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_technical_sequence_template"]')
            .attr('name', 'action_technical_sequence_' + step + '_' + newNumberOfAction).val(-1);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_description_template"]')
            .attr('name', 'action_description_' + step + '_' + newNumberOfAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('#action_action_template')
            .attr('name', 'action_action_' + step + '_' + newNumberOfAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_object_template"]')
            .attr('name', 'action_object_' + step + '_' + newNumberOfAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('input[data-id="action_property_template"]')
            .attr('name', 'action_property_' + step + '_' + newNumberOfAction);
    $('#StepListOfActionDiv' + step + newNumberOfAction).find('#action_forceexestatus_template')
            .attr('name', 'action_forceexestatus_' + step + '_' + newNumberOfAction);

    callEvent();

}

function addTCSACNew(rowID, step, incrementAction, obj) {
    /**
     * rowID : Name of the Row on which we'll add a line bellow
     * incrementStep : number of the set increment
     * obj : Object from where we call this function (button A+)
     */


    /* Increment All control fields bellow the current*/
    var incremAction = obj.parentNode.parentNode.parentNode.querySelector('[data-field="sequence"]').value;
    var actionNumber = parseInt(incremAction);
    var incremControl = obj.parentNode.parentNode.parentNode.querySelector('[data-fieldtype="control_' + step + '_' + incrementAction + '"]') === null ? 0 : obj.parentNode.parentNode.parentNode.querySelector('[data-fieldtype="control_' + step + '_' + incrementAction + '"]').value;
    var incrementControl = parseInt(incremControl);

    incrementRows($('#StepsBorderDiv' + step), 'input[data-fieldtype="control_' + step + '_' + actionNumber + '"]', incrementControl);

    /* Get the number of control on the table and increment it*/
    var numberOfCtrl = 0;
    if (document.getElementsByName('control_increment_' + step + '_' + incrementAction) !== null) {
        numberOfCtrl = document.getElementsByName('control_increment_' + step + '_' + incrementAction).length;
    }
    var nextIncControl = parseInt(numberOfCtrl) + 1;
    var StepNum = document.getElementById('initial_step_number_' + step).value;


    /* Create the new div and insert it in the page*/
    var DIV = document.createElement('div');
    if (document.getElementById("StepControlTemplateDiv")) {
        DIV.innerHTML = (DIV.innerHTML + document
                .getElementById('StepControlTemplateDiv').innerHTML);
    }
    DIV.setAttribute('style', 'display:block; height:50px; width:100%;background-color:#C4FFEB;border-style: solid; border-width:thin ; border-color:#CCCCCC;');
    DIV.setAttribute('id', 'StepListOfControlDiv' + step + incrementAction + nextIncControl);
    DIV.setAttribute('class', 'RowActionDiv');
    var referenceNode = document.getElementById(rowID);
    referenceNode.parentNode.insertBefore(DIV, referenceNode.nextSibling);

    /* Search fields and add new name and value*/
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_color_id"]')
            .attr('style', 'background-color:blue; width:8px;height:100%;display:inline-block;float:left');
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_delete_template"]')
            .attr('name', 'control_delete_' + step + '_' + incrementAction + '_' + nextIncControl)
            .attr('id', 'control_delete_' + step + '_' + incrementAction + '_' + nextIncControl)
            .attr('style', 'display:none');
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_increment_template"]')
            .attr('name', 'control_increment_' + step + '_' + incrementAction).val(nextIncControl);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_step_template"]')
            .attr('name', 'control_step_' + step + '_' + incrementAction + '_' + nextIncControl).val(StepNum);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('img[data-id="control_img_delete"]')
            .attr('id', 'img_delete_' + step + '_' + incrementAction + '_' + nextIncControl)
            .attr('onclick', 'checkDeleteBox(\'img_delete_' + step + '_' + incrementAction + '_' + nextIncControl + '\', \'control_delete_' + step + '_' + incrementAction + '_' + nextIncControl + '\',\'StepListOfControlDiv' + step + incrementAction + nextIncControl + '\', \'RowActionDiv\')');
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('img[data-id="controlAddActionButton_template"]')
            .attr('onclick', 'addTCSANew(\'DivActionEndOfAction' + step + incrementAction + '\', \'' + step + '\', this)');
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('img[data-id="controlAddControlButton_template"]')
            .attr('onclick', 'addTCSACNew(\'StepListOfControlDiv' + step + incrementAction + nextIncControl + '\', \'' + step + '\', \'' + incrementAction + '\', this)');
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_sequence_template"]')
            .attr('name', 'control_sequence_' + step + '_' + incrementAction + '_' + nextIncControl).attr('data-fieldtype', 'ctrlseq_' + step).val(incremAction);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_control_template"]')
            .attr('name', 'control_control_' + step + '_' + incrementAction + '_' + nextIncControl).attr('data-fieldtype', 'control_' + step + '_' + incremAction).val(incrementControl + 1);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_technical_control_template"]')
            .attr('name', 'control_technical_control_' + step + '_' + incrementAction + '_' + nextIncControl).val(-1);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_description_template"]')
            .attr('name', 'control_description_' + step + '_' + incrementAction + '_' + nextIncControl);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('#control_type_template')
            .attr('name', 'control_type_' + step + '_' + incrementAction + '_' + nextIncControl);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_property_template"]')
            .attr('name', 'control_property_' + step + '_' + incrementAction + '_' + nextIncControl);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('input[data-id="control_value_template"]')
            .attr('name', 'control_value_' + step + '_' + incrementAction + '_' + nextIncControl);
    $('#StepListOfControlDiv' + step + incrementAction + nextIncControl).find('#control_fatal_template')
            .attr('name', 'control_fatal_' + step + '_' + incrementAction + '_' + nextIncControl);

    callEvent();
}

function addTCSCNew(rowID, obj) {
    /**
     * rowID : Name of the Row on which we'll add a line bellow
     * incrementStep : number of the set increment
     * obj : Object from where we call this function (button A+)
     */


    /* Increment All step fields bellow the current*/
    var initStep = 0;
    if (obj !== null) {
        var initStep = obj.parentNode.parentNode.querySelector('[data-fieldtype="stepNumber"]') === null ? 0 : obj.parentNode.parentNode.querySelector('[data-fieldtype="stepNumber"]').value;
    }
    var incrementStep = parseInt(initStep);
    incrementRows($('#StepsMainDiv'), 'input[data-fieldtype="stepNumber"]', incrementStep);
    var mySystem = document.getElementById("MySystem").value;

    /* Get the number of control on the table and increment it*/
    var numberOfStep = 0;
    if (document.getElementsByName('step_increment') !== null) {
        numberOfStep = document.getElementsByName('step_increment').length;
    }
    var nextIncStep = parseInt(numberOfStep) + 1;

    /* Create the new div and insert it in the page*/
    var mainDiv = document.createElement('div');
    mainDiv.setAttribute('style', 'display:block; clear:both; margin-top:5px');
    var referenceNode = document.getElementById(rowID);
    referenceNode.parentNode.insertBefore(mainDiv, referenceNode.nextSibling);

    var DIV = document.createElement('div');
    //TODO:FN temporary fix - should be solved when the page is refactored
    //selects should be loaded when the useStep is checked.
    
    var cloneElement = $("#StepTemplateDiv").clone();
    $(cloneElement).find('div[data-id="useStepForNewStep"]').remove();
    if (document.getElementById("StepTemplateDiv")) {
        //DIV.innerHTML = (DIV.innerHTML + document.getElementById('StepTemplateDiv').innerHTML);
        DIV.innerHTML = DIV.innerHTML + $(cloneElement).html();
    }
    DIV.setAttribute('id', 'StepFirstLineDiv' + nextIncStep);
    DIV.setAttribute('class', 'StepHeaderDiv');
    DIV.setAttribute('style', 'display:block; height:70px');
    mainDiv.appendChild(DIV);
    
    //appends the area that contains the actions and controls
    //TODO:FN needs to be refactored
    var DIVStepsBorderDiv = document.createElement('div');
    DIVStepsBorderDiv.setAttribute("style", "display:block;margin-top:0px;border-style: solid; border-width:thin ; border-color:#EEEEEE; clear:both;");
    DIVStepsBorderDiv.setAttribute("id", "StepsBorderDiv" +  nextIncStep);
    
    var DIVStepDetailsDiv = document.createElement('div');
    DIVStepDetailsDiv.setAttribute("style", "clear:both");
    DIVStepDetailsDiv.setAttribute("id", "StepDetailsDiv" +  nextIncStep);
     
    var DIVActionControlDivUnderTitle = document.createElement('div');
    DIVActionControlDivUnderTitle.setAttribute("style", "height:100%;width:100%;clear:both");
    DIVActionControlDivUnderTitle.setAttribute("id", "ActionControlDivUnderTitle" +  nextIncStep);
    
    var DIVAction = document.createElement('div');
    DIVAction.setAttribute("style", "height:100%; width:100%;text-align: left; clear:both");
    DIVAction.setAttribute("id", "Action" +  nextIncStep);
    
    var DIVBeforeFirstAction = document.createElement('div');
    DIVBeforeFirstAction.setAttribute("id", "BeforeFirstAction" +  nextIncStep);
    
    DIVAction.appendChild(DIVBeforeFirstAction);
    DIVActionControlDivUnderTitle.appendChild(DIVAction);
    DIVStepDetailsDiv.appendChild(DIVActionControlDivUnderTitle);
    DIVStepsBorderDiv.appendChild(DIVStepDetailsDiv);
    
    mainDiv.appendChild(DIVStepsBorderDiv);
    
    var DIV2 = document.createElement('div');
    if (document.getElementById("StepButtonTemplateDiv")) {
        DIV2.innerHTML = (DIV2.innerHTML + document
                .getElementById('StepButtonTemplateDiv').innerHTML);
    }
    DIV2.setAttribute('id', 'StepButtonDiv' + nextIncStep);
    DIV2.setAttribute('style', 'display:block;clear:both;margin-top:5px');
    DIV2.setAttribute('ondragover', 'insertTCS(event, \'' + nextIncStep + '\')');
    DIV2.setAttribute('ondrop', 'drop(event, \'' + nextIncStep + '\')');
    mainDiv.appendChild(DIV2);

    var DIV3 = document.createElement('div');
    DIV3.setAttribute('id', 'StepsEndDiv' + nextIncStep);
    DIV3.setAttribute('style', 'display:none');
    mainDiv.appendChild(DIV3);
    /* Search fields and add*/

    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="stepAnchor_template"]')
            .attr('name', 'stepAnchor_' + nextIncStep);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="stepAnchor_steptemplate"]')
            .attr('name', 'stepAnchor_step' + nextIncStep);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_delete_template"]')
            .attr('name', 'step_delete_' + nextIncStep)
            .attr('id', 'step_delete_' + nextIncStep).attr('style', 'display:none');
    $('#StepFirstLineDiv' + nextIncStep).find('img[data-id="step_img_delete"]')
            .attr('id', 'img_delete_step_' + nextIncStep)
            .attr('onclick', 'checkDeleteBox(\'img_delete_step_' + nextIncStep + '\', \'step_delete_' + nextIncStep + '\',\'StepFirstLineDiv' + nextIncStep + '\', \'StepHeaderDiv\')');
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_increment"]')
            .attr('name', 'step_increment').val(nextIncStep);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_number_template"]')
            .attr('name', 'step_number_' + nextIncStep).val(incrementStep + 1);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_technical_number_template"]')
            .attr('name', 'step_technical_number_' + nextIncStep).val(-1);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_description_template"]')
            .attr('name', 'step_description_' + nextIncStep).attr('data-fieldtype', 'Description')
            .attr('id', 'step_description_' + nextIncStep)
            .attr('placeholder', 'Description');
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="initial_step_number_template"]')
            .attr('name', 'initial_step_number_' + nextIncStep).val(nextIncStep);
    $('#StepFirstLineDiv' + nextIncStep).find('input[data-id="step_useStep_template"]')
            .attr('name', 'step_useStep_' + nextIncStep).val("Y")
            .attr('id', 'step_useStep_' + nextIncStep)
            .attr("data-step-number", nextIncStep);
    //adds the element that helps to validate if the step was modified
    $('#StepFirstLineDiv' + nextIncStep).find('input[id="step_useStepChanged_template"]')
            .attr('id', 'step_useStepChanged_' + nextIncStep)
            .attr('name', 'step_useStepChanged_' + nextIncStep);    
    $('#StepFirstLineDiv' + nextIncStep).find('input[id="isToCopySteps_template"]')
            .attr('id', 'isToCopySteps_' + nextIncStep)
            .attr('name', 'isToCopySteps_' + nextIncStep);    
    $('#StepFirstLineDiv' + nextIncStep).find('select[data-id="step_addActionButton_template"]')
            .attr('onclick', 'addTCSANew(\'BeforeFirstAction' + nextIncStep + '\', \'' + nextIncStep + '\', null)');
    $('#StepButtonDiv' + nextIncStep).find('input[data-id="submitButtonActionTemplate"]')
            .attr('onclick', 'submitTestCaseModificationNew(\'stepAnchor_' + nextIncStep + '\')');
    $('#StepButtonDiv' + nextIncStep).find('input[data-id="addStepButtonTemplate"]')
            .attr('onclick', 'addTCSCNew(\'StepsEndDiv' + nextIncStep + '\', this)')
            .attr('id', 'addStepButton' + nextIncStep);

    $("#step_useStep_" + nextIncStep).change(useStepChangeHandlerNew);
    callEvent();


}

function newActivateValue2(value, fieldValue1Id, fieldValue2Id, size) {
    var hideElements = false;
    
    var parents = $("#" + fieldValue1Id).parents("div[id*='propertyRow']");
    var textArea1 = $("#" + fieldValue1Id).find("textarea");
    var textArea2 = $("#" + fieldValue2Id).find("textarea");
    
    if (value === "getAttributeFromHtml" || value === "getFromXml" || value === "getFromCookie" ||
            value === "getFromJson" || value === "getDifferencesFromXml") {
        document.getElementById(fieldValue2Id).style.display = "inline-block";
        document.getElementById(fieldValue2Id).style.width = size / 2 + "%";
        document.getElementById(fieldValue1Id).style.width = size / 2 + "%";
        
    }else{
        document.getElementById(fieldValue2Id).style.display = "none";
        document.getElementById(fieldValue1Id).style.width = size + "%";
    }
    if(value ==="getFromDataLib"){  
            
            //temporary fix. TODO:FN this should be refactored in the future after the update of the list of nature types
            //var natureElement = $("#" + fieldValue2Id).parents("div[id*='propertyRow']").find("select[id*='properties_nature_']");                         
            var natureElement = $(parents).find("select[id*='properties_nature_']");                         
            $(natureElement).find("option[value='RANDOMNEW']").addClass("hideElement");
            $(natureElement).find("option[value='NOTINUSE']").addClass("hideElement");
            //var valueElement = $("#" + fieldValue2Id).parents("div[id*='propertyRow']").find("div[id*='selectEntry_']");
            var valueElement = $(parents).find("div[id*='selectEntry_']");
            $(valueElement).removeClass("hideElement");
            $(valueElement).addClass("showElementInline");
            
            $(textArea1).addClass("getFromDataLib");               
            $(valueElement).siblings("div").css("width", "90%");
            
//          //TODO:FN refactor after the improvement of the TestCase.jsp page
//          //add autocomplete for the first textarea
              
            if($(textArea1).hasClass("ui-autocomplete-input")){
                if($(textArea1).autocomplete("option", "disabled")){
                    $(textArea1).autocomplete("enable"); 
                }                
            }else{ //adds the events on the the textarea
               /* $(textArea1).on("change keyup paste", textArea1ChangeCallback);  */
                setPropertyValuesAutoComplete($(textArea1), callbackAutoCompleteTestDataLibName)             
            } 
      
            
            
    }else{ 
        //var natureElement = $("#" + fieldValue1Id).parents("div[id*='propertyRow']").find("select[id*='properties_nature_']");                         
        var natureElement = $(parents).find("select[id*='properties_nature_']");                         
        $(natureElement).find("option[value='RANDOMNEW']").removeClass("hideElement");
        $(natureElement).find("option[value='NOTINUSE']").removeClass("hideElement");
        //var valueElement = $("#" + fieldValue1Id).parents("div[id*='propertyRow']").find("div[id*='selectEntry_']");
        var valueElement = $(parents).find("div[id*='selectEntry_']");
        $(valueElement).removeClass("showElementInline");
        $(valueElement).addClass("hideElement");
        $(valueElement).siblings("div").css("width", "100%"); 
        
        //remove autocomplete
        $(textArea1).removeClass("getFromDataLib");            
                   
        //disables the autocomplete if exists
        if($(textArea1).hasClass("ui-autocomplete-input")){
            $(textArea1).autocomplete("disable");  
        }
      
    }
}

function addPropertyNew(widthValue) {
    var numberOfProperty = document.getElementsByName("property_increment").length;
    var nextIncrementValue = parseInt(numberOfProperty) + 1;

    var DIV = document.createElement('div');
    if (document.getElementById("PropertyTemplateDiv")) {
        DIV.innerHTML = (DIV.innerHTML + document
                .getElementById('PropertyTemplateDiv').innerHTML);
    }
    DIV.setAttribute('class', 'generalPropertyDiv');
    DIV.setAttribute('id', 'propertyRow' + nextIncrementValue);
    DIV.setAttribute('style', 'height:50px; clear:both; display:block;border-style: solid; border-width:thin ; border-color:#CCCCCC;');
    var referenceNode = document.getElementById('propertyRow' + numberOfProperty);
    referenceNode.parentNode.insertBefore(DIV, referenceNode.nextSibling);

    $('#propertyRow' + nextIncrementValue).find('input[data-id="properties_delete_template"]')
            .attr('name', 'properties_delete_' + nextIncrementValue)
            .attr('id', 'properties_delete_' + nextIncrementValue)
            .attr('style', 'display:none');
    $('#propertyRow' + nextIncrementValue).find('img[data-id="property_img_delete"]')
            .attr('id', 'img_delete_property_' + nextIncrementValue)
            .attr('onclick', 'checkDeleteBox(\'img_delete_property_' + nextIncrementValue + '\', \'properties_delete_' + nextIncrementValue + '\',\'propertyRow' + nextIncrementValue + '\', \'generalPropertyDiv\')');
    $('#propertyRow' + nextIncrementValue).find('input[data-id="property_increment_template"]')
            .attr('name', 'property_increment').val(nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('input[data-id="properties_property_template"]')
            .attr('name', 'properties_property_' + nextIncrementValue)
            .attr('placeholder', 'Feed Property name');
    $('#propertyRow' + nextIncrementValue).find('textarea[data-id="properties_description_template"]')
            .attr('name', 'properties_description_' + nextIncrementValue)
            .attr('placeholder', 'Feed Property description');
    $('#propertyRow' + nextIncrementValue).find('input[data-id="properties_country_template"]')
            .attr('name', 'properties_country_' + nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('#properties_type_template')
            .attr('name', 'properties_type_' + nextIncrementValue)
            .attr('onchange', 'activateDatabaseBox(this.value, \'properties_nodtb_' + nextIncrementValue + '\' ,\'properties_dtb_' + nextIncrementValue + '\');newActivateValue2(this.value, \'divProperties_value1_' + nextIncrementValue + '\', \'divProperties_value2_' + nextIncrementValue + '\',\'' + widthValue + '\')');
    $('#propertyRow' + nextIncrementValue).find('#properties_dtb_template')
            .attr('name', 'properties_dtb_' + nextIncrementValue)
            .attr('id', 'properties_dtb_' + nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('select[data-id="properties_nodtb_template"]')
            .attr('name', 'properties_nodtb_' + nextIncrementValue)
            .attr('id', 'properties_nodtb_' + nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('div[data-id="divProperties_value1_template"]')
            .attr('name', 'divProperties_value1_' + nextIncrementValue)
            .attr('id', 'divProperties_value1_' + nextIncrementValue);
    //new divs
    
    $('#propertyRow' + nextIncrementValue).find('div[data-id="selectEntry_Data_template"]')
            .attr('id', 'selectEntry_Data_' + nextIncrementValue);

    $('#propertyRow' + nextIncrementValue).find('button[data-id="entryButton_template"]')
            .attr('id', 'entryButton_' + nextIncrementValue);
    //adds the handler for the button

    $('button[id="entryButton_' + nextIncrementValue + '"]').click(setEntrybuttonClickHandler);

    $('#propertyRow' + nextIncrementValue).find('div[data-id="divProperties_value2_template"]')
            .attr('name', 'divProperties_value2_' + nextIncrementValue)
            .attr('id', 'divProperties_value2_' + nextIncrementValue);

    $('#propertyRow' + nextIncrementValue).find('textarea[data-id="properties_value1_template"]')
            .attr('name', 'properties_value1_' + nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('textarea[data-id="properties_value2_template"]')
            .attr('name', 'properties_value2_' + nextIncrementValue);
    $('#propertyRow' + nextIncrementValue).find('input[data-id="properties_length_template"]')
            .attr('name', 'properties_length_' + nextIncrementValue).val("0");
    $('#propertyRow' + nextIncrementValue).find('input[data-id="properties_rowlimit_template"]')
            .attr('name', 'properties_rowlimit_' + nextIncrementValue).val("0");
    $('#propertyRow' + nextIncrementValue).find('#properties_nature_template')
            .attr('name', 'properties_nature_' + nextIncrementValue);
}


function openRunManualPopin(test, testcase, env, country, idFromQueue, tag, browser) {
    loadRunManualPopin(test, testcase, env, country, idFromQueue, tag, browser);
    $('#popin').dialog({hide: {duration: 300}, height: $(window).height(), width: $(window).width() - 20, buttons: [
            {text: "Validate", click: function () {
                    submitExecution();
                }},
            {text: "Cancel", click: function () {
                    $("#isCancelExecution").val("Y");
                    submitExecution();
                }}
        ]});
}

function loadRunManualPopin(test, testcase, env, country, idFromQueue, tag, browser) {
//    $('#popin').hide().empty();
    console.log(test);
    $('#popin').load('TestCaseManualRun.jsp?Test=' + test.replace(new RegExp(' ', 'g'), '%20') + '&TestCase=' + testcase + '&Environment=' + env + '&Country=' + country + '&IdFromQueue=' + idFromQueue + '&Tag=' + encodeURIComponent(tag) + '&Browser=' + browser);
//    $('#popin').show();
}

function openChangeTagPopin(value) {
    loadChangeTagPopin(value);
    $('#setTagToExecution').dialog({hide: {duration: 300}, height: 200, width: 200, buttons: [{
                text: "Validate", click: function () {
//                    $('#SetTagToExecution').submit(function(event) {
//                        event.preventDefault();
                    var id = $("#setTagToExecution").find('input[name="executionId"]').val();
                    var tag = $("#setTagToExecution").find('input[name="newTag"]').val();
                    console.log(id + tag);
                    var deferred = $.get("./SetTagToExecution", {executionId: id, newTag: tag});

                    deferred.success(function () {
                        $(location).attr('href', "./ExecutionDetail.jsp?id_tc=" + value);
                    });

//
//                    });
                }},
            {text: "Cancel", click: function () {
                    $(this).dialog("close");
                }}
        ]});
}

function loadChangeTagPopin(value) {
    $('#setTagToExecution').empty().append('<p>Set Tag for Execution</p><input name="executionId" readonly="readonly" value="' + value + '"><input placeholder="Set New Tag Here" name="newTag">');
//                    <input name="newTag">
//    $('#popin').show();
}
 
