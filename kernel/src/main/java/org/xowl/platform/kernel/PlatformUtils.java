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

import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.rdf.SubjectNode;
import org.xowl.infra.utils.logging.Logging;

import java.io.File;
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
     * The name for the platform product
     */
    public static final String NAME = "xOWL Collaboration Platform";
    /**
     * The login for the default administrator account
     */
    public static final String DEFAULT_ADMIN_LOGIN = "admin";
    /**
     * The password for the default administrator account
     */
    public static final String DEFAULT_ADMIN_PASSWORD = "admin";
    /**
     * The identifier of the default administrator group
     */
    public static final String DEFAULT_ADMIN_GROUP = "admins";

    /**
     * Environment variable for the distribution's root
     */
    private static final String DISTRIBUTION_ROOT_ENV = "xowl.root";
    /**
     * The file for the distribution's root
     */
    private static File DISTRIBUTION_ROOT = null;

    /**
     * Gets the file for the distribution's root
     *
     * @return The file for the distribution's root
     */
    public static synchronized File getDistributionRoot() {
        if (DISTRIBUTION_ROOT == null)
            DISTRIBUTION_ROOT = new File(System.getProperty(DISTRIBUTION_ROOT_ENV));
        return DISTRIBUTION_ROOT;
    }

    /**
     * Resolves the file (or folder) specified by the relative path from the distribution's root
     *
     * @param path A relative path from the distribution's root
     * @return The corresponding file
     */
    public static File resolve(String path) {
        File root = getDistributionRoot();
        File target = root;
        if (path == null || path.isEmpty())
            return target;
        String[] parts = path.split("/");
        for (int i = 0; i != parts.length; i++) {
            if (i == 0 && parts[i].isEmpty()) {
                // referring to the file system's root is forbidden, translate to the distribution's root
                // as this is the first segment, do nothing
                Logging.getDefault().warning("File system's absolute path is translated to path relative to the distribution's root: " + path);
            } else if (parts[i].isEmpty()) {
                // invalid path segment
                Logging.getDefault().error("Invalid path specification: " + path);
            } else if (parts[i].equals(".")) {
                // current directory => do nothing
            } else if (parts[i].equals("..")) {
                // parent directory
                if (target == root) {
                    // escaping the distribution root
                    Logging.getDefault().warning("Path specification attempts to escapes the distribution's root: " + path);
                    continue;
                }
                target = target.getParentFile();
            } else if (!isValidPathSegment(parts[i])) {
                Logging.getDefault().error("Invalid path specification: " + path);
            } else {
                target = new File(target, parts[i]);
            }
        }
        return target;
    }

    /**
     * Gets whether the specified path segment is valid
     *
     * @param segment A path segment
     * @return Whether the path segment is valid
     */
    private static boolean isValidPathSegment(String segment) {
        for (int i = 0; i != segment.length(); i++)
            if (!isValidPathChar(segment.charAt(i)))
                return false;
        return true;
    }

    /**
     * Gets whether the specified character is a valid character for a path segment
     *
     * @param c A character
     * @return Whether the character is valid
     */
    private static boolean isValidPathChar(char c) {
        return ((c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9')
                || c == '_'
                || c == '.');
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
