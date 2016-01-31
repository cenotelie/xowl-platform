// Copyright (c) 2015 Laurent Wouters
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
	var linkModule = document.createElement("a");
	linkModule.appendChild(document.createTextNode(myModule.name));
	linkModule.href = "/web/modules/" + myModule.uri + "/";
	header.appendChild(linkModule);
	var body = document.createElement("div");
	body.classList.add("panel-body");
	var list = document.createElement("ul");
	for (var i = 0; i != myModule.parts.length; i++) {
		var part = myModule.parts[i];
		var li = document.createElement("li");
		var link = document.createElement("a");
		link.appendChild(document.createTextNode(part.name));
		link.href = "/web/modules/" + myModule.uri + "/" + part.uri + "/";
		li.appendChild(link);
		list.appendChild(li);
	}
	body.appendChild(list);
	panel.appendChild(header);
	panel.appendChild(body);
	return panel;
}