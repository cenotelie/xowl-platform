// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var NODE_SIZE = 30;
var TEXT_SIZE = 12;
var TEXT_PAD = 4;
var SELECTED_NODE = null;
var CANVAS_SELECTED = false;
var CANVAS_DOWNX = 0;
var CANVAS_DOWNY = 0;
var CANVAS_STARTX = 0;
var CANVAS_STARTY = 0;
var DISPLAY_WIDTH = 1280;
var DISPLAY_HEIGHT = 800;
var DISPLAY = document.getElementById("display");
var CANVAS = document.getElementById("canvas");
var LAYER_NODES = document.getElementById("display-nodes");
var LAYER_CONNECTORS = document.getElementById("display-connectors");
var ZOOM = 1;
var ZOOM_STEP = 0.1;

function init() {
	DISPLAY.setAttribute("width", DISPLAY_WIDTH.toString());
	DISPLAY.setAttribute("height", DISPLAY_HEIGHT.toString());
	instrumentCanvas();
	var node1 = new GraphNode(100, 100, "http://xowl.org/platform/schemas/kernel#Artifact1");
	var node2 = new GraphNode(200, 200, "http://xowl.org/platform/schemas/kernel#Artifact2");
	var c1 = new GraphConnector(node1, node2, "http://xowl.org/platform/schemas/kernel#test");
	LAYER_NODES.appendChild(node1.dom);
	LAYER_NODES.appendChild(node2.dom);
	LAYER_CONNECTORS.appendChild(c1.dom);
}

function instrumentCanvas() {
	DISPLAY.onmousedown = function (evt) {
		CANVAS_SELECTED = true;
		CANVAS_DOWNX = evt.clientX;
		CANVAS_DOWNY = evt.clientY;
	}
	DISPLAY.onmouseup = function (evt) {
		CANVAS_SELECTED = false;
		CANVAS_STARTX = CANVAS_STARTX - (evt.clientX - CANVAS_DOWNX);
		CANVAS_STARTY = CANVAS_STARTY - (evt.clientY - CANVAS_DOWNY);
	}
	DISPLAY.onmousemove = function (evt) {
		if (SELECTED_NODE !== null) {
			var targetX = SELECTED_NODE.currentX + (evt.clientX - SELECTED_NODE.downX) / ZOOM;
			var targetY = SELECTED_NODE.currentY + (evt.clientY - SELECTED_NODE.downY) / ZOOM;
			SELECTED_NODE.downX = evt.clientX;
			SELECTED_NODE.downY = evt.clientY;
			SELECTED_NODE.moveTo(targetX, targetY);
		} else if (CANVAS_SELECTED) {
			var targetX = CANVAS_STARTX - (evt.clientX - CANVAS_DOWNX);
			var targetY = CANVAS_STARTY - (evt.clientY - CANVAS_DOWNY);
			CANVAS.setAttribute("transform", "translate(" + -targetX + " " + -targetY + ") scale(" + ZOOM + ")");
		}
	};
}

function onClickZoomPlus() {
	ZOOM += ZOOM_STEP;
	CANVAS.setAttribute("transform", "translate(" + -CANVAS_STARTX + " " + -CANVAS_STARTY + ") scale(" + ZOOM + ")");
}

function onClickZoomMinus() {
	ZOOM -= ZOOM_STEP;
	CANVAS.setAttribute("transform", "translate(" + -CANVAS_STARTX + " " + -CANVAS_STARTY + ") scale(" + ZOOM + ")");
}

function onClickZoomReset() {
	ZOOM = 1;
	CANVAS.setAttribute("transform", "translate(" + -CANVAS_STARTX + " " + -CANVAS_STARTY + ") scale(" + ZOOM + ")");
}

