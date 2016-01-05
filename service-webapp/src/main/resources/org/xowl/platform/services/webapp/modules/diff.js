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
		}
	}, ARTIFACTID_LEFT);
	xowl.getArtifactMetadata(function (status, ct, content) {
		if (status == 200) {
			renderMetadataRight(content);
		}
	}, ARTIFACTID_RIGHT);
	xowl.diffArtifacts(function (status, ct, content) {
		if (status == 200) {
			renderDiff(content.left, content.right);
		}
	}, ARTIFACTID_LEFT, ARTIFACTID_RIGHT);
	/*xowl.getArtifactContent(function (status, ct, content) {
		if (status == 200) {
			BASE = parseNQuads(content);
			onRendered();
		}
	}, ARTIFACTID_LEFT);*/
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
		var rows = [];
		var rowTitle = document.createElement();
	}
	onRendered();
}

function onRendered() {
	MARKER++;
	if (MARKER === 3)
		document.getElementById("loader").style.display = "none";
}