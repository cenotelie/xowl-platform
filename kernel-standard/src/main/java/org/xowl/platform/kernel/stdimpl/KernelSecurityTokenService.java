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

package org.xowl.platform.kernel.stdimpl;

import fr.cenotelie.commons.utils.IOUtils;
import fr.cenotelie.commons.utils.api.*;
import fr.cenotelie.commons.utils.config.Section;
import fr.cenotelie.commons.utils.logging.Logging;
import org.xowl.platform.kernel.PlatformUtils;
import org.xowl.platform.kernel.security.SecurityTokenService;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Implementation of the security token service for the platform
 *
 * @author Laurent Wouters
 */
public class KernelSecurityTokenService implements SecurityTokenService {
    /**
     * The size of the key to use
     */
    private static final int KEY_SIZE = 256;
    /**
     * The length of the hash in bytes
     */
    private static final int HASH_LENGTH = 32;
    /**
     * The length of the timestamp in bytes
     */
    private static final int TIMESTAMP_LENGTH = 8;

    /**
     * The Message Authentication Code algorithm to use for securing user tokens
     */
    private final Mac securityMAC;
    /**
     * The private security key for the Message Authentication Code
     */
    private final Key securityKey;
    /**
     * The name to use for security tokens
     */
    private final String securityTokenName;
    /**
     * The time to live in seconds of an authentication token
     */
    private final long securityTokenTTL;

    /**
     * Initializes this service
     *
     * @param configuration The configuration to use
     */
    public KernelSecurityTokenService(Section configuration) {
        Mac mac = null;
        Key key = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(KEY_SIZE);
            key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException exception) {
            // should not happen
            Logging.get().error(exception);
        }
        this.securityMAC = mac;
        this.securityKey = key;
        this.securityTokenName = configuration.get("tokenName");
        this.securityTokenTTL = Integer.parseInt(configuration.get("tokenTTL"));
    }

    @Override
    public String getIdentifier() {
        return KernelSecurityTokenService.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return PlatformUtils.NAME + " - Security Token Service";
    }

    @Override
    public String getTokenName() {
        return securityTokenName;
    }

    @Override
    public String newTokenFor(String login) {
        long timestamp = System.currentTimeMillis();
        long validUntil = timestamp + securityTokenTTL * 1000;
        byte[] text = login.getBytes(IOUtils.CHARSET);
        byte[] tokenData = Arrays.copyOf(text, text.length + TIMESTAMP_LENGTH);
        tokenData[text.length] = (byte) ((validUntil & 0xFF00000000000000L) >>> 56);
        tokenData[text.length + 1] = (byte) ((validUntil & 0x00FF000000000000L) >>> 48);
        tokenData[text.length + 2] = (byte) ((validUntil & 0x0000FF0000000000L) >>> 40);
        tokenData[text.length + 3] = (byte) ((validUntil & 0x000000FF00000000L) >>> 32);
        tokenData[text.length + 4] = (byte) ((validUntil & 0x00000000FF000000L) >>> 24);
        tokenData[text.length + 5] = (byte) ((validUntil & 0x0000000000FF0000L) >>> 16);
        tokenData[text.length + 6] = (byte) ((validUntil & 0x000000000000FF00L) >>> 8);
        tokenData[text.length + 7] = (byte) ((validUntil & 0x00000000000000FFL));

        synchronized (securityMAC) {
            try {
                securityMAC.init(securityKey);
                byte[] tokenHash = securityMAC.doFinal(tokenData);
                byte[] token = Arrays.copyOf(tokenData, tokenData.length + tokenHash.length);
                System.arraycopy(tokenHash, 0, token, tokenData.length, tokenHash.length);
                return fr.cenotelie.commons.utils.Base64.encodeBase64(token);
            } catch (InvalidKeyException exception) {
                Logging.get().error(exception);
                return null;
            }
        }
    }

    @Override
    public Reply checkToken(String token) {
        byte[] tokenBytes = fr.cenotelie.commons.utils.Base64.decodeBase64(token);
        if (tokenBytes.length <= HASH_LENGTH + TIMESTAMP_LENGTH)
            return ReplyUnauthenticated.instance();
        byte[] tokenData = Arrays.copyOf(tokenBytes, tokenBytes.length - HASH_LENGTH);
        byte[] hashProvided = new byte[HASH_LENGTH];
        System.arraycopy(tokenBytes, tokenBytes.length - HASH_LENGTH, hashProvided, 0, HASH_LENGTH);

        // checks the hash
        synchronized (securityMAC) {
            try {
                securityMAC.init(securityKey);
                byte[] computedHash = securityMAC.doFinal(tokenData);
                if (!Arrays.equals(hashProvided, computedHash))
                    // the token does not checks out ...
                    return ReplyUnauthenticated.instance();
            } catch (InvalidKeyException exception) {
                Logging.get().error(exception);
                return new ReplyException(exception);
            }
        }

        byte b0 = tokenBytes[tokenBytes.length - HASH_LENGTH - 8];
        byte b1 = tokenBytes[tokenBytes.length - HASH_LENGTH - 7];
        byte b2 = tokenBytes[tokenBytes.length - HASH_LENGTH - 6];
        byte b3 = tokenBytes[tokenBytes.length - HASH_LENGTH - 5];
        byte b4 = tokenBytes[tokenBytes.length - HASH_LENGTH - 4];
        byte b5 = tokenBytes[tokenBytes.length - HASH_LENGTH - 3];
        byte b6 = tokenBytes[tokenBytes.length - HASH_LENGTH - 2];
        byte b7 = tokenBytes[tokenBytes.length - HASH_LENGTH - 1];
        long validUntil = ((long) b0 & 0xFFL) << 56
                | ((long) b1 & 0xFFL) << 48
                | ((long) b2 & 0xFFL) << 40
                | ((long) b3 & 0xFFL) << 32
                | ((long) b4 & 0xFFL) << 24
                | ((long) b5 & 0xFFL) << 16
                | ((long) b6 & 0xFFL) << 8
                | ((long) b7 & 0xFFL);
        if (System.currentTimeMillis() > validUntil)
            // the token expired
            return ReplyExpiredSession.instance();
        return new ReplyResult<>(new String(tokenBytes, 0, tokenBytes.length - HASH_LENGTH - TIMESTAMP_LENGTH, IOUtils.CHARSET));
    }
}
