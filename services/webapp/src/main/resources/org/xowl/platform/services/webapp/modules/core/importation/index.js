// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();
var DOCUMENTS = null;
var DESCRIPTORS = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Import Data"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getUploadedDocuments(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				DOCUMENTS = content;
				DOCUMENTS.sort(function (x, y) {
					return x.name.localeCompare(y.name);
				});
				DESCRIPTORS = new Array(DOCUMENTS.length);
				loadSecurityDescriptors();
			}
		});
	});
}

function loadSecurityDescriptors() {
	if (DOCUMENTS.length == 0)
		return;
	if (!onOperationRequest("Loading ...", DOCUMENTS.length))
		return;
	for (var i = 0; i != DOCUMENTS.length; i++) {
		loadSecurityDescriptor(DOCUMENTS[i], i);
	}
}

function loadSecurityDescriptor(document, index) {
	xowl.getSecuredResourceDescriptor(function (status, ct, content) {
		onOperationEnded(200, content);
		if (status === 200) {
			DESCRIPTORS[index] = content;
		} else {
			DESCRIPTORS[index] = null;
		}
		if (PAGE_BUSY === null)
			renderDocuments();
	}, document.identifier);
}

function renderDocuments() {
	var table = document.getElementById("documents");
	for (var i = 0; i != DOCUMENTS.length; i++) {
		var row = renderDocument(i, DOCUMENTS[i], DESCRIPTORS[i]);
		table.appendChild(row);
	}
}

function renderDocument(index, doc, descriptor) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");

	var icon = renderDescriptorIcon(descriptor);
	cell.appendChild(icon);

	icon = document.createElement("img");
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
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "DELETE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.style.marginRight = "20px";
	button.appendChild(icon);
	button.onclick = function() {
		onClickDelete(doc);
	};
	cell.appendChild(button);
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-plus.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "IMPORT";
	button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(icon);
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/artifact.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "IMPORT";
	button.appendChild(icon);
	button.onclick = function() {
		onClickImport(doc);
	};
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function onClickDelete(doc) {
	popupConfirm("Import Data", richString(["Delete document ", doc, "?"]), function () {
		if (!onOperationRequest(richString(["Dropping document ", doc, " ..."])))
			return;
		xowl.dropUploadedDocument(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Dropped document ", doc, "."]));
				waitAndRefresh();
			}
		}, doc.identifier);
	});
}

function onClickImport(doc) {
	window.location.href = "document-import.html?id=" + encodeURIComponent(doc.identifier);
}