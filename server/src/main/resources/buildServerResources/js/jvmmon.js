/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

BS.JvmMon = {

    setBuildId: function (buildId) {
        this.buildId = buildId;
    },

    showJvmLog: function (jvmLog) {
        var url = base_uri + "/jvmmon.html";

        $j("#loadingLog").show();
        BS.ajaxRequest(url, {
            method: "GET",
            parameters: {
                buildId: this.buildId,
                logId: jvmLog
            },
            onComplete: function (xhr) {
                console.log(xhr);
                $j("#loadingLog").hide();

                var data = JSON.parse(xhr.responseText);
                $j('#jvm-cmdline').text(data.info.cmdline);
                $j('#jvm-args').text(data.info.jvmargs);
                $j('#jvm-version').text(data.info.jvmversion);
                $j('.jvm-info').show();
                BS.JvmMon.updateChart(data)
            }
        });
    },

    updateChart: function (chartData) {
        console.log('chart data:', chartData);

        var chartColors = {
            red: 'rgb(255, 99, 132)',
            orange: 'rgb(255, 159, 64)',
            yellow: 'rgb(255, 205, 86)',
            green: 'rgb(75, 192, 192)',
            blue: 'rgb(54, 162, 235)',
            purple: 'rgb(153, 102, 255)',
            grey: 'rgb(201, 203, 207)'
        };

        $j('#charts').empty();
        BS.JvmMon.createGraph('Eden space', {
            labels: chartData.datasets.timestamp,
            datasets: [{
                label: 'Utilization',
                backgroundColor: chartColors.red,
                data: chartData.datasets['EU'],
                fill: false
            },
            {
                label: 'Capacity',
                backgroundColor: chartColors.blue,
                data: chartData.datasets['EC'],
                fill: false
            }]
        });

        BS.JvmMon.createGraph('Survivor spaces', {
            labels: chartData.datasets.timestamp,
            datasets: [{
                label: 'Survivor space 0 utilization',
                backgroundColor: chartColors.red,
                data: chartData.datasets['S0U'],
                fill: false
            },
            {
                label: 'Survivor space 0 capacity',
                backgroundColor: chartColors.orange,
                data: chartData.datasets['S0C'],
                fill: false
            },
            {
                label: 'Survivor space 1 utilization',
                backgroundColor: chartColors.yellow,
                data: chartData.datasets['S1U'],
                fill: false
            },
            {
                label: 'Survivor space 1 capacity',
                backgroundColor: chartColors.green,
                data: chartData.datasets['S1C'],
                fill: false
            }]
        });

        BS.JvmMon.createGraph('Old Space', {
            labels: chartData.datasets.timestamp,
            datasets: [{
                label: 'Utilization',
                backgroundColor: chartColors.red,
                data: chartData.datasets['OU'],
                fill: false
            },
            {
                label: 'Capacity',
                backgroundColor: chartColors.orange,
                data: chartData.datasets['OC'],
                fill: false
            }]
        });

        if (chartData.datasets['PU'] && chartData.datasets['PC']) {
            BS.JvmMon.createGraph('Permanent space', {
                labels: chartData.datasets.timestamp,
                datasets: [{
                    label: 'Utilization',
                    backgroundColor: chartColors.red,
                    data: chartData.datasets['PU'],
                    fill: false
                },
                {
                    label: 'Capacity',
                    backgroundColor: chartColors.orange,
                    data: chartData.datasets['PC'],
                    fill: false
                }]
            });
        }

        if (chartData.datasets['MU'] && chartData.datasets['MC']) {
            BS.JvmMon.createGraph('Metaspace', {
                labels: chartData.datasets.timestamp,
                datasets: [{
                    label: 'Utilization',
                    backgroundColor: chartColors.red,
                    data: chartData.datasets['MU'],
                    fill: false
                },
                {
                    label: 'Capacity',
                    backgroundColor: chartColors.orange,
                    data: chartData.datasets['MC'],
                    fill: false
                }]
            });
        }

        if (chartData.datasets['CCSU'] && chartData.datasets['CCSC']) {
            BS.JvmMon.createGraph('Compressed Class Space', {
                labels: chartData.datasets.timestamp,
                datasets: [{
                    label: 'Used',
                    backgroundColor: chartColors.red,
                    data: chartData.datasets['CCSU'],
                    fill: false
                },
                {
                    label: 'Capacity',
                    backgroundColor: chartColors.orange,
                    data: chartData.datasets['CCSC'],
                    fill: false
                }]
            });
        }
    },

    createGraph: function(title, data) {
        var config = {
            type: 'line',
            options: {
                title: {
                    display: true,
                    text: title
                },
                tooltips: {
                    mode: 'index',
                    intersect: false
                },
                elements: {
                    line: {
                        tension: 0 // disables bezier curves
                    }
                },
                responsive: true,
                scales: {
                    xAxes: [{
                        type: 'time',
                        distribution: 'series',
                        time: {
                            displayFormats: {
                                second: 'hh:mm:ss'
                            },
                            unit: 'second'
                        },
                        scaleLabel: {
                            display: true,
                            labelString: 'Time'
                        }
                    }],
                    yAxes: [{
                        scaleLabel: {
                            display: true,
                            labelString: 'Memory (bytes)'
                        }
                    }]
                }
            },
            data: data
        };
        var charts = $j('#charts');
        var canvas = $j('<canvas></canvas>').appendTo(charts);
        var ctx = canvas[0].getContext('2d');
        var chart = new Chart(ctx, config);
    }
};
