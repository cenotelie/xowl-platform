// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

XOWL.prototype.getCSVDocuments = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/import/csv", null);
}

XOWL.prototype.getCSVDocument = function (callback, docId) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/import/csv", {document: docId});
}

XOWL.prototype.getCSVFirstLines = function (callback, docId, separator, textMarker) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/import/csv", {
		document: docId,
		separator: separator,
		textMarker: textMarker
	});
}

XOWL.prototype.dropCSVDocument = function (callback, docId) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/import/csv", {drop: docId}, {});
}

XOWL.prototype.uploadCSV = function (callback, name, content) {
	this.doJSRequest(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "PUT", "services/import/csv", {name: name}, content, "text/csv", "application/json");
}

XOWL.prototype.importCSV = function (callback, docId, separator, textMarker, skipFirst, mapping, base, supersede, version, archetype) {
	this.doCommand(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "services/import/csv", {
		import: docId,
		separator: separator,
		textMarker: textMarker,
		skipFirst: skipFirst,
		base: base,
		supersede: supersede,
		version: version,
		archetype: archetype
	}, mapping);
}