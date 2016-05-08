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
 * Represents the core module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLCoreModule extends WebModuleServiceBase {
    /**
     * Initializes this service
     */
    public XOWLCoreModule() {
        super(XOWLCoreModule.class.getCanonicalName(), "Core Module", "core", "/web/assets/xowl.svg");
        this.parts.add(new WebModulePart("Artifacts Management", "artifacts", "/web/assets/artifact.svg"));
        this.parts.add(new WebModulePart("Consistency Management", "consistency", "/web/assets/consistency.svg"));
        this.parts.add(new WebModulePart("Traceability Exploration", "discovery", "/web/assets/exploration.svg"));
        this.parts.add(new WebModulePart("Impact Analysis", "impact", "/web/assets/impact.svg"));
        this.parts.add(new WebModulePart("Evaluation Analysis", "evaluation", "/web/assets/evaluation.svg"));
        this.parts.add(new WebModulePart("Platform Connectors Management", "connectors", "/web/assets/connector.svg"));
        this.parts.add(new WebModulePart("Platform Job Management", "jobs", "/web/assets/jobs.svg"));
        this.parts.add(new WebModulePart("Platform Properties", "platform", "/web/assets/information.svg"));
    }

    @Override
    public URL getResource(String resource) {
        return XOWLCoreModule.class.getResource(Activator.WEBAPP_RESOURCE_ROOT + WebModuleService.MODULES + "core/" + resource);
    }
}
