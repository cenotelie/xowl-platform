// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();
var SCHEMAS = null;
var LINKS = [];
var TYPES = [];
var FILTER_LINKS = [];
var FILTER_TYPES = [];

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Impact Analysis"}], function() {
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getArtifactSchemas(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				SCHEMAS = content;
				renderSchemas(content);
			}
		});
	});
}

function renderSchemas(schemas) {
	schemas.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var select = document.getElementById("root-schema");
	var select_links = document.getElementById("filters-links-schema");
	var select_types = document.getElementById("filters-types-schema");

	for (var i = 0; i != schemas.length; i++) {
		schemas[i].classes.sort(function (x, y) {
			return x.name.localeCompare(y.name);
		});
		schemas[i].objectProperties.sort(function (x, y) {
			return x.name.localeCompare(y.name);
		});
		var option = document.createElement("option");
		option.value = schemas[i].identifier;
		option.appendChild(document.createTextNode(schemas[i].name));
		select.appendChild(option);
		var optgroup1 = document.createElement("optgroup");
		optgroup1.label = schemas[i].name;
		var optgroup2 = document.createElement("optgroup");
		optgroup2.label = schemas[i].name;
		select_links.appendChild(optgroup1);
		select_types.appendChild(optgroup2);
		for (var j = 0; j != schemas[i].objectProperties.length; j++) {
			LINKS.push({
				id: schemas[i].objectProperties[j].identifier,
				name: schemas[i].objectProperties[j].name,
				schemaName: schemas[i].name
			});
			option = document.createElement("option");
			option.value = schemas[i].objectProperties[j].identifier;
			option.appendChild(document.createTextNode(schemas[i].objectProperties[j].name));
			optgroup1.appendChild(option);
		}
		for (var j = 0; j != schemas[i].classes.length; j++) {
			TYPES.push({
				id: schemas[i].classes[j].identifier,
				name: schemas[i].classes[j].name,
				schemaName: schemas[i].name
			});
			option = document.createElement("option");
			option.value = schemas[i].classes[j].identifier;
			option.appendChild(document.createTextNode(schemas[i].classes[j].name));
			optgroup2.appendChild(option);
		}
	}

	if (schemas.length > 0) {
		select.value = schemas[0].identifier;
		onSchemaChange();
	}
}

function onSchemaChange() {
	var id = document.getElementById("root-schema").value;
	var schema = null;
	for (var i = 0; i != SCHEMAS.length; i++) {
		if (SCHEMAS[i].identifier === id) {
			schema = SCHEMAS[i];
			break;
		}
	}
	var select = document.getElementById("root-type");
	while (select.hasChildNodes()) {
		select.removeChild(select.lastChild);
	}
	for (var i = 0; i != schema.classes.length; i++) {
		var option = document.createElement("option");
		option.value = schema.classes[i].identifier;
		option.appendChild(document.createTextNode(schema.classes[i].name));
		select.appendChild(option);
	}
	if (schema.classes.length > 0) {
		select.value = schema.classes[0].identifier;
		onTypeChange();
	}
}

function onTypeChange() {
	var type = document.getElementById("root-type").value;
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.sparql(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			renderElements(content);
		}
	}, "DESCRIBE ?x WHERE { GRAPH ?g { ?x a <" + type + "> }}");
}

function renderElements(data) {
	var entities = parseNQuads(data);
	var names = Object.getOwnPropertyNames(entities);
	var select = document.getElementById("root-element");
	while (select.hasChildNodes()) {
		select.removeChild(select.lastChild);
	}
	for (var p = 0; p != names.length; p++) {
		var entity = entities[names[p]];
		if (!entity.isIRI)
			continue;
		var name = entity.id;
		for (var j = 0; j != entity.properties.length; j++) {
			var property = entity.properties[j];
			if (property.identifier.endsWith("name") || property.identifier.endsWith("label") || property.identifier.endsWith("title")) {
				var value = property.value;
				name = value.value;
				name += " (" + entity.id + ")";
			}
		}
		var option = document.createElement("option");
		option.value = entity.id;
		option.appendChild(document.createTextNode(name));
		select.appendChild(option);
	}
}

function onClickNewFilterLink() {
	var select_links = document.getElementById("filters-links-schema");
	var link = LINKS[select_links.selectedIndex];
	FILTER_LINKS.push(link.identifier);
	var table = document.getElementById("filters-links");
	var row = document.createElement("tr");
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode(link.schemaName + " - " + link.name));
	var cell2 = document.createElement("td");

	var span = document.createElement("span");
	span.classList.add("glyphicon");
	span.classList.add("glyphicon-minus");
	span.setAttribute("aria-hidden", "true");
	var button = document.createElement("a");
	button.classList.add("btn");
	button.classList.add("btn-xs");
	button.classList.add("btn-danger");
	button.title = "DELETE";
	button.appendChild(span);
	button.onclick = function (evt) {
		table.removeChild(row);
		FILTER_LINKS.splice(FILTER_LINKS.indexOf(link.identifier), 1);
	};
	cell2.appendChild(button);
	row.appendChild(cell1);
	row.appendChild(cell2);
	table.appendChild(row);
}

function onClickNewFilterType() {
	var select_types = document.getElementById("filters-types-schema");
	var type = TYPES[select_types.selectedIndex];
	FILTER_TYPES.push(type.identifier);
	var table = document.getElementById("filters-types");
	var row = document.createElement("tr");
	var cell1 = document.createElement("td");
	cell1.appendChild(document.createTextNode(type.schemaName + " - " + type.name));
	var cell2 = document.createElement("td");

	var span = document.createElement("span");
	span.classList.add("glyphicon");
	span.classList.add("glyphicon-minus");
	span.setAttribute("aria-hidden", "true");
	var button = document.createElement("a");
	button.classList.add("btn");
	button.classList.add("btn-xs");
	button.classList.add("btn-danger");
	button.title = "DELETE";
	button.appendChild(span);
	button.onclick = function (evt) {
		table.removeChild(row);
		FILTER_TYPES.splice(FILTER_TYPES.indexOf(type.identifier), 1);
	};
	cell2.appendChild(button);
	row.appendChild(cell1);
	row.appendChild(cell2);
	table.appendChild(row);
}

function onClickRun() {
	var select_root = document.getElementById("root-element");
	var select_degree = document.getElementById("degree");
	var select_is_links_inclusive = document.getElementById("filters-links-inclusive");
	var select_is_types_inclusive = document.getElementById("filters-types-inclusive");
	if (select_root.selectedIndex < 0) {
		return;
	}
	var filterLinks = [];
	for (var i = 0; i != FILTER_LINKS.length; i++) {
		filterLinks.push({
			filtered: FILTER_LINKS[i]
		});
	}
	var filterTypes = [];
	for (var i = 0; i != FILTER_TYPES.length; i++) {
		filterTypes.push({
			filtered: FILTER_TYPES[i]
		});
	}
	var definition = {
		root: select_root.value,
		degree: select_degree.value,
		isFilterLinksInclusive: (select_is_links_inclusive.selectedIndex === 0),
		isFilterResultsInclusive: (select_is_types_inclusive.selectedIndex === 0),
		filterLinks: filterLinks,
		filterResults: filterTypes
	};
	if (!onOperationRequest("Launching the analysis ..."))
		return;
	xowl.newImpactAnalysis(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", { type: "org.xowl.infra.utils.RichString", parts: ["Launched analysis as job ", content, "."]});
			waitAndGo("result.html?id=" + encodeURIComponent(content.identifier) + "&name=" + encodeURIComponent(content.name));
		}
	}, definition);
}