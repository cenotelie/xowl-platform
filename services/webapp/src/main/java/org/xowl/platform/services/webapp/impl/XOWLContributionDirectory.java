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

package org.xowl.platform.services.webapp.impl;

import org.xowl.infra.utils.http.URIUtils;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ui.WebUIContribution;
import org.xowl.platform.services.webapp.ContributionDirectory;

import java.net.URL;
import java.util.*;

/**
 * Implements the UI contribution directory
 *
 * @author Laurent Wouters
 */
public class XOWLContributionDirectory implements ContributionDirectory {
    /**
     * Represents a tree of contributions
     */
    private static class Folder {
        /**
         * The sub trees
         */
        public final Map<String, Folder> subs;
        /**
         * The contributions at this stage
         */
        public final List<WebUIContribution> contributions;

        /**
         * Initializes this folder
         */
        public Folder() {
            this.subs = new HashMap<>();
            this.contributions = new ArrayList<>();
        }
    }

    /**
     * The root of the tree of contributions
     */
    private final Folder root;
    /**
     * The registered contributions
     */
    private Collection<WebUIContribution> contributions;

    /**
     * Initializes this directory
     */
    public XOWLContributionDirectory() {
        this.root = new Folder();
    }

    @Override
    public String getIdentifier() {
        return XOWLContributionDirectory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Web UI Contribution Directory";
    }

    @Override
    public URL resolveResource(String uri) {
        List<String> segments = URIUtils.getSegments(uri);
        if (contributions == null) {
            synchronized (this) {
                if (contributions == null) {
                    contributions = Register.getComponents(WebUIContribution.class);
                    for (WebUIContribution contribution : contributions)
                        register(contribution);
                }
            }
        }
        return resolveResource(root, segments, 0, uri);
    }

    /**
     * Resolves a resource
     *
     * @param folder   The current folder
     * @param segments The segments of the requested URI
     * @param index    The index of the next segment
     * @param uri      The requested URI
     * @return The resolved URL, or null of there is none
     */
    private URL resolveResource(Folder folder, List<String> segments, int index, String uri) {
        if (index < segments.size()) {
            String segment = segments.get(index);
            Folder child = folder.subs.get(segment);
            if (child != null) {
                URL result = resolveResource(child, segments, index + 1, uri);
                if (result != null)
                    return result;
            }
        }
        WebUIContribution top = null;
        for (int i = 0; i != folder.contributions.size(); i++) {
            WebUIContribution contribution = folder.contributions.get(i);
            if (top == null || contribution.getPriority() > top.getPriority())
                top = contribution;
        }
        return top == null ? null : top.getResource(uri);
    }

    /**
     * Registers a web contribution
     *
     * @param contribution The web contribution to register
     */
    private void register(WebUIContribution contribution) {
        List<String> segments = URIUtils.getSegments(contribution.getPrefix());
        Folder current = root;
        for (int i = 0; i != segments.size(); i++) {
            String name = segments.get(i);
            if (name.isEmpty() && i == segments.size() - 1)
                // last segment is empty, break here
                break;
            Folder child = current.subs.get(name);
            if (child == null) {
                child = new Folder();
                current.subs.put(name, child);
            }
            current = child;
        }
        current.contributions.add(contribution);
    }
}
