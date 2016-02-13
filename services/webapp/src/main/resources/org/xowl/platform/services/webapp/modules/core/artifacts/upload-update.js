// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var CONNECTOR = getParameterByName("connector");
var ARTIFACTS = {};

function init() {
	setupPage(xowl);
	if (!CONNECTOR || CONNECTOR === null || CONNECTOR === "")
		document.location.href = "upload.html";
	document.getElementById("input-connector").value = CONNECTOR;
	document.getElementById("panel-next").style.display = "none";
	xowl.getAllArtifacts(function (status, ct, content) {
		if (status == 200) {
			renderFamilies(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (status == 200) {
			renderArchetypes(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderFamilies(data) {
	for (var i = 0; i != data.length; i++) {
		var artifact = data[i];
		if (artifact.hasOwnProperty("base") && artifact.base != "") {
			if (ARTIFACTS.hasOwnProperty(artifact.base)) {
				ARTIFACTS[artifact.base].push(artifact);
			} else {
				ARTIFACTS[artifact.base] = [artifact];
			}
		}
	}
	var select = document.getElementById("input-family");
	var names = Object.getOwnPropertyNames(ARTIFACTS);
	for (var i = 0; i != names.length; i++) {
		var elements = ARTIFACTS[names[i]];
		elements.sort(function (x, y) {
			return y.version.localeCompare(x.version);
		});
		var option = document.createElement("option");
		option.value = elements[0].base;
		option.appendChild(document.createTextNode(elements[0].name));
		select.appendChild(option);
	}
	if (names.length > 0) {
		select.value = ARTIFACTS[names[0]][0].base;
		renderArtifacts(ARTIFACTS[names[0]]);
	}
	displayMessage(null);
}

function renderArtifacts(artifacts) {
	var base = document.getElementById("input-family").value;
	if (base === null || base === "")
		return;
	var select = document.getElementById("input-superseded");
	while (select.hasChildNodes())
		select.removeChild(select.lastChild);
	var option = document.createElement("option");
	option.value = "none";
	option.appendChild(document.createTextNode("None"));
	select.appendChild(option);
	for (var i = 0; i != ARTIFACTS[base].length; i++) {
		var artifact = ARTIFACTS[base][i];
		option = document.createElement("option");
		option.value = artifact.identifier;
		option.appendChild(document.createTextNode(artifact.name + " (" + artifact.version + ")"));
		select.appendChild(option);
	}
	select.value = "none";
}

function renderArchetypes(data) {
	var select = document.getElementById("input-archetype");
	for (var i = 0; i != data.length; i++) {
		var option = document.createElement("option");
		option.value = data[i].id;
		option.appendChild(document.createTextNode(data[i].name));
		select.appendChild(option);
	}
	if (data.length > 0)
		select.value = data[0].id;
}

function onFamilyChange() {
	var base = document.getElementById("input-family").value;
	if (base !== null || base !== "")
		renderArtifacts(ARTIFACTS[base]);
	onInput();
}

function onInput() {
	var base = document.getElementById("input-family").value;
	var superseded = document.getElementById("input-superseded").value;
	var version = document.getElementById("input-new-version").value;
	var archetype = document.getElementById("input-archetype").value;
	if (base === null || base === "" || version === null || version === "" || superseded === null || superseded === "" || archetype === null || archetype === "") {
		document.getElementById("btn-file").href = "";
		document.getElementById("btn-content").href = "";
		document.getElementById("panel-next").style.display = "none";
	} else {
		var name = null;
		if (superseded === "none") {
			name = ARTIFACTS[base][0].name;
		} else {
			for (var i = 0; i != ARTIFACTS[base].length; i++) {
				if (ARTIFACTS[base][i].identifier === superseded) {
					name = ARTIFACTS[base][i].name;
					break;
				}
			}
			if (name === null)
				name = ARTIFACTS[base][0].name;
		}
		var params = getParams(name, base, version, archetype, superseded);
		document.getElementById("btn-file").href = "upload-file.html" + params;
		document.getElementById("btn-content").href = "upload-content.html" + params;
		document.getElementById("panel-next").style.display = "";
	}
}

function getParams(name, base, version, archetype, superseded) {
	return "?connector=" + encodeURIComponent(CONNECTOR) +
		"&what=new" +
		"&name=" + encodeURIComponent(name) +
		"&base=" + encodeURIComponent(base) +
		"&version=" + encodeURIComponent(version) +
		"&archetype=" + encodeURIComponent(archetype) +
		"&superseded=" + encodeURIComponent(superseded);
}