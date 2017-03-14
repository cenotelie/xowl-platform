// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var profileId = getParameterByName("id");
var profile = null;
var badges = null;
var oldName = null;
var oldEmail = null;
var oldOrganization = null;
var oldOccupation = null;
var oldMime = null;
var oldContent = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
			{name: "Community", uri: ROOT + "/modules/collab/community/"},
			{name: "Profile " + profileId, uri: ROOT + "/modules/collab/community/profile-view.html?id=" + encodeURIComponent(profileId)},
			{name: "Edit"}], function() {
		if (!profileId || profileId === null || profileId === "")
			return;
		setupAutocomplete();
		setupDrop();
		doGetData();
		if (profileId === xowl.getLoggedInUserId())
			document.getElementById("link-security").href = ROOT + "/modules/admin/security/myaccount.html";
		else
			document.getElementById("link-security").href = ROOT + "/modules/admin/security/user.html?id=" + encodeURIComponent(profileId);
	});
}

function setupAutocomplete() {
	var autocomplete = new AutoComplete("input-badge");
	autocomplete.lookupItems = function (value) {
		if (badges !== null) {
			autocomplete.onItems(filterItems(badges, value));
			return;
		}
		xowl.getBadges(function (status, ct, content) {
			if (status === 200) {
				badges = content;
				autocomplete.onItems(filterItems(badges, value));
			}
		});
	};
	autocomplete.renderItem = function (item) {
		var result = document.createElement("div");
		result.appendChild(document.createTextNode(item.name + " (" + item.identifier + ")"));
		return result;
	};
	autocomplete.getItemString = function (item) {
		return item.identifier;
	};
}

function filterItems(items, value) {
	var result = [];
	for (var i = 0; i != items.length; i++) {
		if (items[i].identifier.indexOf(value) >= 0 || items[i].name.indexOf(value) >= 0) {
			result.push(items[i]);
		}
	}
	return result;
}

function setupDrop() {
	var drop = document.getElementById("avatar-drop");
	drop.addEventListener("dragover", onEventCancel, false);
	drop.addEventListener("dragenter", onEventDragEnter, false);
	drop.addEventListener("dragexit", onEventDragExit, false);
	drop.addEventListener("drop", onEventDrop, false);
}

function onEventCancel(e) {
	e = e || window.event;
	if (e.preventDefault) {
		e.preventDefault();
	}
	return false;
}

function onEventDragEnter(e) {
	document.getElementById("avatar-drop").style.backgroundColor = "#00914d";
	return onEventCancel(e);
}

function onEventDragExit(e) {
	document.getElementById("avatar-drop").style.backgroundColor = "#DDDDDD";
	return onEventCancel(e);
}

function onEventDrop(e) {
	e = e || window.event;
	if (e.preventDefault) {
		e.preventDefault();
	}
	if (oldMime !== null || oldContent !== null)
		return false;
	var dt = e.dataTransfer;
	var files = dt.files;
	if (files.length <= 0)
		return false;
	var file = files[0];
	var reader = new FileReader();
	reader.onloadend = function (event) {
		if (reader.error !== null) {
			alert("Error while reading dropped file.");
			return;
		}
		onEventDropContent(base64ArrayBuffer(reader.result), file.type);
	}
	reader.readAsArrayBuffer(file);
	return false;
}

function onEventDropContent(content, mimeType) {
	if (oldMime !== null || oldContent !== null)
		return;
	oldMime = profile.avatarMime;
	oldContent = profile.avatarContent;
	document.getElementById("avatar-drop").style.backgroundColor = "#DDDDDD";
	document.getElementById("avatar-commands").style.display = "";
	document.getElementById("avatar").src = "data:" + mimeType + ";base64," + content;
	profile.avatarMime = mimeType;
	profile.avatarContent = content;
}

