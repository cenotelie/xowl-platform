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

import fr.cenotelie.commons.utils.api.Reply;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicyIsResourceOwner;
import org.xowl.platform.kernel.security.SecuredService;

import java.util.Collection;

/**
 * Represents the importation service
 *
 * @author Laurent Wouters
 */
public interface ImportationService extends SecuredService {
    /**
     * Service action to upload documents
     */
    SecuredAction ACTION_UPLOAD_DOCUMENT = new SecuredAction(ImportationService.class.getCanonicalName() + ".Upload", "Importation Service - Upload Document");
    /**
     * Service action to drop uploaded documents
     */
    SecuredAction ACTION_DROP_DOCUMENT = new SecuredAction(ImportationService.class.getCanonicalName() + ".DropDocument", "Importation Service - Drop Document", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);
    /**
     * Service action to store a configuration
     */
    SecuredAction ACTION_STORE_CONFIG = new SecuredAction(ImportationService.class.getCanonicalName() + ".StoreConfig", "Importation Service - Store Configuration");
    /**
     * Service action to delete a configuration
     */
    SecuredAction ACTION_DELETE_CONFIG = new SecuredAction(ImportationService.class.getCanonicalName() + ".DeleteConfig", "Importation Service - Delete Stored Configuration", SecuredActionPolicyIsResourceOwner.DESCRIPTOR);

    /**
     * The actions for this service
     */
    SecuredAction[] ACTIONS = new SecuredAction[]{
            ACTION_UPLOAD_DOCUMENT,
            ACTION_DROP_DOCUMENT,
            ACTION_STORE_CONFIG,
            ACTION_DELETE_CONFIG
    };

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
     * Gets an input stream for a stored document
     *
     * @param documentId The identifier of a document
     * @return The input stream, or null if the document cannot be found
     */
    Reply getStreamFor(String documentId);

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
     * @param configuration The importer's configuration
     * @return The document preview, or null if it cannot be produced
     */
    Reply getPreview(String documentId, ImporterConfiguration configuration);

    /**
     * Gets the preview of a document
     *
     * @param documentId      The identifier of a document
     * @param configurationId The identifier of the stored configuration to use
     * @return The document preview, or null if it cannot be produced
     */
    Reply getPreview(String documentId, String configurationId);

    /**
     * Begins the importation job of a document
     *
     * @param documentId    The identifier of a document
     * @param configuration The importer's configuration
     * @param metadata      The metadata for the artifact to produce
     * @return The job, or null if the job cannot be created
     */
    Reply beginImport(String documentId, ImporterConfiguration configuration, Artifact metadata);

    /**
     * Begins the importation job of a document
     *
     * @param documentId      The identifier of a document
     * @param configurationId The identifier of the stored configuration to use
     * @param metadata        The metadata for the artifact to produce
     * @return The job, or null if the job cannot be created
     */
    Reply beginImport(String documentId, String configurationId, Artifact metadata);

    /**
     * Stores an configuration that can be retrieved later
     *
     * @param configuration The configuration to store
     * @return The protocol reply
     */
    Reply storeConfiguration(ImporterConfiguration configuration);

    /**
     * Retrieves all the stored configurations
     *
     * @return The protocol reply
     */
    Reply retrieveConfigurations();

    /**
     * Retrieves a stored configuration
     *
     * @param configurationId The identifier of the configuration to retrieve
     * @return The protocol reply
     */
    Reply retrieveConfiguration(String configurationId);

    /**
     * Retrieves all the stored configurations related to the specified importer
     *
     * @param importerId The identifier of an importer
     * @return The protocol reply
     */
    Reply retrieveConfigurations(String importerId);

    /**
     * Deletes a stored configuration
     *
     * @param configurationId The identifier of the configuration to delete
     * @return The protocol reply
     */
    Reply deleteConfiguration(String configurationId);
}
