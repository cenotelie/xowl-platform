// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

var DEFAULT_URI_MAPPINGS = [
    ["rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"],
    ["rdfs", "http://www.w3.org/2000/01/rdf-schema#"],
    ["xsd", "http://www.w3.org/2001/XMLSchema#"],
    ["owl", "http://www.w3.org/2002/07/owl#"]];

function getShortURI(value) {
	for (var i = 0; i != DEFAULT_URI_MAPPINGS.length; i++) {
		if (value.indexOf(DEFAULT_URI_MAPPINGS[i][1]) === 0) {
			return DEFAULT_URI_MAPPINGS[i][0] + ":" + value.substring(DEFAULT_URI_MAPPINGS[i][1].length);
		}
	}
	return value;
}

function renderJobPayload(payload) {
	if (payload instanceof String)
		return payload;
	return JSON.stringify(payload);
}

function renderXSPReply(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return "No result ...";
	if (!xsp.isSuccess) {
		return "FAILURE: " + xsp.message;
	} else if (xsp.hasOwnProperty("payload")) {
		// TODO: complete this
		return xsp.payload.name;
	} else {
		return "SUCCESS: " + xsp.message;
	}
}

function rdfToDom(value) {
	if (value.type === "uri" || value.type === "iri") {
		var dom = document.createElement("a");
		dom.appendChild(document.createTextNode(getShortURI(value.value)));
		dom.href = "explorer.html?id=" + encodeURIComponent(value.value);
		dom.classList.add("rdfIRI");
		return dom;
    } else if (value.type === "bnode") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('_:' + value.value));
		dom.classList.add("rdfBlank");
		return dom;
    } else if (value.type === "blank") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('_:' + value.id));
		dom.classList.add("rdfBlank");
		return dom;
    } else if (value.type === "variable") {
		var dom = document.createElement("span");
		dom.appendChild(document.createTextNode('?' + value.value));
		dom.classList.add("rdfVariable");
		return dom;
    } else if (value.hasOwnProperty("lexical")) {
		var span1 = document.createElement("span");
		span1.appendChild(document.createTextNode('"' + value.lexical + '"'));
		var dom = document.createElement("p");
		dom.classList.add("rdfLiteral");
		dom.appendChild(span1);
		if (value.datatype !== null) {
			dom.appendChild(document.createTextNode("^^<"));
			var link = document.createElement("a");
			link.appendChild(document.createTextNode(getShortURI(value.datatype)));
			link.href = "explorer.html?id=" + encodeURIComponent(value.datatype);
			link.classList.add("rdfIRI");
			dom.appendChild(link);
			dom.appendChild(document.createTextNode(">"));
		}
		if (value.lang !== null) {
			var span2 = document.createElement("span");
			span2.appendChild(document.createTextNode('@' + value.lang));
			span2.classList.add("badge");
			dom.appendChild(span2);
		}
		return dom;
    } else {
		var span1 = document.createElement("span");
		span1.appendChild(document.createTextNode('"' + value.value + '"'));
		var dom = document.createElement("p");
		dom.classList.add("rdfLiteral");
		dom.appendChild(span1);
		if (value.datatype !== null) {
			dom.appendChild(document.createTextNode("^^<"));
			var link = document.createElement("a");
			link.appendChild(document.createTextNode(getShortURI(value.datatype)));
			link.href = "explorer.html?id=" + encodeURIComponent(value.datatype);
			link.classList.add("rdfIRI");
			dom.appendChild(link);
			dom.appendChild(document.createTextNode(">"));
		}
		if (value.hasOwnProperty("xml:lang")) {
			var span2 = document.createElement("span");
			span2.appendChild(document.createTextNode('@' + value["xml:lang"]));
			span2.classList.add("badge");
			dom.appendChild(span2);
		}
		return dom;
    }
}

function displayError(text) {
	displayMessage("FAILURE: " + text);
}

function displayMessage(text) {
	var span = document.getElementById("loader-text");
	while (span.hasChildNodes())
		span.removeChild(span.lastChild);
	span.appendChild(document.createTextNode(text));
	document.getElementById("loader").style.display = "";
}