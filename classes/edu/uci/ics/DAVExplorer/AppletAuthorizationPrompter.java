/*
 * Copyright (c) 2003 Regents of the University of California.
 * All rights reserved.
 *
 * This software was developed at the University of California, Irvine.
 *
 * Redistribution and use in source and binary forms are permitted
 * provided that the above copyright notice and this paragraph are
 * duplicated in all such forms and that any documentation,
 * advertising materials, and other materials related to such
 * distribution and use acknowledge that the software was developed
 * by the University of California, Irvine.  The name of the
 * University may not be used to endorse or promote products derived
 * from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 * WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 */

/**
 * Title:       Authentication Dialog for applet use
 * Description: Wrapper around the login dialog
 * Copyright:   Copyright (c) 2003 Regents of the University of California. All rights reserved.
 * @author      Brian Johnson, integrated by Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 */

package edu.uci.ics.DAVExplorer;

import HTTPClient.AuthorizationPrompter;
import HTTPClient.NVPair;
import HTTPClient.AuthorizationInfo;


public class AppletAuthorizationPrompter implements AuthorizationPrompter {
    static int index = 0;

    public static void reset() {
        index = 0;
    }

    public NVPair getUsernamePassword(AuthorizationInfo challenge, boolean forProxy) {
        String[] siteInfo = GlobalData.getGlobalData().getInitialSites()[index];
        String username = siteInfo[1];
        String password = siteInfo[2];
        return new NVPair(username, password);
    }
}
