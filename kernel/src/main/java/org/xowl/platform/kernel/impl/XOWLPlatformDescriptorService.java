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

package org.xowl.platform.kernel.impl;

import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.server.xsp.XSPReplyUnauthorized;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.platform.OSGiBundle;
import org.xowl.platform.kernel.platform.PlatformDescriptor;
import org.xowl.platform.kernel.platform.PlatformDescriptorService;
import org.xowl.platform.kernel.platform.PlatformUserRoleAdmin;
import org.xowl.platform.kernel.security.SecurityService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Implements the platform descriptor service
 *
 * @author Laurent Wouters
 */
public class XOWLPlatformDescriptorService implements PlatformDescriptorService {
    /**
     * The URIs for this service
     */
    private static final String[] URIS = new String[]{
            "services/admin/platform"
    };

    /**
     * Initializes this service
     */
    public XOWLPlatformDescriptorService() {

    }

    @Override
    public String getIdentifier() {
        return XOWLSecurityService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Platform Descriptor Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIS);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        SecurityService securityService = ServiceUtils.getService(SecurityService.class);
        if (securityService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyUnauthorized.instance(), null);
        if (!securityService.checkCurrentHasRole(PlatformUserRoleAdmin.INSTANCE.getIdentifier()))
            return XSPReplyUtils.toHttpResponse(XSPReplyUnauthorized.instance(), null);
        return XSPReplyUtils.toHttpResponse(new XSPReplyResultCollection<>(getPlatformBundles()), null);
    }

    @Override
    public Collection<OSGiBundle> getPlatformBundles() {
        return PlatformDescriptor.INSTANCE.getPlatformBundles();
    }
}
