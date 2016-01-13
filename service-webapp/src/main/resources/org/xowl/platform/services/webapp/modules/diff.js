// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTID_LEFT = getParameterByName("left");
var ARTIFACTID_RIGHT = getParameterByName("right");
var ARTIFACT_LEFT = null;
var ARTIFACT_RIGHT = null;
var BASE = null;
var MARKER = 0;

function init() {
	if (ARTIFACTID_LEFT === null || ARTIFACTID_LEFT == "")
		return;
	if (ARTIFACTID_RIGHT === null || ARTIFACTID_RIGHT == "")
		return;
	xowl.getArtifactMetadata(function (status, ct, content) {
		if (status == 200) {
			renderMetadataLeft(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, ARTIFACTID_LEFT);
	xowl.getArtifactMetadata(function (status, ct, content) {
		if (status == 200) {
			renderMetadataRight(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, ARTIFACTID_RIGHT);
	xowl.diffArtifacts(function (status, ct, content) {
		if (status == 200) {
			renderDiff(content.left, content.right);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, ARTIFACTID_LEFT, ARTIFACTID_RIGHT);
}

function renderMetadataLeft(metadata) {
	metadata = parseNQuads(metadata);
	var properties = metadata[ARTIFACTID_LEFT].properties;
	document.getElementById("artifact-left-identifier").value = ARTIFACTID_LEFT;
	for (var i = 0; i != properties.length; i++) {
		var name = properties[i].id;
		if (name === "http://xowl.org/platform/schemas/kernel#name")
			document.getElementById("artifact-left-name").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#version")
			document.getElementById("artifact-left-version").value = properties[i].value.lexical;
	}
	onRendered();
}

function renderMetadataRight(metadata) {
	metadata = parseNQuads(metadata);
	var properties = metadata[ARTIFACTID_RIGHT].properties;
	document.getElementById("artifact-right-identifier").value = ARTIFACTID_RIGHT;
	for (var i = 0; i != properties.length; i++) {
		var name = properties[i].id;
		if (name === "http://xowl.org/platform/schemas/kernel#name")
			document.getElementById("artifact-right-name").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#version")
			document.getElementById("artifact-right-version").value = properties[i].value.lexical;
	}
	onRendered();
}

function renderDiff(left, right) {
	ARTIFACT_LEFT = parseNQuads(left);
	ARTIFACT_RIGHT = parseNQuads(right);
	var table = document.getElementById("content");
	var namesLeft = Object.getOwnPropertyNames(ARTIFACT_LEFT);
	var namesRight = Object.getOwnPropertyNames(ARTIFACT_RIGHT);
	for (var p = 0; p != namesLeft.length; p++) {
		var entity = ARTIFACT_LEFT[namesLeft[p]];
		var secondary = (ARTIFACT_RIGHT.hasOwnProperty(namesLeft[p]) ? ARTIFACT_RIGHT[namesLeft[p]] : null);
		table.appendChild(renderGetRowPad());
		table.appendChild(renderGetRowHeader(namesLeft[p]));
		var done = [];
		for (var i = 0; i != entity.properties.length; i++) {
			var property = entity.properties[i];
			if (done.indexOf(property.id) >= 0)
				continue;
			for (var j = i; j != entity.properties.length; j++) {
				if (entity.properties[j].id === property.id)
					table.appendChild(renderGetRowDiff("minus", property.id, entity.properties[j].value));
			}
			if (secondary !== null) {
				for (var j = i; j != secondary.properties.length; j++) {
					if (secondary.properties[j].id === property.id)
						table.appendChild(renderGetRowDiff("plus", property.id, secondary.properties[j].value));
				}
			}
			done.push(property.id);
		}
		if (secondary === null)
			continue;
		for (var i = 0; i != secondary.properties.length; i++) {
			var property = secondary.properties[i];
			if (done.indexOf(property.id) >= 0)
				continue;
			for (var j = i; j != secondary.properties.length; j++) {
				if (secondary.properties[j].id === property.id)
					table.appendChild(renderGetRowDiff("plus", property.id, secondary.properties[j].value));
			}
		}
	}
	for (var p = 0; p != namesRight.length; p++) {
		var entity = ARTIFACT_RIGHT[namesRight[p]];
		var secondary = (ARTIFACT_LEFT.hasOwnProperty(namesRight[p]) ? ARTIFACT_LEFT[namesRight[p]] : null);
		var done = [];
		var emitHeader = true;
		for (var i = 0; i != entity.properties.length; i++) {
			var property = entity.properties[i];
			if (done.indexOf(property.id) >= 0)
				continue;
			if (secondary !== null) {
				var found = false;
				for (var j = i; j != secondary.properties.length; j++) {
					if (secondary.properties[j].id === property.id) {
						found = true;
						break;
					}
				}
				if (found)
					continue;
			}
			for (var j = i; j != entity.properties.length; j++) {
				if (entity.properties[j].id === property.id) {
					if (emitHeader) {
						table.appendChild(renderGetRowPad());
						table.appendChild(renderGetRowHeader(namesRight[p]));
						emitHeader = false;
					}
					table.appendChild(renderGetRowDiff("plus", property.id, entity.properties[j].value));
				}
			}
			done.push(property.id);
		}
	}
	onRendered();
}

function renderGetRowPad() {
	var row = document.createElement("tr");
	row.className = "diff-empty";
	row.appendChild(document.createElement("td"));
	row.appendChild(document.createElement("td"));
	row.appendChild(document.createElement("td"));
	row.appendChild(document.createElement("td"));
	return row;
}

function renderGetRowHeader(name) {
	var row = document.createElement("tr");
	row.className = "diff-entity";
	var cellTitle = document.createElement("td");
	var title = document.createElement("strong");
	title.appendChild(rdfToDom({ type: "iri", value: name }));
	cellTitle.appendChild(title);
	var cellButton = document.createElement("td");
	var span = document.createElement("span");
	span.className = "badge";
	span.style.cursor = "pointer";
	span.appendChild(document.createTextNode("context"));
	cellButton.appendChild(span);
	row.appendChild(document.createElement("td"));
	row.appendChild(cellTitle);
	row.appendChild(document.createElement("td"));
	row.appendChild(cellButton);
	return row;
}

function renderGetRowDiff(type, property, value) {
	var row = document.createElement("tr");
	row.className = "diff-" + type;
	var indicator = document.createElement("span");
	indicator.className = "glyphicon glyphicon-" + type;
	indicator.setAttribute("aria-hidden", "true");
	var cell1 = document.createElement("td");
	cell1.appendChild(indicator);
	var cell2 = document.createElement("td");
	cell2.appendChild(rdfToDom({ type: "iri", value: property }));
	var cell3 = document.createElement("td");
	cell3.appendChild(document.createTextNode("="));
	var cell4 = document.createElement("td");
	cell4.appendChild(rdfToDom(value));
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	return row;
}

function onRendered() {
	MARKER++;
	if (MARKER === 3)
		document.getElementById("loader").style.display = "none";
}