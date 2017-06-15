// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var profileId = getParameterByName("id");

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community", uri: ROOT + "/modules/collab/community/"},
			{name: "Profile " + profileId}], function() {
		if (!profileId || profileId === null || profileId === "")
			return;
		document.getElementById("profile-edit-link").href = "profile-update.html?id=" + encodeURIComponent(profileId);
		doGetData();
		if (profileId === xowl.getLoggedInUserId())
			document.getElementById("link-security").href = ROOT + "/modules/admin/security/myaccount.html";
		else
			document.getElementById("link-security").href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(profileId);
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getPlatformUser(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderUser(content);
		}
	}, profileId);
	xowl.getPublicProfile(function (status, ct, content) {
		if (onOperationEnded(status, content, null)) {
			renderProfile(content);
		} else {
			document.getElementById("avatar").src = ROOT + "/assets/user-inactive.svg";
		}
	}, profileId);
}

function renderUser(user) {
	document.getElementById("profile-identifier").value = user.identifier;
	document.getElementById("profile-name").value = user.name;
	user.roles.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("roles");
	for (var  i = 0; i != user.roles.length; i++) {
		table.appendChild(renderPlatformRole(user.roles[i]));
	}
}

function renderPlatformRole(role) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = role.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href = ROOT + "/modules/admin/security/role.html?id=" + encodeURIComponent(role.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}

function renderProfile(profile) {
	if (profile.avatarContent.length > 0) {
		document.getElementById("avatar").src = "data:" + profile.avatarMime + ";base64," + profile.avatarContent;
	} else {
		document.getElementById("avatar").src = ROOT + "/assets/user-inactive.svg";
	}
	if (profile.email.length > 0) {
		document.getElementById("profile-email").appendChild(document.createTextNode(profile.email));
		document.getElementById("profile-email").href = "mailto:" + encodeURIComponent(profile.email);
	}
	document.getElementById("profile-organization").value = profile.organization;
	document.getElementById("profile-occupation").value = profile.occupation;
	profile.badges.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("badges");
	for (var i = 0; i != profile.badges.length; i++) {
		table.appendChild(renderBadge(profile.badges[i]));
	}
}

function renderBadge(badge) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var image = document.createElement("img");
	image.src = "data:" + badge.imageMime + ";base64," + badge.imageContent;
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "5px";
	image.title = badge.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(badge.name));
	link.href = "badge.html?id=" + encodeURIComponent(badge.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	row.appendChild(cell);
	return row;
}