// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Manage Consistency", uri: ROOT + "/modules/core/consistency/"},
			{name: "Reasoning Rules", uri: ROOT + "/modules/core/consistency/rules.html"},
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
	doImport(definitions, 0);
}

function doImport(definitions, index) {
	if (index >= definitions.length)
		return;
	var definition = definitions[index];
	if (!onOperationRequest("Importing reasoning rule " + definition.name + " ..."))
		return false;
	xowl.addReasoningRule(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "fr.cenotelie.commons.utils.RichString", parts: ["Created reasoning rule ", content, "."]});
			if (index + 1 < definitions.length) {
				doImport(definitions, index + 1);
			} else {
				waitAndGo("rules.html");
			}
		}
	}, definition);
}