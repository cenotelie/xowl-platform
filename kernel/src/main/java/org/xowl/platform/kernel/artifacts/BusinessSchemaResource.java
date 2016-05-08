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
import org.xowl.infra.store.*;
import org.xowl.infra.store.storage.StoreFactory;
import org.xowl.infra.utils.logging.Logger;

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
public class BusinessSchemaResource implements BusinessSchema {
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
    private final Repository repository;
    /**
     * The loaded ontology
     */
    private final Ontology ontology;
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
    public BusinessSchemaResource(Class<?> type, String resource, String iri) {
        this.iri = iri;
        this.repository = new Repository(StoreFactory.create().inMemory().make());
        InputStream stream = type.getResourceAsStream(resource);
        if (stream != null) {
            this.ontology = this.repository.loadResource(Logger.DEFAULT,
                    new InputStreamReader(stream),
                    AbstractRepository.SCHEME_RESOURCE + resource,
                    iri, AbstractRepository.getSyntax(resource));
        } else {
            this.ontology = null;
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
        builder.append(IOUtils.escapeStringJSON(BusinessSchema.class.getCanonicalName()));
        builder.append("\", \"id\": \"");
        builder.append(IOUtils.escapeStringJSON(iri));
        builder.append("\", \"name\": \"");
        builder.append(IOUtils.escapeStringJSON(name));
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
            builder.append("{\"id\": \"");
            builder.append(IOUtils.escapeStringJSON(proxy.getIRIString()));
            builder.append("\", \"name\": \"");
            String label = (String) proxy.getDataValue(RDFS_LABEL);
            builder.append(IOUtils.escapeStringJSON(label != null ? label : proxy.getIRIString()));
            builder.append("\"}");
        }
    }
}