function base64ArrayBuffer(buffer) {
	var result = "";
	var bytes = new Uint8Array(buffer);
	var length = bytes.byteLength;
	for (var i = 0; i < length; i++) {
		result += String.fromCharCode(bytes[i]);
	}
	return window.btoa(result);
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
			profile = content;
			renderProfile(content);
		} else {
			document.getElementById("avatar").src = ROOT + "/assets/user-inactive.svg";
			profile = {
				identifier: profileId,
				name: profileId,
				email: "",
				avatarMime: "",
				avatarContent: "",
				organization: "",
				occupation: "",
				badges: []
			};
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
	} else {
		document.getElementById("avatar").src = ROOT + "/assets/user-inactive.svg";
	}
	if (profile.email.length > 0) {
		document.getElementById("profile-email").appendChild(document.createTextNode(profile.email));
		document.getElementById("profile-email").href = "mailto:" + encodeURIComponent(profile.email);
		document.getElementById("profile-email-editor").value = profile.email;
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
	link.href = "badge.html?id=" + badge.identifier;
	cell.appendChild(image);
	cell.appendChild(link);
	return cell;
}

function onClickEditName() {
	if (oldName !== null)
		return;
	document.getElementById("profile-name-edit").style.display = "none";
	document.getElementById("profile-name-validate").style.display = "inline-block";
	document.getElementById("profile-name-cancel").style.display = "inline-block";
	document.getElementById("profile-name").readOnly = false;
	document.getElementById("profile-name").focus();
	document.getElementById("profile-name").select();
	oldName = document.getElementById("profile-name").value;
}

function onClickValidateName() {
	if (!onOperationRequest("Changing name ..."))
		return;
	document.getElementById("profile-name-edit").style.display = "inline-block";
	document.getElementById("profile-name-validate").style.display = "none";
	document.getElementById("profile-name-cancel").style.display = "none";
	document.getElementById("profile-name").readOnly = true;
	xowl.renamePlatformUser(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("profile-name").value = oldName;
		}
		oldName = null;
	}, profileId, document.getElementById("profile-name").value);
}

function onClickCancelName() {
	document.getElementById("profile-name-edit").style.display = "inline-block";
	document.getElementById("profile-name-validate").style.display = "none";
	document.getElementById("profile-name-cancel").style.display = "none";
	document.getElementById("profile-name").value = oldName;
	document.getElementById("profile-name").readOnly = true;
	oldName = null;
}

function onClickEditEmail() {
	if (oldEmail !== null)
		return;
	document.getElementById("profile-email-edit").style.display = "none";
	document.getElementById("profile-email-validate").style.display = "inline-block";
	document.getElementById("profile-email-cancel").style.display = "inline-block";
	document.getElementById("profile-email-display").style.display = "none";
	document.getElementById("profile-email-editor").style.display = "";
	document.getElementById("profile-email-editor").focus();
	document.getElementById("profile-email-editor").select();
	oldEmail = document.getElementById("profile-email-editor").value;
}

function onClickValidateEmail() {
	if (!onOperationRequest("Changing email ..."))
		return;
	profile.email = document.getElementById("profile-email-editor").value;
	document.getElementById("profile-email-edit").style.display = "inline-block";
	document.getElementById("profile-email-validate").style.display = "none";
	document.getElementById("profile-email-cancel").style.display = "none";
	document.getElementById("profile-email-editor").style.display = "none";
	document.getElementById("profile-email-display").style.display = "";
	var element = document.getElementById("profile-email");
	while (element.hasChildNodes())
		element.removeChild(element.lastChild);
	element.appendChild(document.createTextNode(profile.email));
	element.href = "mailto:" + encodeURIComponent(profile.email);
	xowl.updatePublicProfile(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			while (element.hasChildNodes())
				element.removeChild(element.lastChild);
			element.appendChild(document.createTextNode(oldEmail));
			element.href = "mailto:" + encodeURIComponent(oldEmail);
			document.getElementById("profile-email-editor").value = oldEmail;
			profile.email = oldEmail;
		}
		oldEmail = null;
	}, profile);
}

function onClickCancelEmail() {
	document.getElementById("profile-email-edit").style.display = "inline-block";
	document.getElementById("profile-email-validate").style.display = "none";
	document.getElementById("profile-email-cancel").style.display = "none";
	document.getElementById("profile-email-editor").value = oldEmail;
	document.getElementById("profile-email-editor").style.display = "none";
	document.getElementById("profile-email-display").style.display = "";
	profile.email = oldEmail;
	oldEmail = null;
}

