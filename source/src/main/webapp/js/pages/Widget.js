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

var cols=12, rows=20;
var margin=10;
var gridW=$("#grid").width(), gridH=$("#grid").height();
var editMode=false;

var widgetsType={
    count: {w:2,h:1},
    availability:{w:3,h:2},
    timeline:{w:4,h:2},
    bigtimeline:{w:8,h:2}
};

var widgetData=JSON.parse(localStorage.getItem("widgets"))||[
    {id:1,type:"count",option:"Application", content:"Count",x:0,y:0,w:2,h:1},
    {id:2,type:"count",option:"Service",content:"Count",x:2,y:0,w:2,h:1},
    {id:3,type:"count",option:"Testcase",content:"Count",x:4,y:0,w:2,h:1},
    {id:4,type:"count",option:"Execution",content:"Count",x:6,y:0,w:2,h:1},
    {id:5,type:"availability",option:"ExecutionStatus",content: {label:"MWV - 0003", test:"QA - Games", testcase: "MWV - 0003"},x:0,y:1,w:3,h:2 },
    {id:6,type:"timeline",option:"Count",content: "MWV - 0003",x:3,y:1,w:4,h:2 }
];

$.when($.getScript("js/global/global.js")).then(function () {
    $(document).ready(function () {

        displayPageLabel();

        bindToggleCollapse();
        //close all sidebar menu
        //closeEveryNavbarMenu();

        $("#show-hidden").on("click", function () {
            showWidgetToggleModal();
        });

        $('body').tooltip({
            selector: '[data-toggle="tooltip"]'
        });
        $('[data-toggle="popover"]').popover({
            'placement': 'auto',
            'container': 'body'
        });

        $("#editPageBtn").click(function(){
            editMode = !editMode;

            if(editMode){
                // Passage en mode édition
                $(this)
                    .removeClass("glyphicon-pencil")
                    .addClass("glyphicon-floppy-disk");
                $("#addWidgetBtn").show();
            } else {
                // Sauvegarde et retour en mode normal
                $(this)
                    .removeClass("glyphicon-floppy-disk")
                    .addClass("glyphicon-pencil");
                $("#addWidgetBtn").hide();

                // Sauvegarde des widgets
                localStorage.setItem("widgets", JSON.stringify(widgetData));
            }

            renderGridWidgets();
        });

        // Ouvrir modal ajout
        $("#addWidgetBtn").click(function(){
            $("#addWidgetModal").modal("show");
        });

        // Choisir widget
        $(".widget-size").click(function(){

            var w=widgetsType[$(this).attr("data-type")].w;
            var h=widgetsType[$(this).attr("data-type")].h;
            var newId=widgetData.length?Math.max(...widgetData.map(w=>w.id))+1:1;
            var newW={id:newId,option:$(this).attr("data-dv"),type:$(this).attr("data-type"),content:"Count",x:5,y:2,w:w,h:h};
            widgetData.push(newW);
            localStorage.setItem("widgets",JSON.stringify(widgetData));
            renderGridWidgets();
            $("#addWidgetModal").modal("hide");
        });

        // Delete widget
        $(document).on("click",".delete-widget",function(){
            var id=$(this).closest(".crb_card").attr("data-id");
            widgetData=widgetData.filter(w=>w.id!=id);
            renderGridWidgets();
        });

        // Editer widget
        $(document).on("click",".edit-widget",function(){
            var $w=$(this).closest(".crb_card");
            var id=$w.attr("data-id");
            var wd=widgetData.find(o=>o.id==id);
            if(wd.type === "count") {
                editWidgetCount(wd, $w);
            } else if (wd.type === "availability") {
                editWidgetAvailability(wd, $w);
            } else if (wd.type === "timeline") {
                editWidgetTimeline(wd, $w);
            } else if (wd.type === "bigtimeline") {
                editWidgetTimeline(wd, $w);
            }

            $(this).removeClass("edit-widget").addClass("save-widget").text("Save");
        });

        // Sauver widget
        $(document).on("click",".save-widget",function(){
            var $w=$(this).closest(".crb_card");
            var id=$w.attr("data-id");
            var wd=widgetData.find(o=>o.id==id);
            if(wd.type === "count") {
                saveWidgetCount(wd, $w);
            } else if (wd.type === "availability") {
                saveWidgetAvailability(wd, $w);
            } else if (wd.type === "timeline") {
                saveWidgetTimeline(wd, $w);
            } else if (wd.type === "bigtimeline") {
                saveWidgetTimeline(wd, $w);
            }
            $(this).removeClass("save-widget").addClass("edit-widget").text("Edit");
        });

        renderGridWidgets();
    });
});

