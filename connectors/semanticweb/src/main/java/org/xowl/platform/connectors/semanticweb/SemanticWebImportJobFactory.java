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

package org.xowl.platform.connectors.semanticweb;

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobFactory;

/**
 * The factory for Semantic Web dataset importation jobs
 *
 * @author Laurent Wouters
 */
public class SemanticWebImportJobFactory implements JobFactory {
    @Override
    public String getIdentifier() {
        return SemanticWebImportJobFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Semantic Web Importation Job Factory";
    }

    @Override
    public Job newJob(String type, ASTNode definition) {
        if (type.equals(SemanticWebImportJob.class.getCanonicalName()))
            return new SemanticWebImportJob(SemanticWebImporter.INSTANCE, definition);
        return null;
    }
}
