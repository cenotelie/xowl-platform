/*******************************************************************************
 * Copyright (c) 2017 Association Cénotélie (cenotelie.fr)
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

package org.xowl.platform.services.denotation;

import fr.cenotelie.commons.utils.Identifiable;
import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.security.SecurityService;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a document with a representation and one or more denotation
 *
 * @author Laurent Wouters
 */
public class Document implements Identifiable, Serializable {
    /**
     * The base URI for documents
     */
    private static final String URI = "http://xowl.org/platform/services/denotation/Document#";

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
     * Whether this document is shared to others in the collaboration
     */
    private boolean isShared;

    /**
     * Initializes this document
     *
     * @param name     The document's name
     * @param fileName The original client's file name
     */
    public Document(String name, String fileName) {
        this.identifier = URI + UUID.randomUUID().toString();
        this.name = name;
        this.uploadDate = DateFormat.getDateTimeInstance().format(new Date());
        this.fileName = fileName;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        PlatformUser currentUser = securityService == null ? null : securityService.getCurrentUser();
        this.uploader = currentUser == null ? null : currentUser.getIdentifier();
        this.isShared = false;
    }

    /**
     * Initializes this document
     *
     * @param node The descriptor node to load from
     */
    public Document(ASTNode node) {
        String identifier = "";
        String name = "";
        String uploadDate = "";
        String uploader = null;
        String fileName = "";
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
                case "identifier":
                    identifier = value;
                    break;
                case "name":
                    name = value;
                    break;
                case "uploadDate":
                    uploadDate = value;
                    break;
                case "uploader":
                    if (!value.isEmpty())
                        uploader = value;
                    break;
                case "fileName":
                    fileName = value;
                    break;
                case "isShared":
                    isShared = (value.equalsIgnoreCase("true"));
                    break;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.uploadDate = uploadDate;
        this.uploader = uploader;
        this.fileName = fileName;
    }

    /**
     * Gets the storage identifier of this document
     *
     * @return The storage identifier of this document
     */
    public String getStorageId() {
        return identifier.substring(URI.length());
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

    /**
     * Gets whether this document is shared with others in the collaboration
     *
     * @return Whether this document is shared with others in the collaboration
     */
    public boolean isShared() {
        return isShared;
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
                "\", \"isShared\":\"" + Boolean.toString(isShared) +
                "\"}";
    }
}
