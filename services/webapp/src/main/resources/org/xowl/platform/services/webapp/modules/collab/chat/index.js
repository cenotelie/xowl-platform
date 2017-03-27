// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

var xowl = new XOWL();

//We assume here that IRIs are generated and are unique
//Hence, two conversations can have the same topic

/******************** Sample Data  *******************************/
var currentUserIRI = "ali_koudri";

var conversations = [
    {iri:"safety_reqs", topic: "Safety Requirements", owner:"ali_koudri", status: "private", state:"open",
     summary:"Discussion about the elicitation of safety requirements of the system", start:new Date("2017-01-16"),
     participants: ["stephen_creff", "michel_batteux", "zeng_li"], keywords: ["Safety", "OAR", "SysML"]},
    {iri:"sw_arch", topic: "Software Architecture", owner:"stephen_creff", status: "public", state:"closed",
     summary:"Discussion about the software choices for implementing software parts of the system", start:new Date("2017-02-11"),
     end:new Date("2017-02-19"), participants: ["ali_koudri", "michel_batteux"], keywords: ["UML", "SOA", "Java"]},
    {iri:"network_arch", topic: "Network Architecture", owner:"laurent_wouters", status: "public", state:"open",
     summary:"Discussion about the deployment of the system over the network", start:new Date("2017-02-25"),
     participants: ["stephen_creff", "michel_batteux"], keywords: ["SOA", "Server", "Apache"]},
    {iri:"performance_reqs", topic: "Performance Requirements", owner:"michel_batteux", status: "private", state:"open",
     summary:"Discussion about the expected performance of the system", start:new Date("2017-02-25"),
     participants: ["stephen_creff", "makhlouf_hadj"], keywords: ["Performance", "QoS"]}
];

var users = [
    {iri:"ali_koudri", firstName:"Ali", lastName:"Koudri"},
    {iri:"stephen_creff", firstName:"Stephen", lastName:"Creff"},
    {iri:"laurent_wouters", firstName:"Laurent", lastName:"Wouters"},
    {iri:"michel_batteux", firstName:"Michel", lastName:"Batteux"},
    {iri:"makhlouf_hadj", firstName:"Makhlouf", lastName:"Hadj"},
    {iri:"jerome_lenoir", firstName:"Jérôme", lastName:"Le Noir"},
    {iri:"sebastien_madelenat", firstName:"Sébastien", lastName:"Madelénat"},
    {iri:"zeng_li", firstName:"Zeng", lastName:"Li"}
];

/******************** Functions  *******************************/

var isNotCurrentUser = function(user){
    return user.iri !== currentUserIRI;
};

var currentUserConversations = function() {
    var user_iri = currentUserIRI;
    var res = [];
    for (var i = 0; i < conversations.length; i++)
    {
	if (conversations[i].owner === user_iri || conversations[i].status === "public") {
	    res.push(conversations[i]);
	    continue;
	}
	var participants = conversations[i].participants;
	for (var j = 0; j < participants.length; j++)
	{
	    if (participants[j] === user_iri) {
		res.push(conversations[i]);
		break;
	    }
	}
    }
    return res;
}

var formatDate = function(date){
    var d = date.getDate();
    var m = date.getMonth() + 1;
    var day = (d < 10)?"0"+d:d;
    var month = (m < 10)?"0"+m:m;
    return day + "/" + month + "/" + date.getFullYear();
};

var endDate = function(conversation){
    if (conversation.state === "open")
    {
	return "-";
    } else {
	return formatDate(conversation.end);
    }
};

var fullName = function(user_iri){
    var user;
    for (var i = 0; i < users.length; i++) {
	user = users[i];
	if (user.iri === user_iri) {
	    return user.firstName + " " + user.lastName;
	}
    }
    throw "Undefined User";
};

var isParticipant = function(user_iri, conversation) {
    var participants = conversation.participants;
    for(var i = 0; i < participants.length; i++)
    {
	if (participants[i] === user_iri) {
	    return true;
	}
    }
    return false;
};

var enterButton = function() {
    return $('<button type="button" class="btn btn-primary mgmt" data-toggle="tooltip" title="Enter">' +
	     '<span class="glyphicon glyphicon-arrow-right" aria-hidden="true"></span>' +
	     '</button>');
};

var configureButton = function() {
    return $('<button type="button" class="btn btn-success mgmt" data-toggle="tooltip" title="Configure">' +
	     '<span class="glyphicon glyphicon-wrench" aria-hidden="true"></span>' +
	     '</button>');
};


var archiveButton = function() {
    return $('<button type="button" class="btn btn-success mgmt" data-toggle="tooltip" title="Archive">' +
	     '<span class="glyphicon glyphicon-compressed" aria-hidden="true"></span>' +
	     '</button>');
};

var openCloseButton = function() {
    return $('<button type="button" class="btn btn-danger mgmt" data-toggle="tooltip" title="Close">' +
	     '<span class="glyphicon glyphicon-off" aria-hidden="true"></span>' +
	     '</button>');
};

var removeButton = function() {
    return $('<button type="button" class="btn btn-danger mgmt" data-toggle="tooltip" title="Remove">' +
	     '<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>' +
	     '</button>');
}

var subscribeButton = function() {
    return $('<button type="button" class="btn btn-primary mgmt" data-toggle="tooltip" title="Subscribe">' +
             '<span class="glyphicon glyphicon-log-in" aria-hidden="true"></span>' +
             '</button>');
};

var unsubscribeButton = function() {
    return $('<button type="button" class="btn btn-primary mgmt" data-toggle="tooltip" title="Unsubscribe">' +
             '<span class="glyphicon glyphicon-log-out" aria-hidden="true"></span>' +
             '</button>');
};

