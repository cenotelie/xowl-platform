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

package org.xowl.platform.connectors.csv;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.HttpAPIService;

/**
 * An importation service of CSV documents
 *
 * @author Laurent Wouters
 */
public interface CSVImportService extends HttpAPIService {
    /**
     * Gets the uploaded documents not yet completely imported
     *
     * @return The uploaded documents pending importation
     */
    XSPReply getDocuments();

    /**
     * Gets an uploaded document
     *
     * @param documentId The identifier of a document
     * @return The result
     */
    XSPReply getDocument(String documentId);

    /**
     * Gets the first lines of a document
     *
     * @param documentId The identifier of a document
     * @param separator  The character that separates values in rows
     * @param textMarker The character that marks the beginning and end of raw text
     * @param rowCount   The maximum number of rows to get
     * @return The result
     */
    XSPReply getFirstLines(String documentId, char separator, char textMarker, int rowCount);

    /**
     * Uploads a new document
     *
     * @param name    The document 's name
     * @param content The document's content
     * @return The document
     */
    XSPReply upload(String name, byte[] content);

    /**
     * Drops the specified document
     *
     * @param documentId The identifier of a document
     * @return The result
     */
    XSPReply drop(String documentId);

    /**
     * Finalizes a document import as an artifact
     *
     * @param documentId   The identifier of the document to import
     * @param mapping      The mapping to use for the import
     * @param separator    The character that separates values in rows
     * @param textMarker   The character that marks the beginning and end of raw text
     * @param skipFirstRow Whether to skip the first row
     * @param base         The artifact's base family URI
     * @param supersede    URI of the superseded artifacts, if any
     * @param version      The version of the artifact
     * @param archetype    The archetype for the artifact
     * @return The result
     */
    XSPReply importDocument(String documentId, CSVImportMapping mapping, char separator, char textMarker, boolean skipFirstRow, String base, String[] supersede, String version, String archetype);
}
