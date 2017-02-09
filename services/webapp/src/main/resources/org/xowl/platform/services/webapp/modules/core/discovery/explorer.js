// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var ENTITIES = {};
var EXPANDED = [];
var REQUESTED = [];
var NODES = {};
var CONNECTORS = {};
var GRAPH_WIDTH = document.getElementById("display").parentElement.clientWidth - 40;
var GRAPH_HEIGHT = 800;
var GRAPH_SPREAD = 150;
var GRAPH = new GraphCanvas(GRAPH_WIDTH, GRAPH_HEIGHT, document.getElementById("display"));

function init() {
	var rootId = getParameterByName("id");
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Traceability Exploration", uri: ROOT + "/modules/core/discovery"},
			{name: "Entity " + rootId}], function() {
		if (typeof (rootId) === "undefined" || rootId === null || rootId === "") {
			displayMessage("error", "Undefined root entity!");
			return;
		}
		ensureExpanded(rootId);
	});
}

function ensureExpanded(identifier) {
	if (EXPANDED.indexOf(identifier) >= 0)
		return;
	ensureData(identifier, doExpand);
}

function ensureData(identifier, continuation) {
	if (ENTITIES.hasOwnProperty(identifier)) {
		continuation(ENTITIES[identifier]);
		return;
	}
	if (REQUESTED.indexOf(identifier) >= 0)
		return;
	if (!onOperationRequest("Working ..."))
		return;
	REQUESTED.push(identifier);
	xowl.sparql(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			var data = parseNQuads(content);
			var names = Object.getOwnPropertyNames(data);
			var target = null;
			for (var p = 0; p != names.length; p++) {
				var entity = data[names[p]];
				if (!ENTITIES.hasOwnProperty(names[p]))
					ENTITIES[names[p]] = entity;
				if (names[p] === identifier)
					target = entity;
			}
			if (target == null) {
				target = { id: identifier, properties: [] };
				ENTITIES[identifier] = target;
			}
			continuation(target);
		} else {
			REQUESTED.splice(REQUESTED.indexOf(entity.id), 1);
		}
	}, "DESCRIBE <" + identifier + ">");
}

function doExpand(entity) {
	var nodeOrigin = null;
	if (NODES.hasOwnProperty(entity.id)) {
		nodeOrigin = NODES[entity.id];
	} else {
		nodeOrigin = GRAPH.addNode(new GraphNode(GRAPH_WIDTH / 2, GRAPH_HEIGHT / 2, entity.id));
		instrumentNode(nodeOrigin, entity.id);
		NODES[entity.id] = nodeOrigin;
	}
	var newNodes = [];
	for (var i = 0; i != entity.properties.length; i++) {
		var pair = entity.properties[i];
		if (pair.value.type === "iri" || pair.value.type === "uri") {
			var nodeTarget = null;
			if (NODES.hasOwnProperty(pair.value.value)) {
				nodeTarget = NODES[pair.value.value];
			} else {
				nodeTarget = GRAPH.addNode(new GraphNode(GRAPH_WIDTH / 2, GRAPH_HEIGHT / 2, pair.value.value));
				instrumentNode(nodeTarget, pair.value.value);
				NODES[pair.value.value] = nodeTarget;
				newNodes.push(nodeTarget);
			}
			instrumentConnector(GRAPH.addConnector(new GraphConnector(nodeOrigin, nodeTarget, pair.id)), pair.id);
		}
	}
	if (!onOperationRequest("Working ..."))
		return;
	xowl.sparql(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			var solutions = JSON.parse(content).results.bindings;
			for (var i = 0; i != solutions.length; i++) {
				var s = solutions[i].s.value;
				var p = solutions[i].p.value;
				var nodeTarget = null;
				if (NODES.hasOwnProperty(s)) {
					nodeTarget = NODES[s];
				} else {
					nodeTarget = GRAPH.addNode(new GraphNode(GRAPH_WIDTH / 2, GRAPH_HEIGHT / 2, s));
					instrumentNode(nodeTarget, s);
					NODES[s] = nodeTarget;
					newNodes.push(nodeTarget);
				}
				instrumentConnector(GRAPH.addConnector(new GraphConnector(nodeTarget, nodeOrigin, p)), p);
			}
			doLayout(nodeOrigin, newNodes);
			EXPANDED.push(entity.id);
		}
	}, "SELECT DISTINCT ?s ?p WHERE { GRAPH ?g { ?s ?p <" + entity.id + "> } }");
}

function doLayout(nodeOrigin, newNodes) {
	if (newNodes.length > 0) {
		var angleOffset = newNodes.length == 1 ? 0 : (Math.PI / (newNodes.length - 1));
		var angle = 0;
		for (var i = 0; i != newNodes.length; i++) {
			var offsetX = GRAPH_SPREAD * Math.cos(angle);
			var offsetY = GRAPH_SPREAD * Math.sin(angle);
			angle += angleOffset;
			newNodes[i].moveTo(nodeOrigin.currentX + offsetX, nodeOrigin.currentY + offsetY);
		}
	}
}

function instrumentNode(node, identifier) {
	node.onActivate = function (evt) {
		if (evt.detail >= 2) {
			ensureExpanded(identifier);
		} else {
			onNodeSelect(identifier);
		}
	}
}

function instrumentConnector(connector, identifier) {
	if (CONNECTORS.hasOwnProperty(identifier)) {
		CONNECTORS[identifier].elements.push(connector);
		if (!CONNECTORS[identifier].visible)
			connector.hide();
	} else {
		CONNECTORS[identifier] = {
			visible: true,
			elements: [connector]
		};
		var span = document.createElement("span");
		span.appendChild(document.createTextNode(getShortURI(identifier)));
		var cell1 = document.createElement("td");
		cell1.appendChild(span);
		var toggle = document.createElement("div");
		toggle.className = "toggle-button toggle-button-selected";
		toggle.appendChild(document.createElement("button"));
		toggle.onclick = function () {
			for (var i = 0; i != CONNECTORS[identifier].elements.length; i++) {
				if (CONNECTORS[identifier].visible)
					CONNECTORS[identifier].elements[i].hide();
				else
					CONNECTORS[identifier].elements[i].show();
			}
			CONNECTORS[identifier].visible = !CONNECTORS[identifier].visible;
			toggle.className = "toggle-button" + (CONNECTORS[identifier].visible ? " toggle-button-selected" : "");
		}
		var cell2 = document.createElement("td");
		cell2.appendChild(toggle);
		var row = document.createElement("tr");
		row.appendChild(cell1);
		row.appendChild(cell2);
		document.getElementById("options").appendChild(row);
	}
}

function onNodeSelect(identifier) {
	ensureData(identifier, renderAttributes);
}

function renderAttributes(entity) {
	if (entity === null)
		return;
	var table = document.getElementById("properties");
	while (table.hasChildNodes())
		table.removeChild(table.lastChild);
	document.getElementById("entity-identifier").value = entity.id;
	for (var i = 0; i != entity.properties.length; i++) {
		var pair = entity.properties[i];
		var row = document.createElement("tr");
		var cell1 = document.createElement("td");
		var cell2 = document.createElement("td");
		cell1.appendChild(rdfToDom({ type: "iri", value: pair.id }));
		cell2.appendChild(rdfToDom(pair.value));
		row.appendChild(cell1);
		row.appendChild(cell2);
		table.appendChild(row);
	}
}

function onClickPhysics() {
	GRAPH.usePhysics = !GRAPH.usePhysics;
	document.getElementById("button-physics").className = "toggle-button" + (GRAPH.usePhysics ? " toggle-button-selected" : "");
}