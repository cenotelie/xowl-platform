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

package org.xowl.platform.services.lts.impl;

import org.xowl.platform.kernel.ServiceHttpServed;
import org.xowl.platform.services.lts.TripleStore;
import org.xowl.platform.services.lts.TripleStoreService;
import org.xowl.platform.utils.HttpResponse;
import org.xowl.store.sparql.Result;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Implements a triple store service that is backed by a remote store connected to via HTTP
 *
 * @author Laurent Wouters
 */
public class RemoteXOWLStoreService implements TripleStoreService, ServiceHttpServed {
    /**
     * The live store
     */
    private final RemoteXOWLStore storeLive;
    /**
     * The long term store
     */
    private final RemoteXOWLStore storeLongTerm;
    /**
     * The service store
     */
    private final RemoteXOWLStore storeService;

    /**
     * Initializes this service
     */
    public RemoteXOWLStoreService() {
        this.storeLive = new BasicRemoteXOWLStore(this, "live");
        this.storeLongTerm = new BasicRemoteXOWLStore(this, "longTerm");
        this.storeService = new BasicRemoteXOWLStore(this, "service");
    }

    @Override
    public String getIdentifier() {
        return RemoteXOWLStoreService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "xOWL Federation Platform - Triple Store Service";
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
    public HttpResponse onMessage(String method, Map<String, String[]> parameters, String contentType, byte[] content, String accept) {
        if (content == null)
            return new HttpResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        String request = new String(content, Charset.forName("UTF-8"));
        Result result = storeLive.sparql(request);
        String responseType = Result.SYNTAX_JSON;
        switch (accept) {
            case Result.SYNTAX_CSV:
            case Result.SYNTAX_TSV:
            case Result.SYNTAX_XML:
            case Result.SYNTAX_JSON:
                responseType = accept;
                break;
        }
        StringWriter writer = new StringWriter();
        try {
            result.print(writer, responseType);
        } catch (IOException exception) {
            // cannot happen
        }
        return new HttpResponse(HttpURLConnection.HTTP_OK, responseType, writer.toString().getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public TripleStore getLiveStore() {
        return storeLive;
    }

    @Override
    public TripleStore getLongTermStore() {
        return storeLongTerm;
    }

    @Override
    public TripleStore getServiceStore() {
        return storeService;
    }
}
