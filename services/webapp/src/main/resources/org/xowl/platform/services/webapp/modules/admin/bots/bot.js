// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();
var botId = getParameterByName("id");
var bot = null;

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Bots Management", uri: ROOT + "/modules/admin/bots/"},
			{name: "Bot " + botId}], function() {
		if (!botId || botId === null || botId === "")
			return;
		if (!onOperationRequest("Loading ..."))
			return;
		xowl.getBot(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				bot = content;
				render();
			}
		}, botId);
	});
}

function render() {
	document.getElementById("bot-identifier").value = bot.identifier;
	document.getElementById("bot-name").value = bot.name;
	document.getElementById("bot-type").value = bot.botType;
	document.getElementById("bot-wakeup-on-startup").value = (bot.wakeupOnStartup ? "YES" : "NO");
	document.getElementById("bot-security-user").value = bot.securityUser;
	document.getElementById("bot-status").value = bot.status;
}

function doWakeup() {
	var result = confirm("Wake bot " + bot.name + " up?");
	if (!result)
		return;
	if (!onOperationRequest("Waking bot " + bot.name + " up"))
		return;
	xowl.wakeupBot(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Awoke bot " + bot.name);
			waitAndRefresh();
		}
	}, bot.identifier);
}

function doPutToSleep() {
	var result = confirm("Put bot " + bot.name + " to sleep?");
	if (!result)
		return;
	if (!onOperationRequest("Putting bot " + bot.name + " to sleep"))
		return;
	xowl.putBotToSleep(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			displayMessage("success", "Put bot " + bot.name + " to sleep");
			waitAndRefresh();
		}
	}, bot.identifier);
}