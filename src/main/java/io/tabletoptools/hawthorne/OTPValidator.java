/*
 * $Id: YubicoClient 3988 2017-06-21 13:47:09Z cfi $
 * Created on Aug 18, 2017, 4:39:11 PM
 * 
 * Copyright (c) 2017 by bluesky IT-Solutions AG,
 * Kaspar-Pfeiffer-Strasse 4, 4142 Muenchenstein, Switzerland.
 * All rights reserved.
 * 
 * This software is the confidential and proprietary information
 * of bluesky IT-Solutions AG ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with bluesky IT-Solutions AG.
 */
package io.tabletoptools.hawthorne;

import com.yubico.client.v2.YubicoClient;

/**
 *
 * @author cfi
 */
public class OTPValidator {
    private static YubicoClient client;
    
    public static YubicoClient instance() {
        if(client == null) {
            client = YubicoClient.getClient(0, "");
        }
        return client;
    }
}
