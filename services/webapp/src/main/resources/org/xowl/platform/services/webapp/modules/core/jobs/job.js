// Copyright (c) 2016 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var jobId = getParameterByName("id");

function init() {
	if (!jobId || jobId === null || jobId === "")
		return;
	document.getElementById("placeholder-job").innerHTML = jobId;
	displayMessage("Loading ...");
	xowl.getJob(function (status, ct, content) {
		if (status == 200) {
			render(content);
			if (content.status !== "Completed") {
				window.setTimeout(init, 2000);
			}
			displayMessage(null);
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, jobId);
}

function render(job) {
	document.getElementById("job-identifier").value = job.identifier;
	document.getElementById("job-name").value = job.name;
	document.getElementById("job-type").value = job.type;
	document.getElementById("job-status").value = job.status;
	document.getElementById("job-time-scheduled").value = job.timeScheduled;
	document.getElementById("job-time-run").value = job.timeRun;
	document.getElementById("job-time-completed").value = job.timeCompleted;
	document.getElementById("job-payload").innerHTML = renderJobPayload(job.payload);
	document.getElementById("job-result").innerHTML = renderXSPReply(job.result);
}