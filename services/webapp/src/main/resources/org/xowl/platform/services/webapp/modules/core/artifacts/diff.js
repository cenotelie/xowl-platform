// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var ARTIFACTID_LEFT = getParameterByName("left");
var ARTIFACTID_RIGHT = getParameterByName("right");
var ARTIFACT_LEFT = null;
var ARTIFACT_RIGHT = null;
var BASE = null;
var MARKER = 0;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Manage Artifacts", uri: ROOT + "/modules/core/artifacts/"},
			{name: "Diff"}], function() {
		if (ARTIFACTID_LEFT === null || ARTIFACTID_LEFT == "")
			return;
		if (ARTIFACTID_RIGHT === null || ARTIFACTID_RIGHT == "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 3))
		return;
	xowl.getArtifact(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderMetadataLeft(content);
		}
	}, ARTIFACTID_LEFT);
	xowl.getArtifact(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderMetadataRight(content);
		}
	}, ARTIFACTID_RIGHT);
	xowl.diffArtifacts(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderDiff(content.added, content.removed);
		}
	}, ARTIFACTID_LEFT, ARTIFACTID_RIGHT);
}

function renderMetadataLeft(metadata) {
	document.getElementById("artifact-left-identifier").value = ARTIFACTID_LEFT;
	document.getElementById("artifact-left-name").value = metadata.name;
	document.getElementById("artifact-left-version").value = metadata.version;
}

function renderMetadataRight(metadata) {
	document.getElementById("artifact-right-identifier").value = ARTIFACTID_RIGHT;
	document.getElementById("artifact-right-name").value = metadata.name;
	document.getElementById("artifact-right-version").value = metadata.version;
}

function renderDiff(added, removed) {
	ARTIFACT_LEFT = added[0].entities;
	ARTIFACT_RIGHT = removed[0].entities;
	var table = document.getElementById("content");
	for (var i = 0; i != ARTIFACT_LEFT.length; i++) {
		var entity = ARTIFACT_LEFT[i]; // the current entity on the left
		var secondary = null; // the same entity on the right
		for (var j = 0; j != ARTIFACT_RIGHT.length; j++) {
			var candidate = ARTIFACT_RIGHT[j];
			if (entity.subject.type === candidate.subject.type && entity.subject.value == candidate.subject.value) {
				secondary = candidate;
				break;
			}
		}
		table.appendChild(renderGetRowPad());
		table.appendChild(renderGetRowHeader(entity.subject));
		var done = [];
		for (var p = 0; p != entity.properties.length; p++) {
			var property = entity.properties[p];
			for (var v = 0; v != property.values.length; v++) {
				table.appendChild(renderGetRowDiff("minus", property.property, property.values[v]));
			}
			if (secondary !== null) {
				for (var p2 = 0; p2 != secondary.properties.length; p2++) {
					var property2 = secondary.properties[p2];
					if (property.property.value == property2.property.value) {
						for (v = 0; v != property2.values.length; v++) {
							table.appendChild(renderGetRowDiff("plus", property.property, property2.values[v]));
						}
					}
				}
			}
			done.push(property.property.value);
		}
		if (secondary === null)
			continue;
		for (var p = 0; p != secondary.properties.length; p++) {
			var property = secondary.properties[p];
			if (done.indexOf(property.property.value) >= 0)
				continue;
			for (var v = 0; v != property.values.length; v++) {
				table.appendChild(renderGetRowDiff("plus", property.property, property.values[v]));
			}
		}
	}
	for (var i = 0; i != ARTIFACT_RIGHT.length; i++) {
		var entity = ARTIFACT_RIGHT[i]; // the current entity on the right
		var secondary = null; // the same entity on the left
		for (var j = 0; j != ARTIFACT_LEFT.length; j++) {
			var candidate = ARTIFACT_LEFT[j];
			if (entity.subject.type === candidate.subject.type && entity.subject.value == candidate.subject.value) {
				secondary = candidate;
				break;
			}
		}
		if (secondary != null)
			continue;
		table.appendChild(renderGetRowPad());
		table.appendChild(renderGetRowHeader(entity.subject));
		for (var p = 0; p != entity.properties.length; p++) {
			var property = entity.properties[p];
			for (var v = 0; v != property.values.length; v++) {
				table.appendChild(renderGetRowDiff("plus", property.property, property.values[v]));
			}
		}
	}
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

function renderGetRowHeader(subject) {
	var row = document.createElement("tr");
	row.className = "diff-entity";
	var cellTitle = document.createElement("td");
	var title = document.createElement("strong");
	title.appendChild(renderRdfNode(subject));
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
	cell2.appendChild(renderRdfNode(property));
	var cell3 = document.createElement("td");
	cell3.appendChild(document.createTextNode("="));
	var cell4 = document.createElement("td");
	cell4.appendChild(renderRdfNode(value));
	row.appendChild(cell1);
	row.appendChild(cell2);
	row.appendChild(cell3);
	row.appendChild(cell4);
	return row;
}