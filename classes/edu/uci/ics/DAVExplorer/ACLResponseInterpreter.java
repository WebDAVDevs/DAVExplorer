/*
 * Copyright (C) 2004 Regents of the University of California.
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
package edu.uci.ics.DAVExplorer;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        
 */

public class ACLResponseInterpreter extends DeltaVResponseInterpreter
{

    /**
     * Constructor 
     */
    public ACLResponseInterpreter()
    {
        super();
    }

    
    /**
     * Constructor
     * 
     * @param rg    Reference to the WebDAV request generator
     */
    public ACLResponseInterpreter( WebDAVRequestGenerator rg )
    {
        super( rg );
    }

    
    /**
     * Process a response from the server
     * 
     * @param e WebDAVResponseEvent
     *          The event from the client library, containing the response data  
     */
    public void handleResponse( WebDAVResponseEvent e )
        throws ResponseException
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::handleResponse" );
        }
    
        res = e.getResponse();
        Method = e.getMethodName();
        extendedCode = e.getExtendedCode();
        HostName = e.getHost();
        Port = e.getPort();
        Charset = getCharset();

        // get the resource name, and unescape it
        Resource = GlobalData.getGlobalData().unescape( e.getResource(), "ISO-8859-1", null );
        Node = e.getNode();

        try
        {
            if (res.getStatusCode() < 300)
            {
                if (Method.equals("PROPFIND"))
                    parsePropFind();
                else if (Method.equals("ACL"))
                    parseACL();
                else if (Method.equals("REPORT"))
                    parseReport();
                else
                {
                    super.handleResponse(e);
                }
            }
            else
                super.handleResponse(e);
        }
        catch (Exception ex)
         {
             // Most likely an error propagated from HTTPClient
             // We get this error if the server closes the connection
             // and the method is unknown to HTTPClient.
             // HTTPClient does an automatic retry for idempotent HTTP methods,
             // but not for our DeltaV methods, since it doesn't know about them.
             String debugOutput = System.getProperty( "debug", "false" );
             if( debugOutput.equals( "true" ) )
                 System.out.println(ex);
             throw new ResponseException( "HTTP error" );
         }
    }


    /**
     * Parse the response to an OPTIONS request
     */
    public void parseOptions()
    {
        if( GlobalData.getGlobalData().getDebugResponse() )
        {
            System.err.println( "ACLResponseInterpreter::parseOptions" );
        }

        try
        {
            String davheader = res.getHeader( "DAV" );
            if( davheader == null )
            {
                // no WebDAV support
                GlobalData.getGlobalData().errorMsg("ACL Interpreter:\n\nThe server does not support DAV\nat Resource " + Resource + ".");
                return;
            }
            boolean aclFound = false;
            if( davheader.indexOf("access-control") >= 0 )
            {
                aclFound = true;
            }

            String full;
            if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
                full = HostName;
            else
                full = HostName +":" + Port;
            full += Resource;
            acl.put( full, Boolean.valueOf(aclFound) );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("ACL Interpreter:\n\nError encountered \nwhile parsing OPTIONS Response:\n" + e);
            stream = null;
            return;
        }

        if( extendedCode == WebDAVResponseEvent.URIBOX )
        {
            generator.DoPropFind( Resource, false );
        }
    }

    
    protected void parseACL()
    {
        
    }
    

    public boolean isACL( String resource )
    {
        Enumeration enum = acl.keys();
        while( enum.hasMoreElements() )
        {
            String res = (String)enum.nextElement();
            if( resource.indexOf(res) >= 0 )
                return( ((Boolean)acl.get(res)).booleanValue() );
        }
        return false;
    }


    protected Hashtable acl = new Hashtable();
}
