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

package org.xowl.platform.connectors.doors9;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.services.importation.ImporterConfiguration;

/**
 * Configuration for the DOORS 9 importer
 *
 * @author Elie Soubiran
 */
public class DOORS9Configuration extends ImporterConfiguration {
    /**
     * Initializes this configuration
     *
     * @param family     The base URI of the artifact family
     * @param superseded The URI of the superseded artifacts
     * @param version    The version number of the artifact
     * @param archetype  The artifact archetype
     */
    public DOORS9Configuration(String family, String[] superseded, String version, String archetype) {
        super(family, superseded, version, archetype);
    }

    /**
     * Initializes this configuration
     *
     * @param definition The definition of this configuration
     */
    public DOORS9Configuration(ASTNode definition) {
        super(definition);
    }
}
