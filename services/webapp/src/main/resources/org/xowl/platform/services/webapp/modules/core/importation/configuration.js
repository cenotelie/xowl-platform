// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var configId = getParameterByName("id");
var configuration = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Core Services", uri: ROOT + "/modules/core/"},
			{name: "Import Data", uri: ROOT + "/modules/core/importation/"},
			{name: "Stored Configurations", uri: ROOT + "/modules/core/importation/configurations.html"},
			{name: configId}], function() {
		if (!configId || configId === null || configId === "")
			return;
		doGetData();
		setupSRAutocomplete();
		RESOURCE = configId;
	}, ["secured-resource-popups", "secured-resource-descriptor"]);
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getImporterConfiguration(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			configuration = content;
			document.getElementById("configuration-id").value = configuration.identifier;
			document.getElementById("configuration-name").value = configuration.name;
			document.getElementById("configuration-importer").value = configuration.importer;
			document.getElementById("btn-download").href = "data:" + MIME_JSON + ";base64," + btoa(JSON.stringify(configuration));
			document.getElementById("btn-download").download = configuration.name + ".json";
		}
	}, configId);
	xowl.getSecuredResourceDescriptor(function (status, ct, content) {
		onOperationEnded(200, content);
		if (status === 200) {
			renderDescriptor(content);
		}
	}, configId);
}

function onClickDelete() {
	var result = confirm("Delete configuration " + configuration.name + "?");
	if (!result)
		return;
	if (!onOperationRequest("Deleting configuration " + configuration.name + " ..."))
		return;
	xowl.dropUploadedDocument(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("Deleted configuration " + configuration.name + ".");
			waitAndGo("configurations.html");
		}
	}, configuration.identifier);
}