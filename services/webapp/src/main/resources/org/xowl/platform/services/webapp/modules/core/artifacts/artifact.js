// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTID = getParameterByName("id");
var METADATA = null;
var CONTENT = null;

function init() {
	if (ARTIFACTID === null || ARTIFACTID == "")
		return;
	document.getElementById("placeholder-artifact").innerHTML = ARTIFACTID;
	displayMessage("Loading ...");
	xowl.getArtifactMetadata(function (status, ct, content) {
		if (status == 200) {
			renderMetadata(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, ARTIFACTID);
}

function onClickRetrieveContent() {
	if (ARTIFACTID === null || ARTIFACTID == "")
		return;
	displayMessage("Loading ...");
	xowl.getArtifactContent(function (status, ct, content) {
		if (status == 200) {
			renderContent(content);
		} else {
			displayMessage(getErrorFor(status, content));
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
			cell1.appendChild(rdfToDom({ type: "iri", value: name }));
			cell2.appendChild(rdfToDom(properties[i].value));
			row.appendChild(cell1);
			row.appendChild(cell2);
			table.appendChild(row);
		}
	}
	displayMessage(null);
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
			cell1.appendChild(rdfToDom({ type: "iri", value: entity.id }));
			cell1.className = "entity" + (p % 2).toString();
			cell2.appendChild(rdfToDom({ type: "iri", value: property.id }));
			cell2.className = "entity" + (p % 2).toString();
			cell3.appendChild(rdfToDom(property.value));
			cell3.className = "entity" + (p % 2).toString();
			row.appendChild(cell0);
			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell3);
			table.appendChild(row);
		}
	}
	displayMessage(null);
	document.getElementById("button-retrieve").style.display = "none";
}
