// Copyright (c) 2016 Laurent Wouters
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
var SVG_CONNECTOR_SIZE = 256;
var SVG_CONNECTOR_SCALE = 0.25;
var GRAPH_DB_X = 20;
var GRAPH_CONNECTOR_X = 400;

function init() {
	var url = document.URL;
	var index = url.indexOf("/web/");
	if (index > 0)
		document.getElementById("input-uri-addon").innerHTML = url.substring(0, index) + "/api/";
	xowl.getConnectors(function (status, ct, content) {
		if (status == 200) {
			CONNECTORS = content;
			render();
		}
	});
	xowl.getDomains(function (status, ct, content) {
		if (status == 200) {
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
		url = document.URL;
		var index = url.indexOf("/web/");
		if (index > 0)
			url = url.substring(0, index) + "/api/" + connector.uris[0];
		else
			url = "failed to retrieved"
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
	var onerror = false;
	var id = document.getElementById("input-id").value;
	var name = document.getElementById("input-name").value;
	var uri = document.getElementById("input-uri").value;
	if (SELECTED_DOMAIN == null) {
		document.getElementById("input-domain").parentElement.className = "form-group has-error"
		document.getElementById("input-domain-help").innerHTML = "A domain must be selected.";
		onerror = true;
	}
	if (typeof id == "undefined" || id == null || id == "") {
		document.getElementById("input-id").parentElement.className = "form-group has-error"
		document.getElementById("input-id-help").innerHTML = "The identifier must not be empty.";
		onerror = true;
	}
	if (typeof name == "undefined" || name == null || name == "") {
		document.getElementById("input-name").parentElement.className = "form-group has-error"
		document.getElementById("input-name-help").innerHTML = "The name must not be empty.";
		onerror = true;
	}
	if (onerror)
		return;
	document.getElementById("input-domain").parentElement.className = "form-group"
	document.getElementById("input-domain-help").innerHTML = "";
	document.getElementById("input-id").parentElement.className = "form-group"
	document.getElementById("input-id-help").innerHTML = "";
	document.getElementById("input-name").parentElement.className = "form-group"
	document.getElementById("input-name-help").innerHTML = "";

	var data = {
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
	xowl.createConnector(function (status, ct, content) {
		if (status === 200) {
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
		} else {
			document.getElementById("input-id").parentElement.className = "form-group has-error"
			document.getElementById("input-id-help").innerHTML = content;
		}
	}, SELECTED_DOMAIN, data);
}

function onClickDeleteConnector() {
	if (SELECTED_CONNECTOR === null)
		return;
	xowl.deleteConnector(function (status, ct, content) {
		if (status === 200) {
			document.getElementById("connector-properties").style.display = "none";
			CONNECTORS.splice(CONNECTORS.indexOf(SELECTED_CONNECTOR), 1);
			render();
		}
	}, SELECTED_CONNECTOR.identifier);
}

function loadSVG() {
	var doc = document.getElementById("xowlsvg").contentDocument.documentElement;
	SVG_DB = doc.children[0];
	doc = document.getElementById("connectorsvg").contentDocument.documentElement;
	SVG_CONNECTOR = doc.children[0];
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
	text.appendChild(document.createTextNode("Federation Platform"));
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
	text.setAttribute("font-size", "60");
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
	loadSVG();
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