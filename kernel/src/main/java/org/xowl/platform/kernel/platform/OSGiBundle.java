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

package org.xowl.platform.kernel.platform;

import org.osgi.framework.Bundle;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.Identifiable;

/**
 * Encapsulates an OSGi bundle at runtime
 *
 * @author Laurent Wouters
 */
public class OSGiBundle implements Identifiable, Serializable {
    /**
     * The encapsulated OSGi bundle
     */
    private final Bundle bundle;

    /**
     * Initializes this bundle
     *
     * @param bundle The encapsulated bundle
     */
    public OSGiBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    /**
     * Gets the version for this bundle
     *
     * @return The version for this bundle
     */
    public String getVersion() {
        return bundle.getVersion().toString();
    }

    /**
     * Gets the state of this bundle
     *
     * @return The state of this bundle
     */
    public OSGiBundleState getState() {
        switch (bundle.getState()) {
            case Bundle.UNINSTALLED:
                return OSGiBundleState.UNINSTALLED;
            case Bundle.INSTALLED:
                return OSGiBundleState.INSTALLED;
            case Bundle.RESOLVED:
                return OSGiBundleState.RESOLVED;
            case Bundle.STARTING:
                return OSGiBundleState.STARTING;
            case Bundle.STOPPING:
                return OSGiBundleState.STOPPING;
            case Bundle.ACTIVE:
                return OSGiBundleState.ACTIVE;
        }
        return OSGiBundleState.UNINSTALLED;
    }

    /**
     * Gets the vendor for this bundle
     *
     * @return The vendor for this bundle
     */
    public String getVendor() {
        String value = (String) bundle.getHeaders().get("Bundle-Vendor");
        if (value == null)
            return "";
        if (value.startsWith("%"))
            return value.substring(1);
        return value;
    }

    /**
     * Gets the description for this bundle
     *
     * @return The description for this bundle
     */
    public String getDescription() {
        String value = (String) bundle.getHeaders().get("Bundle-Description");
        if (value == null)
            return "";
        if (value.startsWith("%"))
            return value.substring(1);
        return value;
    }

    @Override
    public String getIdentifier() {
        return bundle.getSymbolicName() != null ? bundle.getSymbolicName() : "";
    }

    @Override
    public String getName() {
        String value = (String) bundle.getHeaders().get("Bundle-Name");
        if (value == null)
            return "";
        if (value.startsWith("%"))
            return value.substring(1);
        return value;
    }

    @Override
    public String serializedString() {
        return bundle.getSymbolicName();
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(OSGiBundle.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(getIdentifier()) +
                "\", \"name\": \"" +
                TextUtils.escapeStringJSON(getName()) +
                "\", \"description\": \"" +
                TextUtils.escapeStringJSON(getDescription()) +
                "\", \"vendor\": \"" +
                TextUtils.escapeStringJSON(getVendor()) +
                "\", \"type\": \"" +
                TextUtils.escapeStringJSON(OSGiBundle.class.getCanonicalName()) +
                "\", \"version\": \"" +
                TextUtils.escapeStringJSON(getVersion()) +
                "\", \"state\": \"" +
                TextUtils.escapeStringJSON(getState().toString()) +
                "\"}";
    }
}
