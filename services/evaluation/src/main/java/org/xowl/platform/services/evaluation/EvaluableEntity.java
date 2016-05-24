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

package org.xowl.platform.services.evaluation;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.RDFPatternSolution;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.services.lts.TripleStoreService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base implementation of an evaluable element representing an entity in an artifact
 *
 * @author Laurent Wouters
 */
public class EvaluableEntity implements Evaluable {
    /**
     * The evaluable unique identifier
     */
    protected final String identifier;
    /**
     * The evaluable name
     */
    protected final String name;
    /**
     * The identifier of the containing artifact
     */
    protected final String artifactId;
    /**
     * The URI of the evaluable element
     */
    protected final String elementURI;

    /**
     * Initializes this entity
     *
     * @param parentType The parent type
     * @param artifactId The identifier of the containing artifact
     * @param elementURI The URI of the evaluable element
     */
    public EvaluableEntity(EvaluableType parentType, String artifactId, String elementURI) {
        this.identifier = parentType.getIdentifier() + "#" + UUID.randomUUID().toString();
        this.artifactId = artifactId;
        this.elementURI = elementURI;
        ArtifactStorageService artifactStorageService = ServiceUtils.getService(ArtifactStorageService.class);
        TripleStoreService ltsService = ServiceUtils.getService(TripleStoreService.class);
        if (artifactStorageService == null || ltsService == null) {
            this.name = identifier;
        } else {
            String name = null;
            Result sparqlResult = ltsService.getLongTermStore().sparql("SELECT DISTINCT ?p ?o WHERE { GRAPH <" +
                    IOUtils.escapeAbsoluteURIW3C(artifactId) +
                    "> { <" +
                    IOUtils.escapeAbsoluteURIW3C(elementURI) +
                    "> ?p ?o } }");
            if (!sparqlResult.isSuccess()) {
                name = elementURI;
            } else {
                for (RDFPatternSolution solution : ((ResultSolutions) sparqlResult).getSolutions()) {
                    String property = ((IRINode) solution.get("p")).getIRIValue();
                    if (KernelSchema.NAME.equals("name") || property.equals(Vocabulary.rdfs + "label") || property.endsWith("#name") || property.endsWith("#title")) {
                        name = ((LiteralNode) solution.get("o")).getLexicalValue();
                        break;
                    }
                }
                if (name == null)
                    name = elementURI;
            }
            XSPReply reply = artifactStorageService.retrieve(artifactId);
            if (reply.isSuccess()) {
                Artifact artifact = ((XSPReplyResult<Artifact>) reply).getData();
                name += " in " + artifact.getName() + " (" + artifact.getVersion() + ")";
            } else {
                name += " in " + artifactId;
            }
            this.name = name;
        }
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, String> getParameters() {
        Map<String, String> result = new HashMap<>();
        result.put("artifact", artifactId);
        result.put("element", elementURI);
        return result;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(getClass().getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"artifact\": \"" +
                IOUtils.escapeStringJSON(artifactId) +
                "\", \"element\": \"" +
                IOUtils.escapeStringJSON(elementURI) +
                "\"}";
    }
}
