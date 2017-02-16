// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var storageId = getParameterByName("storageId");
var importerId = localStorage.getItem(storageId + ".importer.identifier");
var doc = {
	type: "org.xowl.platform.services.importation.Document",
	identifier: localStorage.getItem(storageId + ".document.identifier"),
	name: localStorage.getItem(storageId + ".document.name")
};
var metadata = {
	name: localStorage.getItem(storageId + ".artifact.name"),
	base: localStorage.getItem(storageId + ".artifact.base"),
	version: localStorage.getItem(storageId + ".artifact.version"),
	archetype: localStorage.getItem(storageId + ".artifact.archetype"),
	superseded: localStorage.getItem(storageId + ".artifact.superseded")
};
var PREVIEW = null;
var MAPPING = [];

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import", uri: ROOT + "/modules/core/importation/"},
			{name: "Document " + doc.identifier, uri: ROOT + "/modules/core/importation/document.html?id=" + encodeURIComponent(doc.identifier)},
			{name: "CSV Importer"}], function() {
		if (!storageId || storageId === null || storageId === "")
			return;
		document.getElementById("document-id").value = doc.identifier;
		document.getElementById("document-name").value = doc.name;
	});
}

function onPreview() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getUploadedDocumentPreview(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderPreview(content);
		}
	}, doc.identifier, {
		type: "org.xowl.platform.connectors.csv.CSVConfiguration",
		identifier: "anonymous",
		name: "Anonymous Configuration",
		importer: importerId,
		separator: document.getElementById("input-separator").value,
		textMarker: document.getElementById("input-text-marker").value,
		rowCount: document.getElementById("input-row-count").value,
		skipFirstRow: "\"" + document.getElementById("input-has-title-row").checked + "\"",
		mapping: {
			columns: MAPPING
		}
	});
}

function renderPreview(data) {
	PREVIEW = data;
	var tableHead = document.getElementById("preview-heads");
	while (tableHead.hasChildNodes())
		tableHead.removeChild(tableHead.lastChild);
	var tableBody = document.getElementById("preview-rows");
	while (tableBody.hasChildNodes())
		tableBody.removeChild(tableBody.lastChild);
	if (data.rows.length == 0)
		return;
	var hasTitle = document.getElementById("input-has-title-row").checked;
	if (hasTitle)
		tableHead.appendChild(renderPreviewRow(data.rows[0]));
	else
		tableBody.appendChild(renderPreviewRow(data.rows[0]));
	for (var i = 1; i != data.rows.length; i++) {
		tableBody.appendChild(renderPreviewRow(data.rows[i]));
	}
}

function renderPreviewRow(row) {
	var result = document.createElement("tr");
	for (var i = 0; i != row.cells.length; i++) {
		result.appendChild(renderPreviewCell(row.cells[i]));
	}
	return result;
}

function renderPreviewCell(cell) {
	var result = document.createElement("td");
	if (cell.length > 100)
		cell = cell.substring(0, 100) + " ...";
	result.appendChild(document.createTextNode(cell));
	return result;
}

function onInitMapping() {
	MAPPING = [];
	var table = document.getElementById("mapping");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	if (PREVIEW == null || PREVIEW.rows.length <= 0)
		return;
	for (var i = 0; i != PREVIEW.rows[0].cells.length; i++) {
		table.appendChild(mappingNewColumn(i, PREVIEW.rows[0].cells[i]));
	}
}

function mappingNewColumn(index, name) {
	MAPPING[index] = {
		type: "none",
		property: "",
		datatype: "",
		regexp: ""
	};
	var result = document.createElement("tr");
	var cell1 = document.createElement("td");
	var cell2 = document.createElement("td");
	var cell3 = document.createElement("td");
	var cell4 = document.createElement("td");
	var cell5 = document.createElement("td");
	cell1.appendChild(document.createTextNode(name));
	var selectType = createNewSelectMappingType(index);
	cell2.appendChild(selectType);
	var propertyInput = createNewPropertyInput(index);
	cell3.appendChild(propertyInput);
	var selectDatatype = createNewSelectDatatype(index);
	cell4.appendChild(selectDatatype);
	var matcher = createNewSelectRegexp(index);
	cell5.appendChild(matcher);
	result.appendChild(cell1);
	result.appendChild(cell2);
	result.appendChild(cell3);
	result.appendChild(cell4);
	result.appendChild(cell5);
	return result;
}

function createNewSelectMappingType(index) {
	var selectType = document.createElement("select");
	var option1 = document.createElement("option");
	var option2 = document.createElement("option");
	var option3 = document.createElement("option");
	var option4 = document.createElement("option");
	option1.appendChild(document.createTextNode("No mapping"));
	option2.appendChild(document.createTextNode("Entity identifier"));
	option3.appendChild(document.createTextNode("Entity attribute"));
	option4.appendChild(document.createTextNode("Relation"));
	option1.value = "none";
	option2.value = "id";
	option3.value = "attribute";
	option4.value = "relation";
	selectType.appendChild(option1);
	selectType.appendChild(option2);
	selectType.appendChild(option3);
	selectType.appendChild(option4);
	selectType.onchange = function() {
		MAPPING[index].type = selectType.value;
	};
	return selectType;
}

function createNewPropertyInput(index) {
	var input = document.createElement("input");
	input.type = "text";
	input.placeholder = "http://xowl.org/property";
	input.onchange = function() {
		MAPPING[index].property = input.value;
	};
	return input;
}

function createNewSelectDatatype(index) {
	var selectType = document.createElement("select");
	var option1 = document.createElement("option");
	var option2 = document.createElement("option");
	var option3 = document.createElement("option");
	var option4 = document.createElement("option");
	option1.appendChild(document.createTextNode("N/A"));
	option2.appendChild(document.createTextNode("xsd:string"));
	option3.appendChild(document.createTextNode("xsd:integer"));
	option4.appendChild(document.createTextNode("xsd:float"));
	option1.value = "";
	option2.value = "http://www.w3.org/2001/XMLSchema#string";
	option3.value = "http://www.w3.org/2001/XMLSchema#integer";
	option4.value = "http://www.w3.org/2001/XMLSchema#float";
	selectType.appendChild(option1);
	selectType.appendChild(option2);
	selectType.appendChild(option3);
	selectType.appendChild(option4);
	selectType.onchange = function() {
		MAPPING[index].datatype = selectType.value;
	};
	return selectType;
}

function createNewSelectRegexp(index) {
	var input = document.createElement("input");
	input.type = "text";
	input.onchange = function() {
		MAPPING[index].regexp = input.value;
	};
	return input;
}




function onClickOk() {
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Importing document ", doc, " ..."]}))
		return;
	xowl.importUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched importation job for ", doc, "."]});
			waitForJob(content.identifier, content.name, function (job) {
				onJobCompleted(job);
			});
		}
	}, doc.identifier, {
		type: "org.xowl.platform.connectors.csv.CSVConfiguration",
		identifier: "anonymous",
		name: "Anonymous Configuration",
		importer: importerId,
		separator: document.getElementById("document-separator").value,
		textMarker: document.getElementById("document-text-marker").value,
		skipFirstRow: "\"" + document.getElementById("document-has-title-row").checked + "\"",
		mapping: {
			columns: MAPPING
		}
	}, metadata);
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		var artifactId = job.result.payload;
		displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Imported ", doc, " as artifact " + artifactId]});
		waitAndGo(ROOT + "/modules/core/artifacts/artifact.html?id=" + encodeURIComponent(artifactId));
	}
}