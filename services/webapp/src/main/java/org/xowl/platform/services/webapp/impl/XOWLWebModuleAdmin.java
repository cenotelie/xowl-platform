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

import org.xowl.platform.services.webapp.WebModuleBase;
import org.xowl.platform.services.webapp.WebModuleItem;

/**
 * Represents the administration module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLWebModuleAdmin extends WebModuleBase {
    /**
     * Initializes this service
     */
    public XOWLWebModuleAdmin() {
        super(XOWLWebModuleAdmin.class.getCanonicalName(), "Administration", "admin", "/web/assets/xowl.svg");
        this.items.add(new WebModuleItem("Platform Security", "security", "/web/assets/security.svg"));
        this.items.add(new WebModuleItem("Platform Connectors Management", "connectors", "/web/assets/connector.svg"));
        this.items.add(new WebModuleItem("Platform Job Management", "jobs", "/web/assets/jobs.svg"));
        this.items.add(new WebModuleItem("Platform Statistics", "statistics", "/web/assets/statistics.svg"));
        this.items.add(new WebModuleItem("Platform Log", "log", "/web/assets/log.svg"));
        this.items.add(new WebModuleItem("Platform Management", "platform", "/web/assets/platform.svg"));
    }
}
