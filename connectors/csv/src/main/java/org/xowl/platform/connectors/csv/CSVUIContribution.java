/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.connectors.csv;

import org.xowl.platform.kernel.PlatformHttp;
import org.xowl.platform.kernel.ui.WebUIContribution;

import java.net.URL;

/**
 * Implementation of the UI contributions for the CSV importer
 *
 * @author Laurent Wouters
 */
public class CSVUIContribution implements WebUIContribution {
    /**
     * The root resource for the web app files
     */
    private static final String RESOURCES = "/org/xowl/platform/connectors/csv";

    /**
     * The URI prefix for this contribution
     */
    private final String prefix;

    /**
     * Initializes this contribution
     */
    public CSVUIContribution() {
        this.prefix = PlatformHttp.getUriPrefixWeb() + "/contributions/connectors/csv";
    }

    @Override
    public String getIdentifier() {
        return CSVUIContribution.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Collaboration Platform - CSV Importer Contribution";
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
        return CSVUIContribution.class.getResource(RESOURCES + resource.substring(prefix.length()));
    }
}
