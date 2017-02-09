// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

var xowl = new XOWL();
var jobId = getParameterByName("id");
var jobName = getParameterByName("name");
var SCHEMAS = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Impact Analysis", uri: ROOT + "/modules/core/impact/"},
			{name: jobName}], function() {
		if (!jobId || jobId === null || jobId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getArtifactSchemas(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				SCHEMAS = content;
				waitForJob(jobId, jobName, function (job) {
					onJobCompleted(job);
				});
			}
		});
	});
}

function onJobCompleted(job) {
	if (!job.result.hasOwnProperty("isSuccess")) {
		displayMessage("error", "No result ...");
	} else if (!job.result.isSuccess) {
		displayMessage("error", "FAILURE: " + job.result.message);
	} else {
		renderResult(job);
	}
}

function getTypeName(idType) {
	for (var i = 0; i != SCHEMAS.length; i++) {
		for (var j = 0; j != SCHEMAS[i].classes.length; j++) {
			if (idType === SCHEMAS[i].classes[j].identifier) {
				return SCHEMAS[i].name + " - " + SCHEMAS[i].classes[j].name;
			}
		}
	}
	return idType;
}

function getLinkName(idLink) {
	for (var i = 0; i != SCHEMAS.length; i++) {
		for (var j = 0; j != SCHEMAS[i].objectProperties.length; j++) {
			if (idLink === SCHEMAS[i].objectProperties[j].identifier) {
				return SCHEMAS[i].objectProperties[j].name;
			}
		}
	}
	return idLink;
}

function renderResult(job) {
	var table = document.getElementById("result");

	var isFilterInclusive = job.payload.isFilterResultsInclusive;
	var parts = job.result.payload.parts;
	var map = {};

	for (var i = 0; i != parts.length; i++) {
		var types = parts[i].types;
		for (var j = 0; j != types.length; j++) {
			if (map.hasOwnProperty(types[j])) {
				map[types[j]].push(parts[i]);
			} else {
				map[types[j]] = [parts[i]];
			}
		}
	}

	var names = Object.getOwnPropertyNames(map);
	for (var i = 0; i != names.length; i++) {
		var isMatch = false;
		for (var j = 0; j != job.payload.filterResults.length; j++) {
			if (job.payload.filterResults[j].filtered === names[i]) {
				isMatch = true;
				break;
			}
		}
		if (isMatch !== job.payload.isFilterResultsInclusive)
			continue;

		for (var j = 0; j != map[names[i]].length; j++) {
			var row = document.createElement("tr");
			var cell1 = document.createElement("td");
			var cell2 = document.createElement("td");
			var cell3 = document.createElement("td");
			row.appendChild(cell1);
			row.appendChild(cell2);
			row.appendChild(cell3);

			table.appendChild(row);

			var icon = document.createElement("img");
			icon.src = ROOT + "/assets/concept.svg";
			icon.width = 40;
			icon.height = 40;
			icon.style.marginRight = "20px";
			cell1.appendChild(icon);
			cell1.appendChild(document.createTextNode(getTypeName(names[i])));
			cell1.classList.add("entity" + (i % 2).toString());

			icon = document.createElement("img");
			icon.src = ROOT + "/assets/element.svg";
			icon.width = 40;
			icon.height = 40;
			icon.style.marginRight = "20px";
			cell2.appendChild(icon);
			cell2.appendChild(document.createTextNode(map[names[i]][j].name));
			cell2.classList.add("entity" + (i % 2).toString());

			cell3.appendChild(renderPaths(map[names[i]][j].paths, map[names[i]][j].name));
			cell3.classList.add("entity" + (i % 2).toString());
		}
	}
}

function renderPath(path, name) {
	var div = document.createElement("div");
	div.classList.add("path");
	for (var i = 0; i != path.elements.length; i++) {
		var span_target = document.createElement("span");
		span_target.appendChild(document.createTextNode(path.elements[i].target));
		span_target.classList.add("node");
		var span_property = document.createElement("span");
		span_property.appendChild(document.createTextNode(getLinkName(path.elements[i].property)));
		span_property.classList.add("link");

		var icon_element = document.createElement("img");
		icon_element.src = ROOT + "/assets/element.svg";
		icon_element.width = 20;
		icon_element.height = 20;
		icon_element.style.marginRight = "5px";
		div.appendChild(icon_element);
		div.appendChild(span_target);

		var icon_link = document.createElement("img");
		icon_link.src = ROOT + "/assets/relation.svg";
		icon_link.width = 20;
		icon_link.height = 20;
		icon_link.style.marginRight = "5px";
		div.appendChild(icon_link);
		div.appendChild(span_property);
	}
	var span_node = document.createElement("span");
	span_node.appendChild(document.createTextNode(name));
	span_node.classList.add("node");
	var icon_element = document.createElement("img");
	icon_element.src = ROOT + "/assets/element.svg";
	icon_element.width = 20;
	icon_element.height = 20;
	icon_element.style.marginRight = "5px";
	div.appendChild(icon_element);
	div.appendChild(span_node);

	return div;
}

function renderPaths(paths, node) {
	var div_paths = document.createElement("div");
	for (var i = 0; i != paths.length; i++) {
		var div = renderPath(paths[i], node);
		div_paths.appendChild(div);
	}
	return div_paths;
}