/* global icedq, $j, repoNameSelectize, remoteAction, data, iceExeData */
"use strict";
icedq.init();
var SearchFlag = false;
var serverConnTableRef;
var seachResultTableRef;
var executeRuleTableRef;
var counter = 1;
var outputIceExeData = {data: []};
$j(document).ready(function () {
    setTimeout(function () {
        initIceExecTable();
        serverConnTableRef = icedq.initServerConnDataTable();
        seachResultTableRef = icedq.initSearchResultTable();
        addRemoveRuleFrmExecuteTable();
        searchRules();
        addExecuteDataJson();
        $j("#iceSearchResultTableDiv").hide();
        $j("#sTableProgressBarDiv").hide();
        $j("#clearRuleBtn").attr("disabled", true);
        if (outputIceExeData.data.length > 0) {
            counter = outputIceExeData.data.length + 1;
        }
    }, 1000);
});
function initIceExecTable() {
    $j.fn.dataTable.ext.order['dom-text-numeric'] = function (settings, col)
    {
        return this.api().column(col, {order: 'index'}).nodes().map(function (td, i) {
            return $j('input', td).val() * 1;
        });
    };
    var editor;
    editor = new $j.fn.dataTable.Editor({
        table: "#iceExecuteRuleTable",
        fields: [
            {label: "Code:", name: "code"},
            {label: "Name:", name: "name"},
            {label: "Order", name: "order", type: "text"},
            {label: "Parameter:", name: "parameter"}
        ],
        ajax: function (method, url, d, successCallback, errorCallback) {
            if (d.action === 'edit') {
                $j.each(d.data, function (id, value) {
                    for (var i = 0; i < outputIceExeData.data.length; i++) {
                        var dtId = outputIceExeData.data[i].DT_RowId;
                        if (dtId == id) {
                            if ((value.order.match(/^\d+$/)) == null) {
                                errorCallback(outputIceExeData);
                            } else {
                                var temp = outputIceExeData.data[i];
                                temp.parameter = value.parameter;
                                temp.order = value.order;
                            }
                        }
                    }
                });
            }
            // Show Editor what has changed
            successCallback(outputIceExeData);
        }
    });

    executeRuleTableRef = $j("#iceExecuteRuleTable").DataTable({
        dom: "Bfrtip",
        "scrollY": "260px",
        "scrollX": true,
        responsive: true,
        "scrollCollapse": true,
        "paging": false,
        "lengthChange": true,
        "searching": false,
        "order": [[0, 'asc']],
        "info": true,
        "autoWidth": true,
        data: $j.map(outputIceExeData.data, function (value, key) {
            try {
                return value;
            } catch (e) {
            }
        }),
        language: {
            "search": "",
            "searchPlaceholder": "Search...",
            "emptyTable": "Nothing to Execute",
            "info": "**Click on name to select record/s."
        },
        "columnDefs": [
            {
                "targets": [7],
                "visible": false,
                "searchable": false
            },
            {orderable: false, targets: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]},
            {
                targets: [6],
                render: $j.fn.dataTable.render.ellipsis(30, true)
            },
            {
                targets: [1,2,3,5, 11],
                render: $j.fn.dataTable.render.ellipsis(10, true)
            }
        ],
        "columns": [
            {
                data: "order",
                orderDataType: "dom-text-numeric",
                type: "text",
                className: 'inlineEdit',
                render: function (data, type, row) {
                    return "<input  type='text'  name='order' value='" + data + "' id='order' style='width:30px' >";
                }
            },
            {data: "serverName", orderable: false},
            {data: "projectName", orderable: false},
            {data: "folderName", orderable: false},
            {data: "type", orderable: false},
            {data: "code", orderable: false},
            {data: "name", orderable: false, "width": "25%", className: 'selectExecRow'},
            {data: "legalEntityId", orderable: false},
            {
                data: "OnSuccess", orderable: false,
                render: function (data, type, row) {
                    if (data == "Fail") {
                        return "<select class='onSuccess' id='onSuccess'><option value='Pass' >Pass</option><option value='Fail' selected>Fail</option></select>";
                    } else {
                        return "<select class='onSuccess' id='onSuccess'><option value='Pass'>Pass</option><option value='Fail'>Fail</option></select>";
                    }
                }
            },
            {
                data: "onFailure", orderable: false,
                render: function (data, type, row) {
                    if (data == "Fail") {
                        return "<select class='onFailure' id='onSuccess'><option value='Pass'>Pass</option><option value='Fail' selected>Fail</option></select>";
                    } else {
                        return "<select class='onFailure' id='onSuccess'><option value='Pass'>Pass</option><option value='Fail'>Fail</option></select>";
                    }
                }
            },
            {
                data: "onError", orderable: false,
                render: function (data, type, row) {
                    if (data == "Fail") {
                        return  "<select class='onError' id='onSuccess'><option value='Pass'>Pass</option><option value='Fail' selected>Fail</option></select>";
                    } else {
                        return "<select class='onError' id='onSuccess'><option value='Pass'>Pass</option><option value='Fail'>Fail</option></select>";
                    }
                }
            },
            {data: "parameter", orderable: false, className: 'editable', defaultContent: ""},
            {data: null, orderable: false, defaultContent: "", class: "removeObj-control", "width": "4%"}
        ],
        buttons: []
    });
    $j('#iceExecuteRuleTable').on('click', 'td.editable', function (e) {
        e.stopImmediatePropagation();
        editor.disable('name');
        editor.disable('code');
        editor.field('order').hide();
        editor.edit($j(this).closest('tr'), {
            buttons: 'Update',
            submit: 'allIfChanged'
        });
    });
    $j('#iceExecuteRuleTable').on('click', 'td.inlineEdit', function (e) {
        e.stopImmediatePropagation();
        editor.field('order').show();
        editor.inline(this, {
            onBlur: 'submit',
            submit: 'allIfChanged'
        });
    });
    //click on the name of records to remove it frm execution 
    $j('#iceExecuteRuleTable tbody').on('click', '.selectExecRow', function () {
        $j(this).parents('tr').toggleClass('selected');
        var tableData = executeRuleTableRef.rows('.selected').data();
        var dataArray = tableData.toArray();
        if (dataArray.length > 1) {
            $j("#clearRuleBtn").removeAttr("disabled");
        } else {
            $j("#clearRuleBtn").attr("disabled", true);
        }
    });
}
function searchRules() {
    $j("#searchRulesBtn").click(function (event) {
        // alert("call to Start");
        event.preventDefault();
        SearchFlag = false;
        $j("#searchRulesBtn").attr("disabled", true);
        $j("#sTableProgressBarDiv").show();
        $j("#noSearchResultDiv").hide();
        seachResultTableRef.clear().draw();  //clear the table to show the new search result
        var objType = icedq.getObjTypeValue();
        var objCode = '';
        var objName = $j("#objName").val();
        var token = $j("#token").val();
        var data = serverConnTableRef.row('.selected').data();
        var stringData = JSON.stringify(data);
        //  alert("call to Start"+stringData)
        if (data !== undefined) {
            var connData = JSON.parse(stringData);
            var url = connData.connectionUrl;
            var repoName = connData.repository;
            var username = connData.user;
            var password = connData.password;


            var serverConnName = connData.serverConnectionName;


            if (objType !== '' && objType.length > 0) {
                if (objName !== '' || objName.length > 0) {

                    if (token !== '' && token.length > 0)
                    {
                        if (token === password)
                        {
                            password = token;
                            icedq.searchRuleObjects(url, repoName, username, password, objType, objCode, objName, serverConnName);
                        } else {
                            $j("#sTableProgressBarDiv").hide();
                            $j("#searchRulesBtn").removeAttr("disabled");
                            swal({
                                title: "Update Token !",
                                text: "You need to Update Token in Connection.",
                                timer: 2000,
                                showConfirmButton: false
                            });
                        }
                    } else {
                        $j("#sTableProgressBarDiv").hide();
                        $j("#searchRulesBtn").removeAttr("disabled");
                        swal({
                            title: "No Token !",
                            text: "Please Provide Authentication Token.",
                            timer: 2000,
                            showConfirmButton: false
                        });

                    }


                } else {
                    $j("#sTableProgressBarDiv").hide();
                    $j("#searchRulesBtn").removeAttr("disabled");
                    swal({
                        title: "No Search Keyword!",
                        text: "You need to provide search keyword.",
                        timer: 2000,
                        showConfirmButton: false
                    });
                }
            } else {
                $j("#sTableProgressBarDiv").hide();
                $j("#searchRulesBtn").removeAttr("disabled");
                swal({
                    title: "No Object Type!",
                    text: "You need to provide Object Type.",
                    timer: 2000,
                    showConfirmButton: false
                });
            }
        } else {
            $j("#sTableProgressBarDiv").hide();
            $j("#searchRulesBtn").removeAttr("disabled");
            swal({
                title: "No server selected!",
                text: "You need to select server.",
                timer: 2000,
                showConfirmButton: false
            });
        }
    });

    $j("#abortSearchBtn").click(function (event) {
        event.preventDefault();
        SearchFlag = true;
        $j("#sTableProgressBarDiv").hide();
        remoteAction.abortIceObjSearch("true");
        seachResultTableRef.clear().draw();
        $j("#searchRulesBtn").removeAttr("disabled");
        $j("#noSearchResultDiv").hide();
    });
}
function addRemoveRuleFrmExecuteTable() {
    $j('#iceSearchResultTable tbody').on('click', '.addToExec-control', function () {
        var executeTableArray = [];
        var executeTableData;
        if ($j(this).closest('tr').hasClass('selected')) {
        } else {
            seachResultTableRef.$('tr.selected').removeClass('selected');
            $j(this).closest('tr').addClass('selected');
            executeTableData = seachResultTableRef.rows('.selected').data();
            seachResultTableRef.row('.selected').remove().draw(false);
        }
        var executeObj = JSON.stringify(executeTableData.toArray()).slice(1, -1).replace(/\\/g, "");
        executeTableArray = JSON.parse(executeObj);
        executeTableArray.forEach(function (item) {
            var iceExeData = {
                "order": counter,
                "DT_RowId": item.DT_RowId,
                "serverName": item.serverConnName,
                "projectName": item.PROJECT_NAME,
                "folderName": item.FOLDER_NAME,
                "type": item.FLAG_TYPE,
                "code": getCode(item),
                "name": item.NAME,
                "legalEntityId": item.LEGAL_ENTITY_NAME,
                "parameter": ""
            };
            outputIceExeData.data.push(iceExeData);
            executeRuleTableRef.row.add(iceExeData).draw(false);
            counter++;
        });
    });
    //clear button event 
    $j("#clearRuleBtn").click(function (event) {
        event.preventDefault();
        var dataArray = [];
        var tableData = executeRuleTableRef.rows().data();
        dataArray = tableData.toArray();
        if (dataArray.length > 0) {
            var selectedData = executeRuleTableRef.rows('.selected').data();
            if (selectedData.length > 0) {
                swal({
                    title: "Confirm Delete?",
                    text: "Would you like to delete selected records?",
                    type: "warning",
                    showCancelButton: true,
                    cancelButtonText: "No",
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes",
                    closeOnConfirm: false
                },
                        function () {
                            executeRuleTableRef.rows('.selected').remove().draw(false);
                            var tableData = executeRuleTableRef.rows().data();
                            dataArray = tableData.toArray();
                            var successFailError = [];
                            $j('#iceExecuteRuleTable tr').each(function (i, row) {
                                var rowData;
                                var success, failure, error;
                                $j(row).find('select').each(function (j, data) {
                                    var selectClass = $j(data).attr('class');
                                    var selectValue = data.options[data.selectedIndex].value;
                                    if (selectClass === "onSuccess") {
                                        success = selectValue;
                                    } else if (selectClass === "onFailure") {
                                        failure = selectValue;
                                    } else {
                                        error = selectValue;
                                    }
                                });
                                rowData = {
                                    "onSuccess": success,
                                    "onFailure": failure,
                                    "onError": error
                                };
                                successFailError.push(rowData);
                            });
                            try {
                                var count = 1;
                                dataArray.forEach(function (item) {
                                    item.url = getServerUrl(item.serverName);
                                    item.uname = getUserName(item.serverName);
                                    item.repoName = getRepoName(item.serverName);
                                    item.OnSuccess = successFailError[count].onSuccess;
                                    item.onFailure = successFailError[count].onFailure;
                                    item.onError = successFailError[count].onError;

                                    count++;
                                });

                                outputIceExeData.data = dataArray;
                                icedq.writeExecuteTableDataFile(JSON.stringify(dataArray));
                            } catch (e) {
                                //console.log(e);
                                swal({
                                    title: "Oops...",
                                    type: "error",
                                    text: "Something went wrong!",
                                    timer: 2000,
                                    showConfirmButton: false
                                });
                            }
                        });
            }
        } else {
            swal({
                title: "No records to delete!",
                text: "You can add new records from search result.",
                timer: 2000,
                showConfirmButton: false
            });
        }
    });
    //remove icon event for each row 
    $j('#iceExecuteRuleTable tbody').on('click', '.removeObj-control', function (event) {
        event.preventDefault();
        //        console.log("remove icon event");
        executeRuleTableRef.$('tr.selected').removeClass('selected');
        if ($j(this).closest('tr').hasClass('selected')) {
            $j(this).closest('tr').removeClass('selected');
        } else {
            executeRuleTableRef.$('tr.selected').removeClass('selected');
            $j(this).closest('tr').addClass('selected');
        }
        swal({
            title: "Confirm Delete?",
            text: "Would you like to delete selected records?",
            type: "warning",
            showCancelButton: true,
            cancelButtonText: "No",
            confirmButtonColor: "#DD6B55",
            confirmButtonText: "Yes",
            closeOnConfirm: false
        },
                function (isConfirm) {
                    //    console.log(isConfirm);
                    if (isConfirm) {
                        executeRuleTableRef.rows('.selected').remove().draw(false);
                        var tableData = executeRuleTableRef.rows().data();
                        var dataArray = tableData.toArray();
                        var successFailError = [];
                        $j('#iceExecuteRuleTable tr').each(function (i, row) {
                            var rowData;
                            var success, failure, error;
                            $j(row).find('select').each(function (j, data) {
                                var selectClass = $j(data).attr('class');
                                var selectValue = data.options[data.selectedIndex].value;
                                if (selectClass === "onSuccess") {
                                    success = selectValue;
                                } else if (selectClass === "onFailure") {
                                    failure = selectValue;
                                } else {
                                    error = selectValue;
                                }
                            });
                            rowData = {
                                "onSuccess": success,
                                "onFailure": failure,
                                "onError": error
                            };
                            successFailError.push(rowData);
                        });
                        try {
                            var count = 1;
                            dataArray.forEach(function (item) {
                                //  console.log(item);
                                item.url = getServerUrl(item.serverName);
                                item.uname = getUserName(item.serverName);
                                item.repoName = getRepoName(item.serverName);
                                item.OnSuccess = successFailError[count].onSuccess;
                                item.onFailure = successFailError[count].onFailure;
                                item.onError = successFailError[count].onError;

                                count++;
                            });
                            outputIceExeData.data = dataArray;
                            icedq.writeExecuteTableDataFile(JSON.stringify(dataArray));
                        } catch (e) {
                            //console.log(e);
                            swal({
                                title: "Oops...",
                                type: "error",
                                text: "Something went wrong!",
                                timer: 2000,
                                showConfirmButton: false
                            });
                        }
                    } else {
                        executeRuleTableRef.$('tr.selected').removeClass('selected');
                    }
                }
        );
    });
}

