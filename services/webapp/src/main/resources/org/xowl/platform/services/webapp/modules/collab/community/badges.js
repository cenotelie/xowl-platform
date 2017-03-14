// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community", uri: ROOT + "/modules/collab/community/"},
			{name: "Badges"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getBadges(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderBadges(content);
		}
	});
}

function renderBadges(badges) {
	badges.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("badges");
	for (var i = 0; i != badges.length; i++) {
		var row = renderBadge(badges[i]);
		table.appendChild(row);
	}
}

function renderBadge(badge) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "data:" + badge.imageMime + ";base64," + badge.imageContent;
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = badge.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(badge.name));
	link.href = "badge.html?id=" + badge.identifier;
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	cell.appendChild(document.createTextNode(badge.description));
	row.appendChild(cell);
	return row;
}