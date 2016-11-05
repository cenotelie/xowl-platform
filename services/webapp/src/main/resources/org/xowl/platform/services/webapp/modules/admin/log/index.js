// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Administration Module", uri: "/web/modules/admin/"},
			{name: "Log"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getLogMessages(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderLogLines(content);
			}
		});
	});
}

function renderLogLines(messages) {
	var table = document.getElementById("messages");
	for (var  i = 0; i != messages.length; i++) {
		table.appendChild(renderLogLine(messages[i]));
	}
}

function renderLogLine(message) {
	var row = document.createElement("tr");
	var cells = [document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")];
	cells[0].appendChild(document.createTextNode(message.level));
	cells[1].appendChild(document.createTextNode(message.date));
	cells[2].appendChild(renderMessage(message.content));
	row.appendChild(cells[0]);
	row.appendChild(cells[1]);
	row.appendChild(cells[2]);
	return row;
}
