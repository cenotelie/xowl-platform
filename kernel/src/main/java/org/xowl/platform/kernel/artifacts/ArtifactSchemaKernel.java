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

package org.xowl.platform.kernel.artifacts;

import org.xowl.platform.kernel.KernelSchema;

/**
 * The schema for the kernel data for the platform, including the metadata of artifacts
 *
 * @author Laurent Wouters
 */
public class ArtifactSchemaKernel extends ArtifactSchemaFromResource {
    /**
     * The instance for this schema
     */
    public static final ArtifactSchema INSTANCE = new ArtifactSchemaKernel();

    /**
     * Initializes this schema
     */
    private ArtifactSchemaKernel() {
        super(KernelSchema.URI_KERNEL, "xOWL - Kernel Schema", true, KernelSchema.class, "/org/xowl/platform/kernel/schema.fs");
    }
}
