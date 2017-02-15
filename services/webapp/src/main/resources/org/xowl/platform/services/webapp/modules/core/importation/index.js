// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Data Import"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getUploadedDocuments(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderDocuments(content);
			}
		});
	});
}

function renderDocuments(documents) {
	documents.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
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
	icon.src = ROOT + "/assets/document.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = doc.identifier;
	var link = document.createElement("a");
	link.href = "document.html?id=" + encodeURIComponent(doc.identifier);
	link.appendChild(document.createTextNode(doc.name));
	var cell2 = document.createElement("td");
	cell2.appendChild(icon);
	cell2.appendChild(link);
	var cell3 = document.createElement("td");
	cell3.appendChild(document.createTextNode(doc.uploadDate));
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/user.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = doc.uploader;
	link = document.createElement("a");
	link.href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(doc.uploader);
	link.appendChild(document.createTextNode(doc.uploader));
	var cell4 = document.createElement("td");
	cell4.appendChild(icon);
	cell4.appendChild(link);
	var cell5 = document.createElement("td");
	cell5.appendChild(document.createTextNode(doc.fileName));
	var row = document.createElement("tr");
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	row.appendChild(cell5);
	return row;
}