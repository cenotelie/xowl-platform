// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var SELECTED_DOMAIN = null;
var DOMAINS = null;
var SELECTED_CONNECTOR = null;
var CONNECTORS = null;
var GRAPH_WIDTH = 1024;
var GRAPH_MIN_HEIGHT = 300;
var GRAPH_HEIGHT = GRAPH_MIN_HEIGHT;
var SVG_DB = null;
var SVG_CONNECTOR = null;
var SVG_DB_SIZE = 1000;
var SVG_DB_SCALE = 0.15;
var SVG_CONNECTOR_SIZE = 100;
var SVG_CONNECTOR_SCALE = 0.50;
var GRAPH_DB_X = 20;
var GRAPH_CONNECTOR_X = 400;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Connectors Management"}], function() {
			doGetData();
	});
	document.getElementById("input-uri-addon").innerHTML = xowl.endpoint;
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 4))
		return;
	xowl.getConnectors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			CONNECTORS = content;
			render();
		}
	});
	xowl.getDescriptors(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			DOMAINS = content;
			var select = document.getElementById("input-domain");
			while (select.length > 0)
				select.remove(select.length - 1);
			for (var i = 0; i != DOMAINS.length; i++) {
				var option = document.createElement("option");
				option.text = DOMAINS[i].name;
				select.add(option);
			}
			select.selectedIndex = -1;
		}
	});
	loadComponent(ROOT + "/assets/xowl.svg", function (node) {
		if (onOperationEnded(200, "")) {
			SVG_DB = node.children[0];
			render();
		}
	});
	loadComponent(ROOT + "/assets/connector.svg", function (node) {
		if (onOperationEnded(200, "")) {
			SVG_CONNECTOR = node.children[0];
			render();
		}
	});
}

function onDomainSelect() {
	document.getElementById("input-domain-description").value = "";
	var params = document.getElementById("input-params");
	while (params.hasChildNodes()) {
		params.removeChild(params.lastChild);
	}

	var select = document.getElementById("input-domain");
	if (select.selectedIndex == -1)
		return;
	SELECTED_DOMAIN = DOMAINS[select.selectedIndex];
	document.getElementById("input-domain-description").value = SELECTED_DOMAIN.description;
	for (var i = 0; i != SELECTED_DOMAIN.parameters.length; i++) {
		var parameter = SELECTED_DOMAIN.parameters[i];
		var data = "<div class='form-group'>";
		if (parameter.isRequired)
			data += "<span class='col-sm-1 glyphicon glyphicon-star text-danger' aria-hidden='true' title='required'></span>";
		else
			data += "<span class='col-sm-1' aria-hidden='true'></span>";
		data += "<label class='col-sm-2 control-label'>";
		data += parameter.name;
		data += "</label>";
		data += "<div class='col-sm-9'>";
		data += "<input type='" + (parameter.typeHint === "password" ? "password" : "text") + "' class='form-control' id='input-param-" + i + "'>";
		data += "</div></div>";
		params.innerHTML += data;
	}
}

function onClickConnector(connector) {
	SELECTED_CONNECTOR = connector;
	var url = null;
	if (connector.uris.length === 0) {
		url = "not accessible";
	} else {
		url = xowl.endpoint + connector.uris[0];
	}
	document.getElementById("connector-properties").style.display = "";
	document.getElementById("connector-identifier").value = connector.identifier;
	document.getElementById("connector-name").value = connector.name;
	document.getElementById("connector-uri").value = url;
	document.getElementById("connector-can-pull").value = (connector.canPullInput ? "YES" : "NO");
	document.getElementById("connector-queue").value = connector.queue.length.toString();
	document.getElementById("connector-link").href = "connector.html?id=" + encodeURIComponent(connector.identifier);
}

function onClickLink(connector) {
	onClickConnector(connector);
}

function onClickNewConnector() {
	var onError = false;
	var id = document.getElementById("input-id").value;
	var name = document.getElementById("input-name").value;
	var uri = document.getElementById("input-uri").value;
	if (SELECTED_DOMAIN == null) {
		document.getElementById("input-domain").parentElement.className = "form-group has-error"
		document.getElementById("input-domain-help").innerHTML = "A domain must be selected.";
		onError = true;
	}
	if (typeof id == "undefined" || id == null || id == "") {
		document.getElementById("input-id").parentElement.className = "form-group has-error"
		document.getElementById("input-id-help").innerHTML = "The identifier must not be empty.";
		onError = true;
	}
	if (typeof name == "undefined" || name == null || name == "") {
		document.getElementById("input-name").parentElement.className = "form-group has-error"
		document.getElementById("input-name-help").innerHTML = "The name must not be empty.";
		onError = true;
	}
	if (onError)
		return;
	document.getElementById("input-domain").parentElement.className = "form-group"
	document.getElementById("input-domain-help").innerHTML = "";
	document.getElementById("input-id").parentElement.className = "form-group"
	document.getElementById("input-id-help").innerHTML = "";
	document.getElementById("input-name").parentElement.className = "form-group"
	document.getElementById("input-name-help").innerHTML = "";

	var data = {
		"type": "org.xowl.platform.services.connection.ConnectorServiceData",
		"identifier": id,
		"name": name,
		"uris": (typeof uri == "undefined" || uri == null || uri == "" ? [] : [uri])
	};
	for (var i = 0; i != SELECTED_DOMAIN.parameters.length; i++) {
		var value = document.getElementById("input-param-" + i).value;
		if (typeof value == "undefined" || value == null || value == "") {
			if (SELECTED_DOMAIN.parameters[i].isRequired) {
				document.getElementById("input-id").parentElement.className = "form-group has-error"
				document.getElementById("input-id-help").innerHTML = "Parameter " + SELECTED_DOMAIN.parameters[i].name + " is required.";
			}
		} else {
			data[SELECTED_DOMAIN.parameters[i].identifier] = value;
		}
	}

	if (!onOperationRequest("Creating new connector ..."))
		return;
	xowl.createConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			var connector = content;
			document.getElementById("input-id").value = "";
			document.getElementById("input-name").value = "";
			document.getElementById("input-uri").value = "";
			if (CONNECTORS == null)
				CONNECTORS = [connector];
			else
				CONNECTORS.push(connector);
			for (var i = 0; i != SELECTED_DOMAIN.parameters.length; i++) {
				document.getElementById("input-param-" + i).value = "";
			}
			render();
		}
	}, SELECTED_DOMAIN, data);
}

