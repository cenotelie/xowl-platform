// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	refresh();
}

function refresh() {
	displayMessage("Loading ...");
	xowl.getJobs(function (status, ct, content) {
		if (status == 200) {
			renderData(content);
			document.getElementById("loader").style.display = "none";
		} else {
			displayMessage(getErrorFor(status, content));
		}
	});
}

function renderData(data) {
	var table = document.getElementById("jobs");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	renderJobs(table, data.scheduled);
	renderJobs(table, data.running);
	renderJobs(table, data.completed);
}

function renderJobs(table, jobs) {
	for (var i = 0; i != jobs.length; i++) {
		table.appendChild(renderJob(jobs[i]));
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
	link.href="job.html?id=" + encodeURIComponent(job.identifier);
	cells[0].appendChild(link);
	link = document.createElement("a");
	link.appendChild(document.createTextNode(job.owner.name));
	link.href="/web/modules/admin/security/user.html?id=" + encodeURIComponent(job.owner.id);
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
	cells[4].appendChild(document.createTextNode(renderXSPReply(job.result)));
	if (job.status != "Completed" && job.status != "Cancelled") {
		var button = document.createElement("button");
		button.className = "btn btn-danger";
		button.onclick = function () {
			xowl.cancelJob(function (status, ct, content) {
				if (status == 200) {
					refresh();
				} else {
					displayMessage(getErrorFor(status, content));
				}
			}, job.identifier);
		};
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