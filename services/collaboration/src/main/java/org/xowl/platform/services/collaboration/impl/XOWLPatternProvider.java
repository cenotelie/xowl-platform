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

package org.xowl.platform.services.collaboration.impl;

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.collaboration.CollaborationPattern;
import org.xowl.platform.services.collaboration.CollaborationPatternDescriptor;
import org.xowl.platform.services.collaboration.CollaborationPatternFreeStyle;
import org.xowl.platform.services.collaboration.CollaborationPatternProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * The default provider of implementations for collaboration patterns
 *
 * @author Laurent Wouters
 */
public class XOWLPatternProvider implements CollaborationPatternProvider {
    @Override
    public String getIdentifier() {
        return XOWLPatternProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Collaboration Pattern Provider";
    }

    @Override
    public Collection<CollaborationPatternDescriptor> getPatterns() {
        return Collections.singletonList(CollaborationPatternFreeStyle.DESCRIPTOR);
    }

    @Override
    public CollaborationPattern instantiate(String identifier) {
        if (CollaborationPatternFreeStyle.class.getCanonicalName().equals(identifier))
            return new CollaborationPatternFreeStyle();
        return null;
    }

    @Override
    public CollaborationPattern instantiate(CollaborationPatternDescriptor descriptor) {
        return descriptor == null ? null : instantiate(descriptor.getIdentifier());
    }
}
