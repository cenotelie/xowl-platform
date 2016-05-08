// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();

function init() {
	setupPage(xowl);

	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			if (xmlHttp.status === 200) {
				renderModules(JSON.parse(xmlHttp.responseText));
			}
		}
	}
	xmlHttp.open("GET", "/web/modules/index.json", true);
	xmlHttp.setRequestHeader("Accept", "text/plain, application/json");
	xmlHttp.send();

	xowl.getBasicStats(function (code, type, content) {
		if (code === 200) {
			document.getElementById("stat-inconsistencies").innerHTML = content.nbInconsistencies;
			document.getElementById("stat-artifacts-all").innerHTML = content.nbArtifactsTotal;
			document.getElementById("stat-artifacts-live").innerHTML = content.nbArtifactsLive;
		}
	});
}

function renderModules(modules) {
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
	icon.src = myModule.icon;
	icon.width = 30;
	icon.height = 30;
	icon.style.marginRight = "20px";
	var linkModule = document.createElement("a");
	linkModule.appendChild(document.createTextNode(myModule.name));
	linkModule.href = "/web/modules/" + myModule.uri + "/";
	header.appendChild(icon);
	header.appendChild(linkModule);
	var body = document.createElement("div");
	body.classList.add("panel-body");
	for (var i = 0; i != myModule.parts.length; i++) {
		var part = myModule.parts[i];
		var li = document.createElement("div");
		icon = document.createElement("img");
		icon.src = part.icon;
		icon.width = 30;
		icon.height = 30;
		icon.style.marginRight = "20px";
		var link = document.createElement("a");
		link.appendChild(document.createTextNode(part.name));
		link.classList.add("btn");
		link.href = "/web/modules/" + myModule.uri + "/" + part.uri + "/";
		li.appendChild(icon);
		li.appendChild(link);
		body.appendChild(li);
	}
	panel.appendChild(header);
	panel.appendChild(body);
	return panel;
}