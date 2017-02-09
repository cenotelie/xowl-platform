// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Security", uri: ROOT + "/modules/admin/security/"},
			{name: "Authorizations"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getSecurityPolicy(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderConfiguration(content);
			}
		});
	});
}

function renderConfiguration(configuration) {
	configuration.parts.sort(function (x, y) {
		return x.action.name.localeCompare(y.action.name);
	});
	var table = document.getElementById("actions");
	for (var i = 0; i != configuration.parts.length; i++) {
		table.appendChild(renderPart(configuration.parts[i]));
	}
}

function renderPart(part) {
	var row = document.createElement("div");
	row.classList.add("form-group");
	renderAction(row, part.action);
	renderPolicy(row, part.action, part.policy);
	return row;
}

function renderAction(row, action) {
	var cell = document.createElement("div");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/secured_action.svg";
	image.width = 30;
	image.height = 30;
	image.style.marginRight = "20px";
	image.title = action.identifier;
	cell.classList.add("col-sm-4");
	cell.appendChild(image);
	cell.appendChild(document.createTextNode(action.name));
	row.appendChild(cell);
}

function renderPolicy(row, action, policy) {
	var cell = document.createElement("div");
	var policyDisplay = document.createElement("span");
	policyDisplay.classList.add("form-control");
	policyDisplay.appendChild(serializePolicy(policy));
	var policyInput = document.createElement("select");
	for (var i = 0; i != action.policies.length; i++) {
		var option = document.createElement("option");
		option.appendChild(document.createTextNode(action.policies[i].name));
		option.value = action.policies[i].identifier;
		policyInput.appendChild(option);
	}
	policyInput.classList.add("form-control");
	policyInput.style.display = "none";
	policyInput.value = policy.identifier;
	cell.classList.add("col-sm-6");
	cell.appendChild(policyDisplay);
	cell.appendChild(policyInput);
	row.appendChild(cell);

	cell = document.createElement("div");
	var buttonEdit = renderButton("action-edit");
	var buttonValidate = renderButton("action-validate");
	var buttonCancel = renderButton("action-cancel");
	buttonValidate.style.display = "none";
	buttonCancel.style.display = "none";
	buttonValidate.style.marginRight = "10px";
	cell.classList.add("col-sm-2");
	cell.appendChild(buttonEdit);
	cell.appendChild(buttonValidate);
	cell.appendChild(buttonCancel);
	row.appendChild(cell);

	var onSetSuccess = function (newPolicy) {
		policy = newPolicy;
		while (policyDisplay.hasChildNodes())
			policyDisplay.removeChild(policyDisplay.lastChild);
		policyDisplay.appendChild(serializePolicy(newPolicy));
		policyInput.value = newPolicy.identifier;
		policyDisplay.style.display = "";
		policyInput.style.display = "none";
		buttonEdit.style.display = "";
		buttonValidate.style.display = "none";
		buttonCancel.style.display = "none";
	};
	buttonEdit.onclick = function () {
		policyDisplay.style.display = "none";
		policyInput.style.display = "";
		buttonEdit.style.display = "none";
		buttonValidate.style.display = "";
		buttonCancel.style.display = "";
	};
	buttonValidate.onclick = function () {
		onSetPolicy(action, policyInput.value, onSetSuccess);
	};
	buttonCancel.onclick = function () {
		policyDisplay.style.display = "";
		policyInput.style.display = "none";
		policyInput.value = action.identifier;
		buttonEdit.style.display = "";
		buttonValidate.style.display = "none";
		buttonCancel.style.display = "none";
	};
}

function renderButton(icon) {
	var button = document.createElement("span");
	var image = document.createElement("img");
	image.src = ROOT + "/assets/" + icon + ".svg";
	image.width = 20;
	image.height = 20;
	button.appendChild(image);
	button.classList.add("btn");
	button.classList.add("btn-default");
	return button;
}

function serializePolicy(policy) {
	if (policy.identifier == "org.xowl.platform.kernel.security.SecuredActionPolicyHasRole") {
		var span = document.createElement("span");
		span.appendChild(document.createTextNode(policy.name + " : "));
		var link = document.createElement("a");
		link.appendChild(document.createTextNode(policy.role));
		link.href = ROOT + "/modules/admin/security/role.html?id=" + encodeURIComponent(policy.role);
		span.appendChild(link);
		return link;
	} else {
		return document.createTextNode(policy.name);
	}
}

function onSetPolicy(action, descriptorId, onSuccess) {
	var descriptor = null;
	for (var i = 0; i != action.policies.length; i++) {
		if (action.policies[i].identifier == descriptorId) {
			descriptor = action.policies[i];
			break;
		}
	}
	if (descriptor == null)
		return;
	var policy = {
		"type": "org.xowl.platform.kernel.security.SecuredActionPolicy",
		"identifier": descriptor.identifier,
		"name": descriptor.name
	};
	if (!onOperationRequest("Setting policy for action " + action.name))
		return;
	xowl.setSecuredActionPolicy(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Policy has been set.");
			onSuccess(policy);
		}
	}, action.identifier, policy);
}