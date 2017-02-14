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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents the importation service
 */
public interface ImportationService extends SecuredService {
    /**
     * Service action to get the metadata of uploaded documents
     */
    SecuredAction ACTION_GET_DOCUMENT_METADATA = new SecuredAction(ImportationService.class.getCanonicalName() + ".GetMetadata", "Importation Service - Get Document Metadata");
    /**
     * Service action to get the content of uploaded documents
     */
    SecuredAction ACTION_GET_DOCUMENT_CONTENT = new SecuredAction(ImportationService.class.getCanonicalName() + ".GetContent", "Importation Service - Get Document Content");
    /**
     * Service action to upload documents
     */
    SecuredAction ACTION_UPLOAD_DOCUMENT = new SecuredAction(ImportationService.class.getCanonicalName() + ".Upload", "Importation Service - Upload Document");
    /**
     * Service action to drop uploaded documents
     */
    SecuredAction ACTION_DROP_DOCUMENT = new SecuredAction(ImportationService.class.getCanonicalName() + ".DropDocument", "Importation Service - Drop Document");
    /**
     * Service action to store a configuration
     */
    SecuredAction ACTION_STORE_CONFIG = new SecuredAction(ImportationService.class.getCanonicalName() + ".StoreConfig", "Importation Service - Store Configuration");
    /**
     * Service action to retrieve a configuration
     */
    SecuredAction ACTION_RETRIEVE_CONFIG = new SecuredAction(ImportationService.class.getCanonicalName() + ".RetrieveConfig", "Importation Service - Retrieve Stored Configuration");
    /**
     * Service action to delete a configuration
     */
    SecuredAction ACTION_DELETE_CONFIG = new SecuredAction(ImportationService.class.getCanonicalName() + ".DeleteConfig", "Importation Service - Delete Stored Configuration");

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_GET_DOCUMENT_METADATA,
            ACTION_GET_DOCUMENT_CONTENT,
            ACTION_UPLOAD_DOCUMENT,
            ACTION_DROP_DOCUMENT,
            ACTION_STORE_CONFIG,
            ACTION_RETRIEVE_CONFIG,
            ACTION_DELETE_CONFIG
    };

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
     * @return The document, or null if it cannot be found
     */
    XSPReply getDocument(String documentId);

    /**
     * Uploads a new document
     *
     * @param name             The document 's name
     * @param originalFileName The original client's file name
     * @param content          The document's content
     * @return The document
     */
    XSPReply upload(String name, String originalFileName, byte[] content);

    /**
     * Drops the specified document
     *
     * @param documentId The identifier of a document
     */
    XSPReply drop(String documentId);

    /**
     * Gets an input stream for a stored document
     *
     * @param documentId The identifier of a document
     * @return The input stream, or null if the document cannot be found
     */
    XSPReply getStreamFor(String documentId);

    /**
     * Gets the registered importers
     *
     * @return The registered importers
     */
    Collection<Importer> getImporters();

    /**
     * Gets the importer for the specified identifier
     *
     * @param importerId The identifier of an importer
     * @return The importer, or null it cannot be found
     */
    Importer getImporter(String importerId);

    /**
     * Gets the preview of a document
     *
     * @param documentId    The identifier of a document
     * @param importerId    The identifier of an importer
     * @param configuration The importer's configuration
     * @return The document preview, or null if it cannot be produced
     */
    XSPReply getPreview(String documentId, String importerId, ImporterConfiguration configuration);

    /**
     * Gets the preview of a document
     *
     * @param documentId      The identifier of a document
     * @param importerId      The identifier of an importer
     * @param configurationId The identifier of the stored configuration to use
     * @return The document preview, or null if it cannot be produced
     */
    XSPReply getPreview(String documentId, String importerId, String configurationId);

    /**
     * Begins the importation job of a document
     *
     * @param documentId    The identifier of a document
     * @param importerId    The identifier of an importer
     * @param configuration The importer's configuration
     * @param metadata      The metadata for the artifact to produce
     * @return The job, or null if the job cannot be created
     */
    XSPReply beginImport(String documentId, String importerId, ImporterConfiguration configuration, Artifact metadata);

    /**
     * Begins the importation job of a document
     *
     * @param documentId      The identifier of a document
     * @param importerId      The identifier of an importer
     * @param configurationId The identifier of the stored configuration to use
     * @param metadata        The metadata for the artifact to produce
     * @return The job, or null if the job cannot be created
     */
    XSPReply beginImport(String documentId, String importerId, String configurationId, Artifact metadata);

    /**
     * Stores an configuration that can be retrieved later
     *
     * @param configuration The configuration to store
     * @return The protocol reply
     */
    XSPReply storeConfiguration(ImporterConfiguration configuration);

    /**
     * Retrieves all the stored configurations
     *
     * @return The protocol reply
     */
    XSPReply retrieveConfigurations();

    /**
     * Retrieves a stored configuration
     *
     * @param configurationId The identifier of the configuration to retrieve
     * @return The protocol reply
     */
    XSPReply retrieveConfiguration(String configurationId);

    /**
     * Retrieves all the stored configurations related to the specified importer
     *
     * @param importerId The identifier of an importer
     * @return The protocol reply
     */
    XSPReply retrieveConfigurations(String importerId);

    /**
     * Deletes a stored configuration
     *
     * @param configurationId The identifier of the configuration to delete
     * @return The protocol reply
     */
    XSPReply deleteConfiguration(String configurationId);
}
