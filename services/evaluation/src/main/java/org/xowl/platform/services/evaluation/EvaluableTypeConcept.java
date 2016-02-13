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
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.artifacts.ArtifactArchetype;
import org.xowl.platform.services.lts.TripleStoreService;

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
    public XSPReply getElements(String artifactId) {
        TripleStoreService service = ServiceUtils.getService(TripleStoreService.class);
        if (service == null)
            return new XSPReplyFailure("Failed to resolve the LTS service");
        return null;
    }
}
