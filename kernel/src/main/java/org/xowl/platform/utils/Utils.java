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

package org.xowl.platform.utils;

import org.xowl.hime.redist.ASTNode;
import org.xowl.hime.redist.ParseError;
import org.xowl.hime.redist.ParseResult;
import org.xowl.store.loaders.JSONLDLoader;
import org.xowl.utils.logging.BufferedLogger;
import org.xowl.utils.logging.Logger;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;

/**
 * Utility APIs for the server
 *
 * @author Laurent Wouters
 */
public class Utils {
    /**
     * Gets the first service for the specified service type
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S> S getService(Class<S> serviceType) {
        return null;
    }

    /**
     * Gets the service for the specified service type with a specific identifier
     *
     * @param serviceType A type of service as the Java class that must be implemented
     * @param id          The identifier of the service to retrieve
     * @param <S>         The type of service
     * @return The service, or null if there is none
     */
    public static <S> S getService(Class<S> serviceType, String id) {
        return null;
    }

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
        char[] chars = new char[bytes.length * 2];
        int j = 0;
        for (int i = 0; i != bytes.length; i++) {
            chars[j++] = HEX[(bytes[i] & 0xF0) >>> 4];
            chars[j++] = HEX[bytes[i] & 0x0F];
        }
        return new String(chars);
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
}
