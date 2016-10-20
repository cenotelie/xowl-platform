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

package org.xowl.platform.services.connection.events;

import org.xowl.platform.kernel.RichString;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.events.EventBase;
import org.xowl.platform.services.connection.ConnectorService;

/**
 * Represents an event when an artifact was pulled from a connector
 *
 * @author Laurent Wouters
 */
public class ArtifactPulledFromConnectorEvent extends EventBase {
    /**
     * The type of event
     */
    public static final String TYPE = ArtifactPulledFromConnectorEvent.class.getCanonicalName();

    /**
     * The pulled artifact
     */
    private final Artifact artifact;

    /**
     * Initializes this event
     *
     * @param connector The originator connector
     * @param artifact  The pulled artifact
     */
    public ArtifactPulledFromConnectorEvent(ConnectorService connector, Artifact artifact) {
        super(new RichString("Pulled artifact ", artifact, " from connector ", connector), TYPE, connector);
        this.artifact = artifact;
    }

    /**
     * Gets the pulled artifact
     *
     * @return The pulled artifact
     */
    public Artifact getArtifact() {
        return artifact;
    }
}
