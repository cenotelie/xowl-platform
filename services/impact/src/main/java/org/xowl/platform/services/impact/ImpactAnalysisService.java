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

package org.xowl.platform.services.impact;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.webapi.HttpApiService;

/**
 * Represents a service for impact analyses
 * The perform method launches an impact analysis. On an successful launch, the XSPReply contains the job that was launched.
 * The result of the analysis will be provided as the job's result upon completion.
 *
 * @author Laurent Wouters
 */
public interface ImpactAnalysisService extends Service, HttpApiService {
    /**
     * Performs an impact analysis
     *
     * @param setup The setup for the analysis
     * @return The response, that encapsulate the spawned job in case of success
     */
    XSPReply perform(ImpactAnalysisSetup setup);
}
