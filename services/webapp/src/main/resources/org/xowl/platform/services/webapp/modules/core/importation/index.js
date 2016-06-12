// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	xowl.getUploadedDocuments(function (status, ct, content) {
		if (status == 200) {
			renderDocuments(content);
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderDocuments(documents) {
	var table = document.getElementById("documents");
	for (var i = 0; i != documents.length; i++) {
		var row = renderDocument(i, documents[i]);
		table.appendChild(row);
	}
}

function renderDocument(index, doc) {
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode((index + 1).toString()));
	var icon = document.createElement("img");
	icon.src = "/web/assets/document.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	var link = document.createElement("a");
	link.href = "document.html?id=" + encodeURIComponent(doc.identifier);
	link.appendChild(document.createTextNode(doc.name));
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(link);
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	return row;
}