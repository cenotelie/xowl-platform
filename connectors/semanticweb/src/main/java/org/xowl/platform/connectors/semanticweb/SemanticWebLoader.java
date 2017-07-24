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

import org.xowl.infra.store.Repository;
import org.xowl.infra.store.RepositoryRDF;
import org.xowl.infra.store.loaders.*;
import org.xowl.infra.store.owl.TranslationException;
import org.xowl.infra.store.owl.Translator;
import org.xowl.infra.store.rdf.IRINode;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.utils.api.Reply;
import org.xowl.infra.utils.api.ReplyApiError;
import org.xowl.infra.utils.api.ReplyResultCollection;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A structure that can be used to load semantic web datasets
 *
 * @author Laurent Wouters
 */
class SemanticWebLoader {
    /**
     * The repository to use for loading
     */
    private final RepositoryRDF repository;

    /**
     * Initializes this structure
     */
    public SemanticWebLoader() {
        this.repository = new RepositoryRDF();
    }

    /**
     * @param reader      The reader to read from
     * @param resourceIRI The IRI for the resource to load
     * @param syntax      The expected syntax
     * @return The reply
     */
    public Reply load(Reader reader, String resourceIRI, String syntax) {
        IRINode graph = repository.getStore().getIRINode(resourceIRI);
        BufferedLogger bufferedLogger = new BufferedLogger();
        try {
            Collection<Quad> quads = load(bufferedLogger, reader, resourceIRI, syntax);
            if (quads == null || !bufferedLogger.getErrorMessages().isEmpty())
                return new ReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED, bufferedLogger.getErrorsAsString());
            Collection<Quad> result = new ArrayList<>(quads.size());
            for (Quad quad : quads) {
                if (quad.getGraph() != graph)
                    result.add(new Quad(graph, quad.getSubject(), quad.getProperty(), quad.getObject()));
                else
                    result.add(quad);
            }
            return new ReplyResultCollection<>(result);
        } catch (TranslationException exception) {
            bufferedLogger.error(exception);
            return new ReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED, bufferedLogger.getErrorsAsString());
        }
    }

    /**
     * @param logger      The logger to use
     * @param reader      The reader to read from
     * @param resourceIRI The IRI for the resource to load
     * @param syntax      The expected syntax
     * @return The loaded quads
     */
    private Collection<Quad> load(Logger logger, Reader reader, String resourceIRI, String syntax) throws TranslationException {
        switch (syntax) {
            case Repository.SYNTAX_NTRIPLES:
                return loadRDF(logger, reader, resourceIRI, new NTriplesLoader(repository.getStore()));
            case Repository.SYNTAX_NQUADS:
                return loadRDF(logger, reader, resourceIRI, new NQuadsLoader(repository.getStore()));
            case Repository.SYNTAX_TURTLE:
                return loadRDF(logger, reader, resourceIRI, new TurtleLoader(repository.getStore()));
            case Repository.SYNTAX_RDFXML:
                return loadRDF(logger, reader, resourceIRI, new RDFXMLLoader(repository.getStore()));
            case Repository.SYNTAX_JSON_LD:
                return loadRDF(logger, reader, resourceIRI, new JsonLdLoader(repository.getStore()) {
                    @Override
                    protected Reader getReaderFor(Logger logger, String iri) {
                        return null;
                    }
                });
            case Repository.SYNTAX_JSON:
                return loadRDF(logger, reader, resourceIRI, new JsonLoader(repository));
            case Repository.SYNTAX_TRIG:
                return loadRDF(logger, reader, resourceIRI, new TriGLoader(repository.getStore()));
            case Repository.SYNTAX_XRDF:
                return loadRDF(logger, reader, resourceIRI, new xRDFLoader(repository));
            case Repository.SYNTAX_FUNCTIONAL_OWL2:
                return loadOWL(logger, reader, resourceIRI, new FunctionalOWL2Loader());
            case Repository.SYNTAX_OWLXML:
                return loadOWL(logger, reader, resourceIRI, new OWLXMLLoader());
            case Repository.SYNTAX_XOWL:
                return loadOWL(logger, reader, resourceIRI, new xOWLLoader(repository.getExecutionManager()));
            default:
                throw new IllegalArgumentException("Unsupported syntax: " + syntax);
        }
    }

    /**
     * Loads an RDF input
     *
     * @param logger      The logger to use
     * @param reader      The reader to read from
     * @param resourceIRI The IRI for the resource to load
     * @param loader      The RDF loader to use
     * @return The loaded quads
     */
    private Collection<Quad> loadRDF(Logger logger, Reader reader, String resourceIRI, Loader loader) {
        RDFLoaderResult input = loader.loadRDF(logger, reader, resourceIRI, resourceIRI);
        return input.getQuads();
    }

    /**
     * Loads an OWL input
     *
     * @param logger      The logger to use
     * @param reader      The reader to read from
     * @param resourceIRI The IRI for the resource to load
     * @param loader      The RDF loader to use
     * @return The loaded quads
     */
    private Collection<Quad> loadOWL(Logger logger, Reader reader, String resourceIRI, Loader loader) throws TranslationException {
        OWLLoaderResult input = loader.loadOWL(logger, reader, resourceIRI);
        Translator translator = new Translator(null, repository.getStore());
        return translator.translate(input);
    }
}
