/*******************************************************************************
 * Copyright (c) 2016 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.evaluation;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.LiteralNode;
import org.xowl.infra.store.rdf.QuerySolution;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.services.lts.TripleStoreService;

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
     * The parent type
     */
    protected final EvaluableType parentType;
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
        this.parentType = parentType;
        this.artifactId = artifactId;
        this.elementURI = elementURI;
        ArtifactStorageService artifactStorageService = ServiceUtils.getService(ArtifactStorageService.class);
        TripleStoreService ltsService = ServiceUtils.getService(TripleStoreService.class);
        if (artifactStorageService == null || ltsService == null) {
            this.name = identifier;
        } else {
            String name = null;
            Result sparqlResult = ltsService.getLiveStore().sparql("SELECT DISTINCT ?p ?o WHERE { GRAPH <" +
                    IOUtils.escapeAbsoluteURIW3C(artifactId) +
                    "> { <" +
                    IOUtils.escapeAbsoluteURIW3C(elementURI) +
                    "> ?p ?o } }");
            if (!sparqlResult.isSuccess()) {
                name = elementURI;
            } else {
                for (QuerySolution solution : ((ResultSolutions) sparqlResult).getSolutions()) {
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

    /**
     * Initializes this entity
     *
     * @param definition The definition of this entity
     */
    public EvaluableEntity(ASTNode definition) {
        String identifier = null;
        String name = null;
        String parentId = null;
        String artifactId = null;
        String elementId = null;
        for (ASTNode member : definition.getChildren()) {
            String memberName = IOUtils.unescape(member.getChildren().get(0).getValue());
            memberName = memberName.substring(1, memberName.length() - 1);
            switch (memberName) {
                case "id":
                    identifier = IOUtils.unescape(member.getChildren().get(1).getValue());
                    identifier = identifier.substring(1, identifier.length() - 1);
                    break;
                case "name":
                    name = IOUtils.unescape(member.getChildren().get(1).getValue());
                    name = name.substring(1, name.length() - 1);
                    break;
                case "parentType":
                    parentId = IOUtils.unescape(member.getChildren().get(1).getValue());
                    parentId = parentId.substring(1, parentId.length() - 1);
                    break;
                case "artifact":
                    artifactId = IOUtils.unescape(member.getChildren().get(1).getValue());
                    artifactId = artifactId.substring(1, artifactId.length() - 1);
                    break;
                case "element":
                    elementId = IOUtils.unescape(member.getChildren().get(1).getValue());
                    elementId = elementId.substring(1, elementId.length() - 1);
                    break;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.parentType = ServiceUtils.getService(EvaluationService.class).getEvaluableType(parentId);
        this.artifactId = artifactId;
        this.elementURI = elementId;
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
    public EvaluableType getType() {
        return parentType;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                IOUtils.escapeStringJSON(EvaluableEntity.class.getCanonicalName()) +
                "\", \"id\": \"" +
                IOUtils.escapeStringJSON(identifier) +
                "\", \"name\": \"" +
                IOUtils.escapeStringJSON(name) +
                "\", \"parentType\": \"" +
                IOUtils.escapeStringJSON(parentType.getIdentifier()) +
                "\", \"artifact\": \"" +
                IOUtils.escapeStringJSON(artifactId) +
                "\", \"element\": \"" +
                IOUtils.escapeStringJSON(elementURI) +
                "\"}";
    }
}
