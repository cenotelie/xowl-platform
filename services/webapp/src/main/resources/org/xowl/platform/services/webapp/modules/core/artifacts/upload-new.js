// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var CONNECTOR = getParameterByName("connector");

function init() {
	setupPage(xowl);
	if (!CONNECTOR || CONNECTOR === null || CONNECTOR === "")
		document.location.href = "upload.html";
	document.getElementById("panel-next").style.display = "none";
	document.getElementById("input-connector").value = CONNECTOR;
	xowl.getArtifactArchetypes(function (status, ct, content) {
		if (status == 200) {
			renderArchetypes(content);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
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
	displayMessage(null);
}

function onInput() {
	var name = document.getElementById("input-name").value;
	var base = document.getElementById("input-base").value;
	var version = document.getElementById("input-init-version").value;
	var archetype = document.getElementById("input-archetype").value;
	if (name === null || name === "" || base === null || base === "" || version === null || version === "" || archetype === null || archetype === "") {
		document.getElementById("btn-file").href = "";
		document.getElementById("btn-content").href = "";
		document.getElementById("panel-next").style.display = "none";
	} else {
		var params = getParams(name, base, version, archetype);
		document.getElementById("btn-file").href = "upload-file.html" + params;
		document.getElementById("btn-content").href = "upload-content.html" + params;
		document.getElementById("panel-next").style.display = "";
	}
}

function getParams(name, base, version, archetype) {
	return "?connector=" + encodeURIComponent(CONNECTOR) +
		"&what=new" +
		"&name=" + encodeURIComponent(name) +
		"&base=" + encodeURIComponent(base) +
		"&version=" + encodeURIComponent(version) +
		"&archetype=" + encodeURIComponent(archetype) +
		"&superseded=none";
}