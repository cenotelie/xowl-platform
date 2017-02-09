// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Job Management"}], function() {
		refresh();
	});
}

function refresh() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getJobs(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderData(content);
		}
	});
}

function renderData(data) {
	var table = document.getElementById("jobs");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	for (var i = 0; i != data.length; i++) {
		table.appendChild(renderJob(data[i]));
	}
}

function renderJob(job) {
	var row = document.createElement("tr");
	var cells = [ document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")];
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(job.name));
	link.href = "job.html?id=" + encodeURIComponent(job.identifier);
	cells[0].appendChild(link);
	link = document.createElement("a");
	link.appendChild(document.createTextNode(job.owner.name));
	link.href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(job.owner.identifier);
	cells[1].appendChild(link);
	cells[2].appendChild(document.createTextNode(job.status));
	var progress = document.createElement("div");
	progress.className = "progress";
	var progressBar = document.createElement("div");
	progressBar.className = "progress-bar";
	progressBar.role = "progressbar";
	progressBar['aria-valuenow'] = job.completionRate * 100;
	progressBar['aria-valuemin'] = 0;
	progressBar['aria-valuemax'] = 100;
	progressBar.style.width = (job.completionRate * 100).toString() + "%";
	progress.appendChild(progressBar);
	cells[3].appendChild(progress);
	cells[4].appendChild(renderJobResult(job.result));
	if (job.status != "Completed" && job.status != "Cancelled") {
		var button = document.createElement("button");
		button.className = "btn btn-danger";
		button.onclick = function () { onCancelJob(job); };
		button.appendChild(document.createTextNode("Cancel"));
		cells[5].appendChild(button);
	}
	row.appendChild(cells[0]);
	row.appendChild(cells[1]);
	row.appendChild(cells[2]);
	row.appendChild(cells[3]);
	row.appendChild(cells[4]);
	row.appendChild(cells[5]);
	return row;
}

function renderJobResult(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return document.createTextNode("");
	if (!xsp.isSuccess) {
		return document.createTextNode("FAILURE");
	} else {
		return document.createTextNode("SUCCESS");
	}
}

function onCancelJob(job) {
	if (!onOperationRequest("Cancelling job " + job.name + "..."))
		return;
	xowl.cancelJob(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			refresh();
		}
	}, job.identifier);
}