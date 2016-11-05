// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var jobId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Job Management", uri: "/web/modules/admin/jobs/"},
			{name: "Job " + jobId}], function() {
		if (!jobId || jobId === null || jobId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getJob(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				render(content);
				if (content.status !== "Completed")
					window.setTimeout(init, 2000);
			}
		}, jobId);
	});
}

function render(job) {
	document.getElementById("job-identifier").value = job.identifier;
	document.getElementById("job-name").value = job.name;
	document.getElementById("job-type").value = job.jobType;
	document.getElementById("job-owner").href = "/web/modules/admin/security/user.html?id=" + encodeURIComponent();
	document.getElementById("job-owner").appendChild(document.createTextNode(job.owner.name));
	document.getElementById("job-status").value = job.status;
	document.getElementById("job-time-scheduled").value = job.timeScheduled;
	document.getElementById("job-time-run").value = job.timeRun;
	document.getElementById("job-time-completed").value = job.timeCompleted;
	document.getElementById("job-payload").appendChild(renderJobPayload(job.payload));
	document.getElementById("job-result").appendChild(renderJobResult(job.result));
}

function renderJobPayload(payload) {
	if (payload instanceof String || typeof payload === 'string')
		return document.createTextNode(payload);
	return document.createTextNode(JSON.stringify(payload));
}

function renderJobResult(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return document.createTextNode("No result ...");
	if (!xsp.isSuccess) {
		return document.createTextNode("FAILURE: " + xsp.message);
	} else if (xsp.hasOwnProperty("payload")) {
		if (xsp.payload == null)
			return document.createTextNode("SUCCESS: " + xsp.message);
		if (xsp.payload instanceof String)
			return document.createTextNode(xsp.payload);
		return document.createTextNode(JSON.stringify(xsp.payload));
	} else {
		return document.createTextNode("SUCCESS: " + xsp.message);
	}
}