/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

import org.xowl.store.Serializable;
import org.xowl.store.rdf.Quad;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents a domain artifact that is is being provided by or to a domain connector
 *
 * @author Laurent Wouters
 */
public interface Artifact extends Identifiable, Serializable {
    /**
     * Gets the metadata attached to this artifact
     *
     * @return The metadata
     */
    Collection<Quad> getMetadata();

    /**
     * Gets the content MIME type
     *
     * @return The content type
     */
    String getMIMEType();

    /**
     * Gets a stream to the raw data
     *
     * @return A stream to the data, or null if there is none
     */
    InputStream getStream();
}
