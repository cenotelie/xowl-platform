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

package org.xowl.platform.services.importation;

import org.xowl.hime.redist.ASTNode;
import org.xowl.infra.utils.Identifiable;
import org.xowl.infra.utils.Serializable;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.KernelSchema;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a document to be imported
 *
 * @author Laurent Wouters
 */
public class Document implements Identifiable, Serializable {
    /**
     * The document's identifier
     */
    private final String identifier;
    /**
     * The document's name
     */
    private final String name;
    /**
     * The date and time at which this document was initially uploaded
     */
    private final String uploadDate;
    /**
     * The identifier of the platform user that performed the upload (may be null)
     */
    private final String uploader;
    /**
     * The original client's file name
     */
    private final String fileName;

    /**
     * Initializes this document
     *
     * @param name     The document's name
     * @param fileName The original client's file name
     */
    public Document(String name, String fileName) {
        this.identifier = "http://xowl.org/platform/documents#" + UUID.randomUUID().toString();
        this.name = name;
        this.uploadDate = DateFormat.getDateTimeInstance().format(new Date());
        this.fileName = fileName;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        PlatformUser currentUser = securityService == null ? null : securityService.getCurrentUser();
        this.uploader = currentUser == null ? null : currentUser.getIdentifier();
    }

    /**
     * Initializes this document
     *
     * @param node The descriptor node to load from
     */
    public Document(ASTNode node) {
        String tIdentifier = "";
        String tName = "";
        String tUploadDate = "";
        String tUploader = null;
        String tOriginalFileName = "";
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "identifier":
                    tIdentifier = value;
                    break;
                case "name":
                    tName = value;
                    break;
                case "uploadDate":
                    tUploadDate = value;
                    break;
                case "uploader":
                    if (!value.isEmpty())
                        tUploader = value;
                    break;
                case "fileName":
                    tOriginalFileName = value;
                    break;
            }
        }
        this.identifier = tIdentifier;
        this.name = tName;
        this.uploadDate = tUploadDate;
        this.uploader = tUploader;
        this.fileName = tOriginalFileName;
    }

    /**
     * Gets the storage identifier of this document
     *
     * @return The storage identifier of this document
     */
    public String getStorageId() {
        return identifier.substring(KernelSchema.GRAPH_ARTIFACTS.length() + 1);
    }

    /**
     * Gets the date and time at which this document was initially uploaded
     *
     * @return The date and time at which this document was initially uploaded
     */
    public String getUploadDate() {
        return uploadDate;
    }

    /**
     * Gets the identifier of the platform user that performed the upload (may be null)
     *
     * @return The identifier of the platform user that performed the upload (may be null)
     */
    public String getUploader() {
        return uploader;
    }

    /**
     * Gets the original client's file name
     *
     * @return The original client's file name
     */
    public String getOriginalFileName() {
        return fileName;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" + TextUtils.escapeStringJSON(Document.class.getCanonicalName()) +
                "\", \"identifier\": \"" + TextUtils.escapeStringJSON(identifier) +
                "\", \"name\":\"" + TextUtils.escapeStringJSON(name) +
                "\", \"uploadDate\":\"" + TextUtils.escapeStringJSON(uploadDate) +
                "\", \"uploader\":\"" + TextUtils.escapeStringJSON(uploader != null ? uploader : "") +
                "\", \"fileName\":\"" + TextUtils.escapeStringJSON(fileName) +
                "\"}";
    }
}
