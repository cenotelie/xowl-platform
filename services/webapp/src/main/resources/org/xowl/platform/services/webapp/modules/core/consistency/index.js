// Copyright (c) 2016 Laurent Wouters
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
			displayMessage(getErrorFor(status, content));
		}
	});
}
