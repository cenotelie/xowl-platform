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

import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.artifacts.Artifact;

/**
 * Represents an importation method
 *
 * @author Laurent Wouters
 */
public abstract class Importer implements Service, Serializable {
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
    public abstract ImporterConfiguration getConfiguration(ASTNode definition);

    /**
     * Gets the preview of a document to be imported
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this preview
     * @return The preview, or null if it cannot be produced
     */
    public abstract Reply getPreview(String documentId, ImporterConfiguration configuration);

    /**
     * Gets the job for importing a document
     *
     * @param documentId    The identifier of a document
     * @param configuration The configuration for this import
     * @param metadata      The metadata for the artifact to produce
     * @return The job for importing the document
     */
    public abstract Reply getImportJob(String documentId, ImporterConfiguration configuration, Artifact metadata);

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
