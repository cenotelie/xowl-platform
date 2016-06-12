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

import org.xowl.platform.kernel.UIContribution;

import java.net.URL;

/**
 * Represents the main contribution to the web user interface
 *
 * @author Laurent Wouters
 */
public class XOWLMainContribution implements UIContribution {
    /**
     * The root resource for the web app files
     */
    public static final String RESOURCES = "/org/xowl/platform/services/webapp";

    @Override
    public String getIdentifier() {
        return XOWLMainContribution.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Main UI Contribution";
    }

    @Override
    public String getPrefix() {
        return URI_WEB;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public URL getResource(String resource) {
        return XOWLMainContribution.class.getResource(RESOURCES + resource.substring(getPrefix().length()));
    }
}
