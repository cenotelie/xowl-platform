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

import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.jobs.Job;

import java.io.InputStream;
import java.util.Collection;

/**
 * Represents the importation service
 */
public interface ImportationService extends HttpAPIService {
    /**
     * Gets the uploaded documents not yet completely imported
     *
     * @return The uploaded documents pending importation
     */
    Collection<Document> getDocuments();

    /**
     * Gets an uploaded document
     *
     * @param documentId The identifier of a document
     * @return The document, or null if it cannot be found
     */
    Document getDocument(String documentId);

    /**
     * Uploads a new document
     *
     * @param name    The document 's name
     * @param content The document's content
     * @return The document
     */
    Document upload(String name, byte[] content);

    /**
     * Drops the specified document
     *
     * @param document The document to drop
     */
    void drop(Document document);

    /**
     * Gets an input stream for a stored document
     *
     * @param document The document
     * @return The input stream, or null if the document cannot be found
     */
    InputStream getStreamFor(Document document);

    /**
     * Gets the preview of a document
     *
     * @param document      The document to preview
     * @param importer      The importer to use
     * @param configuration The importer's configuration
     * @return The document preview, or null if it cannot be produced
     */
    DocumentPreview getPreview(Document document, Importer importer, ImporterConfiguration configuration);

    /**
     * Gets the registered importers
     *
     * @return The registered importers
     */
    Collection<Importer> getImporters();

    /**
     * Begins the importation job of a document
     *
     * @param document      The document to import
     * @param importer      The importer to use
     * @param configuration The importer's configuration
     * @return The job, or null if the job cannot be created
     */
    Job beginImport(Document document, Importer importer, ImporterConfiguration configuration);
}
