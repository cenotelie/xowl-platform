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

package org.xowl.platform.services.workflow.impl;

import org.xowl.hime.redist.ASTNode;
import org.xowl.platform.kernel.*;
import org.xowl.platform.services.config.ConfigurationService;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.services.workflow.*;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.platform.utils.Utils;
import org.xowl.store.IOUtils;
import org.xowl.store.Vocabulary;
import org.xowl.store.rdf.IRINode;
import org.xowl.store.rdf.LiteralNode;
import org.xowl.store.rdf.Node;
import org.xowl.store.rdf.Quad;
import org.xowl.store.storage.NodeManager;
import org.xowl.store.storage.cache.CachedNodes;
import org.xowl.store.xsp.XSPReply;
import org.xowl.store.xsp.XSPReplyFailure;
import org.xowl.store.xsp.XSPReplyResult;
import org.xowl.store.xsp.XSPReplySuccess;
import org.xowl.utils.Files;
import org.xowl.utils.config.Configuration;
import org.xowl.utils.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Implements the default workflow service for the platform
 * This service uses a configuration element that must define a workflow through a JSON notation
 *
 * @author Laurent Wouters
 */
public class XOWLWorkflowService implements WorkflowService, ServiceHttpServed {
    /**
     * The identifier of the artifact that contains the data for this service
     */
    private static final String ARTIFACT_CONFIG = "http://xowl.org/platform/services/workflow/" + XOWLWorkflowService.class.getCanonicalName();
    /**
     * The property for the current phase
     */
    private static final String PROPERTY_CURRENT_PHASE = "http://xowl.org/platform/services/workflow#currentPhase";
    /**
     * The property for the current activity
     */
    private static final String PROPERTY_CURRENT_ACTIVITY = "http://xowl.org/platform/services/workflow#currentActivity";

    /**
     * Node manager for creating new artifacts
     */
    private final NodeManager nodeManager;
    /**
     * The workflow
     */
    private Workflow workflow;
    /**
     * The current phase
     */
    private WorkflowPhase currentPhase;
    /**
     * The current activity
     */
    private WorkflowActivity currentActivity;

    /**
     * Initializes this service
     */
    public XOWLWorkflowService() {
        nodeManager = new CachedNodes();
    }

    /**
     * Retrieve the workflow from the configuration
     */
    private void retrieveWorkflow() {
        if (workflow != null)
            return;
        ConfigurationService service = ServiceUtils.getService(ConfigurationService.class);
        if (service == null)
            return;
        Configuration configuration = service.getConfigFor(this);
        String file = configuration.get("processFile");
        if (file != null) {
            try (InputStream stream = new FileInputStream(service.resolve(file))) {
                String content = Files.read(stream, Utils.DEFAULT_CHARSET);
                ASTNode root = Utils.parseJSON(Logger.DEFAULT, content);
                workflow = new XOWLWorkflow(root);
                currentPhase = workflow.getPhases().get(0);
                currentActivity = currentPhase.getActivities().get(0);
                retrieveState();
            } catch (IOException exception) {
                Logger.DEFAULT.error(exception);
            }
        }
    }

