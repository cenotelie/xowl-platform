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

import org.xowl.platform.services.webapp.Activator;
import org.xowl.platform.services.webapp.WebModulePart;
import org.xowl.platform.services.webapp.WebModuleService;
import org.xowl.platform.services.webapp.WebModuleServiceBase;

import java.net.URL;

/**
 * Represents the data import module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLImportModule extends WebModuleServiceBase {
    /**
     * Initializes this service
     */
    public XOWLImportModule() {
        super(XOWLCoreModule.class.getCanonicalName(), "Import", "import", "/web/assets/import.svg");
        this.parts.add(new WebModulePart("Import from CSV", "csv", "/web/assets/csv.svg"));
    }

    @Override
    public URL getResource(String resource) {
        return XOWLCoreModule.class.getResource(Activator.WEBAPP_RESOURCE_ROOT + WebModuleService.MODULES + "import/" + resource);
    }
}
