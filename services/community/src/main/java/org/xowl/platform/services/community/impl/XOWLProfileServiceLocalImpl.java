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

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.SHA1;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.config.Section;
import fr.cenotelie.commons.utils.json.Json;
import fr.cenotelie.commons.utils.logging.BufferedLogger;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.kernel.events.EventService;
import org.xowl.platform.kernel.security.SecuredAction;
import org.xowl.platform.services.community.profiles.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the profile service that locally stores the profiles
 *
 * @author Laurent Wouters
 */
public class XOWLProfileServiceLocalImpl implements ProfileService, BadgeProvider {
    /**
     * The storage location for the profiles data
     */
    private final File storage;
    /**
     * The map of the loaded profiles
     */
    private final Map<String, PublicProfile> publicProfiles;

    /**
     * Initializes this service
     *
     * @param configuration The configuration for this service
     */
    public XOWLProfileServiceLocalImpl(Section configuration) {
        this.storage = PlatformUtils.resolve(configuration.get("storage"));
        this.publicProfiles = new HashMap<>();
    }

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
        synchronized (publicProfiles) {
            PublicProfile profile = publicProfiles.get(identifier);
            if (profile != null)
                return profile;
            if (!storage.exists())
                return null;
            File file = new File(storage, getFileNameFor(identifier));
            if (!file.exists())
                return null;
            try (Reader reader = IOUtils.getReader(file)) {
                BufferedLogger logger = new BufferedLogger();
                ASTNode root = Json.parse(logger, reader);
                if (root == null)
                    return null;
                profile = new PublicProfile(root, this);
                publicProfiles.put(identifier, profile);
                return profile;
            } catch (IOException exception) {
                Logging.get().error(exception);
                return null;
            }
        }
    }

    @Override
    public Reply updatePublicProfile(PublicProfile profile) {
        PublicProfile target = resolveProfile(profile.getIdentifier());
        target.update(profile);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new PublicProfileUpdatedEvent(profile));
        return writeBack(target);
    }

    @Override
    public Collection<Badge> getBadges() {
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
    public Reply awardBadge(String userId, String badgeId) {
        Badge badge = getBadge(badgeId);
        if (badge == null)
            return ReplyNotFound.instance();
        PublicProfile target = resolveProfile(userId);
        target.awardBadge(badge);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new BadgeAwardedEvent(target, badge));
        return writeBack(target);
    }

    @Override
    public Reply rescindBadge(String userId, String badgeId) {
        Badge badge = getBadge(badgeId);
        if (badge == null)
            return ReplyNotFound.instance();
        PublicProfile target = resolveProfile(userId);
        target.rescindBadge(badge);
        EventService eventService = Register.getComponent(EventService.class);
        if (eventService != null)
            eventService.onEvent(new BadgeRescindedEvent(target, badge));
        return writeBack(target);
    }

    /**
     * Resolves a profile for the specified identifier
     *
     * @param identifier The identifier of a profile
     * @return The profile
     */
    private PublicProfile resolveProfile(String identifier) {
        synchronized (publicProfiles) {
            PublicProfile target = publicProfiles.get(identifier);
            if (target != null)
                return target;
            target = new PublicProfile(identifier);
            publicProfiles.put(identifier, target);
            return target;
        }
    }

    /**
     * Writes back the specified profile to disk
     *
     * @param profile The profile to write
     * @return The protocol reply
     */
    private Reply writeBack(PublicProfile profile) {
        if (!storage.exists()) {
            if (!storage.mkdirs())
                return new ReplyApiError(ArtifactStorageService.ERROR_STORAGE_FAILED);
        }
        File file = new File(storage, getFileNameFor(profile.getIdentifier()));
        try (Writer writer = IOUtils.getWriter(file)) {
            writer.write(profile.serializedJSON());
            writer.flush();
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyException(exception);
        }
        return ReplySuccess.instance();
    }

    /**
     * Gets the name of the file for a profile
     *
     * @param profileId The identifier of a profile
     * @return The associated file name
     */
    private static String getFileNameFor(String profileId) {
        return "profile-" + SHA1.hashSHA1(profileId) + ".json";
    }
}
