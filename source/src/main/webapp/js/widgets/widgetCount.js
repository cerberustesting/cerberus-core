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

// widgetCountOptions : Options available
// WidgetCountTemplate : Template HTML
// editWidgetCount : Edit Widget
// saveWidgetCount : Save Widget
// getData : Retrieve data from Back

var widgetCountOptions={
    "Application":[{"Count":{label:"Total configured",uri:"api/applications/count"}},{"New":{label:"Modified this week",uri:"api/applications/count"}}],
    "Service":[{"Count":{label:"Total configured",uri:"api/services/count"}},{"New":{label:"Modified this week",uri:"api/services/count"}}],
    "Testcase":[{"Count":{label:"Total created",uri:"api/testcases/count"}},{"New":{label:"Created this week",uri:"api/testcases/count"}}],
    "Execution":[{"Count":{label:"Total execution",uri:"api/executions/count"}},{"New":{label:"Executed this week",uri:"api/executions/count"}}]
};

 function WidgetCountTemplate(w) {
     var items = widgetCountOptions[w.option]; // ex: widgetCountOptions["Application"]
     var label = w.content;
     for (var i = 0; i < items.length; i++) {
         var obj = items[i];
         var objKey = Object.keys(obj)[0];
         if (objKey === w.content) {
             label = obj[objKey].label;
         }
     }
     return `
        <div class="crb_card absolute p-2.5" data-id="${w.id}">
          <div class="drag-handle drag-widget">⋮⋮⋮</div>
          <div class="widget-controls">
            <button class="btn btn-xs btn-info edit-widget">Edit</button>
            <button class="btn btn-xs btn-danger delete-widget">&times;</button>
          </div>
          <h4 class="widget-header" style="text-transform: uppercase;">${w.option}</h4>
          <!--<p class="widget-content"></p>-->
          <div class="row">
             <div class="col-md-8">
                 <p class="widget-content" style="margin:0">${label}</p>
             </div>
            <div class="badge primary" id="${w.id}-widget-badge-total"
              aria-label="Total d’applications configurées"></div>
          </div>
        </div>
      `;
 }

 function editWidgetCount(wd, $w){
    var $title=$('<select class="form-control input-sm"></select>');
    Object.keys(widgetCountOptions).forEach(function(t){
        $title.append(`<option ${t==wd.option?"selected":""}>${t}</option>`);
    });
    var $content=$('<select class="form-control input-sm"></select>');
     widgetCountOptions[wd.option].forEach(function(c){
         var key = Object.keys(c)[0];
         var value = c[key];
         var label = value.label || key;
         $content.append(`<option value="${key}" ${key === wd.content ? "selected" : ""}>${label}</option>`);
    });

    $w.find(".widget-header").replaceWith($title);
    $w.find(".widget-content").replaceWith($content);
    $title.change(function(){
        var val=$(this).val();
        var $c=$('<select class="form-control input-sm"></select>');
        widgetCountOptions[val].forEach(function(c){ $c.append(`<option>${c.key}</option>`); });
        $content.replaceWith($c);
        $content=$c;
    });
}

function saveWidgetCount(wd, $w) {
    var newTitle = $w.find("select").first().val();
    var newContent = $w.find("select").last().val();

    $w.find("select").first().replaceWith(`<h4 class="widget-header">${newTitle}</h4>`);
    $w.find("select").last().replaceWith(`<p class="widget-content">${newContent}</p>`);

    wd.option=newTitle; wd.content=newContent;
    localStorage.setItem("widgets",JSON.stringify(widgetData));

}

function getData(option,elementId){

    var uri = widgetCountOptions[option].find(o => o["Count"])["Count"].uri
    var jqxhr = $.getJSON(uri, getUser().defaultSystemsQuery);
    $.when(jqxhr).then(function (result) {
        $("#"+elementId+"-widget-badge-total").text(result["iTotalRecords"]);
    }).fail(handleErrorAjaxAfterTimeout);
}

function getData2() {

    // Calculate servlet name to call.
    var myServlet = "api/public/applications/Google";

    //var data = {};
    //data['application'] = $('#editTestCaseSimpleCreationApplication').val();
    //data['system'] = getUser().defaultSystem;

    $.ajax({
        url: myServlet,
        async: true,
        method: "GET",
        headers: {"X-API-VERSION": 1},
        contentType: 'application/json',
        dataType: 'json',
        //data: JSON.stringify(data),

        success: function (dataMessage) {
            console.log(dataMessage);
        },
        error: showUnexpectedError
    });

}
