/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
// This class creates an event object which carries the following:
// HostName, Port, MethodName, ResourceName, Headers, Body,
// Extra, User, Password
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
// 1. Added the WebDAVTreeNode that initiated the Request.


package DAVExplorer;

import java.util.*;
import HTTPClient.*;

public class WebDAVRequestEvent extends EventObject
{
    private String HostName;
    private int Port;
    private String MethodName;
    private String ResourceName;
    private NVPair[] Headers;
    private byte[] Body;
    private String Extra;
    private String User;
    private String Pass;

    private WebDAVTreeNode node;


    public WebDAVRequestEvent(Object module, String MethodName, String HostName, int Port, String ResourceName,
                              NVPair[] Headers, byte[] Body, String Extra, String User, String Pass, WebDAVTreeNode n )
    {
        super(module);

        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestEvent::Constructor" );
        }

        this.MethodName = MethodName;
        this.HostName = HostName;
        this.Port = Port;
        this.ResourceName = ResourceName;
        this.Headers = Headers;
        this.Body = Body;
        this.Extra = Extra;
        this.User = User;
        this.Pass = Pass;
        this.node =n;
    }

    public String getHost()
    {
        return HostName;
    }

    public String getUser()
    {
        return User;
    }

    public String getPass()
    {
        return Pass;
    }

    public int getPort()
    {
        return Port;
    }

    public String getMethod()
    {
        return MethodName;
    }

    public String getResource()
    {
        return ResourceName;
    }

    public NVPair[] getHeaders()
    {
        return Headers;
    }

    public byte[] getBody()
    {
        return Body;
    }

    public String getExtraInfo()
    {
        return Extra;
    }

    public WebDAVTreeNode getNode()
    {
        return node;
    }
}
