// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: "/web/modules/core/"},
			{name: "Data Import", uri: "/web/modules/core/importation/"},
			{name: "Upload New Document"}], function() {
	});
}

function onFileSelected() {
	var file = document.getElementById("input-file").files[0];
	var name = document.getElementById("input-name").value;
	if (name == null || name.length == 0) {
		document.getElementById("input-name").value = file.name;
	}
}

function onUpload() {
	var name = document.getElementById("input-name").value;
	if (name === null)
		return;
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
		if (!onOperationRequest("Sending ..."))
			return;
		xowl.uploadDocument(function (status, type, content) {
			if (onOperationEnded(status, content)) {
				progressBar.classList.add("progress-bar-success");
				displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Uploaded document ", content, "."]});
				waitAndGo("document.html?id=" + encodeURIComponent(content.identifier));
			} else {
				progressBar.classList.add("progress-bar-error");
			}
			progressBar['aria-valuenow'] = 100;
			progressBar.style.width = "100%";
		}, name, reader.result, file.name);
	}
	if (!onOperationRequest("Reading ..."))
		return;
	reader.readAsBinaryString(file);
}