function drawGuides(){
    var cols=12, rows=20;
    var margin=10;
    var gridW=$("#grid").width(), gridH=$("#grid").height();
    var cellW=gridW/cols, cellH=cellW;
    var $g=$("#grid .guides").empty();
    for(var c=1;c<cols;c++){
        var x=c*cellW;
        var h = cellH*rows;
        $("<div class='line'>").css({left:x,top:0,height:h,width:0}).appendTo($g);
    }
    for(var r=1;r<rows;r++){
        var y=r*cellH;
        $("<div class='line'>").css({top:y,left:0,width:"100%",height:0}).appendTo($g);
    }
}

function layoutWidgets(){
    var cols=12, rows=20;
    var gridW=$("#grid").width(), gridH=$("#grid").height();
    var cellW=gridW/cols, cellH=cellW;
    var margin=10;
    $(".crb_card").each(function(){
        var id=$(this).attr("data-id");
        var w=widgetData.find(o=>o.id==id);
        $(this).css({
            left:w.x*cellW+margin,
            top:w.y*cellH+margin,
            width:w.w*cellW-2*margin,
            height:w.h*cellH-2*margin
        });
    });
}

function makeDraggable($w,enable){
    var $grid=$("#grid");
    var cols=12, rows=20;
    var margin=10;
    var gridW=$("#grid").width(), gridH=$("#grid").height();
    var cellW=gridW/cols, cellH=cellW;
    $grid.height(cellH * rows);
    if(enable){
        $w.draggable({
            containment:"#grid",
            handle:".drag-handle",
            grid:[cellW,cellH],
            stop:function(e,ui){
                var id=$w.attr("data-id");
                var wd=widgetData.find(o=>o.id==id);
                wd.x=Math.round(ui.position.left/cellW);
                wd.y=Math.round(ui.position.top/cellH);
                localStorage.setItem("widgets",JSON.stringify(widgetData));
            }
        });
    } else {
        if($w.data("ui-draggable")) $w.draggable("destroy");
    }
}

function renderGridWidgets(){
    var $grid=$("#grid");
    $grid.find(".crb_card").remove();
    widgetData.forEach(function(w){
        console.log(w);
        if(w.type === "count") {
            var $w= $(WidgetCountTemplate(w));
        } else if (w.type === "availability") {
            var $w= $(WidgetAvailabilityTemplate(w));
        } else if (w.type === "timeline") {
            var $w= $(WidgetTimelineTemplate(w));
        } else if (w.type === "bigtimeline") {
            var $w= $(WidgetTimelineTemplate(w));
        }
        //console.log($w);
        $grid.append($w);
        makeDraggable($w,editMode);
        if(w.type === "count") {
            getData(w.option, w.id);
        } else if (w.type === "availability") {
            widgetAvailability(w.id,w.content);
        } else if (w.type === "timeline") {
            widgetTimeline(w.id,w.content);
        } else if (w.type === "bigtimeline") {
            widgetTimeline(w.id,w.content);
        }
        console.log($w);

    });
    layoutWidgets();
    $("#grid").toggleClass("edit-mode",editMode);
    $("#addWidgetBtn").toggle(editMode);
    if(editMode) {
        drawGuides();
    } else {
        $("#grid .guides").empty();

        var maxBottom = 0;
        $grid.find(".crb_card").each(function(){
            var bottom = $(this).position().top + $(this).outerHeight(true);
            if(bottom > maxBottom) maxBottom = bottom;
        });
        $grid.height(maxBottom + 10);
    }

}


function displayPageLabel() {
    var doc = new Doc();
    //displayHeaderLabel(doc);
    displayFooter(doc);
    displayGlobalLabel(doc);
}


