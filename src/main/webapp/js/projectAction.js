/* global remoteAction, $j */
"use strict";
var icedq = (function ($j) {
    var module = {};
    var resultData = {};
    $j(document).ready(function () {
        setTimeout(function () {
            icedq.initIceResultTable();
        }, 2000);
    });
    remoteAction.getProjectResultJSON($j.proxy(function (data) {
        if (data) {
            try {
                resultData = data.responseObject();
            } catch (e) {
                console.log(e);
            }
        }
    }, this));
    module.initIceResultTable = function () {
    var   resultTableRef = $j('#iceResultTable').dataTable({
            dom: "Bfrtip",
            "scrollY": true,
            "scrollX": true,
            "scrollCollapse": true,
            "lengthChange": false,
            "ordering": true,
            "info": false,
            "responsive": true,
            "deferRender": true,
            "deferLoading": 15,
            data: resultData,
            "order": [[ 0, 'desc' ]],
            columns: [
                {data: "buildNum"},
                {data: "buildStatus"},
                {data: null},
                {data: "response"}
            ],
            buttons: [],
            "columnDefs": [
                 {
                    "targets": [1,2],
                    "orderable": false
                },
//                {
//                    "targets": [2],
//                    "className": 'details-control',
//                    "orderable": false,
//                    "data": null,
//                    "searchable": false,
//                    "defaultContent": ''
//                },
                {
                    "targets": [3],
                    "visible": false,
                    "searchable": false,
                    "render": function (data, type, row) {
                        return data.length;
                    }
                }
            ],
            language: {
                "search": " ",
                "searchPlaceholder": "Search...",
                "emptyTable": "No data "
            }
        });
        $j('#iceResultTable tbody').on('click', 'td.details-control', function () {
           // alert("I am clicked");
            var tr = $j(this).closest('tr');
            var row = resultTableRef.row(tr);
            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            } else {
                // Open this row
                row.child(formatServerConnData(row.data())).show();
                tr.addClass('shown');
            }
        });
        /* Formatting function for row details - modify as you need */
        function formatServerConnData(data) {
            // `d` is the original data object for the row
            console.log(data);
            return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' + //width:50% !important"
                    '<tr>' +
                    '<td><small>Connection Url:</small></td>' +
                    '<td><small>' + "" + '</small></td>' +
                    '</tr>' +
                    '<tr>' +
                    '<tr>' +
                    '<td><small>password:</small></td>' +
                    '<td><small>' + "" + '</small></td>' +
                    '</tr>' +
                    '</table>';
        }
    };
    return module;
}($j));