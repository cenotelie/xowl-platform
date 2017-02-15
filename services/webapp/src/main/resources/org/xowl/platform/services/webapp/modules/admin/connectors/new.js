// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var descriptors = null;
var selectedDescriptor = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Connectors Management", uri: ROOT + "/modules/admin/connectors/"},
			{name: "Spawn New"}], function() {
			doGetData();
	});
	document.getElementById("input-uri-addon").innerHTML = xowl.endpoint;
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getDescriptors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			descriptors = content;
			renderDescriptors(descriptors);
		}
	});
}

function renderDescriptors(descriptors) {
	descriptors.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("input-descriptor");
	while (select.length > 0)
		select.remove(select.length - 1);
	for (var i = 0; i != descriptors.length; i++) {
		var option = document.createElement("option");
		option.value = descriptors[i].identifier;
		option.appendChild(document.createTextNode(descriptors[i].name));
		select.appendChild(option);
	}
	select.selectedIndex = -1;
}

function onDescriptorSelect() {
	document.getElementById("input-descriptor-description").value = "";
	var params = document.getElementById("input-params");
	while (params.hasChildNodes()) {
		params.removeChild(params.lastChild);
	}

	var select = document.getElementById("input-descriptor");
	if (select.selectedIndex == -1)
		return;
	selectedDescriptor = descriptors[select.selectedIndex];
	document.getElementById("input-descriptor-description").value = selectedDescriptor.description;
	for (var i = 0; i != selectedDescriptor.parameters.length; i++) {
		var parameter = selectedDescriptor.parameters[i];
		var data = "<div class='form-group'>";
		if (parameter.isRequired)
			data += "<span class='col-sm-1 glyphicon glyphicon-star text-danger' aria-hidden='true' title='required'></span>";
		else
			data += "<span class='col-sm-1' aria-hidden='true'></span>";
		data += "<label class='col-sm-2 control-label'>";
		data += parameter.name;
		data += "</label>";
		data += "<div class='col-sm-9'>";
		data += "<input type='" + (parameter.typeHint === "password" ? "password" : "text") + "' class='form-control' id='input-param-" + i + "'>";
		data += "</div></div>";
		params.innerHTML += data;
	}
}

function onClickNewConnector() {
	if (!onOperationRequest("Spawning new connector ..."))
		return false;
	var id = document.getElementById("input-id").value;
	var name = document.getElementById("input-name").value;
	var uri = document.getElementById("input-uri").value;
	if (selectedDescriptor == null
		|| id == null || id == ""
		|| name == null || name == ""
		|| uri == null || uri == "") {
		onOperationAbort("All fields are mandatory.");
		return false;
	}

	var data = {
		"type": "org.xowl.platform.services.connection.ConnectorServiceData",
		"identifier": id,
		"name": name,
		"uris": [uri]
	};
	for (var i = 0; i != selectedDescriptor.parameters.length; i++) {
		var value = document.getElementById("input-param-" + i).value;
		if (typeof value == "undefined" || value == null || value == "") {
			if (selectedDescriptor.parameters[i].isRequired) {
				onOperationAbort("Parameter " + selectedDescriptor.parameters[i].name + " is required.");
				return;
			}
		} else {
			data[selectedDescriptor.parameters[i].identifier] = value;
		}
	}

	xowl.createConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Spawned connector ", content, "."]});
			waitAndGo("connector.html?id=" + encodeURIComponent(content.identifier));
		}
	}, selectedDescriptor, data);
}
