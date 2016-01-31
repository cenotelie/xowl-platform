// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var CONNECTOR = getParameterByName("connector");
var ARTIFACTS_ALL = null;
var ARTIFACTS = {};

function init() {
	setupPage(xowl);
	if (!CONNECTOR || CONNECTOR === null || CONNECTOR === "")
		document.location.href = "upload.html";
	document.getElementById("input-connector").value = CONNECTOR;
	document.getElementById("panel-next").style.display = "none";
	xowl.getAllArtifacts(function (status, ct, content) {
		if (status == 200) {
			ARTIFACTS_ALL = content;
			renderArtifacts();
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderArtifacts() {
	for (var i = 0; i != ARTIFACTS_ALL.length; i++) {
		var artifact = ARTIFACTS_ALL[i];
		if (artifact.hasOwnProperty("base") && artifact.base != "") {
			if (ARTIFACTS.hasOwnProperty(artifact.base)) {
				ARTIFACTS[artifact.base].push(artifact);
			} else {
				ARTIFACTS[artifact.base] = [artifact];
			}
		}
	}
	var select = document.getElementById("input-artifact");
	var names = Object.getOwnPropertyNames(ARTIFACTS);
	for (var i = 0; i != names.length; i++) {
		var elements = ARTIFACTS[names[i]];
		elements.sort(function (x, y) {
			return y.version.localeCompare(x.version);
		});
		var option = document.createElement("option");
		option.value = elements[0].base;
		option.appendChild(document.createTextNode(elements[0].name + " (" + elements[0].version + ")"));
		option.setAttribute("artifact-name", elements[0].name);
		select.appendChild(option);
	}
	if (names.length > 0)
		select.value = ARTIFACTS[names[0]][0].base;
	displayMessage(null);
}

function onInput() {
	var select = document.getElementById("input-artifact");
	var base = select.value;
	var name = select.childNodes.item(select.selectedIndex).getAttribute("artifact-name");
	var version = document.getElementById("input-new-version").value;
	if (name === null || name === "" || base === null || base === "" || version === null || version === "") {
		document.getElementById("btn-file").href = "";
		document.getElementById("btn-content").href = "";
		document.getElementById("panel-next").style.display = "none";
	} else {
		var params = getParams(name, base, version);
		document.getElementById("btn-file").href = "upload-file.html" + params;
		document.getElementById("btn-content").href = "upload-content.html" + params;
		document.getElementById("panel-next").style.display = "";
	}
}

function getParams(name, base, version) {
	return "?connector=" + encodeURIComponent(CONNECTOR) +
		"&what=update" +
		"&name=" + encodeURIComponent(name) +
		"&base=" + encodeURIComponent(base) +
		"&version=" + encodeURIComponent(version);
}