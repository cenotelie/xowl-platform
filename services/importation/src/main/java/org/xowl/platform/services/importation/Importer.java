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

import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Serializable;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.jobs.Job;

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
    public abstract ImporterConfiguration getConfiguration(String definition);

    /**
     * Gets the preview of a document to be imported
     *
     * @param document      The document to preview
     * @param configuration The configuration for this preview
     * @return The preview, or null if it cannot be produced
     */
    public abstract DocumentPreview getPreview(Document document, ImporterConfiguration configuration);

    /**
     * Gets the job for importing a document
     *
     * @param document      The document to import
     * @param configuration The configuration for this import
     * @return The job for importing the document
     */
    public abstract Job getImportJob(Document document, ImporterConfiguration configuration);

    @Override
    public String serializedString() {
        return serializedJSON();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(Importer.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                IOUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(getName()) +
                "\", \"wizardUri\": \"" +
                IOUtils.escapeStringJSON(getWebWizardURI()) +
                "\"}";
    }
}
