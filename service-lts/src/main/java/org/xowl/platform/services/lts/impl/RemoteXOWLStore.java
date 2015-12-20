/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Contributors:
 * Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.lts.impl;

import org.xowl.platform.kernel.Artifact;
import org.xowl.platform.kernel.ArtifactDeferred;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.store.IOUtils;
import org.xowl.store.rdf.*;
import org.xowl.store.sparql.Result;
import org.xowl.store.sparql.ResultFailure;
import org.xowl.store.sparql.ResultQuads;
import org.xowl.store.sparql.ResultSolutions;
import org.xowl.store.storage.cache.CachedNodes;
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
 * Represents a remote xOWL store for this platform
 *
 * @author Laurent Wouters
 */
public class RemoteXOWLStore implements TripleStore {
    /**
     * The parent service
     */
    private final RemoteXOWLStoreService service;
    /**
     * The name of this store
     */
    private final String name;
    /**
     * The connection to the remote host
     */
    private HTTPConnection connection;

    /**
     * Initializes this store
     *
     * @param service The parent service
     * @param name    The name of this store
     */
    public RemoteXOWLStore(RemoteXOWLStoreService service, String name) {
        this.service = service;
        this.name = name;
    }

    /**
     * Gets the HTTP connection
     *
     * @return The HTTP connection
     */
    private HTTPConnection getConnection() {
        if (connection == null) {
            ConfigurationService configurationService = ServiceUtils.getOSGIService(ConfigurationService.class);
            if (configurationService == null)
                return null;
            Configuration configuration = configurationService.getConfigFor(service);
            if (configuration == null)
                return null;
            connection = new HTTPConnection(configuration.get(name, "endpoint"), configuration.get(name, "login"), configuration.get(name, "password"));
        }
        return connection;
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
        HTTPConnection connection = getConnection();
        if (connection == null)
            return false;

        StringWriter writer = new StringWriter();
        writer.write("INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeStringW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { ");
        NTripleSerializer serializer = new NTripleSerializer(writer);
        serializer.serialize(Logger.DEFAULT, artifact.getMetadata().iterator());
        writer.write(" } }; INSERT DATA { GRAPH <");
        writer.write(IOUtils.escapeAbsoluteURIW3C(artifact.getIdentifier()));
        writer.write("> {");
        serializer.serialize(Logger.DEFAULT, artifact.getContent().iterator());
        writer.write(" } }");

        Result result = connection.sparql(writer.toString());
        return result.isSuccess();
    }

    @Override
    public boolean delete(Artifact artifact) {
        return delete(artifact.getIdentifier());
    }

    @Override
    public boolean delete(String identifier) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return false;

        StringWriter writer = new StringWriter();
        writer.write("DELETE WHERE { GRAPH <");
        writer.write(IOUtils.escapeStringW3C(KernelSchema.GRAPH_ARTIFACTS));
        writer.write("> { <");
        writer.write(IOUtils.escapeStringW3C(identifier));
        writer.write("> ?p ?o } }; DROP SILENT GRAPH <");
        writer.write(IOUtils.escapeStringW3C(identifier));
        writer.write(">");

        Result result = connection.sparql(writer.toString());
        return result.isSuccess();
    }

    @Override
    public Artifact retrieve(String identifier) {
        HTTPConnection connection = getConnection();
        if (connection == null)
            return null;

        Result result = sparql("DESCRIBE <" + IOUtils.escapeStringW3C(identifier) + ">");
        if (result.isFailure())
            return null;
        Collection<Quad> metadata = ((ResultQuads) result).getQuads();
        if (metadata.isEmpty())
            return null;
        return buildArtifact(metadata);
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
            result.add(buildArtifact(entry.getValue()));
        }
        return result;
    }

    /**
     * Builds the default artifact from the specified identifier and set of metadata
     *
     * @param metadata The metadata
     * @return The artifact
     */
    private Artifact buildArtifact(Collection<Quad> metadata) {
        return new ArtifactDeferred(metadata) {
            @Override
            protected Collection<Quad> load() {
                Result result = sparql("SELECT ?s ?p ?o WHERE  { GRAPH <" + IOUtils.escapeStringW3C(identifier) + "> { ?s ?p ?o } }");
                if (result.isFailure())
                    return null;
                ResultSolutions solutions = ((ResultSolutions) result);
                Collection<Quad> quads = new ArrayList<>(solutions.getSolutions().size());
                CachedNodes nodes = new CachedNodes();
                IRINode graph = nodes.getIRINode(identifier);
                for (QuerySolution solution : solutions.getSolutions()) {
                    quads.add(new Quad(graph,
                            (SubjectNode) solution.get("s"),
                            (Property) solution.get("p"),
                            solution.get("o")));
                }
                return quads;
            }
        };
    }
}
