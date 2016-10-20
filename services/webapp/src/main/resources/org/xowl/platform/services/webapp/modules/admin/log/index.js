// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	setupPage(xowl);
	xowl.getLogMessages(function (status, ct, content) {
		if (status == 200) {
			renderLogLines(content);
			document.getElementById("loader").style.display = "none";
		} else {
			displayMessage(getErrorFor(status, content));
		}
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
