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
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ...", 2))
		return;
	xowl.getBot(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			bot = content;
			renderBotData();
		}
	}, botId);
	xowl.getBotMessages(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			bot = content;
			renderBotMessages();
		}
	}, botId);
}

function renderBotData() {
	document.getElementById("bot-identifier").value = bot.identifier;
	document.getElementById("bot-name").value = bot.name;
	document.getElementById("bot-type").value = bot.botType;
	document.getElementById("bot-wakeup-on-startup").value = (bot.wakeupOnStartup ? "YES" : "NO");
	document.getElementById("bot-security-user").value = bot.securityUser;
	document.getElementById("bot-status").value = bot.status;
}

function renderBotMessages(messages) {
	var table = document.getElementById("messages");
	for (var  i = 0; i != messages.length; i++) {
		table.appendChild(renderBotMessage(messages[i]));
	}
}

function renderBotMessage(message) {
	var row = document.createElement("tr");
	var cells = [document.createElement("td"),
		document.createElement("td"),
		document.createElement("td")];
	cells[0].appendChild(document.createTextNode(message.level));
	cells[1].appendChild(document.createTextNode(message.date));
	cells[2].appendChild(renderMessage(message.content));
	row.appendChild(cells[0]);
	row.appendChild(cells[1]);
	row.appendChild(cells[2]);
	return row;
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