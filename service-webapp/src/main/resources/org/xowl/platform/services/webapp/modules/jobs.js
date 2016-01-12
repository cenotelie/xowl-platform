// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	xowl.getJobs(function (status, ct, content) {
		if (status == 200) {
			renderQueue(content.scheduled);
			renderRunning(content.running);
			renderCompleted(content.completed);
			document.getElementById("loader").style.display = "none";
		} else {
			displayError(content);
		}
	});
}

function renderQueue(jobs) {
	var data = "";
	for (var  i=0;i != jobs.length; i++) {
		data += "<tr><td><a href=\"job.html?id=";
		data += encodeURIComponent(jobs[i].identifier);
		data += "\">";
		data += i.toString();
		data += "</a></td><td>";
		data += jobs[i].name;
		data += "</td><td>";
		data += jobs[i].timeScheduled;
		data += "</td></tr>";
	}
	document.getElementById("jobs-scheduled").innerHTML = data;
}

function renderRunning(jobs) {
	var data = "";
	for (var  i=0;i != jobs.length; i++) {
		data += "<tr><td><a href=\"job.html?id=";
		data += encodeURIComponent(jobs[i].identifier);
		data += "\">";
		data += i.toString();
		data += "</a></td><td>";
		data += jobs[i].name;
		data += "</td><td>";
		data += jobs[i].timeRun;
		data += "</td></tr>";
	}
	document.getElementById("jobs-scheduled").innerHTML = data;
}

function renderCompleted(jobs) {
	var data = "";
	for (var  i=0;i != jobs.length; i++) {
		data += "<tr><td><a href=\"job.html?id=";
		data += encodeURIComponent(jobs[i].identifier);
		data += "\">";
		data += i.toString();
		data += "</a></td><td>";
		data += jobs[i].name;
		data += "</td><td>";
		data += jobs[i].timeCompleted;
		data += "</td><td>";
		data += renderXSPReply(jobs[i].result);
		data += "</td></tr>";
	}
	document.getElementById("jobs-completed").innerHTML = data;
}