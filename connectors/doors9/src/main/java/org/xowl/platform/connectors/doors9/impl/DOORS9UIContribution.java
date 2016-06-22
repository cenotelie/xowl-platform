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

package org.xowl.platform.connectors.doors9.impl;

import org.xowl.platform.kernel.UIContribution;

import java.net.URL;

/**
 * Implementation of the UI contributions for the DOORS 9 importer
 *
 * @author Elie Soubiran
 */
public class DOORS9UIContribution implements UIContribution {
    /**
     * The root resource for the web app files
     */
    private static final String RESOURCES = "/org/xowl/platform/connectors/doors9";
    /**
     * The URI prefix for this contribution
     */
    public static final String PREFIX = URI_WEB + "/contributions/connectors/doors9";

    @Override
    public String getIdentifier() {
        return DOORS9UIContribution.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - DOORS 9 Importer Contribution";
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public URL getResource(String resource) {
        return DOORS9UIContribution.class.getResource(RESOURCES + resource.substring(PREFIX.length()));
    }
}
