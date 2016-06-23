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

package org.xowl.platform.connectors.doors9.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.store.IOUtils;
import org.xowl.platform.connectors.doors9.DOORS9Configuration;
import org.xowl.platform.connectors.doors9.DOORS9Importer;
import org.xowl.platform.kernel.jobs.JobBase;

/**
 * Represents an importation job for a DOORS 9 document
 *
 * @author Elie Soubiran
 */
public class DOORS9ImportationJob extends JobBase {
    /**
     * The identifier of the document to import
     */
    private final String documentId;
    /**
     * The configuration for the importer
     */
    private final DOORS9Configuration configuration;
    /**
     * The job's result
     */
    private XSPReply result;

    /**
     * Initializes this job
     *
     * @param documentId    The identifier of the document to import
     * @param configuration The configuration for the importer
     */
    public DOORS9ImportationJob(String documentId, DOORS9Configuration configuration) {
        super("Importation of DOORS 9 document " + documentId, DOORS9ImportationJob.class.getCanonicalName());
        this.documentId = documentId;
        this.configuration = configuration;
    }

    /**
     * Initializes this job
     *
     * @param definition The job definition
     */
    public DOORS9ImportationJob(ASTNode definition) {
        super(definition);
        String tDocument = "";
        DOORS9Configuration tConfiguration = null;
        ASTNode payload = getPayloadNode(definition);
        if (payload != null) {
            for (ASTNode member : payload.getChildren()) {
                String head = IOUtils.unescape(member.getChildren().get(0).getValue());
                head = head.substring(1, head.length() - 1);
                if ("document".equals(head)) {
                    String value = IOUtils.unescape(member.getChildren().get(1).getValue());
                    tDocument = value.substring(1, value.length() - 1);
                } else if ("configuration".equals(head)) {
                    tConfiguration = new DOORS9Configuration(member.getChildren().get(1));
                }
            }
        }
        this.documentId = tDocument;
        this.configuration = tConfiguration;
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "{\"document\": \"" +
                IOUtils.escapeStringJSON(documentId) +
                "\", \"configuration\": " +
                configuration.serializedJSON() +
                "}";
    }

    @Override
    public void doRun() {
        result = DOORS9Importer.doImport(documentId, configuration);
    }

    @Override
    public XSPReply getResult() {
        return result;
    }
}