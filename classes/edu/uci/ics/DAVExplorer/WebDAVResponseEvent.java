/*
 * Copyright (c) 1999 Regents of the University of California.
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
// This code was originally written by an undergraduate project
// team at UCI.
//
////////////////////////////////////////////////////////////////
// The code has been modified to include povisions for the final
// WebDAV xml namespaces.  A small number of program errors have
// been corrected.
//
// Please use the following contact:
//
// dav-exp@ics.uci.edu
//
// Version: 0.4
// Changes by: Yuzo Kanomata and Joe Feise
// Date: 3/17/99
//
// Change List:


package DAVExplorer;

import java.util.*;
import HTTPClient.*;

public class WebDAVResponseEvent extends EventObject {
    HTTPResponse httpResponse;
    String MethodName;
    String ExtraInfo;
    String HostName;
    int Port;
    String Resource;

    WebDAVTreeNode Node; // The node to which the expand is directed

    public WebDAVResponseEvent(Object module, String hostname, int Port,String resource, String method, HTTPResponse response, String extra, WebDAVTreeNode node)
    {
        super (module);
        this.httpResponse = response;
        this.MethodName = method;
        this.ExtraInfo = extra;
        this.Resource = resource;
        this.HostName = hostname;
        this.Port = Port;
        this.Node = node;
    }

    public HTTPResponse getResponse()
    {
        return httpResponse;
    }

    public String getMethodName()
    {
        return MethodName;
    }

    public String getExtraInfo()
    {
        return ExtraInfo;
    }

    public String getHost()
    {
        return HostName;
    }

    public int getPort()
    {
        return Port;
    }

    public String getResource()
    {
        return Resource;
    }

    public WebDAVTreeNode getNode()
    {
        return Node;
    }
}
