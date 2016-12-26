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

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyException;
import org.xowl.infra.server.xsp.XSPReplyNotFound;
import org.xowl.infra.utils.TextUtils;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
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
    private XSPReply result;

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
    public XSPReply getResult() {
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
    public static XSPReply installAddon(String addonId) {
        MarketplaceService marketplaceService = ServiceUtils.getService(MarketplaceService.class);
        if (marketplaceService == null)
            return XSPReplyServiceUnavailable.instance();
        PlatformManagementService platformManagementService = ServiceUtils.getService(PlatformManagementService.class);
        if (platformManagementService == null)
            return XSPReplyServiceUnavailable.instance();

        try (InputStream stream = marketplaceService.getAddonPackage(addonId)) {
            if (stream == null)
                return XSPReplyNotFound.instance();
            return platformManagementService.installAddon(addonId, stream);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyException(exception);
        }
    }
}
