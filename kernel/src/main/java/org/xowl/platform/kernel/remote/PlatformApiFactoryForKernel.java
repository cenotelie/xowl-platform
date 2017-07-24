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

package org.xowl.platform.kernel.remote;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.api.ApiDeserializer;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.artifacts.*;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobRemote;
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.*;

/**
 * Implements a factory for the kernel platform objects
 *
 * @author Laurent Wouters
 */
public class PlatformApiFactoryForKernel implements PlatformApiFactory {
    @Override
    public String getIdentifier() {
        return PlatformApiFactoryForKernel.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Kernel Objects Factory";
    }

    @Override
    public Object newObject(ApiDeserializer deserializer, String type, ASTNode definition) {
        if (Product.class.getCanonicalName().equals(type))
            return new ProductBase(definition);
        if (Addon.class.getCanonicalName().equals(type))
            return new Addon(definition);
        if (PlatformUser.class.getCanonicalName().equals(type))
            return new PlatformUserRemote(definition);
        if (PlatformGroup.class.getCanonicalName().equals(type))
            return new PlatformGroupRemote(definition);
        if (PlatformRole.class.getCanonicalName().equals(type))
            return new PlatformRoleBase(definition);
        if (SecuredAction.class.getCanonicalName().equals(type))
            return new SecuredAction(definition);
        if (SecuredActionPolicyDescriptor.class.getCanonicalName().equals(type))
            return new SecuredActionPolicyDescriptor(definition);
        if (SecuredActionPolicy.class.getCanonicalName().equals(type))
            return SecuredActionPolicyBase.load(definition);
        if (SecurityPolicyConfiguration.class.getCanonicalName().equals(type))
            return new SecurityPolicyConfiguration(definition);
        if (SecuredResourceDescriptor.class.getCanonicalName().equals(type))
            return new SecuredResourceDescriptorBase(definition);
        if (Job.class.getCanonicalName().equals(type))
            return new JobRemote(definition, deserializer);
        if (Artifact.class.getCanonicalName().equals(type))
            return new ArtifactRemote(definition);
        if (ArtifactSpecification.class.getCanonicalName().equals(type))
            return new ArtifactSpecification(definition);
        if (ArtifactArchetype.class.getCanonicalName().equals(type))
            return new ArtifactArchetypeRemote(definition);
        if (ArtifactSchema.class.getCanonicalName().equals(type))
            return new ArtifactSchemaRemote(definition);
        return null;
    }
}
