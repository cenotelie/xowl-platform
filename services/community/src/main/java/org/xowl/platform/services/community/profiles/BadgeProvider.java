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

package org.xowl.platform.services.community.profiles;

import org.xowl.platform.kernel.Registrable;

import java.util.Collection;

/**
 * A provider of definitions for badges
 *
 * @author Laurent Wouters
 */
public interface BadgeProvider extends Registrable {
    /**
     * The MIME type for SVG images
     */
    String MIME_IMAGE_SVG = "image/svg+xml";

    /**
     * Gets the badges defined by this provider
     *
     * @return The definitions of badges
     */
    Collection<Badge> getBadges();

    /**
     * Gets the description for a specific badge
     *
     * @param badgeId The identifier of the badge
     * @return The badge, or null if it is not defined by this provider
     */
    Badge getBadge(String badgeId);
}