function GraphNode(x, y, text) {
	var length = getWidthOfText(text, "sans-serif", TEXT_SIZE);
	var ellipse = document.createElementNS("http://www.w3.org/2000/svg", "ellipse");
	ellipse.setAttribute("cx", "0");
	ellipse.setAttribute("cy", "0");
	ellipse.setAttribute("rx", (NODE_SIZE / 2).toString());
	ellipse.setAttribute("ry", (NODE_SIZE / 2).toString());
	var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
	path.setAttribute("d", "M 0 " + NODE_SIZE / 2 +
		" l " + (NODE_SIZE / 2) + " " + (NODE_SIZE / 2) +
		" h " + ((length - NODE_SIZE) / 2 + TEXT_PAD) +
		" v " + (TEXT_SIZE + TEXT_PAD * 2) +
		" h " + -(length + TEXT_PAD * 2) +
		" v " + -(TEXT_SIZE + TEXT_PAD * 2) +
		" h " + ((length - NODE_SIZE) / 2 + TEXT_PAD) +
		" l " + (NODE_SIZE / 2) + " " + -(NODE_SIZE / 2));
	var t = document.createElementNS("http://www.w3.org/2000/svg", "text");
	t.setAttribute("y", (NODE_SIZE + TEXT_PAD * 2 + TEXT_SIZE / 2).toString());
	t.setAttribute("x", "0");
	t.setAttribute("text-anchor", "middle");
	t.setAttribute("font-family", "sans-serif");
	t.setAttribute("font-size", TEXT_SIZE.toString());
	t.appendChild(document.createTextNode(text));
	this.dom = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.dom.classList.add("entity");
	this.dom.appendChild(ellipse);
	this.dom.appendChild(path);
	this.dom.appendChild(t);
	this.dom.setAttribute("transform", "translate(" + x + " " + y + ")");
	this.currentX = x;
	this.currentY = y;
	this.incomings = [];
	this.outgoings = [];
	this.isDown = false;
	this.downX = 0;
	this.downY = 0;
	var node = this;
	this.dom.onmousedown = function (evt) {
		node.isDown = true;
		node.downX = evt.clientX;
		node.downY = evt.clientY;
		SELECTED_NODE = node;
	}
	this.dom.onmouseup = function (evt) {
		node.isDown = false;
		node.downX = evt.clientX;
		node.downY = evt.clientY;
		SELECTED_NODE = null;
	}
	this.dom.onclick = function (evt) {
		if (evt.ctrlKey) {
			node.onActivate();
			return;
		}
	};
}
GraphNode.prototype.moveTo = function (x, y) {
	this.currentX = x;
	this.currentY = y;
	this.dom.setAttribute("transform", "translate(" + x + " " + y + ")");
	for (var i = 0; i != this.incomings.length; i++) {
		this.incomings[i].onTargetMoved(x, y);
	}
	for (var i = 0; i != this.outgoings.length; i++) {
		this.outgoings[i].onOriginMoved(x, y);
	}
}
GraphNode.prototype.onActivate = function () { }

function GraphConnector(origin, target, text) {
	this.domPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
	this.domPath.setAttribute("d", "M " + origin.currentX + " " + origin.currentY + " L " + target.currentX + " " + target.currentY);
	var length = getWidthOfText(text, "sans-serif", TEXT_SIZE);
	var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
	path.setAttribute("d", "M 0 0" +
		" l " + ((TEXT_SIZE + TEXT_PAD * 2) / 3) + " " + ((TEXT_SIZE + TEXT_PAD * 2) / 6) +
		" v " + ((TEXT_SIZE + TEXT_PAD * 2) / 3) +
		" h " + (length + TEXT_PAD * 2) +
		" v " + -(TEXT_SIZE + TEXT_PAD * 2) +
		" h " + -(length + TEXT_PAD * 2) +
		" v " + ((TEXT_SIZE + TEXT_PAD * 2) / 3) +
		" l " + -((TEXT_SIZE + TEXT_PAD * 2) / 3) + " " + ((TEXT_SIZE + TEXT_PAD * 2) / 6));
	var t = document.createElementNS("http://www.w3.org/2000/svg", "text");
	t.setAttribute("x", ((TEXT_SIZE + TEXT_PAD * 2) / 3 + TEXT_PAD).toString());
	t.setAttribute("y", TEXT_PAD.toString());
	t.setAttribute("text-anchor", "start");
	t.setAttribute("font-family", "sans-serif");
	t.setAttribute("font-size", TEXT_SIZE.toString());
	t.appendChild(document.createTextNode(text));
	this.domText = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.domText.appendChild(path);
	this.domText.appendChild(t);
	this.domText.setAttribute("transform", "translate(" + ((target.currentX + origin.currentX) / 2) + " " + ((target.currentY + origin.currentY) / 2) + ")");
	this.dom = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.dom.classList.add("connector");
	this.dom.appendChild(this.domPath);
	this.dom.appendChild(this.domText);
	this.origin = origin;
	this.target = target;
	origin.outgoings.push(this);
	target.incomings.push(this);
}
GraphConnector.prototype.onOriginMoved = function (x, y) {
	this.domPath.setAttribute("d", "M " + x + " " + y + " L " + this.target.currentX + " " + this.target.currentY);
	this.domText.setAttribute("transform", "translate(" + ((this.target.currentX + x) / 2) + " " + ((this.target.currentY + y) / 2) + ")");
}
GraphConnector.prototype.onTargetMoved = function (x, y) {
	this.domPath.setAttribute("d", "M " + this.origin.currentX + " " + this.origin.currentY + " L " + x + " " + y);
	this.domText.setAttribute("transform", "translate(" + ((x + this.origin.currentX) / 2) + " " + ((y + this.origin.currentY) / 2) + ")");
}

var CTXT = document.createElement("canvas").getContext('2d');

function getWidthOfText(text, fontName, fontSize) {
	CTXT.font = fontSize + "px " + fontName;
	return CTXT.measureText(text).width;
}