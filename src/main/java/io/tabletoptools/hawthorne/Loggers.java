/*
 * $Id: HttpFilter 3988 2017-06-21 13:47:09Z cfi $
 * Created on 02.02.18 16:30
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

import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

public class Loggers {

    //Application Logic
    public static final org.slf4j.Logger APPLICATION_LOG = LoggerFactory.getLogger("hawthorne/bot/application");

    //Messages
    public static final org.slf4j.Logger MESSAGE_RECEIVED_LOG = LoggerFactory.getLogger("hawthorne/messages/received");
    public static final org.slf4j.Logger MESSAGE_EDIT_LOG = LoggerFactory.getLogger("hawthorne/messages/edit");
    public static final org.slf4j.Logger MESSAGE_DELETE_LOG = LoggerFactory.getLogger("hawthorne/messages/delete");

}
