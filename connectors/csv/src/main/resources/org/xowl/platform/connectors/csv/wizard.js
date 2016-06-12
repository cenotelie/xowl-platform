// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");
var importerId = "org.xowl.platform.connectors.csv.CSVImporter"
var lastPreview = null;
var mapping = [];

function init() {
	setupPage(xowl);
	if (!docId || docId === null || docId === "")
    	return;
	document.getElementById("placeholder-doc").innerHTML = docId;
	displayMessage("Loading ...");
	xowl.getUploadedDocument(function (status, ct, content) {
		if (status == 200) {
			document.getElementById("document-name").value = content.name;
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId);
}

function onPreview() {
	var separator = document.getElementById("document-separator").value;
	var textMarker = document.getElementById("document-text-marker").value;
	var rowCount = document.getElementById("document-row-count").value;
    if (separator === null || textMarker === null || rowCount <= 0 || separator == "" || textMarker == "")
		return;
    displayMessage("Loading ...");
    xowl.getUploadedDocumentPreview(function (status, ct, content) {
		if (status == 200) {
			renderPreview(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId, importerId, {
		separator: separator,
		textMarker: textMarker,
		rowCount: rowCount
	});
}

function renderPreview(data) {
	lastPreview = data;
	var tableHead = document.getElementById("document-heads");
	while (tableHead.hasChildNodes())
		tableHead.removeChild(tableHead.lastChild);
	var tableBody = document.getElementById("document-rows");
	while (tableBody.hasChildNodes())
		tableBody.removeChild(tableBody.lastChild);
	if (data.rows.length == 0)
		return;
	var hasTitle = document.getElementById("document-has-title-row").checked;
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
	mapping = [];
	var table = document.getElementById("mapping");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	if (lastPreview == null || lastPreview.rows.length <= 0)
		return;
	for (var i = 0; i != lastPreview.rows[0].cells.length; i++) {
		table.appendChild(mappingNewColumn(i, lastPreview.rows[0].cells[i]));
	}
}

function mappingNewColumn(index, name) {
	mapping[index] = {
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
	var toggle = createNewSelectMultivalued(index);
	cell5.appendChild(toggle);
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
		mapping[index].type = selectType.value;
	};
	return selectType;
}

function createNewPropertyInput(index) {
	var input = document.createElement("input");
	input.type = "text";
	input.placeholder = "http://xowl.org/property";
	input.onchange = function() {
		mapping[index].property = input.value;
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
		mapping[index].datatype = selectType.value;
	};
	return selectType;
}

function createNewSelectRegexp(index) {
	var input = document.createElement("input");
	input.type = "text";
	input.onchange = function() {
		mapping[index].regexp = input.value;
	};
	return input;
}

function onImport() {

}