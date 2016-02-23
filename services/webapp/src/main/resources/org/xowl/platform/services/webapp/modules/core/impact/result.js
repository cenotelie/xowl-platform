// Copyright (c) 2015 Laurent Wouters
// Provided under LGPL v3

var xowl = new XOWL();
var jobId = getParameterByName("id");

function init() {
    setupPage(xowl);
    trackJob(jobId, "Working ...", function (isSuccess) {
        if (isSuccess) {
            xowl.getJob(function (status, ct, content) {
                if (status == 200) {
                    renderResult(content);
                } else {
                    displayMessage(getErrorFor(status, content));
                }
            }, jobId);
        }
    });
}

function renderResult(content) {
    var table = document.getElementById("result");
    var row = document.createElement("tr");
    var cell1 = document.createElement("td");
    var cell2 = document.createElement("td");
    var cell3 = document.createElement("td");
    var parts = content.result.payload.parts;
    var types_nodes = [];

    for (var i = 0; parts.length; i++) {
        var type = parts[i].types;
        if (types_nodes.indexOf(type) === -1) {
            types_nodes.push(type);
        }
    }
    
    

    for (var j = 0; types_nodes.length; j++) {

        for (var i = 0; parts.length; i++) {
            if (parts.types.indexOf(types_nodes[j]) != -1) {

                cell1.appendChild(document.createTextNode(types_nodes[j]));

                cell2.appendChild(document.createTextNode(parts[i].node));
                var span = document.createElement("span");
                span.classList.add("glyphicon");
                span.classList.add("glyphicon-plus");
                span.setAttribute("aria-hidden", "true");
                var button = document.createElement("a");
                button.classList.add("btn");
                button.classList.add("btn-xs");
                button.classList.add("btn-info");
                button.title = "MORE";
                button.appendChild(span);
                button.onclick = function (evt) {

                };
                cell3.appendChild(button);
                row.appendChild(cell1);
                row.appendChild(cell2);
                row.appendChild(cell3);
                table.appendChild(row);
            }
        }
    }
}