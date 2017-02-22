// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Consistency Management", uri: ROOT + "/modules/core/consistency/"},
			{name: "Rules", uri: ROOT + "/modules/core/consistency/rules.html"},
			{name: "Upload"}], function() {
	});
}

function onUpload() {
	if (document.getElementById("input-file").files.length == 0)
		return;
	var file = document.getElementById("input-file").files[0];
	var progressBar = document.getElementById("import-progress");
	progressBar['aria-valuenow'] = 0;
	progressBar.style.width = "0%";
	progressBar.classList.remove("progress-bar-success");
	progressBar.classList.remove("progress-bar-error");
	progressBar.innerHTML = null;
	var reader = new FileReader();
	reader.onprogress = function (event) {
		var ratio = 100 * event.loaded / event.total;
		progressBar['aria-valuenow'] = ratio;
		progressBar.style.width = ratio.toString() + "%";
	}
	reader.onloadend = function (event) {
		if (reader.error !== null) {
			onOperationEnded(500, "", reader.error.toString());
			progressBar['aria-valuenow'] = 100;
			progressBar.style.width = "100%";
			progressBar.classList.add("progress-bar-error");
			return;
		}
		onOperationEnded(200, "");
		progressBar.classList.add("progress-bar-success");
		progressBar['aria-valuenow'] = 100;
		progressBar.style.width = "100%";
		onRead(reader.result);
	}
	if (!onOperationRequest("Reading ..."))
		return;
	reader.readAsText(file);
}

function onRead(input) {
	var definitions = JSON.parse(input);
	if (!Array.isArray(definitions)) {
		definitions = [definitions];
	}
	for (var i = 0; i != definitions.length; i++) {
		doImport(definitions[i]);
	}
}

function doImport(definition) {
	if (!onOperationRequest("Importing rule " + definition.name + " ..."))
		return false;
	xowl.addConsistencyRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Created rule ", content, "."]});
			waitAndGo("rules.html");
		}
	}, definition);
}