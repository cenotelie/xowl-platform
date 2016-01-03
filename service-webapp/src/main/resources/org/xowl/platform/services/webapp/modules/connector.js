// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var connectorId = getParameterByName("id");

function init() {
	xowl.getConnector(function (status, ct, content) {
		if (status == 200) {
			render(content);
		}
	}, connectorId);
}

function render(connector) {
	var url = null;
	if (connector.uris.length === 0) {
		url = "not accessible";
	} else {
		url = document.URL;
		var index = url.indexOf("/web/");
		if (index > 0)
			url = url.substring(0, index) + "/api/" + connector.uris[0];
		else
			url = "failed to retrieved"
	}
	
	document.getElementById("connector-identifier").value = connector.identifier;
	document.getElementById("connector-name").value = connector.name;
	document.getElementById("connector-uri").value = url;
	document.getElementById("connector-can-pull").value = (connector.canPullInput ? "YES" : "NO");
	var rows = document.getElementById("connector-queue");
	var data = "";
	for (var i = 0; i != connector.queue.length; i++) {
		data += "<tr><td>" + i.toString() + "</td><td> <img src=\"../assets/artifact.svg\" width=\"40\" height=\"40\" />" + connector.queue[i].name + "</td><td>" + connector.queue[i].version + "</td></tr>";
	}
	rows.innerHTML = data;
}

function onClickPull() {
	xowl.pullFromConnector(function (status, ct, content) {
		if (status == 200) {
			window.open("job.html?id=" + encodeURIComponent(content.identifier));
		} else {
			alert(content);
		}
	}, connectorId);
}