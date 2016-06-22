/*******************************************************************************
 * Copyright (c) 2016 Association Cénotélie (cenotelie.fr)
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General
 * Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.xowl.platform.connectors.doors9.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.jobs.Job;
import org.xowl.platform.kernel.jobs.JobFactory;

/**
 * The factory for CSV importation jobs
 *
 * @author Elie Soubiran
 */
public class DOORS9ImportationJobFactory implements JobFactory {
    @Override
    public String getIdentifier() {
        return DOORS9ImportationJobFactory.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - DOORS 9 Importation Job Factory";
    }

    @Override
    public boolean canDeserialize(String type) {
        return (type.equals(DOORS9ImportationJob.class.getCanonicalName()));
    }

    @Override
    public Job newJob(String type, ASTNode definition) {
        if (type.equals(DOORS9ImportationJob.class.getCanonicalName()))
            return new DOORS9ImportationJob(definition);
        return null;
    }
}
