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

package org.xowl.platform.services.importation;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.api.Reply;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactFuture;
import org.xowl.platform.kernel.jobs.JobBase;

/**
 * Implements a base importation job
 *
 * @param <T> The type of the importer configuration
 * @author Laurent Wouters
 */
public abstract class ImportationJob<T extends ImporterConfiguration> extends JobBase {
    /**
     * The identifier of the document to import
     */
    protected final String documentId;
    /**
     * The configuration for the importer
     */
    protected final T configuration;
    /**
     * The metadata for the artifact to produce
     */
    protected final Artifact metadata;
    /**
     * The job's result
     */
    protected Reply result;

    /**
     * Initializes this job
     *
     * @param jobType       The type of job
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importer
     * @param metadata      The metadata for the artifact to produce
     */
    public ImportationJob(String jobType, String documentId, T configuration, Artifact metadata) {
        super("Importation of document " + documentId, jobType);
        this.documentId = documentId;
        this.configuration = configuration;
        this.metadata = metadata;
    }

    /**
     * Initializes this job
     *
     * @param importer   The parent importer
     * @param definition The job definition
     */
    public ImportationJob(Importer importer, ASTNode definition) {
        super(definition);
        String documentId = "";
        T configuration = null;
        Artifact metadata = null;
        ASTNode payload = getPayloadNode(definition);
        if (payload != null) {
            for (ASTNode member : payload.getChildren()) {
                String head = TextUtils.unescape(member.getChildren().get(0).getValue());
                head = head.substring(1, head.length() - 1);
                if ("document".equals(head)) {
                    String value = TextUtils.unescape(member.getChildren().get(1).getValue());
                    documentId = value.substring(1, value.length() - 1);
                } else if ("configuration".equals(head)) {
                    configuration = (T) importer.getConfiguration(member.getChildren().get(1));
                } else if ("payload".equals(head)) {
                    metadata = new ArtifactFuture(member.getChildren().get(1));
                }
            }
        }
        this.documentId = documentId;
        this.configuration = configuration;
        this.metadata = metadata;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "{\"document\": \"" +
                TextUtils.escapeStringJSON(documentId) +
                "\", \"configuration\": " +
                configuration.serializedJSON() +
                ", \"metadata\": " +
                metadata.serializedJSON() +
                "}";
    }

    @Override
    public Reply getResult() {
        return result;
    }
}
