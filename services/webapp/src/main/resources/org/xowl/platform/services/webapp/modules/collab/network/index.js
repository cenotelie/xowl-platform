// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: "/web/modules/collab/"},
			{name: "Collaborations Network"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getCollaborationNeighbours(function (status, ct, content) {
		if (onOperationEnded(status, content)) {

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
	image.src = "/web/assets/specification.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = element.specification.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(element.specification.name));
	link.href = linkName + ".html?id=" + encodeURIComponent(element.specification.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(renderArchetype(element.specification.archetype));
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(element.artifacts.length));
	row.appendChild(cell);

	cell = document.createElement("td");
	image = document.createElement("img");
	image.src = "/web/assets/action-remove.svg";
	image.width = 20;
	image.height = 20;
	image.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(image);
	button.onclick = toRemove;
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}