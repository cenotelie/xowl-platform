// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTID = getParameterByName("id");
var METADATA = null;
var CONTENT = null;

function init() {
	if (ARTIFACTID === null || ARTIFACTID == "")
		return;
	xowl.getArtifactMetadata(function (status, ct, content) {
		if (status == 200) {
			renderMetadata(content);
		} else {
			displayError(content);
		}
	}, ARTIFACTID);
}

function onClickRetrieveContent() {
	if (ARTIFACTID === null || ARTIFACTID == "")
		return;
	document.getElementById("loader").style.display = "";
	xowl.getArtifactContent(function (status, ct, content) {
		if (status == 200) {
			renderContent(content);
		} else {
			displayError(content);
		}
	}, ARTIFACTID);
}

function renderMetadata(metadata) {
	METADATA = parseNQuads(metadata);
	var properties = METADATA[ARTIFACTID].properties;
	document.getElementById("artifact-identifier").value = ARTIFACTID;
	var table = document.getElementById("metadata");
	for (var i = 0; i != properties.length; i++) {
		var name = properties[i].id;
		if (name === "http://xowl.org/platform/schemas/kernel#name")
			document.getElementById("artifact-name").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#base")
			document.getElementById("artifact-base").value = properties[i].value.value;
		else if (name === "http://xowl.org/platform/schemas/kernel#version")
			document.getElementById("artifact-version").value = properties[i].value.lexical;
		else {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			cell1.appendChild(document.createTextNode(name));
			cell2.appendChild(document.createTextNode(rdfToString(properties[i].value)));
			row.appendChild(cell1);
			row.appendChild(cell2);
			table.appendChild(row);
		}
	}
	document.getElementById("loader").style.display = "none";
}

function renderContent(content) {
	CONTENT = parseNQuads(content);
	var table = document.getElementById("content");
	var names = Object.getOwnPropertyNames(CONTENT);
	for (var p = 0; p != names.length; p++) {
		var entity = CONTENT[names[p]];
		for (var j = 0; j != entity.properties.length; j++) {
			var property = entity.properties[j];
			var row = document.createElement("tr");
			var cell0 = document.createElement("td");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var cell3 = document.createElement("td");
			cell0.appendChild(document.createTextNode((p + 1).toString() + "." + (j + 1).toString()));
			cell0.className = "entity" + (p % 2).toString();
			cell1.appendChild(document.createTextNode(entity.id));
			cell1.className = "entity" + (p % 2).toString();
			cell2.appendChild(document.createTextNode(property.id));
			cell2.className = "entity" + (p % 2).toString();
			cell3.appendChild(document.createTextNode(rdfToString(property.value)));
			cell3.className = "entity" + (p % 2).toString();
			row.appendChild(cell0);
			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell3);
			table.appendChild(row);
		}
	}
	document.getElementById("loader").style.display = "none";
	document.getElementById("button-retrieve").style.display = "none";
}
