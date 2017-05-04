/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.collaboration;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.remote.Deserializer;
import org.xowl.platform.kernel.remote.DeserializerFactory;
import org.xowl.platform.kernel.remote.DeserializerFactoryForKernel;
import org.xowl.platform.services.collaboration.impl.RemoteCollaborationBase;

/**
 * Implements a factory for the collaboration objects
 *
 * @author Laurent Wouters
 */
public class DeserializerFactoryForCollaboration implements DeserializerFactory {
    @Override
    public String getIdentifier() {
        return DeserializerFactoryForKernel.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Collaboration Objects Factory";
    }

    @Override
    public Object newObject(String type, ASTNode definition) {
        return newObject(null, type, definition);
    }

    @Override
    public Object newObject(Deserializer deserializer, String type, ASTNode definition) {
        if (CollaborationManifest.class.getCanonicalName().equals(type))
            return new CollaborationManifest(definition);
        if (CollaborationPatternDescriptor.class.getCanonicalName().equals(type))
            return new CollaborationPatternDescriptor(definition);
        if (CollaborationPattern.class.getCanonicalName().equals(type))
            return new CollaborationPatternBase(definition);
        if (CollaborationSpecification.class.getCanonicalName().equals(type))
            return new CollaborationSpecification(definition);
        if (RemoteCollaboration.class.getCanonicalName().equals(type))
            return new RemoteCollaborationBase(definition);
        return null;
    }
}
