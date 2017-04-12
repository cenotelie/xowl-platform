// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var artifactId = getParameterByName("id");
var artifactName = null;
var CONTENT = null;
var resultCount = 0;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Artifacts Management", uri: ROOT + "/modules/core/artifacts/"},
			{name: "Artifact " + artifactId}], function() {
		if (artifactId === null || artifactId == "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getArtifact(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderMetadata(content);
		}
	}, artifactId);
}


function onClickRetrieveContent() {
	if (artifactId === null || artifactId == "")
		return;
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getArtifactContent(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderContent(content);
		}
	}, artifactId);
}

function onClickDelete() {
	if (artifactId === null || artifactId == "")
		return;
	var result = confirm("Delete artifact " + artifactName + " ?");
	if (!result)
		return;
	if (!onOperationRequest("Launching job for deletion of artifact " + artifactName + " ..."))
		return;
	xowl.deleteArtifact(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched deletion as job ", content, "."]});
			waitForJob(content.identifier, content.name, function (job) {
				onJobCompleted(job);
			});
		}
	}, artifactId);
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		displayMessage("success", "Deleted artifact " + artifactName + ".");
		waitAndGo("index.html");
	}
}

function renderMetadata(metadata) {
	document.getElementById("artifact-identifier").value = metadata.identifier;
	document.getElementById("artifact-name").value = metadata.name;
	document.getElementById("artifact-archetype").value = metadata.archetype;
	document.getElementById("artifact-origin").appendChild(document.createTextNode(metadata.from));
	document.getElementById("artifact-origin").href = ROOT + "/modules/admin/connectors/connector.html?id=" + encodeURIComponent(metadata.from);
	document.getElementById("artifact-base").appendChild(document.createTextNode(metadata.base));
	document.getElementById("artifact-base").href = "base.html?id=" + encodeURIComponent(metadata.base);
	document.getElementById("artifact-creation").value = metadata.creation;
	document.getElementById("artifact-version").value = metadata.version;
	if (metadata.superseded.length > 0) {
		var link = document.createElement("a");
		link.href = "artifact.html?id=" + encodeURIComponent(metadata.superseded);
		link.appendChild(document.createTextNode(metadata.superseded));
		var container = document.createElement("div");
		container.appendChild(link);
		document.getElementById("artifact-superseded").appendChild(container);
	}
}

function renderContent(content) {
	renderRdfQuads(content, injectResult);
}

function injectResult(row) {
	resultCount++;
	var data = document.getElementById("content");
	var cell = document.createElement("td");
	cell.appendChild(document.createTextNode(resultCount.toString()));
	row.replaceChild(cell, row.firstChild);
	data.appendChild(row);
}