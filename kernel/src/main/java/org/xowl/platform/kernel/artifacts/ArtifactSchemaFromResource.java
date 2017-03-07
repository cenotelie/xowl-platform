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

package org.xowl.platform.kernel.artifacts;

import org.xowl.infra.lang.owl2.Ontology;
import org.xowl.infra.store.ProxyObject;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.RepositoryRDF;
import org.xowl.infra.store.Vocabulary;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Implements a business schema that is backed by a resource file
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaFromResource implements ArtifactSchema {
    /**
     * The rdfs:label property
     */
    private static final String RDFS_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

    /**
     * The schema's IRI
     */
    private final String iri;
    /**
     * The schema's name
     */
    private final String name;
    /**
     * The backing repository
     */
    private final RepositoryRDF repository;
    /**
     * The classes in the schema
     */
    private final Collection<ProxyObject> proxyClasses;
    /**
     * The datatypes in the schema
     */
    private final Collection<ProxyObject> proxyDatatypes;
    /**
     * The object properties in the schema
     */
    private final Collection<ProxyObject> proxyObjectProperties;
    /**
     * The data properties in the schema
     */
    private final Collection<ProxyObject> proxyDataProperties;
    /**
     * The individuals in the schema
     */
    private final Collection<ProxyObject> proxyIndividuals;

    /**
     * Initializes this schema
     *
     * @param type     A class that is in the same bundle as the resource
     * @param resource The resource URI, e.g. /com/business/serious/schema.fs
     * @param iri      The iri for this schema
     */
    public ArtifactSchemaFromResource(Class<?> type, String resource, String iri) {
        this.iri = iri;
        this.repository = new RepositoryRDF(StoreFactory.create().inMemory().make());
        InputStream stream = type.getResourceAsStream(resource);
        Ontology ontology = null;
        if (stream != null) {
            try {
                ontology = this.repository.load(Logging.get(),
                        new InputStreamReader(stream),
                        Repository.SCHEME_RESOURCE + resource,
                        iri, Repository.getSyntax(resource));
            } catch (Exception exception) {
                Logging.get().error(exception);
            }
        }
        ProxyObject proxy = repository.getProxy(iri);
        if (proxy != null) {
            String label = (String) proxy.getDataValue(RDFS_LABEL);
            this.name = label != null ? label : iri;
        } else {
            this.name = iri;
        }
        this.proxyClasses = new ArrayList<>();
        this.proxyDatatypes = new ArrayList<>();
        this.proxyObjectProperties = new ArrayList<>();
        this.proxyDataProperties = new ArrayList<>();
        this.proxyIndividuals = new ArrayList<>();
        if (ontology != null) {
            Iterator<ProxyObject> iterator = repository.getProxiesIn(ontology);
            while (iterator.hasNext()) {
                proxy = iterator.next();
                if (proxy.getIRIString() == null)
                    continue;
                for (ProxyObject proxyType : proxy.getObjectValues(Vocabulary.rdfType)) {
                    switch (proxyType.getIRIString()) {
                        case Vocabulary.owlClass:
                            proxyClasses.add(proxy);
                            break;
                        case Vocabulary.rdfsDatatype:
                            proxyDatatypes.add(proxy);
                            break;
                        case Vocabulary.owlObjectProperty:
                            proxyObjectProperties.add(proxy);
                            break;
                        case Vocabulary.owlDataProperty:
                            proxyDataProperties.add(proxy);
                            break;
                        case Vocabulary.owlNamedIndividual:
                            proxyIndividuals.add(proxy);
                            break;
                    }
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return iri;
    }

    @Override
    public String getName() {
        return iri;
    }

    @Override
    public Collection<ProxyObject> getClasses() {
        return Collections.unmodifiableCollection(proxyClasses);
    }

    @Override
    public Collection<ProxyObject> getDatatypes() {
        return Collections.unmodifiableCollection(proxyDatatypes);
    }

    @Override
    public Collection<ProxyObject> getObjectProperties() {
        return Collections.unmodifiableCollection(proxyObjectProperties);
    }

    @Override
    public Collection<ProxyObject> getDataProperties() {
        return Collections.unmodifiableCollection(proxyDataProperties);
    }

    @Override
    public Collection<ProxyObject> getIndividuals() {
        return Collections.unmodifiableCollection(proxyIndividuals);
    }

    @Override
    public ProxyObject getEntity(String uri) {
        return repository.getProxy(uri);
    }

    @Override
    public String serializedString() {
        return iri;
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(ArtifactSchema.class.getCanonicalName()));
        builder.append("\", \"identifier\": \"");
        builder.append(TextUtils.escapeStringJSON(iri));
        builder.append("\", \"name\": \"");
        builder.append(TextUtils.escapeStringJSON(name));
        builder.append("\", \"classes\": [");
        serialize(builder, proxyClasses);
        builder.append("], \"datatypes\": [");
        serialize(builder, proxyDatatypes);
        builder.append("], \"objectProperties\": [");
        serialize(builder, proxyObjectProperties);
        builder.append("], \"dataProperties\": [");
        serialize(builder, proxyDataProperties);
        builder.append("], \"individuals\": [");
        serialize(builder, proxyIndividuals);
        builder.append("]}");
        return builder.toString();
    }

    /**
     * Serializes a collection or proxy objects
     *
     * @param builder The string builder to use
     * @param proxies The proxies
     */
    private void serialize(StringBuilder builder, Collection<ProxyObject> proxies) {
        boolean first = true;
        for (ProxyObject proxy : proxies) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append("{\"identifier\": \"");
            builder.append(TextUtils.escapeStringJSON(proxy.getIRIString()));
            builder.append("\", \"name\": \"");
            String label = (String) proxy.getDataValue(RDFS_LABEL);
            builder.append(TextUtils.escapeStringJSON(label != null ? label : proxy.getIRIString()));
            builder.append("\"}");
        }
    }
}
