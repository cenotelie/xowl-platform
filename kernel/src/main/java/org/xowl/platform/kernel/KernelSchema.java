/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.kernel;

/**
 * Schema URIs for the core of the platform
 *
 * @author Laurent Wouters
 */
public interface KernelSchema {
    /**
     * The base uri for the platform
     */
    String URI_BASE = "http://xowl.org/platform";
    /**
     * Base URI for all schemas
     */
    String URI_SCHEMAS = URI_BASE + "/schemas/";
    /**
     * The URI of the kernel schema
     */
    String URI_KERNEL = URI_SCHEMAS + "kernel";

    /**
     * URI of the artifact registry graphs
     */
    String GRAPH_ARTIFACTS = URI_BASE + "/artifacts";

    /**
     * The Resource concept
     */
    String RESOURCE = URI_KERNEL + "#Resource";
    /**
     * The Artifact concept
     */
    String ARTIFACT = URI_KERNEL + "#Artifact";
    /**
     * The Artifact concept
     */
    String USER = URI_KERNEL + "#User";

    /**
     * The base property
     */
    String BASE = URI_KERNEL + "#base";
    /**
     * The version property
     */
    String VERSION = URI_KERNEL + "#version";
    /**
     * The name property
     */
    String NAME = URI_KERNEL + "#name";
    /**
     * The from property
     */
    String FROM = URI_KERNEL + "#from";
    /**
     * The creator property
     */
    String CREATOR = URI_KERNEL + "#creator";
    /**
     * The created property
     */
    String CREATED = URI_KERNEL + "#created";
    /**
     * The modified property
     */
    String MODIFIED = URI_KERNEL + "#modified";
}
