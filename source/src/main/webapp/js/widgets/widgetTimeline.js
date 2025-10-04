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

// widgetTimelineOptions : Options available
// WidgetTimelineTemplate : Template HTML
// editWidgetCount : Edit Widget
// getData : Retrieve data from Back


var widgetTimelineOptions={
    "Count":["MWV - 0001","MWV - 0002","MWV - 0003","MWV - 0004","MWV - 0005","MWV - 0006"],
    "Time":["MWV - 0001","MWV - 0002","MWV - 0003","MWV - 0004","MWV - 0005","MWV - 0006"]
};
var configTime = {};

function WidgetTimelineTemplate(w) {

    return `
        <div class="crb_card absolute p-2.5" data-id="${w.id}">
          <div class="drag-handle drag-widget">⋮⋮⋮</div>
          <div class="widget-controls">
            <button class="btn btn-xs btn-info edit-widget">Edit</button>
            <button class="btn btn-xs btn-danger delete-widget">&times;</button>
          </div>
          <h4 class="widget-header" style="margin-bottom:0">TESTCASE PREFORMANCE (ms)</h4>
          <p class="widget-content">${w.content}</p>
          <div class="">
                <div class="timelineChart" id="${w.id}-widget-timeline">
                    <div class="row">
                        <div class="col-xs-12" id="${w.id}-ChartTestStat">
                            <canvas id="${w.id}-canvasTestStat"></canvas>
                        </div>
                    </div>
                </div>
          </div>
        </div>
      `;
}


function editWidgetTimeline(wd, $w) {
    // Cacher le graphique
    $w.find(".timelineChart").hide();

    // Construire la combo du titre
    var $title = $('<select class="form-control input-sm widget-title"></select>');
    Object.keys(widgetTimelineOptions).forEach(function (t) {
        $title.append(`<option ${t == wd.option ? "selected" : ""}>${t}</option>`);
    });

    // Construire la combo du contenu
    var $content = $('<select class="form-control input-sm widget-content"></select>');
    widgetTimelineOptions[wd.option].forEach(function(c){
        $content.append(`<option ${c==wd.content?"selected":""}>${c}</option>`);
    });

    // Remplacer le titre (h4) par une combo
    $w.find(".widget-header").replaceWith($title);

    // Insérer la combo content juste après le titre
    $title.after($content);

    // Gérer le changement du titre -> recharge du contenu
    $title.change(function(){
        var val = $(this).val();
        var $c = $('<select class="form-control input-sm widget-content"></select>');
        widgetTimelineOptions[val].forEach(function(c){
            $c.append(`<option>${c}</option>`);
        });
        $content.replaceWith($c);
        $content = $c;
    });
}


function saveWidgetTimeline(wd, $w) {
    var newTitle = $w.find("select.widget-title").val();
    var newContent = $w.find("select.widget-content").val();

    wd.option = newTitle;
    wd.content = newContent;

    $w.find("select.widget-title").replaceWith(
        `<h4 class="widget-header">${newTitle}</h4>`
    );
    $w.find("select.widget-content").remove();
    $w.find(".timelineChart").show();

    wd.option=newTitle; wd.content=newContent;
    localStorage.setItem("widgets",JSON.stringify(widgetData));

}


function buildWidgetTimelineGraphs(id, data, chart) {
    const curves = [...data.datasetExeTime].sort((a, b) => {
        const aKey = `${a.key.testcase.test}-${a.key.testcase.testcase}-${a.key.unit}-${a.key.party}-${a.key.type}`;
        const bKey = `${b.key.testcase.test}-${b.key.testcase.testcase}-${b.key.unit}-${b.key.party}-${b.key.type}`;
        return bKey.localeCompare(aKey);
    });

    const datasets = curves.map((curve, i) => {
        const points = curve.points.map(p => ({
            x: p.x,
            y: p.y,
            id: p.exe,
            controlStatus: p.exeControlStatus,
            falseNegative: p.falseNegative
        }));

        return {
            label: getLabel(
                curve.key.testcase.description,
                curve.key.country,
                curve.key.environment,
                curve.key.robotdecli,
                curve.key.unit,
                curve.key.party,
                curve.key.type,
                curve.key.testcase.testcase
            ),
            data: points,
            borderColor: getLineColorForTimeline(i),
            backgroundColor: getFillColorForTimeline(i),
            pointRadius: 3,
            pointHoverRadius: 6,
            hitRadius: 10,
            fill: true,
            pointBorderWidth: ctx => ctx.raw.falseNegative ? 3 : 1,
            pointBorderColor: ctx => ctx.raw.falseNegative ? '#00d27a' : getLineColorForTimeline(i),
            pointBackgroundColor: ctx => getExeStatusRowColor(ctx.raw.controlStatus)
        };
    });

    chart.data.datasets = datasets;
    chart.update();
}

