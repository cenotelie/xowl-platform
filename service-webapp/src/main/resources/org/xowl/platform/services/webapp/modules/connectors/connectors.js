// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function init() {
	var url = document.URL;
	var index = url.indexOf("/web/");
	if (index > 0)
		document.getElementById("input-uri-addon").innerHTML = url.substring(0, index) + "/api/";
	document.getElementById("xowlsvg").addEventListener('load', function () {
		SVG_DB_LOADED = true;
		render();
	});
	document.getElementById("connectorsvg").addEventListener('load', function () {
		SVG_CONNECTOR_LOADED = true;
		render();
	});
	request("connectors", function (status, ct, content) {
		if (status == 200) {
			CONNECTORS = JSON.parse(content);
			render();
		}
	});
}

function request(uri, callback) {
	var xmlHttp = new XMLHttpRequest();
	xmlHttp.onreadystatechange = function () {
		if (xmlHttp.readyState == 4) {
			var ct = xmlHttp.getResponseHeader("Content-Type");
			callback(xmlHttp.status, ct, xmlHttp.responseText)
		}
	}
	xmlHttp.open("GET", "/api/" + uri, true);
	xmlHttp.setRequestHeader("Accept", "application/json");
	xmlHttp.setRequestHeader("Content-Type", "application/sparql-query");
	xmlHttp.send();
}

function onClickConnector(connector) {
	alert("This is the connector " + connector.name);
}

function onClickLink(connector) {

}

var CONNECTORS = null;
var GRAPH_WIDTH = 700;
var GRAPH_MIN_HEIGHT = 300;
var GRAPH_HEIGHT = GRAPH_MIN_HEIGHT;
var SVG_DB = null;
var SVG_CONNECTOR = null;
var SVG_DB_LOADED = false;
var SVG_DB_SIZE = 1000;
var SVG_DB_SCALE = 0.15;
var SVG_CONNECTOR_LOADED = false;
var SVG_CONNECTOR_SIZE = 256;
var SVG_CONNECTOR_SCALE = 0.25;
var GRAPH_DB_X = 20;
var GRAPH_CONNECTOR_X = 400;

function render() {
	if (CONNECTORS != null && SVG_DB_LOADED && SVG_CONNECTOR_LOADED)
		doRender(CONNECTORS);
}

function loadSVG() {
	var doc = document.getElementById("xowlsvg").contentDocument.documentElement;
	SVG_DB = doc.children[0];
	doc = document.getElementById("connectorsvg").contentDocument.documentElement;
	SVG_CONNECTOR = doc.children[0];
}

function createCanvas(nb) {
	var height = SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE * 2 * nb;
	GRAPH_HEIGHT = (height < GRAPH_MIN_HEIGHT ? GRAPH_MIN_HEIGHT : height);
	var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	var canvas = document.createElementNS("http://www.w3.org/2000/svg", "g");
	svg.setAttribute("height", GRAPH_HEIGHT.toString());
	svg.setAttribute("width", GRAPH_WIDTH.toString());
	svg.appendChild(canvas);
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

function doRender(connectors) {
	loadSVG();
	var svg = createCanvas(connectors.length);
	svg.appendChild(newDB(GRAPH_DB_X, (GRAPH_HEIGHT - SVG_DB_SIZE * SVG_DB_SCALE) / 2));
	var pad = (GRAPH_HEIGHT - connectors.length * SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE) / (connectors.length + 1);
	var y = pad;
	for (var i = 0; i != connectors.length; i++) {
		svg.appendChild(newLink(connectors[i], GRAPH_DB_X + SVG_DB_SIZE * SVG_DB_SCALE + 5, GRAPH_HEIGHT / 2, GRAPH_CONNECTOR_X - 5, y + SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE / 2));
		svg.appendChild(newConnector(connectors[i], GRAPH_CONNECTOR_X, y));
		y += SVG_CONNECTOR_SIZE * SVG_CONNECTOR_SCALE + pad;
	}
}