    /**
     * Retrieves the current phase and activity
     */
    private void retrieveState() {
        TripleStoreService tripleStoreService = ServiceUtils.getService(TripleStoreService.class);
        if (tripleStoreService == null)
            return;
        TripleStore store = tripleStoreService.getServiceStore();
        if (store == null)
            return;
        Artifact artifact = store.retrieve(ARTIFACT_CONFIG);
        if (artifact == null)
            return;
        String phaseID = null;
        String activityID = null;
        for (Quad quad : artifact.getMetadata()) {
            if (quad.getProperty().getNodeType() == Node.TYPE_IRI) {
                if (PROPERTY_CURRENT_PHASE.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    phaseID = ((LiteralNode) quad.getObject()).getLexicalValue();
                } else if (PROPERTY_CURRENT_ACTIVITY.equals(((IRINode) quad.getProperty()).getIRIValue())) {
                    activityID = ((LiteralNode) quad.getObject()).getLexicalValue();
                }
            }
        }
        if (phaseID != null) {
            for (WorkflowPhase phase : workflow.getPhases()) {
                if (phaseID.equals(phase.getIdentifier())) {
                    currentPhase = phase;
                    currentActivity = phase.getActivities().get(0);
                    if (activityID != null) {
                        for (WorkflowActivity activity : phase.getActivities()) {
                            if (activityID.equals(activity.getIdentifier())) {
                                currentActivity = activity;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Pushes the current state
     */
    private void pushNewState() {
        TripleStoreService tripleStoreService = ServiceUtils.getService(TripleStoreService.class);
        if (tripleStoreService == null)
            return;
        TripleStore store = tripleStoreService.getServiceStore();
        if (store == null)
            return;
        IRINode graph = nodeManager.getIRINode(KernelSchema.GRAPH_ARTIFACTS);
        IRINode subject = nodeManager.getIRINode(ARTIFACT_CONFIG);
        IRINode propName = nodeManager.getIRINode(KernelSchema.NAME);
        IRINode propCurrentPhase = nodeManager.getIRINode(PROPERTY_CURRENT_PHASE);
        IRINode propCurrentActivity = nodeManager.getIRINode(PROPERTY_CURRENT_ACTIVITY);
        List<Quad> metadata = new ArrayList<>();
        metadata.add(new Quad(graph, subject, propName, nodeManager.getLiteralNode("State of XOWLWorkflowService", Vocabulary.xsdString, null)));
        metadata.add(new Quad(graph, subject, propCurrentPhase, nodeManager.getLiteralNode(currentPhase.getIdentifier(), Vocabulary.xsdString, null)));
        metadata.add(new Quad(graph, subject, propCurrentActivity, nodeManager.getLiteralNode(currentActivity.getIdentifier(), Vocabulary.xsdString, null)));
        Artifact artifact = new ArtifactSimple(metadata, new ArrayList<Quad>());
        store.delete(ARTIFACT_CONFIG);
        store.store(artifact);
    }

    /**
     * Advances in the workflow
     */
    private void workflowAdvance() {
        int indexPhase = workflow.getPhases().indexOf(currentPhase);
        int indexActivity = currentPhase.getActivities().indexOf(currentActivity);
        indexActivity++;
        if (indexActivity >= currentPhase.getActivities().size()) {
            indexPhase++;
            if (indexPhase < workflow.getPhases().size()) {
                currentPhase = workflow.getPhases().get(indexPhase);
                currentActivity = currentPhase.getActivities().get(0);
                pushNewState();
            }
        } else {
            currentActivity = currentPhase.getActivities().get(indexActivity);
            pushNewState();
        }
    }

    @Override
    public String getIdentifier() {
        return XOWLWorkflowService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Workflow Service";
    }

    @Override
    public String getProperty(String name) {
        if (name == null)
            return null;
        if ("identifier".equals(name))
            return getIdentifier();
        if ("name".equals(name))
            return getName();
        return null;
    }

    @Override
    public Workflow getCurrentWorkflow() {
        retrieveWorkflow();
        return workflow;
    }

    @Override
    public WorkflowPhase getActivePhase() {
        retrieveWorkflow();
        return currentPhase;
    }

    @Override
    public WorkflowActivity getActiveActivity() {
        retrieveWorkflow();
        return currentActivity;
    }

    @Override
    public XSPReply execute(WorkflowAction action, Object parameter) {
        WorkflowActivity activity = getActiveActivity();
        if (activity == null)
            return new XSPReplyFailure("The workflow is not configured");
        if (!activity.getActions().contains(action))
            return new XSPReplyFailure("This action is not available");
        JobExecutionService executor = ServiceUtils.getService(JobExecutionService.class);
        if (executor == null)
            return new XSPReplyFailure("Could not find the job execution service");
        WorkflowJob job = newJob(action.getName(), action);
        executor.schedule(job);
        return new XSPReplyResult<>(job);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (parameters == null || parameters.isEmpty()) {
            retrieveWorkflow();
            if (workflow == null)
                return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Workflow is not configured");
            return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, workflow.serializedJSON());
        }

        String[] values = parameters.get("action");
        if (values == null || values.length < 1)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Expected action parameter");
        String actionID = values[0];
        retrieveWorkflow();
        if (workflow == null)
            return new HttpResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, IOUtils.MIME_TEXT_PLAIN, "Workflow is not configured");

        String message = content == null ? null : new String(content, Utils.DEFAULT_CHARSET);
        for (WorkflowAction action : currentActivity.getActions()) {
            if (action.getIdentifier().equals(actionID)) {
                XSPReply reply = execute(action, message);
                if (reply.isSuccess())
                    return new HttpResponse(HttpURLConnection.HTTP_OK, IOUtils.MIME_JSON, reply.serializedJSON());
                else
                    return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, reply.serializedString());
            }
        }
        return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST, IOUtils.MIME_TEXT_PLAIN, "Action is unavailable");
    }

    /**
     * Creates a new action
     *
     * @param type           The type of action
     * @param jsonDefinition The definition of the action
     * @return The new action
     */
    public static WorkflowAction newAction(String type, ASTNode jsonDefinition) {
        Collection<WorkflowFactoryService> factories = ServiceUtils.getServices(WorkflowFactoryService.class);
        for (WorkflowFactoryService factory : factories) {
            if (factory.getActionTypes().contains(type))
                return factory.newAction(type, jsonDefinition);
        }
        return new XOWLWorkflowAction(jsonDefinition);
    }

    /**
     * Creates a new workflow job
     *
     * @param name   The job's name
     * @param action The associated action
     * @return The job
     */
    public WorkflowJob newJob(String name, WorkflowAction action) {
        return new WorkflowJob(name, action) {
            @Override
            public void onComplete() {
                if (action instanceof XOWLWorkflowAction && ((XOWLWorkflowAction) action).isFinishOnSuccess()) {
                    workflowAdvance();
                }
            }
        };
    }

    /**
     * Creates a new workflow job
     *
     * @param definition The job's definition
     * @param action     The associated action
     * @return The job
     */
    public WorkflowJob newJob(ASTNode definition, WorkflowAction action) {
        return new WorkflowJob(definition, action) {
            @Override
            public void onComplete() {
                if (action instanceof XOWLWorkflowAction && ((XOWLWorkflowAction) action).isFinishOnSuccess()) {
                    workflowAdvance();
                }
            }
        };
    }
}
