/*
 * Copyright (c) 2003-2004 Regents of the University of California.
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
 * Title:       DeltaV Request Generator
 * Description: This is where all of the requests are formed. The class contains
 *              static information needed to form all DeltaV requests.
 * Copyright:   Copyright (c) 2003-2004 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        07 February 2004
 * Changes:     Creating OPTIONS request for activity data
 *              (needed for evtl. Subversion support.)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Enumeration;

import HTTPClient.NVPair;
import HTTPClient.Util;
import com.ms.xml.om.Element;
import com.ms.xml.om.Document;
import com.ms.xml.util.XMLOutputStream;


/**
 * This is where all of the requests are formed. The class contains
 * static information needed to form all DeltaV requests.
 */
public class DeltaVRequestGenerator extends WebDAVRequestGenerator
{
    /**
     * Constructor, just initializing superclass  
     */
    public DeltaVRequestGenerator()
    {
        super();
    }


    /**
     * Generate VERSION-CONTROL request
     * @see     "RFC 3253, section 3.5"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateEnableVersioning()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateEnableVersioning" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName(true);
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
            return false;
        }

        Method = "VERSION-CONTROL";
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
        {
            Headers[0] = new NVPair("Host", HostName);
        }
        else
            Headers[0] = new NVPair("Host", HostName + ":" + Port);
        return true;
    }

    
    
    /**
     * Generate CHECKOUT request
     * @see     "RFC 3253, section 4.3"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateCheckOut()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateCheckOut" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
                return false;
        }

        // see if we have an activity at this host
        String host;
        if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
            host = HostName;
        else
            host = HostName +":" + Port;
        String activityResource = "";
        Enumeration enum = activity.keys();
        while( enum.hasMoreElements() )
        {
            String base = (String)enum.nextElement();
            if( host.equals( base ) )
            {
                activityResource = (String)activity.get( base );
                break;
            }
        }

        if( activityResource.length() > 0 )
        {
            Document miniDoc = new Document();
            miniDoc.setVersion("1.0");
            miniDoc.addChild(WebDAVXML.elemNewline,null);

            AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
            if( asgen == null )
                asgen = WebDAVXML.createNamespace( new AsGen(), null );
            Element options = WebDAVXML.createElement( DeltaVXML.ELEM_CHECKOUT, Element.ELEMENT, null, asgen );
            Element activityset = WebDAVXML.createElement( DeltaVXML.ELEM_ACTIVITY_SET, Element.ELEMENT, options, asgen );
            Element href = WebDAVXML.createElement( WebDAVXML.ELEM_HREF, Element.ELEMENT, activityset, asgen );
            Element val = WebDAVXML.createElement( null, Element.PCDATA, activityset, asgen );
            val.setText(activityResource);
            // keep on same line without whitespace
            addChild( href, val, 0, 0, false, false );
            addChild( activityset, href, 2, 0, true, true );
            addChild( options, activityset, 1, true );
            miniDoc.addChild( options, null );
            miniDoc.addChild( WebDAVXML.elemNewline, null );

            ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
            XMLOutputStream xml_out = new XMLOutputStream(byte_str);

            try
            {
                miniDoc.save(xml_out);
                Body = byte_str.toByteArray();
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
                return false;
            }
        }

        Method = "CHECKOUT";
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
        {
            Headers[0] = new NVPair("Host", HostName);
        }
        else
            Headers[0] = new NVPair("Host", HostName + ":" + Port);

        if( Body != null )
        {
            int size = Headers.length;
            Headers = Util.resizeArray( Headers, size+2 ); 
            Headers[size] = new NVPair( "Content-Type", "text/xml" );
            Headers[size+1] = new NVPair( "Content-Length", new Long(Body.length).toString() );
        }

        return true;
    }
    
    
    /**
     * Generate UNCHECKOUT request
     * @see     "RFC 3253, section 4.5"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateUnCheckOut()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateUnCheckOut" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
                return false;
        }

        Method = "UNCHECKOUT";
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
        {
            Headers[0] = new NVPair("Host", HostName);
        }
        else
            Headers[0] = new NVPair("Host", HostName + ":" + Port);
        return true;
    }
    
    
    /**
     * Generate CHECKIN request
     * @see     "RFC 3253, section 4.4"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateCheckIn()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateCheckIn" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
                return false;
        }

        Method = "CHECKIN";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );

        // should work, but at least Catacomb ignores this
        Element topElem = WebDAVXML.createElement( "checkin", Element.ELEMENT, null, asgen );
        topElem.addChild( WebDAVXML.elemNewline, null );
        Element comment = WebDAVXML.createElement( "comment", Element.ELEMENT, topElem, asgen );
        Element commentData = WebDAVXML.createElement( null, Element.PCDATA, topElem, asgen );
        //commentData.setText("this is a comment");
        addChild( comment, commentData, 0, 0, false, false );
        addChild( topElem, comment, 1, 0, false, true );
        Element author = WebDAVXML.createElement( "creator-displayname", Element.ELEMENT, topElem, asgen );
        Element authorData = WebDAVXML.createElement( null, Element.PCDATA, topElem, asgen );
        //authorData.setText( "this is the author" );
        addChild( author, authorData, 0, 0, false, false );
        addChild( topElem, author, 1, 0, false, true );
        miniDoc.addChild( topElem, null );
        
        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);
        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[1];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair("Host", HostName);
            }
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
            return false;
        }

        return true;
    }
    

    /**
     * Generate REPORT request
     * @see     "RFC 3253, section 3.6"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateVersionHistory(String extra )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateReport" );
        }
        
        Extra = extra;
        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
            return false;
        }
        
        Method = "REPORT";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );
        
        Element reportElem = WebDAVXML.createElement( "version-tree", Element.ELEMENT, null, asgen );
        Element reportElem2 = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, reportElem, asgen );
        reportElem2.addChild( WebDAVXML.elemNewline, null );
        
        Element prop = WebDAVXML.createElement( "version-name", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "creator-displayname", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "getlastmodified", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "getcontentlength", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "successor-set", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "checked-in", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "checked-out", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        prop = WebDAVXML.createElement( "comment", Element.ELEMENT, reportElem2, asgen );
        addChild( reportElem2, prop, 2, false );
        
        addChild( reportElem, reportElem2, 1, true );
        miniDoc.addChild(reportElem,null);
        
        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);

        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[3];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair("Host", HostName);
            }
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
            Headers[1] = new NVPair("Content-Type", "text/xml");
            Headers[2] = new NVPair("Content-Length", new Long(Body.length).toString());

            printXML( miniDoc );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
            return false;
        }
        return true;
    }


    /**
     * Generate OPTIONS request with XML body, asking for activity-collection-set
     * @see     "RFC 3253, section 13.7"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateOptions( String FullPath, boolean getActivity )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateOptions" );
        }

        Headers = null;
        Body = null;

        if( !super.GenerateOptions( FullPath ) )
            return false;

        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );
        Element options = WebDAVXML.createElement( DeltaVXML.ELEM_OPTIONS, Element.ELEMENT, null, asgen );
        Element activityset = WebDAVXML.createElement( DeltaVXML.ELEM_ACTIVITY_COLLECTION_SET, Element.ELEMENT, options, asgen );
        addChild( options, activityset, 1, true );
        miniDoc.addChild( options, null );
        miniDoc.addChild( WebDAVXML.elemNewline, null );

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);

        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            int size = Headers.length;
            Headers = Util.resizeArray( Headers, size+2 ); 
            Headers[size] = new NVPair( "Content-Type", "text/xml" );
            Headers[size+1] = new NVPair( "Content-Length", new Long(Body.length).toString() );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
            return false;
        }
            
        return true;
    }

    /**
     * Generate MKACTIVITY request
     * @see     "RFC 3253, section 13.5"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateMkActivity()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateMkActivity" );
        }

        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);
        if( !ok )
            return false;

        Headers = null;
        Body = null;

        String host;
        if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
            host = HostName;
        else
            host = HostName +":" + Port;
        String root = host + StrippedResource;

        String collection = "";
        Enumeration enum = activityCollection.keys();
        while( enum.hasMoreElements() )
        {
            String base = (String)enum.nextElement();
            if( root.startsWith( base ) )
            {
                collection = (String)activityCollection.get( base );
                break;
            }
        }

        String activity = getUUID();
        StrippedResource = collection + activity; 

        Method = "MKACTIVITY";
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
        {
            Headers[0] = new NVPair("Host", HostName);
        }
        else
            Headers[0] = new NVPair("Host", HostName + ":" + Port);
        return true;
    }


    /**
     * Generate MERGE request
     * @see     "RFC 3253, section 11.2"
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateMerge()
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "DeltaVRequestGenerator::GenerateMerge" );
        }

        StrippedResource = parseResourceName( true );
        boolean ok = (StrippedResource != null);
        if( !ok )
            return false;

        Headers = null;
        Body = null;

        String host;
        if (Port == 0 || Port == WebDAVRequestGenerator.DEFAULT_PORT)
            host = HostName;
        else
            host = HostName +":" + Port;

        // TODO: how to merge subdirectories
        // TODO: is our resource right?
        String activityResource = "";
        Enumeration enum = activity.keys();
        while( enum.hasMoreElements() )
        {
            String base = (String)enum.nextElement();
            if( host.startsWith( base ) )
            {
                activityResource = (String)activity.get( base );
                break;
            }
        }

        if( activityResource.length() == 0 )
        {
            GlobalData.getGlobalData().errorMsg("No activity found: for " + StrippedResource + "\n");
            return false;
        }

        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );
        Element options = WebDAVXML.createElement( DeltaVXML.ELEM_MERGE, Element.ELEMENT, null, asgen );
        Element activityset = WebDAVXML.createElement( DeltaVXML.ELEM_SOURCE, Element.ELEMENT, options, asgen );
        Element href = WebDAVXML.createElement( WebDAVXML.ELEM_HREF, Element.ELEMENT, activityset, asgen );
        Element val = WebDAVXML.createElement( null, Element.PCDATA, activityset, asgen );
        val.setText( host + activityResource );
        // keep on same line without whitespace
        addChild( href, val, 0, 0, false, false );
        addChild( activityset, href, 2, 0, true, true );
        addChild( options, activityset, 1, true );
        miniDoc.addChild( options, null );
        miniDoc.addChild( WebDAVXML.elemNewline, null );

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);

        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML generation error: \n" + e);
            return false;
        }

        Method = "MERGE";
        Headers = new NVPair[3];
        if (Port == 0 || Port == DEFAULT_PORT)
        {
            Headers[0] = new NVPair("Host", HostName);
        }
        else
            Headers[0] = new NVPair("Host", HostName + ":" + Port);
        Headers[1] = new NVPair( "Content-Type", "text/xml" );
        Headers[2] = new NVPair( "Content-Length", new Long(Body.length).toString() );

        return true;
    }


    /**
     * 
     * @param activityCollection
     */
    public void SetActivityCollection( Hashtable activityCollection )
    {
        this.activityCollection = activityCollection;    
    }


    /**
     * 
     * @param activity
     */
    public void SetActivity( Hashtable activity )
    {
        this.activity = activity;    
    }


    /**
     * 
     * @return      UUID
     */
    protected String getUUID()
    {
        /* Needs JDK 1.5 to compile, and JDK 1.5 compilation fails elsewhere
        try
        {
            // java.util.UUID only available from JDK 1.5 on
            java.util.UUID uuid = java.util.UUID.randomUUID();
            return uuid.toString();
        }
        catch( NoClassDefFoundError e )
        {
            java.rmi.server.UID uid = new java.rmi.server.UID();
            return uid.toString();
        }
        */
        
        java.rmi.server.UID uid = new java.rmi.server.UID();
        return uid.toString();
    }


    protected Hashtable activityCollection = new Hashtable();
    protected Hashtable activity = new Hashtable();
}
