/* DB */
var db;
var currentAction;
 document.addEventListener("deviceready", onModuleReady, false);

function onModuleReady() {

    try {

        console.log("★★★★★★★ ::  onReadyTest  openDatabase");
        db = window.sqlitePlugin.openDatabase({
            name: 'QdriveDB.db',
            location: 'default',
            androidDatabaseProvider: 'system'
        });
    } catch(e) {
        alert("Error. Please try again." + e.message);
    }
}

function closeDataBase(db, data, successCB) {

     console.log('★★★★★★★ : closeDataBase');

    db.transaction(function(tx) {
        // SQL 실행 및 컨트롤
        tx.executeSql('SELECT opId FROM USER_INFO WHERE opId = ' + data , [], null);
    }, function(error) {
        // 트랜잭션 처리에 문제가 있을 시 호출
        console.log('★★★★★★★ transaction error: ' + error.message + '/' + error.code);
        alert('closeDataBase Error : ' + error.message);
        //db.close();
    }, function() {
        // 트랜잭션 처리 성공 시 호출
        console.log('★★★★★★★ transaction ok');
        db.close(function() {
            console.log('★★★★★★★ database is closed ok');
            successCB();
        });
    });
}

function createTable(db, tableName, columns, successCB, failCB) {

    // console.log("★★★★★★★ ::  createTable  :: ★★★★★★★");
    currentAction = "CREATE TABLE :: " + tableName;
    db.transaction(function(tx) {
        tx.executeSql('CREATE TABLE IF NOT EXISTS ' + tableName + ' (' + columns + ')', [], successCB);
    }, failCB);
}

function insertTableData(db, tableName, columns, data, successCB, failCB) {

    currentAction = "INSERT TABLE :: " + tableName;
    db.transaction(function(tx) {
        tx.executeSql('INSERT INTO ' + tableName + '(' + columns + ') VALUES(' + data + ')', [], successCB);
    }, failCB);
}

function selectTable(db, columns, tableName, where, groupBy, orderBy, option, successCB, failCB) {

    if (where != '' && where != undefined)
        where = ' WHERE ' + where;

    if (groupBy != '' && groupBy != undefined)
        groupBy = ' GROUP BY ' + groupBy;

    if (orderBy != '' && orderBy != undefined)
        orderBy = ' ORDER BY ' + orderBy;

    // console.log("★★★★★★★ ::  selectTable  :: ★★★★★★★");
    currentAction = "SELECT TABLE :: " + tableName;

    db.transaction(function(tx) {

        tx.executeSql('SELECT ' + columns + ' FROM ' + tableName + where + groupBy + orderBy + option, [], successCB);
    }, failCB);
}

function updateTableData(db, tableName, columns, where, successCB, failCB) {

    if (where != '' && where != undefined)
        where = ' WHERE ' + where;

    currentAction = "UPDATE TABLE :: " + tableName;
    db.transaction(function(tx) {
        tx.executeSql('UPDATE ' + tableName + ' SET ' + columns + where, [], successCB);
    }, failCB);
}

function deleteTable(db, tableName, where, successCB, failCB) {

    if (where != '' && where != undefined)
        where = ' WHERE ' + where;

    currentAction = "DELETE TABLE :: " + tableName;
    db.transaction(function(tx) {
        tx.executeSql('DELETE FROM ' + tableName + where, [], successCB);
    }, failCB);
}

function failCB_Common(err) {

	alert("Fail > " + currentAction + " / error code : " + err.code);
	return;
}



/* RMSHelper.js */
var RMSHelper = function() {};
RMSHelper.getXMLHTTP = function() {
    if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
    }
    var versions = [
        "MSXML2.XMLHTTP.6.0",
        "MSXML2.XMLHTTP.4.0",
        "Microsoft.XMLHTTP",
        "MSXML2.XMLHTTP.5.0",
        "MSXML2.XMLHTTP.3.0",
        "MSXML2.XMLHTTP"
    ];
    for (var i = 0; i < versions.length; i++) {
        try {
            var oXMLHTTP = new ActiveXObject(versions[i]);
            return oXMLHTTP;
        } catch (e) {};
    }
    throw new Error("No XMLHTTP");
}


RMSHelper.callWebMethod = function(serviceName, methodName, argument) {
    var svc = serviceName + "/" + methodName;
    console.log("callWebMethod URL : " + svc);

    if (!argument) argument = "";

    var xmlHttp = RMSHelper.getXMLHTTP();
    xmlHttp.open("POST", svc, false);
    xmlHttp.setRequestHeader('Content-Type', 'application/json');

    try {
        xmlHttp.send(argument);
    } catch (e) {
        xmlHttp.abort();
        exceptionClose(); //common
        console.log("callWebMethod exception - " + methodName);
        console.log(e.message);
        alert("Please check your network connection status");
        return null;
    }

    var result = null;
    try {
        result = eval("(" + xmlHttp.responseText + ")");
    } catch (ex) {}

    if (result && result.ExceptionType != undefined) {
        alert(result.Message);
        throw result;
    }

    if (result && result.d != undefined)
        return result.d;
    return result;
}


var RMSParam = function() {
    this._pl = new Array();
}

RMSParam.prototype.add = function(name, value) {
    this._pl[name] = value;
    return this;
}
RMSParam.prototype.toXml = function() {
    var xml = "";
    for (var p in this._pl) {
        if (typeof(this._pl[p]) != "function")
            xml += "<" + p + ">" + this._pl[p].toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;") + "</" + p + ">";
    }
    return xml;
}

RMSParam.prototype.toJson = function() {
    var query = "";
    for (var p in this._pl) {
        if (typeof(this._pl[p]) != "function") {
            if (query.length > 0)
                query += ",";

            if (this._pl[p] == null)
                query += "\"" + p + "\"" + ":null";
            else
                query += "\"" + p + "\"" + ":\"" + this._pl[p].toString().replace(/\\/g, "\\\\").replace(/"/g, "\\\"") + "\"";
        }
    }
    return "{" + query + "}";
}
/* RMSHelper.js End*/
