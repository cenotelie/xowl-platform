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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyApiError;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.loaders.*;
import org.xowl.infra.store.owl.TranslationException;
import org.xowl.infra.store.owl.Translator;
import org.xowl.infra.store.rdf.Quad;
import org.xowl.infra.store.storage.NodeManager;
import org.xowl.infra.store.storage.cache.CachedNodes;
import org.xowl.infra.utils.logging.BufferedLogger;
import org.xowl.infra.utils.logging.Logger;
import org.xowl.platform.kernel.webapi.HttpApiService;

import java.io.Reader;
import java.util.Collection;

/**
 * A structure that can be used to load semantic web datasets
 *
 * @author Laurent Wouters
 */
class SemanticWebLoader {
    /**
     * The node manager when loading quads
     */
    private final NodeManager nodeManager;

    /**
     * Initializes this structure
     */
    public SemanticWebLoader() {
        this.nodeManager = new CachedNodes();
    }

    /**
     * @param reader      The reader to read from
     * @param resourceIRI The IRI for the resource to load
     * @param syntax      The expected syntax
     * @return The reply
     */
    public XSPReply load(Reader reader, String resourceIRI, String syntax) {
        BufferedLogger bufferedLogger = new BufferedLogger();
        try {
            Collection<Quad> quads = load(bufferedLogger, reader, resourceIRI, syntax);
            if (quads == null || !bufferedLogger.getErrorMessages().isEmpty())
                return new XSPReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED, bufferedLogger.getErrorsAsString());
            return new XSPReplyResultCollection<>(quads);
        } catch (TranslationException exception) {
            bufferedLogger.error(exception);
            return new XSPReplyApiError(HttpApiService.ERROR_CONTENT_PARSING_FAILED, bufferedLogger.getErrorsAsString());
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
                return loadRDF(logger, reader, resourceIRI, new NTriplesLoader(nodeManager));
            case Repository.SYNTAX_NQUADS:
                return loadRDF(logger, reader, resourceIRI, new NQuadsLoader(nodeManager));
            case Repository.SYNTAX_TURTLE:
                return loadRDF(logger, reader, resourceIRI, new TurtleLoader(nodeManager));
            case Repository.SYNTAX_RDFT:
                return loadRDF(logger, reader, resourceIRI, new RDFTLoader(nodeManager));
            case Repository.SYNTAX_RDFXML:
                return loadRDF(logger, reader, resourceIRI, new RDFXMLLoader(nodeManager));
            case Repository.SYNTAX_JSON_LD:
                return loadRDF(logger, reader, resourceIRI, new JSONLDLoader(nodeManager) {
                    @Override
                    protected Reader getReaderFor(Logger logger, String iri) {
                        return null;
                    }
                });
            case Repository.SYNTAX_TRIG:
                return loadRDF(logger, reader, resourceIRI, new TriGLoader(nodeManager));
            case Repository.SYNTAX_FUNCTIONAL_OWL2:
                return loadOWL(logger, reader, resourceIRI, new FunctionalOWL2Loader());
            case Repository.SYNTAX_OWLXML:
                return loadOWL(logger, reader, resourceIRI, new OWLXMLLoader());
            case Repository.SYNTAX_XOWL:
                return loadOWL(logger, reader, resourceIRI, new XOWLLoader());
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
        Translator translator = new Translator(null, nodeManager);
        return translator.translate(input);
    }
}
