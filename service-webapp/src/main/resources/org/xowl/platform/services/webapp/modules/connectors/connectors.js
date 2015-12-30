// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function init() {
	var url = document.URL;
	var index = url.indexOf("/web/");
	if (index > 0)
		document.getElementById("input-uri-addon").innerHTML = url.substring(0, index) + "/api/";
	request("connectors", function (status, ct, content) {
		if (status == 200) {
			setup(JSON.parse(content));
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

function onClickDB(db) {
	alert("This is a triple store");
}

function onClickConnector(connector) {
	alert("This is the connector " + connector.name);
}

function onClickLink(connector) {

}

var GRAPH_WIDTH = 1024;
var GRAPH_HEIGHT = 512;
var SVG_DEFINITIONS = null;
var SVG_DB = null;
var SVG_CONNECTOR = null;
var SVG_DB_SCALE = 0.5;
var SVG_CONNECTOR_SCALE = 0.25;

function setupLoadSVG() {
	SVG_DEFINITIONS = document.createElementNS("http://www.w3.org/2000/svg", "defs");
	var doc = document.getElementById("dbsvg").contentDocument.documentElement;
	for (var i = 0; i != doc.children[0].children.length; i++)
		SVG_DEFINITIONS.appendChild(doc.children[0].children[i].cloneNode(true));
	SVG_DB = doc.children[1];
	doc = document.getElementById("connectorsvg").contentDocument.documentElement;
	SVG_CONNECTOR = doc.children[0];
}

function createCanvas(nb) {
	var height = 256 * SVG_CONNECTOR_SCALE * 2 * nb;
	GRAPH_HEIGHT = (height < 300 ? 300 : height);
	var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	var canvas = document.createElementNS("http://www.w3.org/2000/svg", "g");
	svg.setAttribute("height", GRAPH_HEIGHT);
	svg.setAttribute("width", GRAPH_WIDTH);
	svg.appendChild(SVG_DEFINITIONS);
	svg.appendChild(canvas);
	document.getElementById("display").appendChild(svg);
	return canvas;
}

function newDB(name, x, y) {
	var node = SVG_DB.cloneNode(true);
	node.setAttribute("transform", "translate(" + x + "," + y + ")scale(" + SVG_DB_SCALE + ")");
	node.setAttribute("class", "db");
	node.onclick = function () { onClickDB(name); };
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", 128);
	text.setAttribute("y", 287 + 40);
	text.setAttribute("text-anchor", "middle");
	text.setAttribute("font-family", "sans-serif");
	text.setAttribute("font-size", "40");
	text.appendChild(document.createTextNode(name));
	node.appendChild(text);
	return node;
}

function newConnector(def, x, y) {
	var node = SVG_CONNECTOR.cloneNode(true);
	node.setAttribute("transform", "translate(" + x + "," + y + ")scale(" + SVG_CONNECTOR_SCALE + ")");
	node.setAttribute("class", "connector");
	node.onclick = function () { onClickConnector(def); };
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", 256 + 10);
	text.setAttribute("y", 256 / 2);
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

function newDBLink(x1, y1, x2, y2) {
	var link = document.createElementNS("http://www.w3.org/2000/svg", "path");
	link.setAttribute("class", "dblink");
	link.setAttribute("fill", "none");
	link.setAttribute("stroke-width", "3");
	link.setAttribute("stroke-linecap", "round");
	link.setAttribute("d", "M " + x1 + " " + y1 + " L " + x2 + " " + y2);
	return link;
}

function setup(connectors) {
	setupLoadSVG();
	var svg = createCanvas(connectors.length);
	svg.appendChild(newDBLink(20 + 256 * SVG_DB_SCALE + 5, GRAPH_HEIGHT / 2, 300 - 5, GRAPH_HEIGHT / 2));
	svg.appendChild(newDB("Live Store", 20, (GRAPH_HEIGHT - 286 * SVG_DB_SCALE) / 2));
	svg.appendChild(newDB("Long Term", 300, (GRAPH_HEIGHT - 286 * SVG_DB_SCALE) / 2));
	var pad = (GRAPH_HEIGHT - connectors.length * 256 * SVG_CONNECTOR_SCALE) / (connectors.length + 1);
	var y = pad;
	for (var i = 0; i != connectors.length; i++) {
		svg.appendChild(newLink(connectors[i], 300 + 256 * SVG_DB_SCALE + 5, GRAPH_HEIGHT / 2, 600 - 5, y + 256 * SVG_CONNECTOR_SCALE / 2));
		svg.appendChild(newConnector(connectors[i], 600, y));
		y += 256 * SVG_CONNECTOR_SCALE + pad;
	}
}