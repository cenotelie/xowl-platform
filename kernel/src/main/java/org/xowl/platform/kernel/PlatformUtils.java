/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.xowl.hime.redist.ASTNode;
import org.xowl.hime.redist.ParseError;
import org.xowl.hime.redist.ParseResult;
import org.xowl.infra.store.loaders.JSONLDLoader;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.rdf.SubjectNode;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
     * The default charset
     */
    public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

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
     * Hexadecimal characters
     */
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Encodes a string
     *
     * @param input The string to encode
     * @return The encoded text
     */
    public static String encode(String input) {
        byte[] bytes = input.getBytes(Charset.forName("UTF-8"));
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            bytes = md.digest(bytes);
            char[] chars = new char[bytes.length * 2];
            int j = 0;
            for (int i = 0; i != bytes.length; i++) {
                chars[j++] = HEX[(bytes[i] & 0xF0) >>> 4];
                chars[j++] = HEX[bytes[i] & 0x0F];
            }
            return new String(chars);
        } catch(NoSuchAlgorithmException exception) {
            Logger.DEFAULT.error(exception);
            return null;
        }
    }

    /**
     * Gets the content of the log
     *
     * @param logger The logger
     * @return The content of the log
     */
    public static String getLog(BufferedLogger logger) {
        StringBuilder builder = new StringBuilder();
        for (Object error : logger.getErrorMessages()) {
            builder.append(error.toString());
            builder.append("\n");
        }
        return builder.toString();
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
