// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var NODE_SIZE = 30;
var TEXT_SIZE = 12;
var TEXT_PAD = 4;

function init() {
	var node = createNode(100, 100, "http://xowl.org/platform/schemas/kernel#Artifact");
	document.getElementById("display").appendChild(node.dom);
}



function createNode(x, y, text) {
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
	var g = document.createElementNS("http://www.w3.org/2000/svg", "g");
	g.classList.add("entity");
	g.appendChild(ellipse);
	g.appendChild(path);
	g.appendChild(t);
	g.setAttribute("transform", "translate(" + x + " " + y + ")");
	var result = new GraphNode(g, x, y);
	result.onMovedTo = function (newX, newY) {
		g.setAttribute("transform", "translate(" + newX + " " + newY + ")");
	};
	result.onSelected = function () {
		g.classList.add("entity-selected");
	};
	result.onUnselected = function () {
		g.classList.remove("entity-selected");
	};
	return result;
}

function GraphNode(dom, x, y) {
	this.dom = dom;
	this.currentX = x;
	this.currentY = y;
	this.incomings = [];
	this.outgoings = [];
	this.isDown = false;
	this.downX = 0;
	this.downY = 0;
	var node = this;
	this.dom.onclick = function (evt) {
		if (evt.ctrlKey) {
			node.onActivate();
			return;
		}
		node.isDown = !node.isDown;
		if (node.isDown)
			node.onSelected();
		else
			node.onUnselected();
		node.downX = evt.clientX;
		node.downY = evt.clientY;
	};
	this.dom.onmousemove = function (evt) {
		if (!node.isDown)
			return;
		var offsetX = evt.clientX - node.downX;
		var offsetY = evt.clientY - node.downY;
		node.downX += offsetX;
		node.downY += offsetY;
		node.move(node.currentX + offsetX, node.currentY + offsetY);
	};
}
GraphNode.prototype.move = function (x, y) {
	this.currentX = x;
	this.currentY = y;
	this.onMovedTo(x, y);
	for (var i = 0; i != this.incomings.length; i++) {
		this.incomings[i].onTargetMoved(x, y);
	}
	for (var i = 0; i != this.outgoings.length; i++) {
		this.outgoings[i].onOriginMoved(x, y);
	}
}
GraphNode.prototype.onMovedTo = function (x, y) { }
GraphNode.prototype.onSelected = function () { }
GraphNode.prototype.onUnselected = function () { }
GraphNode.prototype.onActivate = function () { }

function GraphConnector(dom, origin, target) {
	this.dom = dom;
	this.origin = origin;
	this.target = target;
}
GraphConnector.prototype.onOriginMoved = function (x, y) { }
GraphConnector.prototype.onTargetMoved = function (x, y) { }

var CTXT = document.createElement("canvas").getContext('2d');

function getWidthOfText(text, fontName, fontSize) {
	CTXT.font = fontSize + "px " + fontName;
	return CTXT.measureText(text).width;
}