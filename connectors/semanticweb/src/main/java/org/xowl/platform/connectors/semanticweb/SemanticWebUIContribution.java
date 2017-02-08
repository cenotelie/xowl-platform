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

package org.xowl.platform.connectors.semanticweb;

import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.ui.WebUIContribution;

import java.net.URL;

/**
 * Represents the UI contribution for this importer and connector
 *
 * @author Laurent Wouters
 */
public class SemanticWebUIContribution implements WebUIContribution {
    /**
     * The root resource for the web app files
     */
    private static final String RESOURCES = "/org/xowl/platform/connectors/semanticweb";
    /**
     * The URI prefix for this contribution
     */
    private final String prefix;

    /**
     * Initializes this contribution
     */
    public SemanticWebUIContribution() {
        this.prefix = PlatformHttp.getUriPrefixWeb() + "/contributions/connectors/semanticweb";
    }

    @Override
    public String getIdentifier() {
        return SemanticWebUIContribution.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Semantic Web Importer Contribution";
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public URL getResource(String resource) {
        return SemanticWebUIContribution.class.getResource(RESOURCES + resource.substring(prefix.length()));

    }
}