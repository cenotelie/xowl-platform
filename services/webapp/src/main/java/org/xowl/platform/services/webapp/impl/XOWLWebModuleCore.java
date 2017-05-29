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
 * Represents the core module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLWebModuleCore extends WebModuleBase {
    /**
     * Initializes this service
     */
    public XOWLWebModuleCore() {
        super(XOWLWebModuleCore.class.getCanonicalName(), "Core Services", "core", "/assets/module-core.svg", 0);
        this.items.add(new WebModuleItem("Manage Artifacts", "artifacts", "/assets/artifact.svg"));
        this.items.add(new WebModuleItem("Share Meaning", "denotation", "/assets/denotation.svg"));
        this.items.add(new WebModuleItem("Import Data", "importation", "/assets/import.svg"));
        this.items.add(new WebModuleItem("Manage Consistency", "consistency", "/assets/consistency.svg"));
        this.items.add(new WebModuleItem("Simulate / Execute", "execution", "/assets/execution.svg"));
        this.items.add(new WebModuleItem("Query and Explore", "discovery", "/assets/exploration.svg"));
        this.items.add(new WebModuleItem("Analysis: Impact", "impact", "/assets/impact.svg"));
        this.items.add(new WebModuleItem("Analysis: Evaluation", "evaluation", "/assets/evaluation.svg"));
    }
}