function getLineColorForTimeline(index){
    const rootStyle = getComputedStyle(document.documentElement);
    const lineColor = rootStyle.getPropertyValue("--crb-green-color").trim();

    const colors = [lineColor];
    return colors[index % colors.length];
}

function getFillColorForTimeline(index){
    const rootStyle = getComputedStyle(document.documentElement);
    const fillColor = rootStyle.getPropertyValue("--crb-green-light-color").trim();

    const colors = [fillColor];
    return colors[index % colors.length];
}

function getLabel(tcDesc, country, env, robot, unit, party, type, testcaseid) {
    let label = tcDesc.length > 20 ? testcaseid : tcDesc;

    if (party && party !== "total") {
        label += ` - ${party}`;
    }
    if (type && type !== "total") {
        label += (label ? " - " : "") + type;
    }
    if (unit && ["totalsize", "sizemax", "totaltime", "timemax"].includes(unit)) {
        label += (label ? " [" : "[") + unit + "]";
    }

    return label;
}

function widgetTimeline(id, tc) {

    const chart = initTimelineGraph(id);
    let qS = "from=2025-07-30T22:00:00.000Z&to=2025-09-28T22:00:00.563Z&parties=total&types=total&units=request&units=totalsize&tests=QA - Games&testcases=" + tc;

    $.ajax({
        url: "ReadExecutionStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            if (data.messageType === "OK") {
                buildWidgetTimelineGraphs(id, data, chart);
            } else {
                showMessageMainPage(getAlertType(data.messageType), data.message, false);
            }
            hideLoader($("#otFilterPanel"));
        },
        error: showUnexpectedError
    });
}

function initTimelineGraph(id) {
    const ctx = document.getElementById(id + '-canvasTestStat').getContext('2d');

    const chart = new Chart(ctx, {
        type: 'line',
        data: { datasets: [] },
        options: getOptions("Test Case Duration", "time")
    });

    // gestion du clic sur un point
    document.getElementById(id + '-canvasTestStat').onclick = evt => {
        const points = chart.getElementsAtEventForMode(evt, 'nearest', { intersect: true }, false);
        if (points.length) {
            const { datasetIndex, index } = points[0];
            const exe = chart.data.datasets[datasetIndex].data[index].id;
            window.open('./TestCaseExecution.jsp?executionId=' + exe, '_blank');
        }
    };

    return chart;
}

function getOptions(title, unit) {
    const rootStyle = getComputedStyle(document.documentElement);
    const lineColor = rootStyle.getPropertyValue("--crb-green-color").trim();
    const fillColor = rootStyle.getPropertyValue("--crb-green-light-color").trim();

    return {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
            mode: 'nearest',
            intersect: true
        },
        plugins: {
            title: {
                display: false,
                text: title,
                color: '#1f2937',
                font: { size: 16, weight: 'bold' }
            },
            tooltip: {
                callbacks: {
                    label: context => {
                        const label = context.dataset.label || '';
                        const value = context.parsed.y;
                        if (unit === "size") {
                            return `${label}: ${formatNumber(Math.round(value / 1024))} kb`;
                        } else if (unit === "time") {
                            return `${label}: ${formatNumber(value)} ms`;
                        } else {
                            return `${label}: ${value}`;
                        }
                    }
                }
            },
            legend: {
                display: false,
                position: 'top',
                labels: {
                    usePointStyle: true,
                    color: '#374151'
                }
            }
        },
        scales: {
            x: {
                type: 'time',
                time: { tooltipFormat: 'dd MMM yyyy HH:mm' },
                title: { display: false, text: 'Date' },
                ticks: {
                    maxTicksLimit: 4,
                    font: { size: 10 }
                },
            },
            y: {
                title: { display: false, text: title },
                ticks: {
                    maxTicksLimit: 4,
                    font: { size: 10 },
                    callback: value => {
                        if (unit === "size") {
                            return formatNumber(Math.round(value / 1024));
                        } else if (unit === "time") {
                            return formatNumber(value);
                        } else {
                            return value;
                        }
                    }
                }
            }
        },
        elements: {
            line: {
                borderWidth: 2,
                tension: 0.4,
                borderColor: lineColor,
                backgroundColor: fillColor,
                fill: true
            },
            point: {
                radius: 3,
                hoverRadius: 6
            }
        }
    };
}

function formatNumber(num) {
    return num.toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, "$1,")
}