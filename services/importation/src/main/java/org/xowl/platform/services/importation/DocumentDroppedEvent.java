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

import fr.cenotelie.commons.utils.RichString;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.events.EventBase;

/**
 * Event when a document was dropped (deleted)
 *
 * @author Laurent Wouters
 */
public class DocumentDroppedEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = DocumentDroppedEvent.class.getCanonicalName();

    /**
     * The dropped document
     */
    private final Document document;

    /**
     * Gets the dropped document
     *
     * @return The dropped document
     */
    public Document getDroppedDocument() {
        return document;
    }

    /**
     * Initializes this event
     *
     * @param document The dropped document
     * @param emitter  The service that emitted this event
     */
    public DocumentDroppedEvent(Document document, Service emitter) {
        super(TYPE, emitter, new RichString("Dropped document ", document));
        this.document = document;
    }
}
