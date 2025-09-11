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

// widgetAvailabilityOptions : Options available
// WidgetAvailabilityTemplate : Template HTML
// editWidgetCount : Edit Widget
// getData : Retrieve data from Back


var widgetAvailabilityOptions={
    "ExecutionStatus":{label:"Execution Status"},
    "ExecutionTime":{label:"Execution Time"}
};

//configAvailability2.data.labels = ["OK duration (s)", "Others duration (s)"];
//document.getElementById('ChartAvailabilty2Counter').innerHTML = Math.round(durOK / (durOK + durKO) * 100) + " %";
//document.getElementById('ChartAvailabilty2CounterDet').innerHTML = "<b style='color:lightgrey'>" + getHumanReadableDuration(durKO) + "</b> / " + getHumanReadableDuration((durOK + durKO));

function WidgetAvailabilityTemplate(w) {

    return `
        <div class="widget" data-id="${w.id}">
          <div class="drag-handle drag-widget">⋮⋮⋮</div>
          <div class="widget-controls">
            <button class="btn btn-xs btn-info edit-widget">Edit</button>
            <button class="btn btn-xs btn-danger delete-widget">&times;</button>
          </div>
          <h4 class="widget-header" style="margin-bottom:0">TESTCASE AVAILABILITY</h4>
          <p class="widget-content">${w.content.label}</p>
          <div class="">
                <div class="availabiltyChart" id="${w.id}-widget-availability">
                    <div class="row">
                        <div class="col-xs-8 paddingRight0" id="${w.id}-ChartAvailabilty1" >
                            <canvas width="300px" height="150px" id="${w.id}-canvasAvailability1"></canvas>
                        </div>
                        <div class="col-xs-4 paddingLeft0"  >
                            <h2 class="statistic-counter" id="${w.id}-ChartAvailabilty1Counter"></h2>
                            <p class="statistic-counter" id="${w.id}-ChartAvailabilty1CounterDet"></p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
      `;

}


function editWidgetAvailability(wd, $w) {
    // Cacher le graphique
    $w.find(".availabiltyChart").hide();
    $w.find(".widget-content").hide();

    // Construire la combo du titre
    var $title = $('<select class="form-control input-sm widget-title"></select>');
    Object.keys(widgetAvailabilityOptions).forEach(function(t){
        $title.append(`<option ${t==wd.title?"selected":""}>${t}</option>`);
    });

    // Créer les champs test et testcase
    var $test = $('<select class="form-control input-sm widget-test"></select>');
    var $testcase = $('<select class="form-control input-sm widget-testcase"></select>');

    // Fonction pour remplir test depuis le backend
    function loadTest() {
        $test.empty().append('<option>Chargement...</option>');
        fetch(`./ReadTest`)
            .then(res => res.json())
            .then(data => {
                $test.empty();
                data.contentTable.forEach(t => {
                    $test.append(`<option ${t.test == wd.content.test ? "selected" : ""}>${t.test}</option>`);
                });
                // Charger testcase pour la valeur sélectionnée
                loadTestCase($test.val());
            });
    }

    // Fonction pour remplir testcase depuis le backend
    function loadTestCase(testVal) {
        $testcase.empty().append('<option>Chargement...</option>');
        fetch(`./ReadTestCase?test=${encodeURIComponent(testVal)}`)
            .then(res => res.json())
            .then(data => {
                $testcase.empty();
                data.contentTable.forEach(tc => {
                    $testcase.append(`<option ${tc.testcase == wd.content.testcase ? "selected" : ""}>${tc.testcase}</option>`);
                });
            });
    }

    // Remplacer le titre (h4) par une combo
    $w.find(".widget-header").replaceWith($title);

    // Insérer test et testcase après le titre
    $title.after($test);
    $test.after($testcase);

    // Initialiser test et testcase
    loadTest();

    // Gérer le changement du titre -> recharge test
    //$title.change(function(){
    //    loadTest();
    //});

    // Gérer le changement de test -> recharge testcase
    $test.change(function(){
        loadTestCase($(this).val());
    });
}