function onClickEditOrganization() {
	if (oldOrganization !== null)
		return;
	document.getElementById("profile-organization-edit").style.display = "none";
	document.getElementById("profile-organization-validate").style.display = "inline-block";
	document.getElementById("profile-organization-cancel").style.display = "inline-block";
	document.getElementById("profile-organization").readOnly = false;
	document.getElementById("profile-organization").focus();
	document.getElementById("profile-organization").select();
	oldOrganization = document.getElementById("profile-organization").value;
}

function onClickValidateOrganization() {
	if (!onOperationRequest("Changing organization ..."))
		return;
	document.getElementById("profile-organization-edit").style.display = "inline-block";
	document.getElementById("profile-organization-validate").style.display = "none";
	document.getElementById("profile-organization-cancel").style.display = "none";
	document.getElementById("profile-organization").readOnly = true;
	profile.organization = document.getElementById("profile-organization").value;
	xowl.updatePublicProfile(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("profile-organization").value = oldOrganization;
			profile.organization = oldOrganization;
		}
		oldOrganization = null;
	}, profile);
}

function onClickCancelOrganization() {
	document.getElementById("profile-organization-edit").style.display = "inline-block";
	document.getElementById("profile-organization-validate").style.display = "none";
	document.getElementById("profile-organization-cancel").style.display = "none";
	document.getElementById("profile-organization").value = oldOrganization;
	document.getElementById("profile-organization").readOnly = true;
	profile.organization = oldOrganization;
	oldOrganization = null;
}

function onClickEditOccupation() {
	if (oldOccupation !== null)
		return;
	document.getElementById("profile-occupation-edit").style.display = "none";
	document.getElementById("profile-occupation-validate").style.display = "inline-block";
	document.getElementById("profile-occupation-cancel").style.display = "inline-block";
	document.getElementById("profile-occupation").readOnly = false;
	document.getElementById("profile-occupation").focus();
	document.getElementById("profile-occupation").select();
	oldOccupation = document.getElementById("profile-occupation").value;
}

function onClickValidateOccupation() {
	if (!onOperationRequest("Changing occupation ..."))
		return;
	document.getElementById("profile-occupation-edit").style.display = "inline-block";
	document.getElementById("profile-occupation-validate").style.display = "none";
	document.getElementById("profile-occupation-cancel").style.display = "none";
	document.getElementById("profile-occupation").readOnly = true;
	profile.occupation = document.getElementById("profile-occupation").value;
	xowl.updatePublicProfile(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("profile-occupation").value = oldOccupation;
			profile.occupation = oldOccupation;
		}
		oldOccupation = null;
	}, profile);
}

function onClickCancelOccupation() {
	document.getElementById("profile-occupation-edit").style.display = "inline-block";
	document.getElementById("profile-occupation-validate").style.display = "none";
	document.getElementById("profile-occupation-cancel").style.display = "none";
	document.getElementById("profile-occupation").value = oldOccupation;
	document.getElementById("profile-occupation").readOnly = true;
	profile.occupation = oldOccupation;
	oldOccupation = null;
}

function onClickValidateAvatar() {
	if (!onOperationRequest("Updating avatar ..."))
		return;
	document.getElementById("avatar-commands").style.display = "none";
	xowl.updatePublicProfile(function (status, ct, content) {
		if (!onOperationEnded(status, content)) {
			document.getElementById("avatar").src = "data:" + oldMime + ";base64," + oldContent;
			profile.avatarMime = oldMime;
			profile.avatarContent = oldContent;
		}
		oldMime = null;
        oldContent = null;
	}, profile);
}

function onClickCancelAvatar() {
	document.getElementById("avatar-commands").style.display = "none";
	document.getElementById("avatar").src = "data:" + oldMime + ";base64," + oldContent;
	profile.avatarMime = oldMime;
	profile.avatarContent = oldContent;
	oldMime = null;
	oldContent = null;
}

function onClickBadgeAward() {
	var badgeId = document.getElementById("input-badge").value;
	if (badgeId == null || badgeId.length == "")
		return;
	if (!onOperationRequest("Awarding badge ..."))
		return;
	xowl.awardBadge(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Awarded badge " + badgeId);
			waitAndRefresh();
		}
	}, profileId, badgeId);
}