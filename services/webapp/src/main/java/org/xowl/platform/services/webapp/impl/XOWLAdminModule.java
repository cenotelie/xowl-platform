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
import org.xowl.platform.services.webapp.WebModuleItem;
import org.xowl.platform.services.webapp.WebModule;
import org.xowl.platform.services.webapp.WebModuleBase;

import java.net.URL;

/**
 * Represents the administration module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLAdminModule extends WebModuleBase {
    /**
     * Initializes this service
     */
    public XOWLAdminModule() {
        super(XOWLCoreModule.class.getCanonicalName(), "Administration", "admin", "/web/assets/xowl.svg");
        this.items.add(new WebModuleItem("Platform Connectors Management", "connectors", "/web/assets/connector.svg"));
        this.items.add(new WebModuleItem("Platform Job Management", "jobs", "/web/assets/jobs.svg"));
        this.items.add(new WebModuleItem("Platform Properties", "platform", "/web/assets/information.svg"));
    }

    @Override
    public URL getResource(String resource) {
        return XOWLCoreModule.class.getResource(Activator.WEBAPP_RESOURCE_ROOT + WebModule.MODULES + "admin/" + resource);
    }
}
