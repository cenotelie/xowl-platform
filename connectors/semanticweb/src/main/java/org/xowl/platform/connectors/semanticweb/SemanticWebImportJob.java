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

package org.xowl.platform.connectors.semanticweb;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.services.importation.ImportationJob;
import org.xowl.platform.services.importation.Importer;

/**
 * Represents an import job for a Semantic Web dataset
 *
 * @author Laurent Wouters
 */
public class SemanticWebImportJob extends ImportationJob<SemanticWebImporterConfiguration> {
    /**
     * Initializes this job
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importer
     * @param metadata      The metadata for the artifact to produce
     */
    public SemanticWebImportJob(String documentId, SemanticWebImporterConfiguration configuration, Artifact metadata) {
        super(SemanticWebImportJob.class.getCanonicalName(), documentId, configuration, metadata);
    }

    /**
     * Initializes this job
     *
     * @param importer   The parent importer
     * @param definition The job definition
     */
    public SemanticWebImportJob(Importer importer, ASTNode definition) {
        super(importer, definition);
    }

    @Override
    public void run() {
        result = SemanticWebImporter.doImport(documentId, configuration, metadata);
    }
}