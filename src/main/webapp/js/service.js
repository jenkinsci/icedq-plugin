/* global remoteAction, $j, CryptoJS, serverConnTableRef, outputIceExeData, SearchFlag */
"use strict";
var icedq = (function ($j) {
    var module = {};
    module.init = function () {
        icedq.readServerConnFile();
        icedq.readExecuteTableDataFile();
    };
    var iterationCount = 1000;
    var keySize = 128;
    var passphrase = 'abcdefghxyzpqr@132!';
    var flagSet = "335525cff4b2815c580714d322113c7f";
    var suger = "0be37276e7fd37fa1af65287bfbe2524";
    var searchResultObj = {data: []};
    var dataSet = {};
    var output = {data: []};
    var searchResultTable;
    module.getObjTypeValue = function () {
        return $j('#selectObjTypeId').val();
    };
    module.initServerConnDataTable = function () {
        var editor;
        editor = new $j.fn.dataTable.Editor({
            table: "#iceServerConnTable",
            fields: [
                {
                    label: "Server Connection Name:",
                    name: "serverConnectionName"
                },
                {
                    label: "Connection Url:",
                    name: "connectionUrl"
                },
                {
                    label: "Repository:",
                    name: "repository",
                    type: "selectize",
                    opts: {
                        'searchField': 'label',
                        valueField: 'id',
                        labelField: 'title',
                        create: false
                    }
                },
                {
                    label: "User:",
                    name: "user"
                },
                {
                    label: "Token:",
                    name: "password",
                   
                }
                
            ],
            ajax: function (method, url, d, successCallback, errorCallback) {
                if (d.action === 'create') {
                   
                    output = {data: []};
                    var dateKey = +new Date();
                    $j.each(d.data, function (key, value) {
                        var id = dateKey;
                        value.DT_RowId = id;
                        dataSet[ id ] = value;
                        output.data.push(value);
                    });
                } else
                if (d.action === 'edit') {
                     //editor.disable('name');
                    $j.each(d.data, function (id, value) {
                        value.DT_RowId = id;
                        $j.extend(dataSet[ id ], value);
                        output.data.push(dataSet[ id ]);
                    });
                } else if (d.action === 'remove') {
                    // Remove items from the object
                    $j.each(d.data, function (id) {
                        delete dataSet[ id ];
                        output.data.pop(id);
                    });
                }
                
                icedq.writeServerConnFile(JSON.stringify(dataSet));
                // Show Editor what has changed
                successCallback(output);
            }
        });
        //populate the selectize value
        editor.on('open', function (e, type,action) {
            
            try {
                var repo = {};
                repo = serverConnTable.row('.selected').data();
                var selectizeRepo = editor.field('repository');
                var repoSelectizeInst = editor.field('repository').inst();
                repoSelectizeInst.addOption({id: repo.repository, title: repo.repository});
                selectizeRepo.set(repo.repository);
               if(action === "edit")
               {
                   //alert("call if");
                editor.disable('serverConnectionName');
                }
                else if(action ==="create")
                {
                     //alert("call else");
                   editor.enable('serverConnectionName'); 
                }
            } catch (e) {

            }
        });
        var serverConnectionName = editor.field('serverConnectionName');
        var connectionUrl = editor.field('connectionUrl');
        var repository = editor.field('repository');
        var user = editor.field('user');
        var password = editor.field('password');
        
        //validation editor
        editor.on('preSubmit', function (e, data, action) {
            ;
            var serverNameData = serverConnTable.columns(1).data().eq(0).sort().toArray();
            if (action !== 'remove') {
                if (!serverConnectionName.val()) {
                    serverConnectionName.error('This field is required.');
                }
                if (action === 'create') {
                    if (serverNameData.indexOf(serverConnectionName.val()) !== -1) {
                        serverConnectionName.error('Server name must be unique.');
                    }
                }
                if (!connectionUrl.val()) {
                    connectionUrl.error('This field is required.');
                }
                if (!repository.val()) {
                    repository.error('This field is required.');
                }
                if (!user.val()) {
                    user.error('This field is required.');
                }
                if (!password.val()) {
                    password.error('This field is required.');
                }
                
                if (this.inError()) {
                    return false;
                }
            }
        });
        function  validateConnection() {
            //alert("call By Rahul IN Add"+token.val());
            try {
                editor.field('password').message('');
                if (!connectionUrl.val() || !user.val() || !password.val() || !repository.val() ) {
//                    editor.field('password').message("<strong style='color:red;'>Sorry, something went wrong.</strong>");
                    editor.field('password').message("<strong style='color:red;'>Sorry, something went wrong.</strong>");
                } else {
                    var valObj = {
                        "connectionUrl": connectionUrl.val(),
                        "userName": user.val(),
                        "token": password.val(),
                        "repository": repository.val()
                    };
//                    var ivA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);  //By Rahul for Token validation 
//                  var saltA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
//                  var authScript = getAuthScript(saltA, ivA, JSON.stringify(valObj));
                        var authScript = JSON.stringify(valObj);
                        //alert("Rahul check "+authScript);
//                    remoteAction.authenticateUser(saltA, ivA, authScript, $j.proxy(function (response) {
//                        var responseString = response.responseObject();
//                        if (responseString.indexOf("SUCCESS") !== -1) {
//                            var res = responseString.split(":\n");
//                            editor.field('password').message("<strong style='color:#7A80DD;'>" + res[1] + ".</strong>");
//                        } else if (responseString.indexOf("EXCEPTION") !== -1) {
//                            var res = responseString.split(":\n");
//                            editor.field('password').message("<strong style='color:#D24939;'>" + res[1] + "</strong>");
//                        } else {
//                            editor.field('password').message("<strong style='color:#D24939;'>Sorry, something went wrong.</strong>");
//                        }
//                    }, this));
                        remoteAction.authenticateRestUser(authScript, $j.proxy(function (response) {
                           
                        var responseString = response.responseObject();
                         //alert("check res"+responseString)
                         var jb =responseString ;
                         // console.log(responseString);
                        // console.log(responseString.indexOf("EXCEPTION") !== -1);
                        if (responseString.indexOf("userObj") !== -1) {
                            var res = responseString.split(":\n");
                            editor.field('password').message("<strong style='color:#7A80DD;'>CONNECTION TEST SUCCESSFULLY </strong>");
                        } else if (responseString.indexOf("Authorization") !== -1) {
                          var res =[];
                          res =responseString;
                          
                            editor.field('password').message("<strong style='color:#D24939;'>" +  res + "</strong>");
                        } 
                        else if (responseString.indexOf("Request-URI") !== -1) {
                          var res =[];
                          res =responseString;
                          
                            editor.field('password').message("<strong style='color:#D24939;'>" +  res + "</strong>");
                        } 
                        else if (responseString.indexOf("Forbbiden") !== -1) {
                          var res =[];
                          res =responseString;
                          
                            editor.field('password').message("<strong style='color:#D24939;'>" +  res + "</strong>");
                        } 
                       
                          else {
                            editor.field('password').message("<strong style='color:#D24939;'>Sorry, something went wrong.</strong>");
                        }
                    }, this));
                }
            } catch (e) {

            }
        }
        ;

        var serverConnTable = $j('#iceServerConnTable').DataTable({
            dom: "Bfrtip",
            responsive: true,
            "lengthChange": false,
            "ordering": false,
            "info": false,
            data: $j.map(dataSet, function (value, key) {
                try {
                    return value;
                } catch (e) {
                }
            }),
            language: {
                "search": " ",
                "searchPlaceholder": "Search...",
                "emptyTable": "No data "
            },
            columns: [
                {
                    data: null,
                    defaultContent: '',
                    className: 'select-checkbox',
                    orderable: false
                },
                {data: "serverConnectionName"},
                {data: "repository"},
                {data: "user"},
                {data: "connectionUrl"}
            ],
            select: true,
            buttons: [
                {extend: "create", editor: editor, text: 'Add', formTitle: "Add server",
                    formButtons: [
                        'Add',
                        {label: 'Test', fn: function () {
                               
                             serverConnectionName.error('');
                             connectionUrl.error('');
                            repository.error('');
                            user.error('');
                            password .error('');
                               
                                if (this.inError()) {
                                    return false;
                                }
                                validateConnection();
                            }
                        },
                        {label: 'Retrieve Repositories', fn: function () {
                                  serverConnectionName.error('');
                             connectionUrl.error('');
                            repository.error('');
                            user.error('');
                            password .error('');
                                if (!connectionUrl.val()) {
                                    connectionUrl.error('This field is required.');
                                } else {
                                    connectionUrl.error('');
                                    retriveRepoNameEditor();
                                }
                            }
                        }
                    ]},
                {extend: "edit", editor: editor, text: 'Edit', formTitle: "Edit server",
                    formButtons: [
                        'Update',
                        {label: 'Test', fn: function () {

                                if (this.inError()) {
                                    return false;
                                }
                                validateConnection();
                            }
                        },
                        {label: 'Retrieve Repositories', fn: function () {
                                if (!connectionUrl.val()) {
                                    connectionUrl.error('This field is required.');
                                } else {
                                    connectionUrl.error('');
                                    retriveRepoNameEditor();
                                }
                            }
                        }
                    ]},
                {extend: "remove", editor: editor, text: 'Delete', formTitle: "Delete server",
                    formButtons: [
                        'Delete',
                        {label: 'Cancel', fn: function () {
                                this.close();
                            }}
                    ],
                    formMessage: "Are you sure you want to delete server connection ?"
                },
                {
                    extend: "selectedSingle",
                    text: "Test",
                    action: function (e, dt, node, config) {
                        var data = serverConnTableRef.row('.selected').data();
                        if (!data) {
                            swal({
                                title: "No server selected!",
                                text: "You need to select server connection.",
                                timer: 3000,
                                showConfirmButton: true
                            });
                        } else {
                            var valObj = {
//                                "connectionUrl": data.connectionUrl,
//                                "userName": data.user,
//                                "password": data.password,
//                                "repository": data.repository
                                "connectionUrl": data.connectionUrl,
                                "userName": data.user,
                                "token": data.password,
                                "repository": data.repository
                            };
                            
                             var authScript = JSON.stringify(valObj);
//                            var ivA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
//                            var saltA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
//                            var authScript = getAuthScript(saltA, ivA, JSON.stringify(valObj));
                            remoteAction.authenticateRestUser(authScript, $j.proxy(function (response) {
                                var responseString = response.responseObject();
//                                console.log(responseString);
                                if (responseString.indexOf("userObj") !== -1) {
                                    var sResp = responseString;
                                    swal({
                                        title: "Success!",
                                        type: "success",
                                        text: "CONNECTION TEST SUCCESSFULLY ",
                                        timer: 3000,
                                        showConfirmButton: true
                                    });
                                } else if (responseString.indexOf("EXCEPTION") !== -1) {
                                    var fResp = responseString.split(":\n");
                                    swal({
                                        title: "Failure!",
                                        type: "error",
                                        text: fResp,
                                        timer: 3000,
                                        showConfirmButton: true
                                    });
                                } else {
                                    swal({
                                        title: "Oops...",
                                        type: "error",
                                        text: "Something went wrong!",
                                        timer: 3000,
                                        showConfirmButton: true
                                    });
                                }
                            }, this));
                        }
                    }
                }
            ]
        });
        var retriveRepoNameEditor = function () {
//            alert("call By Rahul");
            var value = editor.field('connectionUrl').val();
            var RepoSelectize = editor.field('repository');
            var RepoSelectize1 = editor.field('repository').inst();
            try {
                editor.field('password').message('');
                remoteAction.getRepoNameList(value, 'somestring', $j.proxy(function (t) {
                    var data = t.responseObject();
//                    console.log(data);
                    if (data !== null) {
                        var dataSize = data.size();
//                        console.log(dataSize)
                        if (dataSize > 0) {
//                            console.log(data);
                            for (var i = 0; i < dataSize; i++) {
                                RepoSelectize1.addOption({id: data[i], title: data[i]});
                                RepoSelectize.set(data[0]);
                                repository.error('');
                            }
                        }
                    } else {
                        repository.error('Unable to retrieve repository names.');
                        editor.field('password').message('');
                        RepoSelectize1.clear();
                    }
                }, this));
            } catch (e) {
                console.log(e);
            }
        };
        return serverConnTable;
    };
    module.initSearchResultTable = function () {
        searchResultTable = $j("#iceSearchResultTable").DataTable({
            "scrollCollapse": true,
            "deferRender": true,
            responsive: true,
            "lengthChange": false,
            "ordering": false,
            "deferLoading": 15,
            columns: [
                {data: "serverConnName", "defaultContent": '-'},
                {data: "PROJECT_NAME", "defaultContent": '-'},
                {data: "FOLDER_NAME", "defaultContent": '-'},
                {data: "NAME", "defaultContent": '-', "width": "30%"},
                {data: null, "width": "1%"},
                {data: "FLAG_TYPE", "defaultContent": '-'},
                {data: "CODE", "defaultContent": '-'},
                {data: "RULE_OBJECT_TYPE", "defaultContent": '-'},
                {data: "LEGAL_ENTITY_NAME", "defaultContent": '-'},
                {data: null, "defaultContent": '-', "width": "1%"}
            ],
            "columnDefs": [
                {
                    targets: [3],
                    render: $j.fn.dataTable.render.ellipsis(35, true)
                },
                {
                    "targets": [5, 6, 7, 8],
                    "visible": false,
                    "searchable": false
                },
                {
                    "targets": [4],
                    "className": 'details-control',
                    "orderable": false,
                    "data": null,
                    "searchable": false,
                    "defaultContent": ''
                },
                {
                    "targets": [9],
                    "className": 'addToExec-control',
                    "orderable": false,
                    "data": null,
                    "searchable": false,
                    "defaultContent": ''
                }
            ],
            language: {
                "zeroRecords": "No Search Result or Search aborted...",
                "info": "**Click on plus icon to add Records for execution.\n**Click on info icon for more info.",
                "search": "",
                "searchPlaceholder": "Search..."
            }
        });
        return searchResultTable;
    };
    var getJobNameValue = function () {
        var co = false;
        var path = window.location.pathname.toString();

        var jobName = path.split("/");
        for (var i = 0; i < jobName.length; i++)
        {
            //What to do here to get the first word :)
            var firstWord = jobName[i].split(' ')[0];
            if (firstWord === "jenkins" || firstWord === "Jenkins" || firstWord.toUpperCase() === "JENKINS")
            {
                co = true;
            }

        }
        if (co === true)
        {
            console.log(co);
            return jobName[3];
        } else {
            console.log(co);
            return jobName[2];
        }
    };

    var getAuthScript = function (saltA, ivA, data) {
        var aesUtilA = new AesUtil(keySize, iterationCount);
        return aesUtilA.encrypt(saltA, ivA, passphrase, data);
    };


    module.writeServerConnFile = function (data) {
        var iv = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
        var salt = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
        var aesUtil = new AesUtil(keySize, iterationCount);
        var ciphertext = aesUtil.encrypt(salt, iv, passphrase, data);
        remoteAction.writeServerConnFile(iv, salt, ciphertext, getJobNameValue());
    };
    module.readServerConnFile = function () {
        remoteAction.readServerConnFile(getJobNameValue(), $j.proxy(function (data) {
            console.log(data)
            if (data.responseObject()) {
                try {
                    if (data.responseObject() !== null) {
                        var aesUtil1 = new AesUtil(keySize, iterationCount);
                        var plainText = aesUtil1.decrypt(suger, flagSet, passphrase, data.responseObject());
                        dataSet = JSON.parse(plainText);
                    } else {
                        dataSet = {};
                    }
                } catch (e) {

                }
            }
        }, this));
    };
    module.writeExecuteTableDataFile = function (data) {
//        console.log(data)
        try {
            var plainText;
            if (data != "") {
                plainText = JSON.parse(data);
            }
//            console.log(plainText);
           // alert(plainText);
            var iv = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
            var salt = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
            var aesUtil = new AesUtil(keySize, iterationCount);
            var ciphertext = aesUtil.encrypt(salt, iv, passphrase, plainText);
            remoteAction.writeExecuteTableDataFile(iv, salt, ciphertext, getJobNameValue(), $j.proxy(function (data) {
                if (data.responseObject() !== null) {
                    //confirm dialog
                    swal({
                        title: "Success!",
                        type: "success",
                        text: "",
                        timer: 1000,
                        showConfirmButton: false
                    });
                }
            })
                    );  //working
        } catch (e) {
            console.log(e);
        }
    };
    module.readExecuteTableDataFile = function () {
        remoteAction.readExecuteTableDataFile(getJobNameValue(), $j.proxy(function (data) {
            if (data.responseObject()) {
                try {
                    if (data.responseObject() !== null) {
                        var aesUtil1 = new AesUtil(keySize, iterationCount);
                        var plainText = aesUtil1.decrypt(suger, flagSet, passphrase, data.responseObject());
                        outputIceExeData.data = JSON.parse(plainText);
                    }
                } catch (e) {

                }
            }
        }, this));
    };
    module.searchRuleObjects = function (url, repoName, username, password, objType, objCode, objName, serverConnName) {
            
        searchResultTable.clear().draw();//clear the table to show the new search result
        searchResultObj = {data: []};
        try {
            var valObj = {
                "connectionUrl": url,
                "repository": repoName,
                "userName": username,
                "token": password,
                "objectType": objType,
                "objectCode": objCode,
                "objectName": objName
            };
            var ivA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
            var saltA = CryptoJS.lib.WordArray.random(128 / 8).toString(CryptoJS.enc.Hex);
            var authScript = getAuthScript(saltA, ivA, JSON.stringify(valObj));
            remoteAction.getRuleObject(saltA,ivA,authScript, $j.proxy(function (data) {
               
                if (data.responseObject()) {
//                    console.log(data.responseObject());
                   // var responceData = JSON.stringify(data.responseObject()).slice(1, -4).replace(/\\/g, "");
                   // var responceData = JSON.parse(data.responseObject());
                 //    alert("Rahul Respons Data ::"+responceData);
                 //    console.log(responceData);
//                     console.log(data.responseObject().indexOf("EXCEPTION") == -1);
                    if (data.responseObject().indexOf("EXCEPTION") == -1) {
                          var responceData = JSON.parse(data.responseObject());
//                        console.log(responceData.icelist.length);
                        if (responceData.icelist.length > 0) {
                            $j("#iceSearchResultTableDiv").show();
                            $j("#noSearchResultDiv").hide();
                          
                            var searchResultData = responceData.icelist;
//                            console.log(searchResultData);
                            for (var i = 0; i < searchResultData.length; i++) {
//                                console.log(searchResultData[i]);
                                if (SearchFlag !== true) {
                                    var dateKey = +new Date() + i;
                                    var searchTemp = searchResultData[i];
                                    searchTemp.serverConnName = serverConnName;
                                    searchTemp.DT_RowId = dateKey;
                                    searchResultObj.data.push(searchTemp);
                                } else {
                                    $j("#sTableProgressBarDiv").hide();
                                    $j("#noSearchResultDiv").show();
                                    searchResultData.clear().clear().draw();
                                    $j("#iceSearchResultTableDiv").hide();
                                    $j("#searchRulesBtn").removeAttr("disabled");
                                    searchResultObj = {data: []};
                                    break;
                                }
                            }
                            if (SearchFlag !== true) {
                                searchResultTable.rows.add(searchResultObj.data).draw();
                            }
                        } else {
                            $j("#noSearchResultDiv").show();
                            $j("#iceSearchResultTableDiv").hide();
                            $j("#searchRulesBtn").removeAttr("disabled");
                        }
                    } else {
                        $j("#noSearchResultDiv").show();
                        var errorMsg = responceData.split(":n");
                        $j("#noSearchResultDiv").text(errorMsg[1]);
                        $j("#iceSearchResultTableDiv").hide();
                        $j("#searchRulesBtn").removeAttr("disabled");
                    }
                    $j("#sTableProgressBarDiv").hide();
                    $j("#searchRulesBtn").removeAttr("disabled");
                } else {
                    $j("#searchRulesBtn").removeAttr("disabled");
                    $j("#sTableProgressBarDiv").hide();
                }
                //more info
                $j('#iceSearchResultTable tbody').on('click', 'td.details-control', function (event) {
                    event.stopImmediatePropagation();
                    var tr = $j(this).closest('tr');
                    var row = searchResultTable.row(tr);
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
                    if (data.FLAG_TYPE.toLowerCase() == "rule") {
                        return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' + //width:50% !important"
                                '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Details:</small></Strong></caption>' +
                                '<tr>' +
                                '<td align="left"><small>Code:</small></td>' +
                                '<td align="left"><small>' + data.CODE + '</small></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td align="left" ><small>Object Type:</small></td>' +
                                '<td align="left" ><small>' + data.FLAG_TYPE + '</small></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td align="left" ><small>Rule Type:</small></td>' +
                                '<td align="left" ><small>' + data.RULE_OBJECT_TYPE + '</small></td>' +
                                '</tr>' +
                                '</table>';
                    } else {
                        return '<table cellpadding="5" cellspacing="0" border="0" style="padding-left:50px;">' + //width:50% !important"
                                '<caption align="left" style="display: table-caption; text-align: -webkit-left;"><Strong><small>Details:</small></Strong></caption>' +
                                '<tr>' +
                                '<td align="left" ><small>Code:</small></td>' +
                                '<td align="left" ><small>' + getCode(data) + '</small></td>' +
                                '</tr>' +
                                '<tr>' +
                                '<td align="left" ><small>Object Type:</small></td>' +
                                '<td align="left" ><small>' + data.FLAG_TYPE + '</small></td>' +
                                '</tr>' +
                                '</table>';
                    }
                }
            }, this));
        } catch (e) {
        }
    };
    return module;
}($j));
