/* global remoteAction, $j */
"use strict";
var dataArray = [];
remoteAction.getResult($j.proxy(function (data) {
    if (data) {
        try {
            var resultData = JSON.parse(data.responseObject());
            resultData.forEach(function (item) {
                var dateKey = +new Date();
                //  console.log(item);
                var temp = item;
                temp.DT_RowId = dateKey;
                dataArray.push(temp);
            });
        } catch (e) {
            //console.log(e);
        }
    }
}, this));
$j(document).ready(function () {
    var editor;
    editor = new $j.fn.dataTable.Editor({
        table: "#iceResultTable",
        fields: [
            {
                label: "Type:",
                name: "type"
            }, {
                label: "Name:",
                name: "name"
            }, {
                label: "Code:",
                name: "code"
            }, {
                label: "Exit Code:",
                name: "exitcode"
            },
            {
                label: "Reason Code:",
                name: "reasonCode"
            },
            {
                label: "Status:",
                name: "status"
            }
        ]
    });
    setTimeout(function () {
        var iceResultTable = $j('#iceResultTable').DataTable({
            "dom": 'Bfrtip',
            "scrollY": "600px",
            "scrollCollapse": true,
            "lengthChange": false,
            "ordering": false,
            "info": true,
            stateSave: true,
            data: dataArray,
            columns: [
                {data: "type", defaultContent: "-"},
                {data: "name", defaultContent: "-"},
                {data: "code", defaultContent: "-"},
                {data: "exitcode", defaultContent: "-"},
                {data: "reasonCode", defaultContent: "-"},
                {data: "status", defaultContent: "-"},
                {
                    "className": 'details-control',
                    "orderable": false,
                    "data": null,
                    "defaultContent": '',
                    "visible": false
                }
            ],
            buttons: [
                {
                    extend: 'collection',
                    text: 'Export',
                    buttons: [
                        'copy',
                        'excel',
                        'csv',
                        'print'
                    ]
                }
            ],
            language: {
                "search": "",
                "searchPlaceholder": "Search...",
                "info": "**Click the records for more Details"
            }
        });
        $j('#iceResultTable tbody').on('click', 'tr.details-control', function () {
            $j(this).toggleClass('selected');
        });

        /*Add event listener for opening and closing details */
        $j('#iceResultTable tbody').on('click', 'td', function (event) {
            event.stopImmediatePropagation();
            // to avoid to receive the button click inside the td you need:
            if ($j(event.target).is(':not(td)')) {
                return;
            }
            var tr = $j(this).closest('tr');
            var id = $j(this).closest('table').attr('id');
            var table = $j('#' + id).DataTable();
            var row = table.row(tr);
            if (row.child.isShown()) {
                // This row is already open - close it
                row.child.hide();
                tr.removeClass('shown');
            } else {
                try {
                    row.child(formatServerConnData(row.data(), id)).show();
                    tr.addClass('shown');
                } catch (e) {
                }
            }
        });
        $j('#iceResultTable tbody').on('click', 'td >a', function (e) {
            e.preventDefault();
            try {
                $j(this).parent().find('.testSetDiv').toggle();
                return false;
            } catch (e) {

            }
        });
    }, 1000);
    /* Formatting function for row details - modify as you need */
    function formatServerConnData(data, id) {
        try {

            if (data.type == 'Rule') {
//            console.log("I AM RULE");
                var riid = (data.riid != null) ? data.riid : "-";
                var code = (data.code != null) ? data.code : "-";
                var srcCount = (data.srccount != null) ? data.srccount : "-";
                var trgCount = (data.trgcount != null) ? data.trgcount : "-";
                var successFlag = (data.successflag != null) ? data.successflag : "-";
                var failureFlag = (data.failureflag != null) ? data.failureflag : "-";
                return '<table cellpadding="5" cellspacing="0" border="1" style="padding-left:0px;">' + //width:50% !important"
                        '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Rule Details:</small></Strong></caption>' +
                        '<tr>' +
                        '<td align="left"><small><Strong>Rule Instance Id</Strong></small></td>' +
                        '<td align="left"><small><Strong>Code</Strong></small></td>' +
                        '<td align="left"><small><Strong>Src Count</Strong></small></td>' +
                        '<td align="left"><small><Strong>Trg Count</Strong></small></td>' +
                        '<td align="left"><small><Strong>Success Flag</Strong></small></td>' +
                        '<td align="left"><small><Strong>Failure  Flag</Strong></small></td>' +
                        '</tr>' +
                        '<tr>' +
                        '<td align="left"><small>' + riid + '</small></td>' +
                        '<td align="left"><small>' + code + '</small></td>' +
                        '<td align="left"><small>' + srcCount + '</small></td>' +
                        '<td align="left"><small>' + trgCount + '</small></td>' +
                        '<td align="left"><small>' + successFlag + '</small></td>' +
                        '<td align="left"><small>' + failureFlag + '</small></td>' +
                        '</tr>' +
                        '</table>';
            } else if (data.type == 'Batch') {
//            console.log("I AM BATCH");
                var seqString = '';
                var seq = [];
                seq = data.seq;
                seq.forEach(function (item) {
                    var seqResult = '';
                    seqResult =
                            '<tr>' +
                            '<td align="left"><small>' + item.name + '</small></td>' +
                            '<td align="left" ><small>' + item.id + '</small></td>' +
                            '<td align="left" ><small>' + item.content + '</small></td>' +
                            '</tr>';
                    seqString += seqResult;
                });
                return '<table cellpadding="5" cellspacing="0" border="1" style="padding-left:0px;">' + //width:50% !important"
                        '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Batch Details:</small></Strong></caption>' +
                        '<tr>' +
                        '<td align="left" ><small><Strong>Sequence Name</Strong></small></td>' +
                        '<td align="left" ><small><Strong>Sequence Id</Strong></small></td>' +
                        '<td align="left" ><small><Strong>Sequence content</Strong></small></td>' +
                        '</tr>' +
                        seqString +
                        '</table>';
            } else if (data.type == 'Test Set Folder') {
                //   console.log("I AM Test Set Folder");
                var testSetsString = '';
                var testSetsArr = [];
                testSetsArr = data.testset;
                testSetsArr.forEach(function (item) {
                    var testSetResult = '';
//                console.log("item******************************************************");
//                console.log(item.id);
                    var testString = '';
                    var testArr = [];
                    testArr = item.test;
                    testArr.forEach(function (test) {
//                    console.log("test######################################");
//                    console.log(test.name);
                        var testData = '';
                        testData =
                                '<tr>' +
                                '<td align="left"><small>' + test.name + '</small></td>' +
                                '<td align="left" ><small>' + test.id + '</small></td>' +
                                '<td align="left" ><small>' + test.content + '</small></td>' +
                                '</tr>';
                        testString += testData;

                    })
//               console.log("testString:: "+testString);
                    testSetResult =
                            '<tr>' +
                            '<td align="left"><small>' + item.name + '</small></td>' +
                            '<td align="left" ><small>' + item.id + '</small></td>' +
                            '<td align="left" ><small>' + item.exitcode + '</small></td>' +
                            '<td align="left" ><small>' + item.status + '</small></td>' +
                            '<td align="left"  ><a class="details-infoBtn">&nbsp &nbsp  &nbsp</a>' +
                            '<div  class="testSetDiv" style="display:none">' +
                            '<table  cellpadding="5" cellspacing="0" border="1">' +
                            '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Test Details:</small></Strong></caption>' +
                            '<tr>' +
                            '<td align="left" ><small><Strong>Name</Strong></small></td>' +
                            '<td align="left" ><small><Strong>Id</Strong></small></td>' +
                            '<td align="left" ><small><Strong>Content</Strong></small></td>' +
                            '</tr>' +
                            testString +
                            '</table>' +
                            '</div>'
                            + '</td>' +
                            '</tr>';
                    testSetsString += testSetResult;
                });
//            console.log("testSetsString:: ");
                return '<table id="testSetsTable" cellpadding="5" cellspacing="0" border="1" style="padding-left:0px;">' + //width:50% !important"
                        '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Test Set Folder Details:</small></Strong></caption>' +
                        '<tr>' +
                        '<td align="left" ><small><Strong>Name</Strong></small></td>' +
                        '<td align="left" ><small><Strong>Id</Strong></small></td>' +
                        '<td align="left" ><small><Strong>Exit code</Strong></small></td>' +
                        '<td align="left" ><small><Strong>status</Strong></small></td>' +
                        '<td align="left" ><small><Strong></Strong></small></td>' +
                        '</tr>' +
                        testSetsString +
                        '</table>';

            } else {
                return '<table cellpadding="5" cellspacing="0" border="1" style="padding-left:0px;">' +
                        '<tr>' +
                        '<td align="left" ><small>No Details to show.</small></td>' +
                        '</tr>' +
                        '</table>';
            }
        } catch (e) {

        }
    }
    ;
});