function addExecuteDataJson() {
    $j("#finalizeRuleBtn").click(function (event) {
        event.preventDefault();
        var dataArray = [];
        var tableData = executeRuleTableRef.rows().data();
        dataArray = tableData.toArray();

        if (dataArray.length > 0) {
            var orderArr = [];
            dataArray.forEach(function (jsonObj) {
                orderArr.push(jsonObj.order);
            });
            var sorted_arr = orderArr.slice().sort();
            var notUniqueOrders = [];
            for (var i = 0; i < orderArr.length - 1; i++) {
                if (sorted_arr[i + 1] == sorted_arr[i]) {
                    notUniqueOrders.push(sorted_arr[i]);
                }
            }
            if (notUniqueOrders.length !== 0) {
                swal({
                    title: "Not unique.",
                    type: "error",
                    text: "Unique order number not found.\nRepeated order numbers :" + notUniqueOrders.toString(),
                    //  timer: 2000,
                    showConfirmButton: true
                });
//                console.log("Unique order not found");
            } else {
                swal({
                    title: "Confirm Save?",
                    text: "You want to save all Records.",
                    type: "warning",
                    showCancelButton: true,
                    cancelButtonText: "No",
                    confirmButtonColor: "#DD6B55",
                    confirmButtonText: "Yes",
                    closeOnConfirm: false
                },
                        function (isConfirm) {
                            if (isConfirm) {
                                var successFailError = [];
                                $j('#iceExecuteRuleTable tr').each(function (i, row) {
                                    var rowData;
                                    var success, failure, error;
                                    $j(row).find('select').each(function (j, data) {
                                        var selectClass = $j(data).attr('class');
                                        var selectValue = data.options[data.selectedIndex].value;
                                        if (selectClass === "onSuccess") {
                                            success = selectValue;
                                        } else if (selectClass === "onFailure") {
                                            failure = selectValue;
                                        } else {
                                            error = selectValue;
                                        }
                                    });
                                    rowData = {
                                        "onSuccess": success,
                                        "onFailure": failure,
                                        "onError": error
                                    };
                                    successFailError.push(rowData);
                                });
                                try {
                                    var count = 1;
                                    dataArray.forEach(function (item) {
//                                         console.log(item);
                                        item.url = getServerUrl(item.serverName);
                                        item.uname = getUserName(item.serverName);
                                        item.password =getPassword(item.serverName);
                                        item.repoName = getRepoName(item.serverName);
                                        item.OnSuccess = successFailError[count].onSuccess;
                                        item.onFailure = successFailError[count].onFailure;
                                        item.onError = successFailError[count].onError;

                                        count++;
                                    });
                                    icedq.writeExecuteTableDataFile(JSON.stringify(dataArray));
                                } catch (e) {
                                    swal({
                                        title: "Oops...",
                                        type: "error",
                                        text: "Something went wrong!",
                                        timer: 2000,
                                        showConfirmButton: false
                                    });
                                }
                            }
                        });
            }
        } else {
            swal({
                title: "No recodes to save!",
                type: "warning",
                text: "You can add new records from search table.",
                timer: 2000,
                showConfirmButton: false
            });
        }
    });
}
function getCode(item) {
    try {
        if (item.FLAG_TYPE.toLowerCase() == 'testset' || item.FLAG_TYPE.toLowerCase() == 'testsetfolder') {
            var code = item.CODE.split("@");
            return code[1];
        } else {
            return item.CODE;
        }
    } catch (e) {

    }
}
function getServerUrl(serverName) {
    var serverUrl = '';
    try {
        serverConnTableRef.rows().every(function (rowIdx, tableLoop, rowLoop) {
            var data = this.data();
            if (data.serverConnectionName === serverName) {
                serverUrl = data.connectionUrl;
            }
        });
        return serverUrl.trim();
    } catch (e) {
    }
}
function getRepoName(serverName) {
    var repoName;
    try {
        serverConnTableRef.rows().every(function (rowIdx, tableLoop, rowLoop) {
            var data = this.data();
            if (data.serverConnectionName === serverName) {
                repoName = data.repository;
            }
        });
        return repoName.trim();
    } catch (e) {
    }
}
function getUserName(serverName) {
    var username;
    try {
        serverConnTableRef.rows().every(function (rowIdx, tableLoop, rowLoop) {
            var data = this.data();
            if (data.serverConnectionName === serverName) {
                username = data.user;
            }
        });
        return username.trim();
    } catch (e) {
    }
}

function getPassword(serverName) {
    var password;
    try {
        serverConnTableRef.rows().every(function (rowIdx, tableLoop, rowLoop) {
            var data = this.data();
            if (data.serverConnectionName === serverName) {
                password = data.password;
            }
        });
        return password.trim();
    } catch (e) {
        //console.log(e);
    }
}
