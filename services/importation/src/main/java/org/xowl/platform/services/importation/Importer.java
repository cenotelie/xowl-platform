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
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredService;

/**
 * Represents an importation method
 *
 * @author Laurent Wouters
 */
public abstract class Importer implements SecuredService, Serializable {
    /**
     * Service action to preview the import of a document
     */
    protected final SecuredAction actionPreview;
    /**
     * Service action to import a document
     */
    protected final SecuredAction actionImport;
    /**
     * The actions for this service
     */
    protected final SecuredAction[] actions;

    /**
     * Initializes this importer
     */
    protected Importer() {
        this.actionPreview = new SecuredAction(getIdentifier() + ".Preview", getName() + " - Preview Import");
        this.actionImport = new SecuredAction(getIdentifier() + ".Import", getName() + " - Do Import");
        this.actions = new SecuredAction[]{
                actionPreview,
                actionImport
        };
    }

    @Override
    public SecuredAction[] getActions() {
        return actions;
    }

    /**
     * Gets the URI for the web wizard
     *
     * @return The URI for the web wizard
     */
    protected abstract String getWebWizardURI();

    /**
     * Gets the configuration from the specified serialized definition
     *
     * @param definition The configuration's definition
     * @return The configuration
     */
    public abstract ImporterConfiguration getConfiguration(String definition);

    /**
     * Gets the preview of a document to be imported
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this preview
     * @return The preview, or null if it cannot be produced
     */
    public abstract XSPReply getPreview(String documentId, ImporterConfiguration configuration);

    /**
     * Gets the preview of a document to be imported
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this preview
     * @return The preview, or null if it cannot be produced
     */
    public XSPReply getPreview(String documentId, String configuration) {
        return getPreview(documentId, getConfiguration(configuration));
    }

    /**
     * Gets the job for importing a document
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this import
     * @return The job for importing the document
     */
    public abstract XSPReply getImportJob(String documentId, ImporterConfiguration configuration);

    /**
     * Gets the job for importing a document
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this import
     * @return The job for importing the document
     */
    public XSPReply getImportJob(String documentId, String configuration) {
        return getImportJob(documentId, getConfiguration(configuration));
    }

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(Importer.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"wizardUri\": \"" +
                TextUtils.escapeStringJSON(getWebWizardURI()) +
                "\"}";
    }
}
