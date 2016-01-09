// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var GRAPH = new GraphCanvas(1280, 800, document.getElementById("display"));

function init() {
	var node1 = GRAPH.addNode(new GraphNode(100, 100, "http://xowl.org/platform/schemas/kernel#Artifact1"));
	var node2 = GRAPH.addNode(new GraphNode(200, 200, "http://xowl.org/platform/schemas/kernel#Artifact2"));
	var c1 = GRAPH.addConnector(new GraphConnector(node1, node2, "http://xowl.org/platform/schemas/kernel#test"));
}