// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	displayMessage(null);
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
		displayMessage("Reading ...");
	}
	reader.onloadend = function (event) {
		if (reader.error !== null) {
			displayMessage("Error: " + reader.error.toString());
			progressBar['aria-valuenow'] = 100;
			progressBar.style.width = "100%";
			progressBar.classList.add("progress-bar-error");
			return;
		}
		displayMessage("Sending ...");
		xowl.uploadDocument(function (code, type, content) {
			if (code === 200) {
				progressBar.classList.add("progress-bar-success");
				displayMessage(null);
				window.location.href = "document.html?id=" + encodeURIComponent(content.identifier);
			} else {
				displayMessage(getErrorFor(code, content));
				progressBar.classList.add("progress-bar-error");
			}
			progressBar['aria-valuenow'] = 100;
			progressBar.style.width = "100%";
		}, name, reader.result);
	}
	reader.readAsBinaryString(file);
}