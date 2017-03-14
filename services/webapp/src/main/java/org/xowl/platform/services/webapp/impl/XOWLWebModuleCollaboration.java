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

package org.xowl.platform.services.webapp.impl;

import org.xowl.platform.services.webapp.WebModuleBase;
import org.xowl.platform.services.webapp.WebModuleItem;

/**
 * Represents the collaboration module in the web application
 *
 * @author Laurent Wouters
 */
public class XOWLWebModuleCollaboration extends WebModuleBase {
    /**
     * Initializes this service
     */
    public XOWLWebModuleCollaboration() {
        super(XOWLWebModuleAdmin.class.getCanonicalName(), "Collaboration", "collab", "/assets/module-collaboration.svg", 50);
        this.items.add(new WebModuleItem("Community", "community", "/assets/group.svg"));
        this.items.add(new WebModuleItem("Conversations", "chat", "/assets/conversation.svg"));
        this.items.add(new WebModuleItem("Local Collaboration", "local", "/assets/collaboration.svg"));
        this.items.add(new WebModuleItem("Collaborations Network", "network", "/assets/network.svg"));
    }
}