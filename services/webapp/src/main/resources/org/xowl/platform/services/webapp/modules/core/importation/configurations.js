// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var IMPORTERS = null;
var CONFIGURATIONS = null;
var DESCRIPTORS = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Import Data", uri: ROOT + "/modules/core/importation/"},
			{name: "Stored Configurations"}], function() {
		doGetData1();
	});
}

function doGetData1() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getDocumentImporters(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			IMPORTERS = content;
			doGetData2();
		}
	});
}

function doGetData2() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getImporterConfigurations(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			CONFIGURATIONS = content;
			CONFIGURATIONS.sort(function (x, y) {
				var left = x.importer + x.name;
				var right = y.importer + y.name;
				return left.localeCompare(right);
			});
			DESCRIPTORS = new Array(CONFIGURATIONS.length);
			doGetData3();
		}
	});
}

function doGetData3() {
	if (CONFIGURATIONS.length == 0) {
		render();
		return;
	}
	if (!onOperationRequest("Loading ...", CONFIGURATIONS.length))
		return;
	for (var i = 0; i != CONFIGURATIONS.length; i++) {
		loadSecurityDescriptor(CONFIGURATIONS[i], i);
	}
}

function loadSecurityDescriptor(configuration, index) {
	xowl.getSecuredResourceDescriptor(function (status, ct, content) {
		onOperationEnded(200, content);
		if (status === 200) {
			DESCRIPTORS[index] = content;
		} else {
			DESCRIPTORS[index] = null;
		}
		if (PAGE_BUSY === null)
			render();
	}, configuration.identifier);
}

function render() {
	var table = document.getElementById("configurations");
	for (var i = 0; i != CONFIGURATIONS.length; i++) {
		var row = renderConfiguration(CONFIGURATIONS[i], DESCRIPTORS[i]);
		table.appendChild(row);
	}
	document.getElementById("btn-download").href = "data:" + MIME_JSON + ";base64," + btoa(JSON.stringify(CONFIGURATIONS));
}

function renderConfiguration(configuration, descriptor) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");

	var icon = renderDescriptorIcon(descriptor);
	cell.appendChild(icon);

	icon = document.createElement("img");
	icon.src = ROOT + "/assets/importer-configuration.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = configuration.identifier;
	var link = document.createElement("a");
	link.href = "configuration.html?id=" + encodeURIComponent(configuration.identifier);
	link.appendChild(document.createTextNode(configuration.name));
	cell.appendChild(icon);
	cell.appendChild(link);
	row.appendChild(cell);

	row.appendChild(renderImporter(configuration.importer));

	cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(image);
	button.onclick = function() { deleteConfiguration(configuration); };
	cell.appendChild(button);
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-download.svg";
	image.width = 20;
	image.height = 20;
	image.title = "DOWNLOAD";
	button = document.createElement("a");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.href = "data:" + MIME_JSON + ";base64," + btoa(JSON.stringify(configuration));
	button.download = configuration.name + ".json";
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function renderImporter(importerId) {
	var cell = document.createElement("td");
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/importer.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = importerId;
	cell.appendChild(icon);
	for (var i = 0; i != IMPORTERS.length; i++) {
		if (IMPORTERS[i].identifier == importerId) {
			cell.appendChild(document.createTextNode(IMPORTERS[i].name));
			break;
		}
	}
	return cell;
}

function deleteConfiguration(configuration) {
	popupConfirm("Import Data", richString(["Delete configuration ", configuration, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting configuration ", configuration, " ..."])))
			return;
		xowl.deleteImporterConfiguration(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage(richString(["Deleted configuration ", configuration, "."]));
				waitAndRefresh();
			}
		}, configuration.identifier);
	});
}