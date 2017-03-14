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

package org.xowl.platform.services.community.impl;

import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.services.community.profiles.Badge;
import org.xowl.platform.services.community.profiles.BadgeProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * Implements the provider for the default badges on the platform
 *
 * @author Laurent Wouters
 */
public class XOWLDefaultBadges implements BadgeProvider {
    /**
     * A test badge
     */
    private static final Badge BADGE_TEST = new Badge(
            XOWLDefaultBadges.class.getCanonicalName() + ".TestBadge",
            "Test Badge",
            "This is a test badge!",
            MIME_IMAGE_SVG,
            XOWLDefaultBadges.class,
            "/org/xowl/platform/services/community/badge-test.svg");

    @Override
    public String getIdentifier() {
        return XOWLDefaultBadges.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Default Badge Provider";
    }

    @Override
    public Collection<Badge> getBadges() {
        return Collections.singletonList(BADGE_TEST);
    }

    @Override
    public Badge getBadge(String badgeId) {
        if (BADGE_TEST.getIdentifier().equals(badgeId))
            return BADGE_TEST;
        return null;
    }
}
