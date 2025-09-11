<%--

    Cerberus Copyright (C) 2013 - 2025 cerberustesting
    DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.

    This file is part of Cerberus.

    Cerberus is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Cerberus is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page import="java.util.Date" %>
<%@page import="org.springframework.web.context.support.WebApplicationContextUtils" %>
<%@page import="org.springframework.context.ApplicationContext" %>
<%@page import="org.springframework.web.context.WebApplicationContext" %>
<%@page import="org.cerberus.core.crud.entity.Invariant" %>
<%@page import="org.cerberus.core.session.SessionCounter" %>
<%@page import="java.util.List" %>
<%@page import="org.cerberus.core.crud.service.IInvariantService" %>
<%@page import="org.cerberus.core.database.IDatabaseVersioningService" %>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
    <head>
        <meta content="text/html; charset=UTF-8" http-equiv="content-type">
        <title>My Dashboard</title>
        <%@ include file="include/global/dependenciesInclusions.html" %>
        <script type="text/javascript" src="dependencies/Moment-2.30.1/moment-with-locales.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@4"></script>
        <script src="https://cdn.jsdelivr.net/npm/chartjs-adapter-date-fns"></script>
        <script type="text/javascript" src="js/pages/Widget.js"></script>
        <script type="text/javascript" src="js/widgets/widgetAvailability.js"></script>
        <script type="text/javascript" src="js/widgets/widgetCount.js"></script>
        <script type="text/javascript" src="js/widgets/widgetTimeline.js"></script>
        <script type="text/javascript" src="js/widgets/chartjsTimeLine.js"></script>
        <script src="https://code.jquery.com/ui/1.13.2/jquery-ui.min.js"></script>
        <style>
            :root{
                --card-bg:var(--crb-white-color);
                --text:var(--crb-dark-color);
                --muted:#a9b2bf;
                --primary:#4e7bf2;
                --success:#39a983;
                --link:#6690ff;
                --shadow:var(--crb-shadow-bottom-color);
                --radius:18px;
            }
            .card{
                max-width: 92vw;
                background:var(--card-bg); color:var(--text);
                border-radius:var(--radius); box-shadow:var(--shadow);
                padding:22px 24px 18px; line-height:1.25;
            }
            .card header{display:flex; align-items:center; gap:10px; margin-bottom:14px;}
            .card h3{margin:0; font-size:24px; font-weight:700; letter-spacing:.2px;}
            .gear{width:22px;height:22px;opacity:.9;flex:0 0 auto}
            .row{display:flex; align-items:center; justify-content:space-between; padding:2px 2px;}
            .left{margin-left:10px; font-size:12px;}
            .label{color:var(--text)}
            .muted{color:var(--muted)}
            .badge{min-width: 34px;height: 34px;border-radius: 999px;display: grid;
            place-items: center;font-weight: 800;font-size: 14px;margin-right: 10px;}
            .badge.primary{ background:var(--primary); color:white;}
            .badge.success{ background:var(--success); color:white;}
            a.cta{
                display:inline-flex; align-items:center; gap:8px;
                margin-top:10px; color:var(--link); text-decoration:none; font-weight:600;
            }
            a.cta:hover{ text-decoration:underline; }
            /* Pictos (emoji fallback) */
            .picto{font-size:22px; width:24px; text-align:center}
            /* Optional: focus ring */
            a:focus-visible{ outline:3px solid #94b0ff; outline-offset:3px; border-radius:10px }
        </style>
        <style>
            #grid {
                position:relative;
                width:100%;
                height:600px; /* 5 lignes */
                /*border:1px solid #ddd;
                background:#fafafa;*/
            }
            .widget {
                position:absolute;
                /*border:1px solid #ccc;*/
                background:var(--crb-white-color);
                padding:10px;
                border-radius:4px;
                overflow:hidden;
            }
            .drag-handle {
                cursor:move;
                position:absolute;
                top:5px; left:5px;
                font-weight:bold;
            }
            .widget-controls {
                position:absolute;
                top:5px; right:5px;
                display:none;
            }
            .drag-widget{
                display:none;
            }
            .widget-controls button {
                margin-left:3px;
            }
            #grid.edit-mode .widget-controls { display:block; }
            #grid.edit-mode .drag-widget { display:block; }
            #grid .guides { position:absolute; top:0; left:0; width:100%; height:100%; pointer-events:none; }
            #grid .guides .line {
                position:absolute;
                border:1px dashed #aaa;
            }
        </style>
    </head>
    <body>
        <%@ include file="include/global/header.html" %>
        <%@ include file="include/pages/homepage/tagSettingsModal.html" %>
        <%@ include file="include/utils/modal-confirmation.html" %>


        <div class="container-fluid center" id="page-layout">
            <%@ include file="include/global/messagesArea.html" %>
            <h1 class="page-title-line" id="title">
                My Dashboard
                <i id="editPageBtn" class="glyphicon glyphicon-pencil" style="cursor:pointer; margin-left:10px;"></i>
                <i id="addWidgetBtn" class="glyphicon glyphicon-plus-sign" style="cursor:pointer; margin-left:10px; display:none;"></i>
            </h1>
            <div class="container" style="width:100%">
                <div id="grid">
                    <div class="guides"></div>
                </div>
            </div>

            <!-- Modal Ajout Widget -->
            <div id="addWidgetModal" class="modal fade" role="dialog">
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <button type="button" class="close" data-dismiss="modal">&times;</button>
                            <h4 class="modal-title">Ajouter un widget</h4>
                        </div>
                        <div class="modal-body">
                            <p>Choisissez une taille de widget :</p>
                            <button class="btn btn-default widget-size" data-type="count" data-dv="Application">Count (1×2)</button>
                            <button class="btn btn-default widget-size" data-type="availability" data-dv="Count">Availability (2×3)</button>
                            <button class="btn btn-default widget-size" data-type="timeline" data-dv="Count">Timeline (2×4)</button>
                            <button class="btn btn-default widget-size" data-type="bigtimeline" data-dv="Count">Grand (2×8)</button>
                        </div>
                    </div>
                </div>
            </div>
            <footer class="footer">
                <div class="container-fluid" id="footer"></div>
            </footer>
        </div>
    </body>
</html>
