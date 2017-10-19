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

package org.xowl.platform.services.marketplace.jobs;

import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.commons.utils.api.ReplyException;
import fr.cenotelie.commons.utils.api.ReplyNotFound;
import fr.cenotelie.commons.utils.logging.Logging;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.jobs.JobBase;
import org.xowl.platform.kernel.platform.PlatformManagementService;
import org.xowl.platform.services.marketplace.MarketplaceService;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implements a job for the installation of an addon
 *
 * @author Laurent Wouters
 */
public class AddonInstallationJob extends JobBase {
    /**
     * The identifier of the addon to install
     */
    private final String addonId;
    /**
     * The job's result
     */
    private Reply result;

    /**
     * Initializes this job
     *
     * @param addonId The identifier of the addon to install
     */
    public AddonInstallationJob(String addonId) {
        this(AddonInstallationJob.class.getCanonicalName(), addonId);
    }

    /**
     * Initializes this job
     *
     * @param type    The custom type of this job
     * @param addonId The identifier of the addon to install
     */
    public AddonInstallationJob(String type, String addonId) {
        super("Install addon " + addonId, type);
        this.addonId = addonId;
    }

    /**
     * Initializes this job
     *
     * @param definition The job's definition
     */
    public AddonInstallationJob(ASTNode definition) {
        super(definition);
        String connector = TextUtils.unescape(getPayloadNode(definition).getValue());
        this.addonId = connector.substring(1, connector.length() - 1);
    }

    @Override
    protected String getJSONSerializedPayload() {
        return "\"" + addonId + "\"";
    }

    @Override
    public Reply getResult() {
        return result;
    }

    @Override
    public void doRun() {
        result = installAddon(addonId);
    }

    /**
     * Installs on this platform the addon identifier by the specified addin identifier
     *
     * @param addonId The identifier of the addon to install
     * @return The protocol reply
     */
    public static Reply installAddon(String addonId) {
        MarketplaceService marketplaceService = Register.getComponent(MarketplaceService.class);
        if (marketplaceService == null)
            return ReplyServiceUnavailable.instance();
        PlatformManagementService platformManagementService = Register.getComponent(PlatformManagementService.class);
        if (platformManagementService == null)
            return ReplyServiceUnavailable.instance();

        try (InputStream stream = marketplaceService.getAddonPackage(addonId)) {
            if (stream == null)
                return ReplyNotFound.instance();
            return platformManagementService.installAddon(addonId, stream);
        } catch (IOException exception) {
            Logging.get().error(exception);
            return new ReplyException(exception);
        }
    }
}
