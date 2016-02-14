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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.store.IOUtils;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.QuerySolution;
import org.xowl.infra.store.sparql.Result;
import org.xowl.infra.store.sparql.ResultFailure;
import org.xowl.infra.store.sparql.ResultSolutions;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.ArtifactArchetype;
import org.xowl.platform.services.lts.TripleStoreService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a type of evaluable element that is backed by a concept in a schema
 *
 * @author Laurent Wouters
 */
public class EvaluableTypeConcept extends EvaluableTypeBase {
    /**
     * The URI of the concept that can be evaluated
     */
    private final String conceptyURI;

    /**
     * Initializes this element
     *
     * @param identifier  The type's unique identifier
     * @param name        The type's name
     * @param archetype   The associated archetype
     * @param conceptyURI The URI of the concept that can be evaluated
     */
    public EvaluableTypeConcept(String identifier, String name, ArtifactArchetype archetype, String conceptyURI) {
        super(identifier, name, archetype);
        this.conceptyURI = conceptyURI;
    }

    @Override
    public XSPReply getElements() {
        TripleStoreService service = ServiceUtils.getService(TripleStoreService.class);
        if (service == null)
            return XSPReplyServiceUnavailable.instance();
        String query = "SELECT DISTINCT ?a ?e WHERE { GRAPH <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.GRAPH_ARTIFACTS) +
                "> { ?a a <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARTIFACT) +
                "> . ?a <" +
                IOUtils.escapeAbsoluteURIW3C(KernelSchema.ARCHETYPE) +
                "> \"" +
                IOUtils.escapeStringW3C(archetype.getIdentifier()) +
                "\" } . GRAPH ?a { ?e a <" +
                IOUtils.escapeAbsoluteURIW3C(conceptyURI) +
                "> } }";
        Result sparqlResult = service.getLongTermStore().sparql(query);
        if (sparqlResult.isFailure())
            return new XSPReplyFailure(((ResultFailure) sparqlResult).getMessage());
        Collection<Evaluable> result = new ArrayList<>();
        for (QuerySolution solution : ((ResultSolutions) sparqlResult).getSolutions()) {
            String artifactId = ((IRINode) solution.get("a")).getIRIValue();
            String elementId = ((IRINode) solution.get("e")).getIRIValue();
            result.add(new EvaluableEntity(this, artifactId, elementId));
        }
        return new XSPReplyResultCollection<>(result);
    }

    @Override
    public Evaluable getElement(Map<String, String> parameters) {
        return new EvaluableEntity(this, parameters.get("artifact"), parameters.get("element"));
    }
}
