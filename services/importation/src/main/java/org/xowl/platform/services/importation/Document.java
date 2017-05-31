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

import fr.cenotelie.hime.redist.ASTNode;
import org.xowl.infra.utils.TextUtils;
import org.xowl.platform.kernel.security.SecuredResourceBase;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Represents a document to be imported
 *
 * @author Laurent Wouters
 */
public class Document extends SecuredResourceBase {
    /**
     * The base URI for documents
     */
    private static final String URI = "http://xowl.org/platform/services/importation/Document#";

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
        super(URI + UUID.randomUUID().toString(), name);
        this.uploader = getOwners().iterator().next();
        this.uploadDate = DateFormat.getDateTimeInstance().format(new Date());
        this.fileName = fileName;
    }

    /**
     * Initializes this document
     *
     * @param node The descriptor node to load from
     */
    public Document(ASTNode node) {
        super(node);
        String uploader = "";
        String uploadDate = "";
        String originalName = "";
        for (ASTNode pair : node.getChildren()) {
            String key = TextUtils.unescape(pair.getChildren().get(0).getValue());
            key = key.substring(1, key.length() - 1);
            String value = TextUtils.unescape(pair.getChildren().get(1).getValue());
            value = value.substring(1, value.length() - 1);
            switch (key) {
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
        this.uploader = uploader;
        this.uploadDate = uploadDate;
        this.fileName = originalName;
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
    public String serializedJSON() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\": \"");
        builder.append(TextUtils.escapeStringJSON(Document.class.getCanonicalName()));
        builder.append("\"");
        serializedJsonBase(builder);
        builder.append(", \"uploader\": \"");
        builder.append(TextUtils.escapeStringJSON(uploader));
        builder.append("\", \"uploadDate\": \"");
        builder.append(TextUtils.escapeStringJSON(uploadDate));
        builder.append("\", \"fileName\": \"");
        builder.append(TextUtils.escapeStringJSON(fileName));
        builder.append("\"}");
        return builder.toString();
    }
}
