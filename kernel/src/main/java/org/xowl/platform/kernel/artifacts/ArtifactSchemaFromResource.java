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

import fr.cenotelie.commons.utils.AutoReader;
import fr.cenotelie.commons.utils.logging.Logging;
import org.xowl.infra.store.Repository;
import org.xowl.infra.store.RepositoryRDF;
import org.xowl.infra.store.rdf.Quad;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * Implements a business schema that is backed by a resource file
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaFromResource extends ArtifactSchemaBase {
    /**
     * A type in the same package as the resource
     */
    private final Class<?> loaderType;
    /**
     * The resource URI, e.g. /com/business/serious/schema.fs
     */
    private final String loaderResource;
    /**
     * The original quads for the definition
     */
    private Collection<Quad> quads;

    /**
     * Initializes this schema
     *
     * @param iri        The iri for this schema
     * @param name       The name for this schema
     * @param deployable Whether this schema can be deployed in a triple store
     * @param loaderType A type in the same package as the resource
     * @param resource   The resource URI, e.g. /com/business/serious/schema.fs
     */
    public ArtifactSchemaFromResource(String iri, String name, boolean deployable, Class<?> loaderType, String resource) {
        super(iri, name, deployable);
        this.loaderType = loaderType;
        this.loaderResource = resource;
    }

    @Override
    public Collection<Quad> getDefinition(boolean deployable) {
        if (!deployable)
            return doGetDefinition();
        return toDeployable(doGetDefinition());
    }

    private synchronized Collection<Quad> doGetDefinition() {
        if (quads != null)
            return Collections.unmodifiableCollection(quads);

        RepositoryRDF repository = new RepositoryRDF();
        try (InputStream stream = loaderType.getResourceAsStream(loaderResource)) {
            Reader reader = new AutoReader(stream);
            repository.load(Logging.get(), reader, identifier, identifier, Repository.getSyntax(loaderResource));
        } catch (Exception exception) {
            Logging.get().error(exception);
            return Collections.emptyList();
        }
        quads = new ArrayList<>();
        Iterator<Quad> iterator = repository.getStore().getAll();
        while (iterator.hasNext())
            quads.add(iterator.next());
        return Collections.unmodifiableCollection(quads);
    }
}
