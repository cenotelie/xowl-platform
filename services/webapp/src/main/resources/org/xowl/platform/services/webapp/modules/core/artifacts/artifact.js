// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var artifactId = getParameterByName("id");
var artifactName = null;
var METADATA = null;
var CONTENT = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Artifacts Management", uri: "/web/modules/core/artifacts/"},
			{name: "Artifact " + artifactId}], function() {
		if (artifactId === null || artifactId == "")
			return;
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getArtifactMetadata(function (status, ct, content) {
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
			displayMessage("success", { type: "org.xowl.platform.kernel.RichString", parts: ["Launched deletion as job ", content, "."]});
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
	METADATA = parseNQuads(metadata);
	var properties = METADATA[artifactId].properties;
	document.getElementById("artifact-identifier").value = artifactId;
	for (var i = 0; i != properties.length; i++) {
		var name = properties[i].id;
		if (name === "http://xowl.org/platform/schemas/kernel#name") {
			artifactName = properties[i].value.lexical;
			document.getElementById("artifact-name").value = artifactName;
		} else if (name === "http://xowl.org/platform/schemas/kernel#archetype")
			document.getElementById("artifact-archetype").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#from") {
			document.getElementById("artifact-origin").appendChild(document.createTextNode(properties[i].value.lexical));
			document.getElementById("artifact-origin").href = "/web/modules/admin/connectors/connector.html?id=" + encodeURIComponent(properties[i].value.lexical);
		} else if (name === "http://xowl.org/platform/schemas/kernel#created")
			document.getElementById("artifact-creation").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#base") {
			document.getElementById("artifact-base").appendChild(document.createTextNode(properties[i].value.value));
			document.getElementById("artifact-base").href = "base.html?id=" + encodeURIComponent(properties[i].value.value);
		} else if (name === "http://xowl.org/platform/schemas/kernel#version")
			document.getElementById("artifact-version").value = properties[i].value.lexical;
		else if (name === "http://xowl.org/platform/schemas/kernel#supersede") {
			var link = document.createElement("a");
			link.href = "artifact.html?id=" + encodeURIComponent(properties[i].value.value);
			link.appendChild(document.createTextNode(properties[i].value.value));
			var container = document.createElement("div");
			container.appendChild(link);
			document.getElementById("artifact-supersede").appendChild(container);
		}
	}
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
	document.getElementById("button-retrieve").style.display = "none";
}
