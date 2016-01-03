// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function renderJobPayload(payload) {
	if (payload instanceof String)
		return payload;
	return JSON.stringify(payload);
}

function renderXSPReply(xsp) {
	if (!xsp.hasOwnProperty("isSuccess"))
		return "Not completed yet";
	if (!xsp.isSuccess) {
		return "FAILURE: " + xsp.message;
	} else if (xsp.hasOwnProperty("payload")) {
		// TODO: complete this
		return xsp.payload.name;
	} else {
		return "SUCCESS: " + xsp.message;
	}
}