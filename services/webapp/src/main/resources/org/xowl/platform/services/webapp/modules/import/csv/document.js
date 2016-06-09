// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var docId = getParameterByName("id");

function init() {
	setupPage(xowl);
	if (!docId || docId === null || docId === "")
    	return;
	document.getElementById("placeholder-doc").innerHTML = docId;
	displayMessage("Loading ...");
	xowl.getCSVDocument(function (status, ct, content) {
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
    xowl.getCSVFirstLines(function (status, ct, content) {
		if (status == 200) {
			renderPreview(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, docId, separator, textMarker, rowCount);
}

function renderPreview(data) {
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