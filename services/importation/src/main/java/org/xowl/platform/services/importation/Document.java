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

import fr.cenotelie.commons.utils.Serializable;
import fr.cenotelie.commons.utils.TextUtils;
import fr.cenotelie.commons.utils.api.Reply;
import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.platform.kernel.Register;
import org.xowl.platform.kernel.ReplyServiceUnavailable;
import org.xowl.platform.kernel.platform.PlatformUser;
import org.xowl.platform.kernel.platform.PlatformUserRoot;
import org.xowl.platform.kernel.security.SecuredResource;
import org.xowl.platform.kernel.security.SecurityService;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a document to be imported
 *
 * @author Laurent Wouters
 */
public class Document implements SecuredResource, Serializable {
    /**
     * The base URI for documents
     */
    protected static final String URI = "http://xowl.org/platform/services/importation/Document#";

    /**
     * The document's identifier
     */
    private final String identifier;
    /**
     * The document's name
     */
    private final String name;
    /**
     * The identifier of the platform user that performed the upload
     */
    private final String uploader;
    /**
     * The date and time at which this document was initially uploaded
     */
    private final String uploadDate;
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
        this.identifier = URI + UUID.randomUUID().toString();
        this.name = name;
        SecurityService securityService = Register.getComponent(SecurityService.class);
        PlatformUser currentUser = securityService == null ? null : securityService.getCurrentUser();
        this.uploader = currentUser == null ? PlatformUserRoot.INSTANCE.getIdentifier() : currentUser.getIdentifier();
        this.uploadDate = DateFormat.getDateTimeInstance().format(new Date());
        this.fileName = fileName;
    }

    /**
     * Initializes this document
     *
     * @param node The descriptor node to load from
     */
    public Document(ASTNode node) {
        String identifier = "";
        String name = "";
        String uploader = "";
        String uploadDate = "";
        String originalName = "";
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
                case "uploader":
                    uploader = value;
                    break;
                case "uploadDate":
                    uploadDate = value;
                    break;
                case "fileName":
                    originalName = value;
                    break;
            }
        }
        this.identifier = identifier;
        this.name = name;
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.fileName = originalName;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getName() {
        return name;
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
     * Gets the identifier of the platform user that performed the upload
     *
     * @return The identifier of the platform user that performed the upload
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
    public Reply checkAccess() {
        SecurityService securityService = Register.getComponent(SecurityService.class);
        if (securityService == null)
            return ReplyServiceUnavailable.instance();
        return securityService.checkAction(SecurityService.ACTION_RESOURCE_ACCESS, this);
    }

    @Override
    public String serializedString() {
        return identifier;
    }

    @Override
    public String serializedJSON() {
        return "{\"type\": \"" +
                TextUtils.escapeStringJSON(Document.class.getCanonicalName()) +
                "\", \"identifier\": \"" +
                TextUtils.escapeStringJSON(identifier) +
                "\", \"name\":\"" +
                TextUtils.escapeStringJSON(name) +
                "\", \"uploadDate\":\"" +
                TextUtils.escapeStringJSON(uploadDate) +
                "\", \"uploader\":\"" +
                TextUtils.escapeStringJSON(uploader != null ? uploader : "") +
                "\", \"fileName\":\"" +
                TextUtils.escapeStringJSON(fileName) +
                "\"}";
    }
}
