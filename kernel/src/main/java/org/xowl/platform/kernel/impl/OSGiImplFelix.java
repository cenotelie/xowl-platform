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

import org.xowl.infra.utils.Files;
import org.xowl.infra.utils.config.Configuration;
import org.xowl.infra.utils.logging.Logging;
import org.xowl.platform.kernel.Env;
import org.xowl.platform.kernel.jobs.JobExecutionService;
import org.xowl.platform.kernel.platform.PlatformRebootJob;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

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
    public void enforceHttpConfig(Configuration platformConfiguration, JobExecutionService executionService) {
        File root = new File(System.getProperty(Env.ROOT));
        File confDir = new File(root, "conf");
        File confFile = new File(confDir, "config.properties");
        Configuration felixConfiguration = new Configuration();
        try {
            felixConfiguration.load(confFile.getAbsolutePath(), Files.CHARSET);
        } catch (IOException exception) {
            Logging.getDefault().error(exception);
            return;
        }
        boolean mustReboot = false;

        // Felix default for org.apache.felix.http.enable is true
        String valueTarget = platformConfiguration.get("httpEnabled");
        String valueReal = felixConfiguration.get("org.apache.felix.http.enable");
        if ("true".equalsIgnoreCase(valueTarget)) {
            if (valueReal != null && "false".equalsIgnoreCase(valueReal)) {
                // must enable HTTP
                felixConfiguration.set("org.apache.felix.http.enable", "true");
                mustReboot = true;
            }
        } else {
            if (valueReal == null || "true".equalsIgnoreCase(valueReal)) {
                // must disable HTTP
                felixConfiguration.set("org.apache.felix.http.enable", "false");
                mustReboot = true;
            }
        }

        // Felix default for org.apache.felix.https.enable is false
        valueTarget = platformConfiguration.get("httpsEnabled");
        valueReal = felixConfiguration.get("org.apache.felix.https.enable");
        if ("true".equalsIgnoreCase(valueTarget)) {
            if (valueReal == null || "false".equalsIgnoreCase(valueReal)) {
                // must enable HTTPS
                felixConfiguration.set("org.apache.felix.https.enable", "true");
                mustReboot = true;
            }
        } else {
            if (valueReal != null && "true".equalsIgnoreCase(valueReal)) {
                // must disable HTTPS
                felixConfiguration.set("org.apache.felix.https.enable", "false");
                mustReboot = true;
            }
        }

        // Felix default for org.osgi.service.http.port is 8080
        valueTarget = platformConfiguration.get("httpPort");
        valueReal = felixConfiguration.get("org.osgi.service.http.port");
        if ((valueReal == null && !"8080".equals(valueTarget))
                || (!Objects.equals(valueReal, valueTarget))) {
            // must update port
            felixConfiguration.set("org.osgi.service.http.port", valueTarget);
            mustReboot = true;
        }

        // Felix default for org.osgi.service.http.port.secure is 8443
        valueTarget = platformConfiguration.get("httpsPort");
        valueReal = felixConfiguration.get("org.osgi.service.http.port.secure");
        if ((valueReal == null && !"8443".equals(valueTarget))
                || (!Objects.equals(valueReal, valueTarget))) {
            // must update port
            felixConfiguration.set("org.osgi.service.http.port.secure", valueTarget);
            mustReboot = true;
        }

        valueTarget = platformConfiguration.get("tlsKeyStore");
        valueReal = felixConfiguration.get("org.apache.felix.https.keystore");
        if (!Objects.equals(valueReal, valueTarget)) {
            felixConfiguration.set("org.apache.felix.https.keystore", valueTarget);
            mustReboot = true;
        }

        valueTarget = platformConfiguration.get("tlsKeyPassword");
        valueReal = felixConfiguration.get("org.apache.felix.https.keystore.password");
        if (!Objects.equals(valueReal, valueTarget)) {
            felixConfiguration.set("org.apache.felix.https.keystore.password", valueTarget);
            felixConfiguration.set("org.apache.felix.https.keystore.key.password", valueTarget);
            mustReboot = true;
        }

        if (mustReboot) {
            try {
                felixConfiguration.save(confFile.getAbsolutePath(), Files.CHARSET);
            } catch (IOException exception) {
                Logging.getDefault().error(exception);
                return;
            }
            executionService.schedule(new PlatformRebootJob());
        }
    }
}
