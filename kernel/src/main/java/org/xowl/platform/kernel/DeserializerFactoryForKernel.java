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

package org.xowl.platform.kernel;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.product.Product;
import org.xowl.platform.kernel.platform.*;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.kernel.security.SecuredActionPolicyDescriptor;

/**
 * Implements a factory for the kernel platform objects
 *
 * @author Laurent Wouters
 */
public class DeserializerFactoryForKernel implements DeserializerFactory {
    @Override
    public String getIdentifier() {
        return DeserializerFactoryForKernel.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Kernel Objects Factory";
    }

    @Override
    public Object newObject(String type, ASTNode definition) {
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
        return null;
    }
}
