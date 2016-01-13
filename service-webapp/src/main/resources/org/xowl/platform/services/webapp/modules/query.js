// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var xowl = new XOWL();
var DEFAULT_QUERY =
	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
	"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
	"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
	"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
	"PREFIX kernel: <http://xowl.org/platform/schemas/kernel#>\n\n" +
	"SELECT DISTINCT ?x ?y WHERE { GRAPH ?g { ?x a ?y } }";
var HISTORY = [];

function init() {
	document.getElementById("sparql").value = DEFAULT_QUERY;
}

function onExecute() {
	var query = document.getElementById("sparql").value;
	HISTORY.push(query);
	renderHistory(HISTORY.length - 1);
	displayMessage("Working ...");
	xowl.sparql(function (status, ct, content) {
		if (status == 200) {
			
			document.getElementById("loader").style.display = "none";
		} else {
			displayMessage(getErrorFor(status, content));
		}
	}, query);
}

function renderHistory(index) {
	var date = new Date();
	var span = document.createElement("span");
	span.appendChild(document.createTextNode("recall " + (index + 1).toString()));
	span.classList.add("badge");
	span.style.cursor = "pointer";
	var cell1 = document.createElement("td");
	cell1.appendChild(span);
	var cell2 = document.createElement("td");
	cell2.appendChild(document.createTextNode(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds()));
}

function renderSparqlResults(ct, content) {
	var index = ct.indexOf(";");
    if (index !== -1)
        ct = ct.substring(0, index);
	if (ct === "application/sparql-results+json") {
		var data = JSON.parse(content);
		if (data.hasOwnProperty("boolean")) {
			var isSuccess = data.boolean;
			if (isSuccess)
				alert("OK");
			else
				alert(data.error);
			return;
		}
		var vars = data.head.vars;
		var solutions = data.results.bindings;
		for (var i = 0; i != vars.length; i++) {
			$scope.data.headers.push(vars[i]);
		}
		for (var i = 0; i != solutions.length; i++) {
			var solution = solutions[i];
			var row = { cells: [(i + 1).toString()] };
			for (var j = 0; j != vars.length; j++) {
				if (solution.hasOwnProperty(vars[j])) {
					row.cells.push(rdfToString(solution[vars[j]]));
				} else {
					row.cells.push('');
				}
			}
			$scope.data.rows.push(row);
		}
	} else if (type === "application/n-quads") {
		$scope.data.headers = ['s', 'p', 'o', 'g'];
		var entities = parseNQuads(content);
		var names = Object.getOwnPropertyNames(entities);
		for (var p = 0; p != names.length; p++) {
			var entity = entities[names[p]];
			for (j = 0; j != entity.properties.length; j++) {
				var property = entity.properties[j];
				var row = { cells: [] };
				if (entity.isIRI)
					row.cells.push(entity.id);
				else
					row.cells.push('_:' + entity.id);
				row.cells.push(property.id);
				row.cells.push(rdfToString(property.value));
				row.cells.push(property.graph);
				$scope.data.rows.push(row);
			}
		}
	}
}

function renderSparqlHeader(columns) {
	
}