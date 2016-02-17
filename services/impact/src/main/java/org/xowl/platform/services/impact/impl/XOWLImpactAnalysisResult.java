/*******************************************************************************
 * Copyright (c) 2016 Madeleine Wouters
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
 *     Madeleine Wouters - woutersmadeleine@gmail.com
 ******************************************************************************/

package org.xowl.platform.services.impact.impl;

import org.xowl.infra.store.IOUtils;
import org.xowl.platform.services.impact.ImpactAnalysisResult;
import org.xowl.platform.services.impact.ImpactAnalysisResultPart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Implements the result of an impact analysis
 *
 * @author Laurent Wouters
 */
class XOWLImpactAnalysisResult implements ImpactAnalysisResult {
    /**
     * The result parts
     */
    private final Collection<ImpactAnalysisResultPart> parts;

    /**
     * Initializes this result
     *
     * @param parts The result parts
     */
    public XOWLImpactAnalysisResult(Collection<XOWLImpactAnalysisResultPart> parts) {
        this.parts = new ArrayList<>(parts.size());
        for (ImpactAnalysisResultPart part : parts)
            this.parts.add(part);
    }

    @Override
    public Collection<ImpactAnalysisResultPart> getParts() {
        return Collections.unmodifiableCollection(parts);
    }

    @Override
    public String serializedString() {
        return ImpactAnalysisResult.class.getCanonicalName();
    }

    @Override
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder("{\"type\": \"");
        builder.append(IOUtils.escapeStringJSON(ImpactAnalysisResult.class.getCanonicalName()));
        builder.append("\", \"parts\": [");
        boolean first = true;
        for (ImpactAnalysisResultPart part : parts) {
            if (!first)
                builder.append(", ");
            first = false;
            builder.append(part.serializedJSON());
        }
        builder.append("]}");
        return builder.toString();
    }
}
