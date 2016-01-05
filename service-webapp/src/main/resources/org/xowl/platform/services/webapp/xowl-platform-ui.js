// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

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

function rdfToString(value) {
    if (value.type === "uri" || value.type === "iri") {
        return value.value;
    } else if (value.type === "bnode") {
        return '_:' + value.value;
    } else if (value.type === "blank") {
		return '_:' + value.id;
    } else if (value.type === "variable") {
		return '?' + value.value;
    } else if (value.hasOwnProperty("lexical")) {
		return '"' + value.lexical + '"' +
			(value.datatype !== null ? '^^<' + value.datatype + '>' : '') +
			(value.lang !== null ? '@' + value.lang : '');
    } else {
		return '"' + value.value + '"' +
			(value.datatype !== null ? '^^<' + value.datatype + '>' : '') +
			(value.hasOwnProperty("xml:lang") ? '@' + value["xml:lang"] : '');
    }
}