function saveWidgetAvailability(wd, $w) {
    var newTitle = $w.find("select.widget-title").val();
    var newTest = $w.find("select.widget-test").val();
    var newTestcase = $w.find("select.widget-testcase").val();

    // Construire le nouvel objet content
    wd.title = newTitle;
    wd.content = {
        label: newTestcase, // affichage principal
        test: newTest,
        testcase: newTestcase
    };

    // Remplacer le select du titre par un h4 affichant le label (= testcase)
    $w.find("select.widget-title").replaceWith(
        `<h4 class="widget-header">${wd.content.label}</h4>`
    );

    // Supprimer les selects test et testcase
    $w.find("select.widget-test").remove();
    $w.find("select.widget-testcase").remove();

    // Réafficher le graphique et le contenu
    $w.find(".availabiltyChart").show();
    $w.find(".widget-content").show();

    // Sauvegarde dans le localStorage
    localStorage.setItem("widgets", JSON.stringify(widgetData));
}

function buildWidgetAvailabilityGraphs(id, data, chart) {
    const curves = data.datasetExeTime;

    let nbOK = 0, nbKO = 0;
    let durOK = 0, durKO = 0;

    curves.forEach(curve => {
        curve.points.forEach((point, j) => {
            const nextPoint = curve.points[j + 1];
            const dur = nextPoint ? (new Date(nextPoint.x) - new Date(point.x)) / 1000 : 0;

            if (point.exeControlStatus === "OK" || point.falseNegative) {
                nbOK++;
                durOK += dur;
            } else {
                nbKO++;
                durKO += dur;
            }
        });
    });

    chart.data.datasets = [{
        data: [nbOK, nbKO],
        backgroundColor: [
            getExeStatusRowColor("OK"),
            getExeStatusRowColor("OTHERS")
        ],
        borderWidth: 2,
        hoverOffset: 6
    }];
    chart.data.labels = ["nb OK", "nb Others"];

    document.getElementById(id + '-ChartAvailabilty1Counter').innerHTML =
        Math.round(nbOK / (nbOK + nbKO) * 100) + " %";
    document.getElementById(id + '-ChartAvailabilty1CounterDet').innerHTML =
        `<b style='color:lightgrey'>${nbKO}</b> / ${nbOK + nbKO}`;

    chart.update();
}

function widgetAvailability(id, tc) {
    const chart = initGraph(id);

    const qS =
        "from=2025-07-30T22:00:00.000Z&to=2025-09-28T22:00:00.563Z&tests="+tc.test+"&testcases=" + tc.testcase;

    $.ajax({
        url: "ReadExecutionStat?" + qS,
        method: "GET",
        async: true,
        dataType: 'json',
        success: function (data) {
            if (data.messageType === "OK") {
                buildWidgetAvailabilityGraphs(id, data, chart);
            } else {
                showMessageMainPage(getAlertType(data.messageType), data.message, false);
            }
            hideLoader($("#otFilterPanel"));
        },
        error: showUnexpectedError
    });
}

function initGraph(id) {
    const ctx = document.getElementById(id + '-canvasAvailability1').getContext('2d');

    const chart = new Chart(ctx, {
        type: 'doughnut', // ou 'pie'
        data: {
            labels: ["nb OK", "nb Others"],
            datasets: [{
                data: [1, 1],
                backgroundColor: [
                    getExeStatusRowColor("OK"),
                    getExeStatusRowColor("OTHERS")
                ],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            rotation: -90,
            circumference: 180 ,
            cutout: '30%',
            plugins: {
                title: {
                    display: false,
                    text: "Execution Availability (Nb)",
                    color: '#1f2937',
                    font: { size: 16, weight: 'bold' }
                },
                legend: {
                    display: false,
                    position: 'bottom',
                    labels: {
                        color: '#374151',
                        usePointStyle: true,
                        pointStyle: 'circle'
                    }
                },
                tooltip: {
                    callbacks: {
                        label: context => {
                            const label = context.label || '';
                            const value = context.parsed;
                            return `${label}: ${value}`;
                        }
                    }
                }
            }
        }
    });

    window[id + "-myAvailability1"] = chart;
    return chart;
}
