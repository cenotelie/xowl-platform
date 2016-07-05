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

package org.xowl.platform.services.statistics;

import org.xowl.infra.server.xsp.XSPReply;
import org.xowl.infra.server.xsp.XSPReplyResult;
import org.xowl.infra.server.xsp.XSPReplyResultCollection;
import org.xowl.infra.server.xsp.XSPReplyUtils;
import org.xowl.infra.store.http.HttpConstants;
import org.xowl.infra.store.http.HttpResponse;
import org.xowl.infra.store.storage.StoreStatistics;
import org.xowl.platform.kernel.HttpAPIService;
import org.xowl.platform.kernel.Service;
import org.xowl.platform.kernel.ServiceUtils;
import org.xowl.platform.kernel.XSPReplyServiceUnavailable;
import org.xowl.platform.kernel.artifacts.Artifact;
import org.xowl.platform.kernel.artifacts.ArtifactStorageService;
import org.xowl.platform.services.consistency.ConsistencyService;
import org.xowl.platform.services.consistency.Inconsistency;
import org.xowl.platform.services.lts.TripleStoreService;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * The provider of statistics services
 *
 * @author Laurent Wouters
 */
public class StatisticsProvider implements Service, HttpAPIService {
    /**
     * The URIs for this service
     */
    private static final String[] URIs = new String[]{
            "services/admin/statistics",
            "services/admin/statistics/databases"
    };

    /**
     * Refresh time for the stats (60 seconds), in nano-seconds
     */
    private static final long REFRESH = 60000000000L;

    /**
     * Timestamp of the last update
     */
    private long lastUpdate;
    /**
     * Total number of artifacts
     */
    private int nbArtifactsTotal;
    /**
     * Number of live artifacts
     */
    private int nbArtifactsLive;
    /**
     * The number of inconsistencies
     */
    private int nbInconsistencies;

    /**
     * Initializes this provider
     */
    public StatisticsProvider() {
        this.lastUpdate = System.nanoTime() - REFRESH;
        this.nbArtifactsTotal = 0;
        this.nbArtifactsLive = 0;
        this.nbInconsistencies = 0;
    }

    /**
     * Updates the stats if required
     */
    private void updateStats() {
        long now = System.nanoTime();
        if (now - lastUpdate >= REFRESH)
            doUpdateStats();
    }

    /**
     * Updates the stats
     */
    private void doUpdateStats() {
        ArtifactStorageService serviceArtifacts = ServiceUtils.getService(ArtifactStorageService.class);
        if (serviceArtifacts != null) {
            XSPReply reply = serviceArtifacts.getAllArtifacts();
            if (reply.isSuccess())
                nbArtifactsTotal = ((XSPReplyResultCollection<Artifact>) reply).getData().size();
            reply = serviceArtifacts.getLiveArtifacts();
            if (reply.isSuccess())
                nbArtifactsLive = ((XSPReplyResultCollection<Artifact>) reply).getData().size();
        }
        ConsistencyService serviceConsistency = ServiceUtils.getService(ConsistencyService.class);
        if (serviceConsistency != null) {
            XSPReply reply = serviceConsistency.getInconsistencies();
            if (reply.isSuccess()) {
                nbInconsistencies = ((XSPReplyResultCollection<Inconsistency>) reply).getData().size();
            }
        }
        lastUpdate = System.nanoTime();
    }


    @Override
    public String getIdentifier() {
        return StatisticsProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Statistics Service";
    }

    @Override
    public Collection<String> getURIs() {
        return Arrays.asList(URIs);
    }

    @Override
    public HttpResponse onMessage(String method, String uri, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (uri.equals("services/admin/statistics/databases"))
            return onMessageGetDBStats();
        return onMessageGetBasicStats();
    }

    /**
     * Responds to a request for the database stats
     *
     * @return The response
     */
    private HttpResponse onMessageGetDBStats() {
        TripleStoreService ltsService = ServiceUtils.getService(TripleStoreService.class);
        if (ltsService == null)
            return XSPReplyUtils.toHttpResponse(XSPReplyServiceUnavailable.instance(), null);
        XSPReply reply = ltsService.getLongTermStore().getStatistics();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        StoreStatistics statsLongTerm = ((XSPReplyResult<StoreStatistics>) reply).getData();
        reply = ltsService.getLiveStore().getStatistics();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        StoreStatistics statsLive = ((XSPReplyResult<StoreStatistics>) reply).getData();
        reply = ltsService.getServiceStore().getStatistics();
        if (!reply.isSuccess())
            return XSPReplyUtils.toHttpResponse(reply, null);
        StoreStatistics statsService = ((XSPReplyResult<StoreStatistics>) reply).getData();
        String result = "{\"longTerm\": " +
                statsLongTerm.serializedJSON() +
                ", \"live\": " +
                statsLive.serializedJSON() +
                ", \"service\": " +
                statsService.serializedJSON()
                + "}";
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON, result);
    }

    /**
     * Responds to a request for the basic stats
     *
     * @return The response
     */
    private HttpResponse onMessageGetBasicStats() {
        updateStats();
        return new HttpResponse(HttpURLConnection.HTTP_OK, HttpConstants.MIME_JSON,
                "{\"nbArtifactsTotal\": " + nbArtifactsTotal +
                        ", \"nbArtifactsLive\": " + nbArtifactsLive +
                        ", \"nbInconsistencies\": " + nbInconsistencies + "}");
    }
}