function onClickDeleteConnector() {
	if (SELECTED_CONNECTOR === null)
		return;
	var result = confirm("Delete connector " + SELECTED_CONNECTOR.identifier + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting connector ..."))
		return;
	xowl.deleteConnector(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			document.getElementById("connector-properties").style.display = "none";
			CONNECTORS.splice(CONNECTORS.indexOf(SELECTED_CONNECTOR), 1);
			render();
		}
	}, SELECTED_CONNECTOR.identifier);
}

function createCanvas(nb) {
	var height = SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE * nb * 1.5;
	GRAPH_HEIGHT = (height < GRAPH_MIN_HEIGHT ? GRAPH_MIN_HEIGHT : height);
	var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	var canvas = document.createElementNS("http://www.w3.org/2000/svg", "g");
	svg.setAttribute("height", GRAPH_HEIGHT.toString());
	svg.setAttribute("width", GRAPH_WIDTH.toString());
	svg.appendChild(canvas);
	var display = document.getElementById("display");
	while (display.hasChildNodes())
		display.removeChild(display.lastChild);
	document.getElementById("display").appendChild(svg);
	return canvas;
}

function newDB(x, y) {
	var node = SVG_DB.cloneNode(true);
	node.setAttribute("transform", "translate(" + x + "," + y + ")scale(" + SVG_DB_SCALE + ")");
	node.setAttribute("class", "db");
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", (SVG_DB_SIZE / 2).toString());
	text.setAttribute("y", (SVG_DB_SIZE + SVG_DB_SIZE / 10).toString());
	text.setAttribute("text-anchor", "middle");
	text.setAttribute("font-family", "sans-serif");
	text.setAttribute("font-size", "70");
	text.appendChild(document.createTextNode("Platform"));
	node.appendChild(text);
	return node;
}

function newConnector(def, x, y) {
	var node = SVG_CONNECTOR.cloneNode(true);
	node.setAttribute("transform", "translate(" + x + "," + y + ")scale(" + SVG_CONNECTOR_SCALE + ")");
	node.setAttribute("class", "connector");
	node.onclick = function () { onClickConnector(def); };
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", (SVG_CONNECTOR_SIZE + SVG_CONNECTOR_SIZE / 10).toString());
	text.setAttribute("y", (SVG_CONNECTOR_SIZE / 2).toString());
	text.setAttribute("text-anchor", "start");
	text.setAttribute("font-family", "sans-serif");
	text.setAttribute("font-size", "30");
	text.appendChild(document.createTextNode(def.name));
	node.appendChild(text);
	return node;
}

function newLink(def, x1, y1, x2, y2) {
	var c1 = x1 + 100;
	var c2 = x2 - 100;
	var link = document.createElementNS("http://www.w3.org/2000/svg", "path");
	link.onclick = function () { onClickLink(def); };
	var classes = "link";
	if (def.queue.length > 0)
		classes += " linkWaiting";
	if (def.canPullInput)
		classes += " linkCanPull";
	link.setAttribute("class", classes);
	link.setAttribute("fill", "none");
	link.setAttribute("stroke", "black");
	link.setAttribute("stroke-width", "3");
	link.setAttribute("stroke-linecap", "round");
	link.setAttribute("d", "M " + x1 + " " + y1 + " C " + c1 + " " + y1 + " " + c2 + " " + y2 + " " + x2 + " " + y2);
	return link;
}

function render() {
	if (CONNECTORS == null || SVG_DB == null || SVG_CONNECTOR == null)
		return;
	var svg = createCanvas(CONNECTORS.length);
	svg.appendChild(newDB(GRAPH_DB_X, (GRAPH_HEIGHT - SVG_DB_SIZE * SVG_DB_SCALE) / 2));
	var pad = (GRAPH_HEIGHT - CONNECTORS.length * SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE) / (CONNECTORS.length + 1);
	var y = pad;
	for (var i = 0; i != CONNECTORS.length; i++) {
		svg.appendChild(newLink(CONNECTORS[i], GRAPH_DB_X + SVG_DB_SIZE * SVG_DB_SCALE + 5, GRAPH_HEIGHT / 2, GRAPH_CONNECTOR_X - 5, y + SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE / 2));
		svg.appendChild(newConnector(CONNECTORS[i], GRAPH_CONNECTOR_X, y));
		y += SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE + pad;
	}
}