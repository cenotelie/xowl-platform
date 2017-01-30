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

package org.xowl.platform.kernel;

/**
 * Schema URIs for the core of the platform
 *
 * @author Laurent Wouters
 */
public interface KernelSchema {
    /**
     * URI of the artifact registry graphs
     */
    String GRAPH_ARTIFACTS = "http://xowl.org/platform/artifacts";

    /**
     * The URI of the kernel schema
     */
    String URI_KERNEL = "http://xowl.org/platform/kernel";

    /**
     * The Resource concept
     */
    String RESOURCE = URI_KERNEL + "#Resource";
    /**
     * The Artifact concept
     */
    String ARTIFACT = URI_KERNEL + "#Artifact";
    /**
     * The PlatformUser concept
     */
    String PLATFORM_USER = URI_KERNEL + "#PlatformUser";
    /**
     * The PlatformUserGroup concept
     */
    String PLATFORM_USER_GROUP = URI_KERNEL + "#PlatformUserGroup";
    /**
     * The PlatformUserRole concept
     */
    String PLATFORM_USER_ROLE = URI_KERNEL + "#PlatformUserRole";


    /**
     * The name property
     */
    String NAME = URI_KERNEL + "#name";
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

    /**
     * The from property
     */
    String FROM = URI_KERNEL + "#from";
    /**
     * The base property
     */
    String BASE = URI_KERNEL + "#base";
    /**
     * The supersede property
     */
    String SUPERSEDE = URI_KERNEL + "#supersede";
    /**
     * The version property
     */
    String VERSION = URI_KERNEL + "#version";
    /**
     * The archetype property
     */
    String ARCHETYPE = URI_KERNEL + "#archetype";

    /**
     * The hasMember property
     */
    String HAS_MEMBER = URI_KERNEL + "#hasMember";
    /**
     * The hasAdmin property
     */
    String HAS_ADMIN = URI_KERNEL + "#hasAdmin";
    /**
     * The hasRole property
     */
    String HAS_ROLE = URI_KERNEL + "#hasRole";
}
