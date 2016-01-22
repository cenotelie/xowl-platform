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

package org.xowl.platform.services.webapp;

import java.net.URL;

/**
 * The default branding service for the platform
 *
 * @author Laurent Wouters
 */
class XOWLBrandingService implements BrandingService {
    @Override
    public String getIdentifier() {
        return XOWLBrandingService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Webapp Branding Service";
    }

    @Override
    public URL getResource(String name) {
        return XOWLBrandingService.class.getResource(Activator.WEBAPP_RESOURCE_ROOT + BRANDING + name);
    }

    @Override
    public String getTitleHeader() {
        return "xOWL";
    }
}
