// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Connectors Management"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getConnectors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			render(content);
		}
	});
}

function render(connectors) {
	connectors.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("connectors");
	for (var  i = 0; i != connectors.length; i++) {
		table.appendChild(renderConnector(connectors[i]));
	}
}

function renderConnector(connector) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/connector.svg";
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = connector.identifier;
	cell.appendChild(icon);
	var link = document.createElement("a");
	link.href = "connector.html?id=" + encodeURIComponent(connector.identifier);
	link.appendChild(document.createTextNode(connector.name));
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	icon = document.createElement("img");
	icon.src = ROOT + "/assets/action-remove.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "REMOVE";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(icon);
	button.onclick = function() { deleteConnector(connector); };
	cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function deleteConnector(connector) {
	popupConfirm("Connectors Management", richString(["Delete connector ", connector, "?"]), function () {
		if (!onOperationRequest(richString(["Deleting connector ", connector, " ..."])))
			return;
		xowl.deleteConnector(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Deleted connector ", connector]));
				waitAndRefresh();
			}
		}, connector.identifier);
	});
}