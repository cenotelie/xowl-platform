// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Import Data"}], function() {
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
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/document.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = doc.identifier;
	var link = document.createElement("a");
	link.href = "document.html?id=" + encodeURIComponent(doc.identifier);
	link.appendChild(document.createTextNode(doc.name));
	cell.appendChild(icon);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(doc.uploadDate));
	row.appendChild(cell);

	cell = document.createElement("td");
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/user.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = doc.uploader;
	link = document.createElement("a");
	link.href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(doc.uploader);
	link.appendChild(document.createTextNode(doc.uploader));
	cell.appendChild(icon);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(image);
	button.onclick = function() {
		onClickDelete(doc);
	};
	cell.appendChild(button);
	image = document.createElement("img");
	image.src = ROOT + "/assets/action-plus.svg";
	image.width = 20;
	image.height = 20;
	image.title = "IMPORT";
	button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	image = document.createElement("img");
	image.src = ROOT + "/assets/artifact.svg";
	image.width = 20;
	image.height = 20;
	image.title = "IMPORT";
	button.appendChild(image);
	button.onclick = function() {
		onClickImport(doc);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onClickDelete(doc) {
	var result = confirm("Drop document " + doc.name + "?");
	if (!result)
		return;
	if (!onOperationRequest({ type: "org.xowl.infra.utils.RichString", parts: ["Dropping document ", doc, " ..."]}))
		return;
	xowl.dropUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Dropped document ", doc, "."]});
			waitAndRefresh();
		}
	}, doc.identifier);
}

function onClickImport(doc) {
	window.location.href = "document-import.html?id=" + encodeURIComponent(doc.identifier);
}