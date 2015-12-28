/*******************************************************************************
 * Copyright (c) 2015 Laurent Wouters
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
 *
 * Contributors:
 *     Laurent Wouters - lwouters@xowl.org
 ******************************************************************************/

package org.xowl.platform.services.workflow;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.xowl.platform.kernel.ServiceHttpServed;
import org.xowl.platform.services.workflow.impl.XOWLWorkflowFactoryService;
import org.xowl.platform.services.workflow.impl.XOWLWorkflowService;

import java.util.Hashtable;

/**
 * Activator for the workflow service bundle
 *
 * @author Laurent Wouters
 */
public class Activator implements BundleActivator {
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        XOWLWorkflowService service = new XOWLWorkflowService();
        Hashtable<String, Object> properties = new Hashtable<>();
        properties.put("id", service.getIdentifier());
        properties.put("uri", "workflow");
        bundleContext.registerService(WorkflowService.class, service, properties);
        bundleContext.registerService(ServiceHttpServed.class, service, properties);

        bundleContext.registerService(WorkflowFactoryService.class, new XOWLWorkflowFactoryService(), null);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
