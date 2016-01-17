// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var CONNECTOR = getParameterByName("connector");

function init() {
	if (!CONNECTOR || CONNECTOR === null || CONNECTOR === "")
		document.location.href = "upload.html";
	document.getElementById("panel-next").style.display = "none";
	document.getElementById("input-connector").value = CONNECTOR;
}

function onInput() {
	var name = document.getElementById("input-name").value;
	var base = document.getElementById("input-base").value;
	var version = document.getElementById("input-init-version").value;
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
		"&what=new" +
		"&name=" + encodeURIComponent(name) +
		"&base=" + encodeURIComponent(base) +
		"&version=" + encodeURIComponent(version);
}