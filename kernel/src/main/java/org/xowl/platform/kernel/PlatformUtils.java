/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.xowl.hime.redist.ASTNode;
import org.xowl.hime.redist.ParseError;
import org.xowl.hime.redist.ParseResult;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.rdf.SubjectNode;
import org.xowl.infra.utils.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility APIs for the server
 *
 * @author Laurent Wouters
 */
public class PlatformUtils {
    /**
     * Parses the JSON content
     *
     * @param logger  The logger to use
     * @param content The content to parse
     * @return The AST root node, or null of the parsing failed
     */
    public static ASTNode parseJSON(Logger logger, String content) {
        JSONLDLoader loader = new JSONLDLoader(null) {
            @Override
            protected Reader getReaderFor(Logger logger, String iri) {
                return null;
            }
        };
        ParseResult result = loader.parse(logger, new StringReader(content));
        if (result == null)
            return null;
        if (!result.getErrors().isEmpty()) {
            for (ParseError error : result.getErrors())
                logger.error(error);
            return null;
        }
        return result.getRoot();
    }

    /**
     * Maps a collection of quads by their subject
     *
     * @param quads A collection of quads
     * @return The mapped quads
     */
    public static Map<SubjectNode, Collection<Quad>> mapBySubject(Collection<Quad> quads) {
        Map<SubjectNode, Collection<Quad>> result = new HashMap<>();
        for (Quad quad : quads) {
            Collection<Quad> target = result.get(quad.getSubject());
            if (target == null) {
                target = new ArrayList<>();
                result.put(quad.getSubject(), target);
            }
            target.add(quad);
        }
        return result;
    }
}
