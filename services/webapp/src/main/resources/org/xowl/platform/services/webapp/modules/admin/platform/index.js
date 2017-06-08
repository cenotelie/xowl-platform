// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Management"}], function() {
	});
}

function onClickShutdown() {
	popupConfirm("Shutdown the platform?", function() {
		if (!onOperationRequest("Shutting down the platform ..."))
			return;
		xowl.platformShutdown(function (status, ct, content) {
			if (status == 0) {
				displayMessage("success", "The platform shut down.");
			} else if (onOperationEnded(status, content)) {
				displayMessage("success", "The platform shut down.");
			}
		});
	});
}

function onClickRestart() {
	popupConfirm("Restart the platform?", function() {
		if (!onOperationRequest("Restarting the platform ..."))
			return;
		xowl.platformRestart(function (status, ct, content) {
			if (status == 0) {
				displayMessage("success", "The platform is restarting.");
			} else if (onOperationEnded(status, content)) {
				displayMessage("success", "The platform is restarting.");
			}
		});
	});
}
