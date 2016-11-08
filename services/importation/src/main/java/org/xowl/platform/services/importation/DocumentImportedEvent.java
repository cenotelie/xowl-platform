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

package org.xowl.platform.services.importation;

import org.xowl.platform.kernel.Identifiable;
import org.xowl.platform.kernel.RichString;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a document was imported as an artifact
 *
 * @author Laurent Wouters
 */
public class DocumentImportedEvent extends EventBase {
    /**
     * The imported document
     */
    private final Document document;
    /**
     * The resulting artifact
     */
    private final Artifact artifact;

    /**
     * Gets the imported document
     *
     * @return The imported document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * Gets the resulting artifact
     *
     * @return The resulting artifact
     */
    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * Initializes this event
     *
     * @param document   The imported document
     * @param artifact   The resulting artifact
     * @param originator The originator for this event
     */
    public DocumentImportedEvent(Document document, Artifact artifact, Identifiable originator) {
        super(new RichString("Imported document ", document, " as artifact ", artifact), DocumentImportedEvent.class.getCanonicalName(), originator);
        this.document = document;
        this.artifact = artifact;
    }
}
