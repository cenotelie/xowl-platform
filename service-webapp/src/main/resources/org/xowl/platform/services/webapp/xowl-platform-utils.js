// Copyright (c) 2015 Laurent Wouters
// Provided under LGPLv3

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

/**
 * Parses a string containing RDF quads and builds a dictionnary of entities with their properties.
 * The result is a map of entity identifier (iri, or blank id) to an object describing their property:
 * key -> {id: "key", properties:[], graph: "where this entity is defined"}
 * Each property is an object:
 * {id: "iri of the property", value: "the property value", graph: "IRI of the graph defining the property-value couple"}
 * 
 * @method parseNQuads
 * @param {string} content The string to parse
 * @return {Object} The parsed data
 */
function parseNQuads(content) {
    var result = {};
    var tokens = lexNQuads(content);
    for (var i = 0; i != tokens.length; i++) {
        var subject = tokens[i];
        var property = tokens[i + 1];
        var object = tokens[i + 2];
        var graph = tokens[i + 3];
        var key = subject.type === "iri" ? subject.value : subject.id;
        var item = key in result ? result[key] : { id: key, isIRI: subject.type === "iri", properties: [], graph: graph.value };
        item.properties.push({ id: property.value, value: object, graph: graph.value });
        result[key] = item;
        i += 3;
    }
    return result;
}

/**
 * Lexes a string of RDF NQuads
 * Builds an array of token from the specified content string.
 * IRI tokens: {type: "iri", value: "the iri value"}
 * Blank tokens: {type: "blank", id: "the blank id"}
 * Literal tokens {type: "literal", lexical: "the lexical value", lang: "the language tag, if any", datatype: "the datatype if any"}
 * 
 * @method lexNQuads
 * @param {string} content The string to parse
 * @return {Object[]} An array of token
 */
function lexNQuads(content) {
    var result = [];
    for (var i = 0; i < content.length; i++) {
        var c = content.charAt(i);
        if (c === '<') {
            // beginning of a an URI
            var j = i + 1;
            while (j < content.length && content.charAt(j) !== '>') {
                j++;
            }
            var tokenIRI = { type: "iri", value: content.substring(i + 1, j) };
            result.push(tokenIRI);
            i = j;
        } else if (c === '"') {
            // beginning of a string
            var j = i + 1;
            while (j < content.length && (content.charAt(j) !== '"' || content.charAt(j - 1) === '\\')) {
                j++;
            }
            var lexicalValue = content.substring(i + 1, j);
            lexicalValue = lexicalValue.replace(new RegExp("\\\"", 'g'), "\"");
            var langTag = null;
            var datatype = null;
            i = j;
            j++;
            if (content.charAt(j) === '@') {
                j++;
                while (j < content.length && isLangTagChar(content.charAt(j))) {
                    j++;
                }
                langTag = content.substring(i + 2, j);
                i = j - 1;
            } else if (content.charAt(j) === '^') {
                j += 3;
                while (j < content.length && content.charAt(j) !== '>') {
                    j++;
                }
                datatype = content.substring(i + 4, j);
                i = j;
            }
            var tokenLiteral = { type: "literal", lexical: lexicalValue, lang: langTag, datatype: datatype };
            result.push(tokenLiteral);
        } else if (c === '.') {
            // marker for the end of a quad
            // drop this
        } else if (c === '_') {
            // start of a blank node
            var j = i + 2;
            while (j < content.length && content.charAt(j).trim().length !== 0) {
                j++;
            }
            var tokenBlank = { type: "blank", id: content.substring(i + 2, j) };
            result.push(tokenBlank);
            i = j - 1;
        } else {
            // something else, drop it
        }
    }
    return result;
}

/**
 * Determines whether the specified character can be used in a language tag
 * 
 * @method isLangTagChar
 * @param {string} c The character to test
 * @return {boolean} true if the character can be used in a language tag, false otherwise
 */
function isLangTagChar(c) {
    if (c === '-')
        return true;
    var code = c.charCodeAt(0);
    return (((code >= 65) && (code <= 90)) || ((code >= 97) && (code <= 122)));
}
