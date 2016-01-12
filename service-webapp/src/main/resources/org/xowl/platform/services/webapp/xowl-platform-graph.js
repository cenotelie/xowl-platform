// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function GraphCanvas(width, height, svg) {
	this.domSVG = svg;
	this.domCanvas = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.domLayerNodes = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.domLayerConnectors = document.createElementNS("http://www.w3.org/2000/svg", "g");
	this.domSVG.setAttribute("width", width.toString());
	this.domSVG.setAttribute("height", height.toString());
	this.domSVG.appendChild(this.domCanvas);
	this.domCanvas.appendChild(this.domLayerConnectors);
	this.domCanvas.appendChild(this.domLayerNodes);
	this.isCanvasSelected = false;
	this.canvasDownX = 0;
	this.canvasDownY = 0;
	this.canvasStartX = 0;
	this.canvasStartY = 0;
	this.zoom = 1;
	this.nodes = [];
	this.connectors = [];
	this.selectedNode = null;
	var canvasThis = this;

	this.domSVG.onmousedown = function (evt) {
		if (canvasThis.selectedNode !== null)
			return;
		canvasThis.isCanvasSelected = true;
		canvasThis.canvasDownX = evt.clientX;
		canvasThis.canvasDownY = evt.clientY;
	}
	this.domSVG.onmouseup = function (evt) {
		if (canvasThis.selectedNode === null && canvasThis.isCanvasSelected) {
			canvasThis.isCanvasSelected = false;
			canvasThis.canvasStartX = canvasThis.canvasStartX - (evt.clientX - canvasThis.canvasDownX);
			canvasThis.canvasStartY = canvasThis.canvasStartY - (evt.clientY - canvasThis.canvasDownY);
			canvasThis.setupViewport();
		}
	}
	this.domSVG.onmousemove = function (evt) {
		if (canvasThis.selectedNode !== null) {
			var targetX = canvasThis.selectedNode.currentX + (evt.clientX - canvasThis.selectedNode.downX) / canvasThis.zoom;
			var targetY = canvasThis.selectedNode.currentY + (evt.clientY - canvasThis.selectedNode.downY) / canvasThis.zoom;
			canvasThis.selectedNode.downX = evt.clientX;
			canvasThis.selectedNode.downY = evt.clientY;
			canvasThis.selectedNode.moveTo(targetX, targetY);
		} else if (canvasThis.isCanvasSelected) {
			var targetX = canvasThis.canvasStartX - (evt.clientX - canvasThis.canvasDownX);
			var targetY = canvasThis.canvasStartY - (evt.clientY - canvasThis.canvasDownY);
			canvasThis.domCanvas.setAttribute("transform", "translate(" + -targetX + " " + -targetY + ") scale(" + canvasThis.zoom + ")");
		}
	};
}
GraphCanvas.prototype.setupViewport = function () {
	this.domCanvas.setAttribute("transform", "translate(" + -this.canvasStartX + " " + -this.canvasStartY + ") scale(" + this.zoom + ")");
}
GraphCanvas.prototype.zoomReset = function () {
	this.zoom = 1;
	this.setupViewport();
}
GraphCanvas.prototype.zoomUpdate = function (step) {
	this.zoom += step;
	this.setupViewport();
}
GraphCanvas.prototype.addNode = function (node) {
	this.nodes.push(node);
	this.domLayerNodes.appendChild(node.dom);
	node.parent = this;
	return node;
}
GraphCanvas.prototype.removeNode = function (node) {
	this.nodes.remove(node);
	this.domLayerNodes.removeChild(node.dom);
	node.parent = null;
	return node;
}
GraphCanvas.prototype.addConnector = function (connector) {
	this.connectors.push(connector);
	this.domLayerConnectors.appendChild(connector.dom);
	connector.parent = this;
	return connector;
}
GraphCanvas.prototype.removeConnector = function (connector) {
	this.connectors.remove(connector);
	this.domLayerConnectors.removeChild(connector.dom);
	connector.parent = null;
	return connector;
}

var NODE_SIZE = 30;
var TEXT_SIZE = 12;
var TEXT_PAD = 4;

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
		node.parent.selectedNode = node;
	}
	this.dom.onmouseup = function (evt) {
		node.isDown = false;
		node.downX = evt.clientX;
		node.downY = evt.clientY;
		node.parent.selectedNode = null;
	}
	this.dom.onclick = function (evt) {
		node.onActivate(evt);
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
GraphNode.prototype.onActivate = function (evt) { }

var ARROW_LENGTH = 20;
var ARROW_WIDTH = 10;

function GraphConnector(origin, target, text) {
	this.domPath = document.createElementNS("http://www.w3.org/2000/svg", "path");
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
	this.updateLink();
}
GraphConnector.prototype.onOriginMoved = function (x, y) {
	this.domText.setAttribute("transform", "translate(" + ((this.target.currentX + x) / 2) + " " + ((this.target.currentY + y) / 2) + ")");
	this.updateLink();
}
GraphConnector.prototype.onTargetMoved = function (x, y) {
	this.domText.setAttribute("transform", "translate(" + ((x + this.origin.currentX) / 2) + " " + ((y + this.origin.currentY) / 2) + ")");
	this.updateLink();
}
GraphConnector.prototype.updateLink = function () {
	var dx = this.target.currentX - this.origin.currentX;
	var dy = this.target.currentY - this.origin.currentY;
	var totalLength = Math.sqrt(dx * dx + dy * dy);
	var ratioPoint = (totalLength - NODE_SIZE / 2) / totalLength;
	var ratioRoot = (totalLength - NODE_SIZE / 2 - ARROW_LENGTH) / totalLength;
	var ratioSide = (ARROW_WIDTH / (2 * totalLength));
	var pointX = this.origin.currentX + dx * ratioPoint;
	var pointY = this.origin.currentY + dy * ratioPoint;
	var rootX = this.origin.currentX + dx * ratioRoot;
	var rootY = this.origin.currentY + dy * ratioRoot;
	var p1x = rootX + (-dy * ratioSide);
	var p1y = rootY + (dx * ratioSide);
	var p2x = rootX + (dy * ratioSide);
	var p2y = rootY + (-dx * ratioSide);
	this.domPath.setAttribute("d", "M " + this.origin.currentX + " " + this.origin.currentY +
		" L " + pointX + " " + pointY +
		" L " + p1x + " " + p1y +
		" L " + pointX + " " + pointY +
		" L " + p2x + " " + p2y +
		" L " + pointX + " " + pointY);
}

var CTXT = document.createElement("canvas").getContext('2d');

function getWidthOfText(text, fontName, fontSize) {
	CTXT.font = fontSize + "px " + fontName;
	return CTXT.measureText(text).width;
}