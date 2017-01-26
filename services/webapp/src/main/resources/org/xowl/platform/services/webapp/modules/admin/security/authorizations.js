// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: "/web/modules/admin/"},
			{name: "Platform Security", uri: "/web/modules/admin/security/"},
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
	image.src = "/web/assets/secured_action.svg";
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
	var policyInput = document.createElement("input");
	policyInput.type = "text";
	policyInput.classList.add("form-control");
	policyInput.readOnly = true;
	policyInput.value = serializePolicy(policy);
	cell.classList.add("col-sm-6");
	cell.appendChild(policyInput);
	row.appendChild(cell);

	cell = document.createElement("div");
	var buttonEdit = renderButton("action-edit");
	var buttonValidate = renderButton("action-validate");
	var buttonCancel = renderButton("action-cancel");
	buttonValidate.style.display = "none";
	buttonCancel.style.display = "none";
	cell.classList.add("col-sm-2");
	cell.appendChild(buttonEdit);
	cell.appendChild(buttonValidate);
	cell.appendChild(buttonCancel);
	row.appendChild(cell);
}

function renderButton(icon) {
	var button = document.createElement("span");
	var image = document.createElement("img");
	image.src = "/web/assets/" + icon + ".svg";
	image.width = 20;
	image.height = 20;
	button.appendChild(image);
	button.classList.add("btn");
	button.classList.add("btn-default");
	return button;
}

function serializePolicy(policy) {
	if (policy.identifier == "org.xowl.platform.kernel.security.SecuredActionPolicyHasRole") {
		return policy.name + " : " + policy.role;
	} else {
		return policy.name;
	}
}