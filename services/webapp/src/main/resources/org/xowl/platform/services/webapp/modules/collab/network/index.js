// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Collaborations Network"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationNeighbours(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderNeighbours(content);
		}
	});
}

function renderNeighbours(neighbours) {
	neighbours.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("neighbours");
	for (var i = 0; i != neighbours.length; i++) {
		var row = renderNeighbour(neighbours[i]);
		table.appendChild(row);
	}
}

function renderNeighbour(neighbour) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/" + renderNeighbourGetIcon(neighbour);
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = neighbour.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(neighbour.name));
	link.href = "neighbour.html?id=" + encodeURIComponent(neighbour.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(neighbour.status));
	row.appendChild(cell);

	cell = document.createElement("td");
	var linkValue = neighbour.endpoint.substring(0, neighbour.endpoint.length - "api".length) + "web/";
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(linkValue));
	link.href = linkValue;
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	row.appendChild(cell);
	return row;
}

function renderNeighbourGetIcon(neighbour) {
	if (neighbour.status == "Provisioning")
		return "collaboration-provisioning.svg";
	if (neighbour.status == "Running")
		return "collaboration-running.svg";
	if (neighbour.status == "Stopped")
		return "collaboration-stopped.svg";
	if (neighbour.status == "Archived")
		return "collaboration-archived.svg";
	return "collaboration.svg";
}