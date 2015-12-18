/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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

package org.xowl.platform.services.lts.impl;

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.BaseArtifact;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.LiteralNode;
import org.xowl.store.rdf.Node;
import org.xowl.store.rdf.Quad;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.storage.remote.HTTPConnection;
import org.xowl.store.writers.NTripleSerializer;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.logging.Logger;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements a triple store service that is backed by a remote store connected to via HTTP
 *
 * @author Laurent Wouters
 */
public class RemoteLTSService implements TripleStoreService {
    /**
     * The connection to the remote host
     */
    private HTTPConnection connection;

    /**
     * Gets the HTTP connection
     *
     * @return The HTTP connection
     */
    private HTTPConnection getConnection() {
        if (connection == null) {
            ConfigurationService configurationService = ServiceUtils.getService(ConfigurationService.class);
            if (configurationService == null)
                return null;
            Configuration configuration = configurationService.getConfigFor(this);
            if (configuration == null)
                return null;
            connection = new HTTPConnection(configuration.get("endpoint"), configuration.get("login"), configuration.get("password"));
        }
        return connection;
    }

    @Override
    public String getIdentifier() {
        return RemoteLTSService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Remote Triple Store Service";
    }

    @Override
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
        return null;
    }

    @Override
    public Result sparql(String query) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return new ResultFailure("The connection to the remote host is not configured");
        return connection.sparql(query);
    }

    @Override
    public boolean store(Artifact artifact) {
        StringWriter writer = new StringWriter();
        writer.write("INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeStringW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ");
        NTripleSerializer serializer = new NTripleSerializer(writer);
        serializer.serialize(Logger.DEFAULT, artifact.getMetadata().iterator());
        writer.write(" } }");
        Result result = sparql(writer.toString());
        return result.isSuccess();
    }

    @Override
    public boolean delete(Artifact artifact) {
        return delete(artifact.getIdentifier());
    }

    @Override
    public boolean delete(String identifier) {
        Result result = sparql("DELETE WHERE { GRAPH <" + KernelSchema.GRAPH_ARTIFACTS + "> { <" + IOUtils.escapeStringW3C(identifier) + "> ?p ?o } }");
        return result.isSuccess();
    }

    @Override
    public Artifact retrieve(String identifier) {
        Result result = sparql("DESCRIBE <" + IOUtils.escapeStringW3C(identifier) + ">");
        if (result.isFailure())
            return null;
        Collection<Quad> quads = ((ResultQuads) result).getQuads();
        return buildArtifact(identifier, quads);
    }

    @Override
    public Collection<Artifact> listArtifacts() {
        Collection<Artifact> result = new ArrayList<>();
        Result sparqlResult = sparql("DESCRIBE ?a WHERE { GRAPH <" + KernelSchema.GRAPH_ARTIFACTS + "> { ?a a <" + KernelSchema.ARTIFACT + "> } }");
        if (sparqlResult.isFailure())
            return result;
        Collection<Quad> quads = ((ResultQuads) sparqlResult).getQuads();
        Map<IRINode, Collection<Quad>> data = new HashMap<>();
        for (Quad quad : quads) {
            if (quad.getSubject().getNodeType() == Node.TYPE_IRI) {
                IRINode subject = (IRINode) quad.getSubject();
                Collection<Quad> metadata = data.get(subject);
                if (metadata == null) {
                    metadata = new ArrayList<>();
                    data.put(subject, metadata);
                }
                metadata.add(quad);
            }
        }
        for (Map.Entry<IRINode, Collection<Quad>> entry : data.entrySet()) {
            result.add(buildArtifact(entry.getKey().getIRIValue(), entry.getValue()));
        }
        return result;
    }

    /**
     * Builds the default artifact from the specified identifier and set of metadata
     *
     * @param identifier The identifier
     * @param metadata   The metadata
     * @return The artifact
     */
    private Artifact buildArtifact(String identifier, Collection<Quad> metadata) {
        String name = "";
        for (Quad quad : metadata) {
            if (quad.getProperty().getNodeType() == Node.TYPE_IRI
                    && KernelSchema.HAS_NAME.equals(((IRINode) quad.getProperty()).getIRIValue())
                    && quad.getSubject().getNodeType() == Node.TYPE_IRI
                    && identifier.equals(((IRINode) quad.getSubject()).getIRIValue())
                    && quad.getObject().getNodeType() == Node.TYPE_LITERAL) {
                name = ((LiteralNode) quad.getObject()).getLexicalValue();
            }
        }
        return new BaseArtifact(identifier, name, metadata);
    }
}
