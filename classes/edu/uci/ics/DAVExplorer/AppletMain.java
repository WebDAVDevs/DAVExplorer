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
 * Title:       Main applet class
 * Description: Applet version of DAV Explorer
 *              Requires DAVExplorer.jar to be signed.
 *              Usage:
 *              <APPLET	ARCHIVE="DAVExplorer.jar"
 *                  CODE="edu/uci/ics/DAVExplorer/AppletMain.class"
 *                  WIDTH=800
 *                  HEIGHT=400>
 *                  <PARAM NAME=uri VALUE="http://dav.somewhere.com/webdav/">
 *                  <PARAM NAME=username VALUE="username">
 *                  <PARAM NAME=password VALUE="password">
 *                  ...
 *              </APPLET>

 * Copyright:   Copyright (c) 2003 Regents of the University of California. All rights reserved.
 * @author      Brian Johnson, integrated by Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 */


package edu.uci.ics.DAVExplorer;

import java.util.StringTokenizer;

public class AppletMain extends javax.swing.JApplet {

    public void init()
    {
    }


    public void start()
    {
        // clear tree
        GlobalData.reset();
        GlobalData.getGlobalData().setAddStartDir(false);
        GlobalData.getGlobalData().setAppletMode(parseBooleanParameter("appletMode"));
        GlobalData.getGlobalData().setHideURIBox(parseBooleanParameter("hideURIBox"));

        // register the initial tree nodes and get 'em going
        GlobalData.getGlobalData().setInitialSites(getUriUnPwParameters());
        setRootPane(new Main("DAV Explorer Applet").getRootPane());
        GlobalData.getGlobalData().getTree().initTree();
    }


    public void stop()
    {
        GlobalData.reset();
    }


    private boolean parseBooleanParameter(String name)
    {
        String appletMode = getParameter(name);
        if (appletMode != null) {
            return Boolean.valueOf(appletMode).booleanValue();
        }
        return true;
    }


    private String[][] getUriUnPwParameters()
    {
        // array of triplets: URI, Username, Password
        String[][] retVal = new String[][] { {} };

//        retVal = new String[][] {
//            { "http://dav.somewhere.com/webdav", "username", "password"}
//        };

        String uri = getParameter("uri");
        String username = getParameter("username");
        String password = getParameter("password");

        if( uri == null )
            return retVal;

        StringTokenizer uriST = new StringTokenizer(uri, "||");

        if (uriST.countTokens() == 0)
            return retVal;

        // allocate the first level of the array
        retVal = new String[uriST.countTokens()][];
        int index = 0;
        while (uriST.hasMoreElements()) {
            retVal[index] = new String[3];
            retVal[index][0] = uriST.nextToken();
            retVal[index][1] = username;
            retVal[index][2] = password;
            index++;
        }

        return retVal;
    }
}