var observeButton = function() {
    return $('<button type="button" class="btn btn-primary mgmt" data-toggle="tooltip" title="Observe">' +
	     '<span class="glyphicon glyphicon-eye-open" aria-hidden="true"></span>' +
	     '</button>');
};

var setActions = function(conversation, cell){
    var user_iri = currentUserIRI;
    var isClosed = (conversation.state === "closed");
    if (conversation.owner === user_iri)
    {
	if (!isClosed){
	    cell.append(enterButton());
	    var cb = configureButton();
	    cell.append(cb);
	    cb.click(function(){
    		$(".modal-title").text("Configure Conversation");
		$("#e_topic").val(conversation.topic);
		$("#e_keywords").val(conversation.keywords.toString());
		$("#e_abstract").val(conversation.summary);
		$("#e_visibility").val(conversation.status);
    		var $participants = $('#e_participants');
    		var $non_participants = $('#e_non_participants');
    		$participants.empty();
    		$non_participants.empty();
    		$.each(users.filter(isNotCurrentUser), function(i, item){
    		    if (isParticipant(item.iri, conversation)) {
    			$participants.append($('<option>', {
    			    value: item.iri,
    			    text: item.firstName + " " + item.lastName
    			}));
    		    } else {
    			$non_participants.append($('<option>', {
    			    value: item.iri,
    			    text: item.firstName + " " + item.lastName
    			}));
    		    }
    		});
		$("#conversation-save").click(function(){
		    modifyConversation();
		});
    		$("#conversation-edition").modal('show');
	    });
	} else {
	    cell.append(archiveButton()).append(removeButton());
	}
	var ocb = openCloseButton();
	cell.append(ocb);
	ocb.click(function(){
	    if (conversation.state === "open") {
		conversation.state = "closed";
		$(this).removeClass("btn-danger").addClass("btn-success").attr("title","Open");
		conversation.end = new Date();
		$(this).parent().prev().text(formatDate(conversation.end));
	    } else {
		conversation.state = "open";
		$(this).removeClass("btn-success").addClass("btn-danger").attr("title","Close");
		conversation.end = null;
		$(this).parent().prev().text("-");
	    }
	});
    } else if (isParticipant(user_iri, conversation)) {
	if (!isClosed)
	{
	    cell.append(enterButton()).append(unsubscribeButton());
	}
	cell.append(observeButton());
    } else {
	if (conversation.status === "public") {
	    cell.append(observeButton());
	}
	cell.append(subscribeButton());
    }
};

var addSelectedUsers = function() {
    $("#e_non_participants option:selected").each(function(){
	$(this).remove().appendTo($("#e_participants"));
    });
};

var addAllUsers = function() {
    $("#e_non_participants option").each(function(){
	$(this).remove().appendTo($("#e_participants"));
    });
};

var removeSelectedUsers = function() {
    $("#e_participants option:selected").each(function(){
	$(this).remove().appendTo($("#e_non_participants"));
    });
};

var removeAllUsers = function() {
    $("#e_participants option").each(function(){
	$(this).remove().appendTo($("#e_non_participants"));
    });
};

var conversation2row = function(conv)
{
    var $conv_table = $("#conversations_table");
    var $row, $cell;
    $row = $('<tr></tr>');
    $row.append($('<td>' + conv.topic + '</td>'));
    $row.append($('<td>' + fullName(conv.owner) + '</td>'));
    $row.append($('<td>' + conv.summary + '</td>'));
    $row.append($('<td>' + formatDate(conv.start) + '</td>'));
    $row.append($('<td>' + endDate(conv) + '</td>'));
    $cell = $('<td></td>');
    setActions(conv, $cell);
    $row.append($cell);
    if (conv.status === "public")
    {
	$row.addClass("public-conversation");
    } else {
	$row.addClass("private-conversation");
    }
    $row.children().css("vertical-align", "middle");
    if (conv.state === "closed") {
	$row.css("font-style", "italic");
    } else {
	$row.css("font-weight", "200");
    }
    $conv_table.append($row);
}

var addConversation = function() {
    var topic = $("#e_topic").val();
    var iri = topic.toLowerCase().replace(" ", "_");
    var conv = {};
    conv.iri = iri;
    conv.topic = topic;
    conv.owner = currentUserIRI;
    conv.status = $("#e_visibility").val();
    conv.state = "open";
    conv.summary = $("#e_abstract").val();
    conv.start = new Date();
    conv.participants = $('#e_participants').val();
    conv.keywords = $("#e_keywords").val().split(",");
    conversations.push(conv);
    conversation2row(conv);
    $("#conversation-edition").modal('hide');
};

var modifyConversation = function() {
   $("#conversation-edition").modal('hide');
};

var openCreationDialog = function() {
    $(".modal-title").text("Create New Conversation");
    $("#e_topic").val("");
    $("#e_keywords").val("");
    $("#e_abstract").val("");
    $("#e_visibility").val("public");
    var $participants = $('#e_participants');
    var $non_participants = $('#e_non_participants');
    $participants.empty();
    $non_participants.empty();
    $.each(users.filter(isNotCurrentUser), function(i, item){
	$non_participants.append($('<option>', {
	    value: item.iri,
	    text: item.firstName + " " + item.lastName
	}));
    });
    $("#conversation-save").click(function(){
    	addConversation();
    });
    $("#conversation-edition").modal('show');
};

 /******************** Main  *******************************/

 $(function(){
    doSetupPage(xowl, true, [
    			{name: "Collaboration", uri: ROOT + "/modules/collab/"},
    			{name: "Conversations"}], function() {
    	});

     $.each(currentUserConversations(), function(i, item){
 	conversation2row(item);
     });
 });