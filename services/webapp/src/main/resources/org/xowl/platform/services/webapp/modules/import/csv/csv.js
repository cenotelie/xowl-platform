// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPL v3

XOWL.prototype.getCSVDocuments = function (callback) {
	this.doQuery(function (code, type, content) {
		if (code === 200) {
			callback(code, "application/json", JSON.parse(content).payload);
		} else {
			callback(code, type, content);
		}
	}, "domains/syseng/collaboration?type=stakeholders");
}