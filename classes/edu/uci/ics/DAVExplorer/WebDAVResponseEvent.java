/*
 * Copyright (c) 1998-2004 Regents of the University of California.
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
 * Title:       WebDAVResponseEvent
 * Description: The event object sent for responses from the server
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import java.util.EventObject;
import HTTPClient.HTTPResponse;


/**
 * 
 */
public class WebDAVResponseEvent extends EventObject {
    HTTPResponse httpResponse;
    String MethodName;
    String ExtraInfo;
    String HostName;
    int Port;
    String Resource;

    WebDAVTreeNode Node; // The node to which the expand is directed


    /**
     * Constructor
     * @param module
     * @param hostname
     * @param Port
     * @param resource
     * @param method
     * @param response
     * @param extra
     * @param node
     */
    public WebDAVResponseEvent(Object module, String hostname, int Port,String resource, String method, HTTPResponse response, String extra, WebDAVTreeNode node)
    {
        super (module);

        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "WebDAVResponseEvent::Constructor" );
        }

        this.httpResponse = response;
        this.MethodName = method;
        this.ExtraInfo = extra;
        this.Resource = resource;
        this.HostName = hostname;
        this.Port = Port;
        this.Node = node;
    }


    /**
     * 
     * @return
     */
    public HTTPResponse getResponse()
    {
        return httpResponse;
    }


    /**
     * 
     * @return
     */
    public String getMethodName()
    {
        return MethodName;
    }


    /**
     * 
     * @return
     */
    public String getExtraInfo()
    {
        return ExtraInfo;
    }


    /**
     * 
     * @return
     */
    public String getHost()
    {
        return HostName;
    }


    /**
     * 
     * @return
     */
    public int getPort()
    {
        return Port;
    }


    /**
     * 
     * @return
     */
    public String getResource()
    {
        return Resource;
    }


    /**
     * 
     * @return
     */
    public WebDAVTreeNode getNode()
    {
        return Node;
    }
}
