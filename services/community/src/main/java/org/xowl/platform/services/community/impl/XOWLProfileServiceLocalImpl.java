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

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyUnsupported;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.services.community.profiles.Badge;
import org.xowl.platform.services.community.profiles.BadgeProvider;
import org.xowl.platform.services.community.profiles.ProfileService;
import org.xowl.platform.services.community.profiles.PublicProfile;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of the profile service that locally stores the profiles
 *
 * @author Laurent Wouters
 */
public class XOWLProfileServiceLocalImpl implements ProfileService {

    @Override
    public String getIdentifier() {
        return XOWLProfileServiceLocalImpl.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Profile Service (Local Impl)";
    }

    @Override
    public SecuredAction[] getActions() {
        return ProfileService.ACTIONS;
    }

    @Override
    public PublicProfile getPublicProfile(String identifier) {
        return null;
    }

    @Override
    public XSPReply updatePublicProfile(PublicProfile profile) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public Collection<Badge> getAllBadges() {
        Collection<Badge> result = new ArrayList<>();
        for (BadgeProvider provider : Register.getComponents(BadgeProvider.class)) {
            result.addAll(provider.getBadges());
        }
        return result;
    }

    @Override
    public Badge getBadge(String badgeId) {
        for (BadgeProvider provider : Register.getComponents(BadgeProvider.class)) {
            Badge badge = provider.getBadge(badgeId);
            if (badge != null)
                return badge;
        }
        return null;
    }

    @Override
    public XSPReply awardBadge(String userId, String badgeId) {
        return XSPReplyUnsupported.instance();
    }

    @Override
    public XSPReply rescindBadge(String userId, String badgeId) {
        return XSPReplyUnsupported.instance();
    }
}
