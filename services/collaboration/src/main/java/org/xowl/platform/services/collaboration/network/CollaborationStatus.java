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

package org.xowl.platform.services.collaboration.network;

/**
 * The status of collaborations
 *
 * @author Laurent Wouters
 */
public enum CollaborationStatus {
    /**
     * The collaboration has not started yet, the platform is being provisioned
     */
    Provisioning,
    /**
     * The collaboration is on-going, the platform is running
     */
    Running,
    /**
     * The collaboration is temporarily stopped, the platform is not running
     */
    Stopped,
    /**
     * The collaboration has stopped, the platform is not running and archived
     */
    Archived
}
