// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var importers = null;
var configurations = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Stored Configurations"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getImporterConfigurations(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			configurations = content;
			render();
		}
	});
	xowl.getDocumentImporters(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			importers = content;
			render();
		}
	});
}

function render() {
	if (configurations != null && importers != null)
		doRender();
}

function doRender() {
	configurations.sort(function (x, y) {
		var left = x.importer + x.name;
		var right = y.importer + y.name;
		return left.localeCompare(right);
	});
	var table = document.getElementById("configurations");
	for (var i = 0; i != configurations.length; i++) {
		var row = renderConfiguration(configurations[i]);
		table.appendChild(row);
	}
	document.getElementById("btn-download").href = "data:" + MIME_JSON + ";base64," + btoa(JSON.stringify(configurations));
}

function renderConfiguration(configuration) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/importer_configuration.svg";
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
	for (var i = 0; i != importers.length; i++) {
		if (importers[i].identifier == importerId) {
			cell.appendChild(document.createTextNode(importers[i].name));
			break;
		}
	}
	return cell;
}

function deleteConfiguration(configuration) {
	var result = confirm("Delete configuration " + configuration.name + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting configuration " + configuration.name))
		return;
	xowl.deleteImporterConfiguration(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Deleted configuration " + configuration.name);
			waitAndRefresh();
		}
	}, configuration.identifier);
}