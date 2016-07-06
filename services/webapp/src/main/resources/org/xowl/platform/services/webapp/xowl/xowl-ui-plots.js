// Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
// Provided under LGPLv3

GRAPH_MARGIN = 20;

function renderLinePlot(definition, width, height) {
	var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
	var canvas = document.createElementNS("http://www.w3.org/2000/svg", "g");
	svg.setAttribute("height", height.toString());
	svg.setAttribute("width", width.toString());
	svg.appendChild(canvas);
	svg.appendChild(getAxisXForLinePlot(width, height));
	svg.appendChild(getAxisYForLinePlot(width, height));
	svg.appendChild(getLabelXForLinePlot(width, height, definition.dataX.title));
	svg.appendChild(getLabelYForLinePlot(width, height, definition.dataY.title));
	for (var i = 0; i != definition.dataY.series.length; i++) {
		var path = getLineForPlot(definition.dataX, definition.dataY.series[i], definition.dataY.min, width, height);
		if (path != null) {
			svg.appendChild(path);
		}
	}
	return svg;
}

function getAxisXForLinePlot(width, height) {
	var d = "M " + GRAPH_MARGIN + " " + (height - GRAPH_MARGIN) +
		" L " + (width - GRAPH_MARGIN) + " " + (height - GRAPH_MARGIN) +
		" L " + (width - GRAPH_MARGIN * 1.5) + " " + (height - GRAPH_MARGIN * 1.5) +
		" L " + (width - GRAPH_MARGIN) + " " + (height - GRAPH_MARGIN) +
		" L " + (width - GRAPH_MARGIN * 1.5) + " " + (height - GRAPH_MARGIN * 0.5);
	var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
	path.setAttribute("fill", "none");
	path.setAttribute("stroke", "black");
	path.setAttribute("stroke-width", "1");
	path.setAttribute("d", d);
	return path;
}

function getAxisYForLinePlot(width, height) {
	var d = "M " + GRAPH_MARGIN + " " + (height - GRAPH_MARGIN) +
		" L " + GRAPH_MARGIN + " " + GRAPH_MARGIN +
		" L " + (GRAPH_MARGIN * 0.5) + " " + (GRAPH_MARGIN * 1.5) +
		" L " + GRAPH_MARGIN + " " + GRAPH_MARGIN +
		" L " + (GRAPH_MARGIN * 1.5) + " " + (GRAPH_MARGIN * 1.5);
	var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
	path.setAttribute("fill", "none");
	path.setAttribute("stroke", "black");
	path.setAttribute("stroke-width", "1");
	path.setAttribute("d", d);
	return path;
}

function getLabelXForLinePlot(width, height, value) {
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", (GRAPH_MARGIN / 2).toString());
	text.setAttribute("y", (GRAPH_MARGIN / 2).toString());
	text.setAttribute("text-anchor", "start");
	text.setAttribute("font-family", "sans-serif");
	text.setAttribute("font-size", "10");
	text.appendChild(document.createTextNode(value));
	return text;
}

function getLabelYForLinePlot(width, height, value) {
	var text = document.createElementNS("http://www.w3.org/2000/svg", "text");
	text.setAttribute("x", (width - GRAPH_MARGIN / 2).toString());
	text.setAttribute("y", (height - GRAPH_MARGIN / 2).toString());
	text.setAttribute("text-anchor", "end");
	text.setAttribute("font-family", "sans-serif");
	text.setAttribute("font-size", "10");
	text.appendChild(document.createTextNode(value));
	return text;
}

function getMinMax(series) {
	var min = series[0];
	var max = series[0];
	for (var i = 1; i != series.length; i++) {
		var value = series[i];
		if (value < min) min = value;
		if (value > max) max = value;
	}
	return {min: min, max: max};
}

function getLineForPlot(seriesX, seriesY, yMin, width, height) {
	if (seriesX.series.length == 0)
		return null;

	var dataX = getMinMax(seriesX.series);
	var dataY = getMinMax(seriesY.data);
	dataY.max = dataY.max == yMin ? yMin + 1 : dataY.max * 1.1;
	var spanX = dataX.max - dataX.min;
	var spanY = dataY.max - yMin;
	var startX = (width - GRAPH_MARGIN * 2) * (seriesX.totalSpan - spanX) / seriesX.totalSpan;
	var ratioX = (width - GRAPH_MARGIN * 2) / spanX;
	var ratioY = (height - GRAPH_MARGIN * 2) / dataY.max;

	var d = "";
	for (var i = 0; i != seriesX.series.length; i++) {
		var x = GRAPH_MARGIN + startX + (seriesX.series[i] - dataX.min) * ratioX;
		var y = height - (GRAPH_MARGIN + seriesY.data[i] * ratioY);
		d += (i == 0 ? "M " : "L ");
		d += x.toString();
		d += " ";
		d += y.toString();
	}

	var path = document.createElementNS("http://www.w3.org/2000/svg", "path");
	path.setAttribute("fill", "none");
	path.setAttribute("stroke", seriesY.color);
	path.setAttribute("stroke-width", "3");
	path.setAttribute("d", d);
	return path;
}
