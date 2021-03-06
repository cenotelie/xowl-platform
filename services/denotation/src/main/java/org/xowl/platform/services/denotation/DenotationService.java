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

package org.xowl.platform.services.denotation;

import fr.cenotelie.commons.utils.api.Reply;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents the denotation service for the capture of meaning
 *
 * @author Laurent Wouters
 */
public interface DenotationService extends SecuredService {
    /**
     * Gets the uploaded documents not yet completely imported
     *
     * @return The uploaded documents pending importation
     */
    Reply getDocuments();

    /**
     * Gets an uploaded document
     *
     * @param documentId The identifier of a document
     * @return The document, or null if it cannot be found
     */
    Reply getDocument(String documentId);

    /**
     * Gets the representation of the specified document
     *
     * @param documentId The identifier of a document
     * @return The representation
     */
    Reply getDocumentRepresentation(String documentId);

    /**
     * Gets the linguistic phrase within the specified document
     *
     * @param documentId The identifier of a document
     * @return The linguistic phrase within the specified document
     */
    Reply getDocumentPhrase(String documentId);

    /**
     * Uploads a new document
     *
     * @param name             The document 's name
     * @param originalFileName The original client's file name
     * @param content          The document's content
     * @return The document
     */
    Reply upload(String name, String originalFileName, byte[] content);

    /**
     * Drops the specified document
     *
     * @param documentId The identifier of a document
     */
    Reply drop(String documentId);

    /**
     * Gets the known document parsers
     *
     * @return The parsers
     */
    Collection<DocumentParser> getParsers();

    /***
     * Gets a specific document parser
     * @param identifier The identifier of a parser
     * @return The corresponding parser
     */
    DocumentParser getParser(String identifier);
}
