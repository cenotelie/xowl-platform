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
import org.xowl.platform.kernel.PlatformUtils;
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
     * Initializes this contribution
     */
    public CSVUIContribution() {
    }

    @Override
    public String getIdentifier() {
        return CSVUIContribution.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - CSV Importer Contribution";
    }

    @Override
    public String getPrefix() {
        return PlatformHttp.getUriPrefixWeb() + "/contributions/importers/csv";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public URL getResource(String resource) {
        return CSVUIContribution.class.getResource(RESOURCES + resource.substring(getPrefix().length()));
    }
}
