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

import org.xowl.infra.server.xsp.XSPReplyFailure;
import org.xowl.infra.server.xsp.XSPReplySuccess;
import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.jobs.JobExecutionService;

import java.io.File;
import java.io.IOException;

/**
 * Represents the Felix implementation of OSGi the platform is running on
 *
 * @author Laurent Wouters
 */
class OSGiImplFelix extends OSGiImpl {
    @Override
    public String getIdentifier() {
        return OSGiImplFelix.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "Apache Felix";
    }

    @Override
    public void enforceHttpConfig(Configuration configuration, JobExecutionService executionService) {
        File root = new File(System.getProperty(Env.ROOT));
        File confDir = new File(root, "conf");
        File confFile = new File(confDir, "config.properties");
        Configuration platformConfig = new Configuration();
        try {
            platformConfig.load(confFile.getAbsolutePath(), Files.CHARSET);
            String value = platformConfig.get("org.osgi.service.http.port.secure");
            if (value == null)
                platformConfig.set("org.osgi.service.http.port.secure", "8443");
            value = platformConfig.get("org.apache.felix.https.enable");
            if (value == null || "false".equalsIgnoreCase(value))
                platformConfig.set("org.apache.felix.https.enable", "true");
            platformConfig.set("org.apache.felix.https.keystore", keyStore.getAbsolutePath());
            platformConfig.set("org.apache.felix.https.keystore.password", password);
            platformConfig.set("org.apache.felix.https.keystore.key.password", password);
            platformConfig.save(confFile.getAbsolutePath(), Files.CHARSET);
            return XSPReplySuccess.instance();
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return new XSPReplyFailure(exception.getMessage());
        }
    }
}
