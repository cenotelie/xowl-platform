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
 * Event when a document was uploaded
 *
 * @author Laurent Wouters
 */
public class DocumentUploadedEvent extends EventBase {
    /**
     * The type for this event
     */
    public static final String TYPE = DocumentUploadedEvent.class.getCanonicalName();

    /**
     * The uploaded document
     */
    private final Document document;

    /**
     * Gets the uploaded document
     *
     * @return The uploaded document
     */
    public Document getUploadedDocument() {
        return document;
    }

    /**
     * Initializes this event
     *
     * @param document The uploaded document
     * @param emitter  The service that emitted this event
     */
    public DocumentUploadedEvent(Document document, Service emitter) {
        super(TYPE, emitter, new RichString("Uploaded document ", document));
        this.document = document;
    }
}
