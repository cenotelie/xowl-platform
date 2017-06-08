// Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

function init() {
	doSetupPage(xowl, true, [
			{name: "Platform Administration", uri: ROOT + "/modules/admin/"},
			{name: "Platform Bots Management"}], function() {
		doGetData();
	});
}

function doGetData() {
	if (!onOperationRequest("Loading ..."))
		return;
	xowl.getBots(function (status, ct, content) {
		if (onOperationEnded(status, content)) {
			render(content);
		}
	});
}

function render(bots) {
	bots.sort(function (x, y) {
		return x.name.localeCompare(y.name);
	});
	var table = document.getElementById("bots");
	for (var  i = 0; i != bots.length; i++) {
		table.appendChild(renderBot(bots[i]));
	}
}

function renderBot(bot) {
	var row = document.createElement("tr");
	var cell = document.createElement("td");
	var icon = document.createElement("img");
	icon.src = ROOT + renderBotGetIcon(bot);
	icon.width = 40;
	icon.height = 40;
	icon.style.marginRight = "20px";
	icon.title = bot.identifier;
	cell.appendChild(icon);
	var link = document.createElement("a");
	link.href = "bot.html?id=" + encodeURIComponent(bot.identifier);
	link.appendChild(document.createTextNode(bot.name));
	cell.appendChild(link);
	row.appendChild(cell);

	cell = document.createElement("td");
	var button = renderBotAction(bot);
	if (button != null)
		cell.appendChild(button);
	row.appendChild(cell);
	return row;
}

function renderBotGetIcon(bot) {
	if (bot.status === "Asleep" || bot.status === "GoingToSleep")
		return "/assets/bot-inactive.svg";
	if (bot.status === "WakingUp" || bot.status === "Awaken" || bot.status === "Working")
		return "/assets/bot.svg";
	return "/assets/bot-invalid.svg";
}

function renderBotAction(bot) {
	if (bot.status === "Asleep" || bot.status === "GoingToSleep")
		return renderBotActionWakeup(bot);
	if (bot.status === "WakingUp" || bot.status === "Awaken" || bot.status === "Working")
		return renderBotActionPutToSleep(bot);
	return null;
}

function renderBotActionWakeup(bot) {
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/bot.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "WAKEUP";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(icon);
	button.appendChild(document.createTextNode("Wake up"));
	button.onclick = function() { doWakeup(bot); };
	return button;
}

function renderBotActionPutToSleep(bot) {
	var icon = document.createElement("img");
	icon.src = ROOT + "/assets/bot-inactive.svg";
	icon.width = 20;
	icon.height = 20;
	icon.title = "SLEEP";
	var button = document.createElement("span");
	button.classList.add("btn");
	button.classList.add("btn-default");
	button.appendChild(icon);
	button.appendChild(document.createTextNode("Put to sleep"));
	button.onclick = function() { doPutToSleep(bot); };
	return button;
}

function doWakeup(bot) {
	popupConfirm(richString(["Wake bot ", bot, " up?"]), function() {
		if (!onOperationRequest(richString(["Waking bot ", bot, " up ..."])))
			return;
		xowl.wakeupBot(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Awoke bot ", bot, "."]));
				waitAndRefresh();
			}
		}, bot.identifier);
	});
}

function doPutToSleep(bot) {
	popupConfirm(richString(["Put bot ", bot, " to sleep?"]), function() {
		if (!onOperationRequest(richString(["Putting bot ", bot, " to sleep ..."])))
			return;
		xowl.putBotToSleep(function (status, ct, content) {
			if (onOperationEnded(status, content)) {
				displayMessage("success", richString(["Put bot ", bot, " to sleep."]));
				waitAndRefresh();
			}
		}, bot.identifier);
	});
}