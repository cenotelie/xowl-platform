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
		} else if (status === 404) {
			document.getElementById("row-new-profile").style.display = "";
			document.getElementById("profile-new-link").href= "profile-update.html?id=" + encodeURIComponent(profileId);
		}
	}, profileId);
}

function renderUser(user) {
	document.getElementById("profile-identifier").value = user.identifier;
	document.getElementById("profile-name").value = user.name;
	var panel = document.getElementById("panel-roles");
	for (var i = 0; i != user.roles.length; i++) {
		panel.appendChild(renderRole(user.roles[i]));
	}
}

function renderRole(role) {
	var cell = document.createElement("span");
	cell.style.marginLeft = "10px";
	cell.style.marginRight = "10px";
	cell.style.marginTop = "10px";
	cell.style.marginBottom = "10px";
	var image = document.createElement("img");
	image.src = ROOT + "/assets/role.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "5px";
	image.title = role.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(role.name));
	link.href = ROOT + "/modules/admin/security/role.html?id=" + encodeURIComponent(role.identifier);
	cell.appendChild(image);
	cell.appendChild(link);
	return cell;
}

function renderProfile(profile) {
	if (profile.avatarContent.length > 0) {
		document.getElementById("avatar").src = "data:" + profile.avatarMime + ";base64," + profile.avatarContent;
	}
	if (profile.email.length > 0) {
		document.getElementById("profile-email").appendChild(document.createTextNode(profile.email));
		document.getElementById("profile-email").href = "mailto:" + encodeURIComponent(profile.email);
	}
	document.getElementById("profile-organization").value = profile.organization;
	document.getElementById("profile-occupation").value = profile.occupation;
	var panel = document.getElementById("panel-badges");
	for (var i = 0; i != profile.badges.length; i++) {
		panel.appendChild(renderBadge(profile.badges[i]));
	}
}

function renderBadge(badge) {
	var cell = document.createElement("span");
	cell.style.marginLeft = "10px";
	cell.style.marginRight = "10px";
	cell.style.marginTop = "10px";
	cell.style.marginBottom = "10px";
	var image = document.createElement("img");
	image.src = "data:" + badge.imageMime + ";base64," + badge.imageContent;
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "5px";
	image.title = badge.identifier;
	var link = document.createElement("a");
	link.appendChild(document.createTextNode(badge.name));
	link.title = badge.description;
	cell.appendChild(image);
	cell.appendChild(link);
	return cell;
}