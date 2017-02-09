// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [], function() {
		loadComponent(ROOT + "/widgets/placeholder/index.html", function (node) {
			document.getElementById("panel-custom1").appendChild(node.cloneNode(true));
			document.getElementById("panel-custom2").appendChild(node.cloneNode(true));
		});
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getWebModules(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				renderModules(content);
			}
		});
	});
}

function renderModules(modules) {
	modules.sort(function (x, y) {
		return x.order - y.order;
	});
	var panel = document.getElementById("panel-modules");
	for (var i = 0; i != modules.length; i++) {
		var content = renderModule(modules[i]);
		panel.appendChild(content);
	}
}

function renderModule(myModule) {
	var panel = document.createElement("div");
	panel.classList.add("panel");
	panel.classList.add("panel-default");
	var header = document.createElement("div");
	header.classList.add("panel-heading");
	var icon = document.createElement("img");
	icon.src = ROOT + myModule.icon;
	icon.width = 30;
	icon.height = 30;
	icon.style.marginRight = "20px";
	icon.title = myModule.identifier;
	var linkModule = document.createElement("a");
	linkModule.appendChild(document.createTextNode(myModule.name));
	linkModule.href = ROOT + "/modules/" + myModule.uri + "/";
	var button = document.createElement("a");
	var buttonImage = document.createElement("img");
	buttonImage.src = ROOT + "/assets/action-plus.svg";
	buttonImage.width = 30;
	buttonImage.height = 30;
	buttonImage.style.float = "right";
	buttonImage.title = "TOGGLE";
	button.style.cursor = "pointer";
	button.appendChild(buttonImage);
	header.appendChild(icon);
	header.appendChild(linkModule);
	header.appendChild(button);
	var body = document.createElement("div");
	body.classList.add("panel-body");
	body.style.display = "none";
	for (var i = 0; i != myModule.items.length; i++) {
		var part = myModule.items[i];
		var li = document.createElement("div");
		icon = document.createElement("img");
		icon.src = ROOT + part.icon;
		icon.width = 30;
		icon.height = 30;
		icon.style.marginRight = "20px";
		icon.title = myModule.items[i].name;
		var link = document.createElement("a");
		link.appendChild(document.createTextNode(part.name));
		link.classList.add("btn");
		link.href = ROOT + "/modules/" + myModule.uri + "/" + part.uri + "/";
		li.appendChild(icon);
		li.appendChild(link);
		body.appendChild(li);
	}
	panel.appendChild(header);
	panel.appendChild(body);
	button.onclick = function() {
		if (body.style.display === "none") {
			buttonImage.src = ROOT + "/assets/action-minus.svg";
			body.style.display = "";
		} else {
			buttonImage.src = ROOT + "/assets/action-plus.svg";
			body.style.display = "none";
		}
	};
	return panel;
}