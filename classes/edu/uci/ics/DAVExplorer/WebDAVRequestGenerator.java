/*
 * Copyright (c) 1998-2005 Regents of the University of California.
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

import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import HTTPClient.NVPair;
import HTTPClient.Util;
import com.ms.xml.om.Element;
import com.ms.xml.om.Document;
import com.ms.xml.util.XMLOutputStream;
import com.ms.xml.util.Name;


/**
 * Title:       WebDAVRequest Generator
 * Description: This is where all of the requests are formed. The class contains
 *              static information needed to form all WebDAV requests. When GUI
 *              sends an event indicating that another resource has been
 *              selected it is properly handled by either
 *              tableSelectionChanged() or treeSelectionChanged()
 * Copyright:   Copyright (c) 1998-2005 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * Changes:     Added the WebDAVTreeNode that initiated the Request.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 November 2001
 * Changes:     Added locktoken support to proppatch code
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        25 June 2002
 * Changes:     Special handling of PUT to support files > 2GB
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        27 April 2003
 * Changes:     Added shared lock functionality.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Code cleanup.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        10 February 2005
 * Changes:     Some refactoring
 */
public class WebDAVRequestGenerator implements Runnable
{
    protected static final int DEFAULT_PORT = 80;
    protected static String HostName = "";
    protected static int Port = 0;
    protected static String Method = null;
    protected static String Path = "";
    protected static String ResourceName = "";
    protected static String tableResource = "";
    protected static String StrippedResource = "";
    protected static NVPair[] Headers = null;
    protected static byte[] Body = null;
    protected static int extendedCode = 0;
    protected static String extendedData = null;
    protected static String User = "";
    protected static String Password = "";
    protected static Vector listeners = new Vector();

    protected WebDAVTreeNode Node = null;
    protected WebDAVTreeNode parentNode = null;

    protected boolean debugXML = false;

    protected String userAgent = null;

    // Need to save the following values for the second
    // go around for the Move and Delete when trying
    protected WebDAVTreeNode Node2;
    protected String ResourceName2;
    protected String Dest2;
    protected String dir2;
    protected boolean Overwrite2;
    protected boolean KeepAlive2;
    protected boolean secondTime = false;


    /**
     * reset the internal data
     */
    public static void reset()
    {
        HostName = "";
        Port = 0;
        Method = null;
        Path = "";
        ResourceName = "";
        tableResource = "";
        StrippedResource = "";
        Headers = null;
        Body = null;
        extendedCode = 0;
        extendedData = null;
        User = "";
        Password = "";
        listeners = new Vector();
    }


    /**
     * Constructor
     */
    public WebDAVRequestGenerator()
    {
        super();
    }


    /**
     * Store the user name
     * @param username
     */
    public void setUser(String username)
    {
        User = username;
    }


    /**
     * Store the user's password
     * @param pass
     */
    public void setPass(String pass)
    {
        Password = pass;
    }


    /**
     * Store the user agent string
     * @param ua
     */
    public void setUserAgent( String ua )
    {
        userAgent = ua;
    }


    /**
     * 
     * @param resource
     * @param fullPath
     */
    public void DoPropFind( String resource, boolean fullPath )
    {
        String str = resource;
        if( !fullPath )
        {
            str = HostName;
            if (Port > 0)
                str += ":" + Port;
            str += resource;
        }
        // 1999-June-08, Joachim Feise (dav-exp@ics.uci.edu):
        // workaround for IBM's DAV4J, which does not handle propfind properly
        // with the prop tag. To use the workaround, run DAV Explorer with
        // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
        // Note that this prevents the detection of DeltaV information, since
        // RFC 3253 states in section 3.11 that "A DAV:allprop PROPFIND request
        // SHOULD NOT return any of the properties defined by this document."
        String doAllProp = System.getProperty( "propfind" );
        if( (doAllProp != null) && doAllProp.equalsIgnoreCase("allprop") )
        {
            if( GeneratePropFind( str, "allprop", "one", null, null, false ) )
            {
                execute();
            }
        }
        else
        {
            String[] props = preparePropFind();
            if( GeneratePropFind( str, "prop", "one", props, null, false ) )
            {
                execute();
            }
        }
    }


    /**
     * 
     * @return
     */
    protected String[] preparePropFind()
    {
        String[] props = new String[6];
        props[0] = "displayname";
        props[1] = "resourcetype";
        props[2] = "getcontenttype";
        props[3] = "getcontentlength";
        props[4] = "getlastmodified";
        props[5] = "lockdiscovery";
        return props;
    }
    
    
    /**
     * 
     * @param e
     */
    public void tableSelectionChanged(ViewSelectionEvent e)
    {
        if (e.getNode() != null)
        {
            return;
        }
        else
        {
            tableResource = (String)e.getPath().toString();
            if (Path.length() == 0)
            {
                ResourceName = tableResource;
            }
            ResourceName = Path + tableResource;
        }
    }


    /**
     * Indicate that we are generating a second request based on
     * information from the response to an earlier one.
     * This is used for example if we need to gather additional
     * information before executing a request.
     * @param  
     */
    public void setSecondTime(boolean b)
    {
        secondTime = b;
    }


    /**
     * 
     * @param name
     * @param node
     */
    public void setResource(String name, WebDAVTreeNode node)
    {
        if( name == null )
            return;

        tableResource = name;
        if (Path.length() == 0)
        {
            ResourceName = name;
        }
        else
        {
            ResourceName = Path + name;
        }

        Node = node;
    }


    /**
     * 
     * @param node
     */
    public void setNode( WebDAVTreeNode node )
    {
        Node = node;
    }


    /**
     * 
     * @param e
     */
    public void treeSelectionChanged(ViewSelectionEvent e)
    {
        String Item;
        Path = (String)e.getPath().toString();
        ResourceName = Path + "/";
    }


    /**
     * 
     * @param escape
     * 
     * @return
     */
    public String parseResourceName( boolean escape )
    {
        if (ResourceName.equals(""))
        {
            GlobalData.getGlobalData().errorMsg("No resource selected!");
            return null;
        }
        if( !ResourceName.startsWith(GlobalData.WebDAVPrefix) && !ResourceName.startsWith(GlobalData.WebDAVPrefixSSL) )
        {
            GlobalData.getGlobalData().errorMsg("This operation cannot be executed\non a local resource.");
            return null;
        }
        String stripped;
        if( ResourceName.startsWith(GlobalData.WebDAVPrefix) )
            stripped = ResourceName.substring(GlobalData.WebDAVPrefix.length());
        else
            stripped = ResourceName.substring(GlobalData.WebDAVPrefixSSL.length());
        return parseStripped( stripped, escape );
    }


    /**
     * 
     * @param stripped
     * @param escape
     * 
     * @return
     */
    public String parseStripped( String stripped, boolean escape )
    {
        StringTokenizer str = new StringTokenizer(stripped, "/");
        boolean isColl = false;

        if (!str.hasMoreTokens())
        {
            GlobalData.getGlobalData().errorMsg("Invalid host name.");
            return null;
        }
        if (stripped.endsWith("/"))
            isColl = true;

        String host = str.nextToken();

        int pos = host.indexOf(":");
        if (pos < 0)
        {
            HostName = host;
            Port = 0;
        }
        else
        {
            HostName = host.substring(0,pos);
            String port = host.substring(pos+1);
            try
            {
                Port = Integer.parseInt(port);
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg("Invalid port number.");
                Port = 0;
                return null;
            }
        }
        String newRes = "";
        while (str.hasMoreTokens())
            newRes = newRes + "/" + str.nextToken();
        if (newRes.length() == 0)
            newRes = "/";
        else if( isColl )
            newRes = newRes + "/";

        if( escape )
        {
            StringReader sr = new StringReader( newRes+"\n" );
            EscapeReader er = new EscapeReader( sr, false );
            BufferedReader br = new BufferedReader( er );
            try
            {
                return br.readLine();
            }
            catch( IOException e )
            {
                GlobalData.getGlobalData().errorMsg("URI generation error: \n" + e);
                return null;
            }
        }
        else
            return newRes;
    }


    /**
     * 
     * @param appendix
     * 
     * @return
     */
    public String getDefaultName( String appendix )
    {
        String defaultName = parseResourceName( false );
        if( defaultName == null )
            return null;

        if( defaultName.endsWith( "/" ) )
            defaultName = defaultName.substring( 0, defaultName.length()-1 );
        if( appendix != null )
            defaultName += appendix;

        return defaultName;
    }


    /**
     * Execute a request
     */
    public void execute()
    {
        AsGen.clear();
        Thread th = new Thread(this);
        th.start();
    }


    /**
     * Sending a request to the server
     */
    public void run()
    {
        if (Headers == null)
        {
            GlobalData.getGlobalData().errorMsg("Invalid Request.");
            return;
        }
        Vector ls;
        synchronized (this)
        {
            ls = (Vector) listeners.clone();
        }

        // add our own headers: user-agent and translate
        if( userAgent != null )
        {
            int size = Headers.length;
            Headers = Util.resizeArray( Headers, size+2 ); 
            Headers[size] = new NVPair( "Translate", "f" );
            Headers[size+1] = new NVPair( "User-Agent", userAgent );
        }

        WebDAVRequestEvent e = new WebDAVRequestEvent( this, Method, HostName,
                                                       Port, StrippedResource,
                                                       Headers, Body, extendedCode,
                                                       extendedData, User, Password,
                                                       Node );
        Node = null;
        for (int i=0;i<ls.size();i++)
        {
            WebDAVRequestListener l = (WebDAVRequestListener) ls.elementAt(i);
            l.requestFormed(e);
        }
    }


    /**
     * Add a listener
     * 
     * @param l     listener to add
     */
    public synchronized void addRequestListener(WebDAVRequestListener l)
    {
        listeners.addElement(l);
    }


    /**
     * Remove a listener
     * 
     * @param l     listener to remove
     */
    public synchronized void removeRequestListener(WebDAVRequestListener l)
    {
        listeners.removeElement(l);
    }


    /**
     * Generate a PROPFIND request to do lock discovery
     * @see     "RFC 2518"
     * @param   code
     * @param   data
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean DiscoverLock( int code, String data )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::DiscoverLock" );
        }

        extendedCode = code;
        extendedData = data;
        String[] prop = new String[1];
        String[] schema = new String[1];

        // 1999-June-08, Joachim Feise (dav-exp@ics.uci.edu):
        // workaround for IBM's DAV4J, which does not handle propfind properly
        // with the prop tag. To use the workaround, run DAV Explorer with
        // 'java -jar -Dpropfind=allprop DAVExplorer.jar'
        boolean retval = false;
        String doAllProp = System.getProperty( "propfind" );
        if( (doAllProp != null) && doAllProp.equalsIgnoreCase("allprop") )
        {
            retval = GeneratePropFind( null, "allprop", "zero", null, schema, false );
        }
        else
        {
            prop[0] = "lockdiscovery";
            schema[0] = WebDAVProp.DAV_SCHEMA;
            retval = GeneratePropFind( null, "prop", "zero", prop, schema, false );
        }
        if( retval )
        {
            execute();
        }
        return retval;
    }


    /**
     * Generate a PROPFIND request to do lock discovery
     * @see     "RFC 2518"
     * @param FullPath
     * @param command
     * @param Depth
     * @param props
     * @param schemas
     * @param flag
     * @param n
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GeneratePropFindForNode( String FullPath, String command,
                                                         String Depth, String[] props,
                                                         String[] schemas, boolean flag,
                                                         WebDAVTreeNode n )
    {
        Node = n;
        return GeneratePropFind( FullPath, command, Depth, props, schemas, flag);
    }


    /**
     * Generate an OPTIONS request
     * @see     "RFC 2518"
     * @param FullPath
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateOptions( String FullPath )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateOptions" );
        }

        Headers = null;
        Body = null;

        if (FullPath != null)
            StrippedResource = parseStripped( FullPath, true );
        else
            StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        Method = "OPTIONS";
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
     * Generate a PROPFIND request
     * @see     "RFC 2518"
     * @param FullPath
     * @param command
     * @param Depth
     * @param props
     * @param schemas
     * @param flagGetFilesBelow
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GeneratePropFind( String FullPath, String command,
                                                  String Depth, String[] props,
                                                  String[] schemas,
                                                  boolean flagGetFilesBelow )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GeneratePropFind" );
            if( command != null )
                System.err.println( "\tCommand: "+ command );
            if( Depth != null )
                System.err.println( "\tDepth: " + Depth );
        }

        Headers = null;
        Body = null;

        boolean ok;
        if (flagGetFilesBelow)
        {   // In this case, loadChildren Flags
            // this boolean in order to have
            // the ResourceName be set to the FullPath.
            // This is to ensure that Expanding a non
            // selected tree node will actually have the
            // properties of the children of the node
            // loaded into our tree.
            if( extendedCode == WebDAVResponseEvent.SELECT )
            {
            // Skip on a selection
            }
            else if (ResourceName.equals(FullPath))
            {
                extendedCode = WebDAVResponseEvent.INDEX;
            }
            else
            {
                extendedCode = WebDAVResponseEvent.EXPAND;
            }
            ResourceName = FullPath;
            StrippedResource = parseResourceName( true );
        }
        else if (FullPath != null)
            StrippedResource = parseStripped( FullPath, true );
        else
            StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        String com = "allprop";
        String dep = "infinity";
        if ( command.equalsIgnoreCase("prop") || command.equalsIgnoreCase("propname"))
            com = command.toLowerCase();
        if ( Depth.equalsIgnoreCase("zero") || Depth.equalsIgnoreCase("one"))
            dep = Depth;
        if (dep.equalsIgnoreCase("zero"))
            dep = "0";
        else if (dep.equalsIgnoreCase("one"))
            dep = "1";


        Method = "PROPFIND";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
        if( asgen == null )
            asgen = WebDAVXML.createNamespace( new AsGen(), null );
        Element propFind = WebDAVXML.createElement( WebDAVXML.ELEM_PROPFIND, Element.ELEMENT, null, asgen );
        if (com.equals("allprop"))
        {
            Element allpropElem = WebDAVXML.createElement( WebDAVXML.ELEM_ALLPROP, Element.ELEMENT, propFind, asgen );
            addChild( propFind, allpropElem, 1, true );
        }
        else if (com.equals("propname"))
        {
            Element propnameElem = WebDAVXML.createElement( WebDAVXML.ELEM_PROPNAME,Element.ELEMENT, propFind, asgen );
            addChild( propFind, propnameElem, 1, true );
        }
        else
        {
            addProperties( propFind, asgen, props, 1 );
        }
        miniDoc.addChild(propFind,null);
        miniDoc.addChild(WebDAVXML.elemNewline, null);

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);

        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[4];
            if (Port == 0 || Port == DEFAULT_PORT)
            {
                Headers[0] = new NVPair("Host", HostName);
            }
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
            Headers[1] = new NVPair("Depth", dep);
            Headers[2] = new NVPair("Content-Type", "text/xml");
            Headers[3] = new NVPair("Content-Length", new Long(Body.length).toString());

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
     * 
     * @param parent
     * @param namespace
     * @param props
     * @param indent
     */
    protected void addProperties( Element parent, AsGen namespace, String[] props, int indent )
    {
        Element propElem = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, parent, namespace );
        propElem.addChild( WebDAVXML.elemNewline, null );
        for (int i=0;i<props.length;i++)
        {
            Element prop = WebDAVXML.createElement( props[i], Element.ELEMENT, propElem, namespace );
            addChild( propElem, prop, 2, false );
        }
        addChild( parent, propElem, 1, true );
    }


    /**
     * Add child to XML tree, do pretty formatting
     * @param parent
     * @param child
     * @param tabcount
     * @param leadingCR
     */
    protected void addChild( Element parent, Element child, int tabcount, boolean leadingCR )
    {
        addChild( parent, child, tabcount, tabcount, leadingCR, true );
    }


    /**
     * Add child to XML tree, do pretty formatting
     * @param parent
     * @param child
     * @param leadingTabcount
     * @param trailingTabcount
     * @param leadingCR
     * @param trailingCR
     */
    protected void addChild( Element parent, Element child, int leadingTabcount,
                             int trailingTabcount, boolean leadingCR, boolean trailingCR )
    {
        if( parent != null )
        {
            // format nicely
            if( child.numElements() > 0 )
            {
                for( int i=0; i<trailingTabcount; i++ )
                    child.addChild( WebDAVXML.elemDSpace, null );
            }
            if( leadingCR )
                parent.addChild( WebDAVXML.elemNewline, null );
            for( int i=0; i<leadingTabcount; i++ )
                parent.addChild( WebDAVXML.elemDSpace, null );
            parent.addChild( child, null );
            if( trailingCR )
              parent.addChild( WebDAVXML.elemNewline, null );
        }
    }


    /**
     *
     * @param doc
     * @param e
     * 
     * @return 
     */
    protected static boolean docContains(Document doc, Element e)
    {
        Enumeration docEnum = doc.getElements();
        while (docEnum.hasMoreElements())
        {
            Element propEl = (Element) docEnum.nextElement();
            Name propTag = propEl.getTagName();
            if (propTag == null)
                continue;
            if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
                continue;
            Enumeration propEnum = propEl.getElements();
            while (propEnum.hasMoreElements())
            {
                Element prop = (Element) propEnum.nextElement();
                Name nameTag = prop.getTagName();
                if (prop.getType() != Element.ELEMENT)
                    continue;
                if ( (nameTag.getName().equals(e.getTagName().getName())) &&
                    (nameTag.getNameSpace().equals(e.getTagName().getNameSpace())) )
                return true;
            }
        }
        return false;
    }


    /**
     * Generate a PROPPATCH request
     * @see     "RFC 2518"
     * @param FullPath
     * @param addProps
     * @param removeProps
     * @param locktoken
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GeneratePropPatch( String FullPath, Element addProps,
                                                   Element removeProps, String locktoken )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GeneratePropPatch" );
        }

        if (FullPath != null)
            StrippedResource = parseStripped( FullPath, true );
        else
            StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        Headers = null;
        Body = null;
        boolean setUsed = false;
        boolean removeUsed = false;
        Method = "PROPPATCH";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen DAVNS = WebDAVXML.findNamespace( new AsGen(), null );
        if( DAVNS == null )
            DAVNS = WebDAVXML.createNamespace( new AsGen(), null );
        Element propUpdate = WebDAVXML.createElement( WebDAVXML.ELEM_PROPERTY_UPDATE, Element.ELEMENT, null, DAVNS, true );

        if( removeProps != null )
        {
            propUpdate.addChild( WebDAVXML.elemNewline, null );
            propUpdate.addChild( removeProps, null );
            propUpdate.addChild( WebDAVXML.elemNewline, null);
        }
        if( addProps != null )
        {
            propUpdate.addChild( WebDAVXML.elemNewline, null );
            propUpdate.addChild( addProps, null );
            propUpdate.addChild( WebDAVXML.elemNewline, null );
        }

        miniDoc.addChild( propUpdate, null );
        miniDoc.addChild( WebDAVXML.elemNewline, null );

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream( byte_str );
        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            if (locktoken != null)
            {
                Headers = new NVPair[4];
                Headers[3] = new NVPair( "If", "(<" + locktoken + ">)" );
            }
            else
                Headers = new NVPair[3];
            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair( "Host", HostName );
            else
            {
                // 2001-Oct-29 jfeise (dav-exp@ics.uci.edu):
                // workaround for Apache 1.3.x on non-default port
                // Apache returns a 500 error on the first try
                String apache = System.getProperty( "Apache", "no" );
                if( apache.equalsIgnoreCase("yes") || apache.equalsIgnoreCase( "true" ) )
                    Headers[0] = new NVPair( "Host", HostName );
                else
                    Headers[0] = new NVPair( "Host", HostName + ":" + Port );
            }
            Headers[1] = new NVPair( "Content-Type", "text/xml" );
            Headers[2] = new NVPair( "Content-Length", new Long(Body.length).toString() );

            printXML( miniDoc );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "XML Generator Error: \n" + e );
            return false;
        }
        return true;
    }


    /**
     * Generate a MKCOL request
     * @see     "RFC 2518"
     * @param parentDir
     * @param dirname
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateMkCol( String parentDir, String dirname )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateMkCol" );
        }

        Headers = null;
        Body = null;


        ResourceName = parentDir;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        String dest = dirname;
        int pos = dest.lastIndexOf( File.separatorChar );
        if( pos >= 0 )
            dest = dest.substring( pos + 1 );
        StrippedResource = StrippedResource + dest;

       Method = "MKCOL";
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
            Headers[0] = new NVPair("Host",HostName);
        else
            Headers[0] = new NVPair("Host",HostName + ":" + Port);
        return true;
    }


    /**
     * Generate a GET request
     * @see     "RFC 2518"
     * @param localName
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateGet( int code )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateGet" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        extendedCode = code;
        Method = "GET";
        Body = null;
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
            Headers[0] = new NVPair("Host",HostName);
        else
            Headers[0] = new NVPair("Host",HostName + ":" + Port);
        return true;
    }


    /**
     * Generate a DELETE request
     * @see     "RFC 2518"
     * @param lockToken
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateDelete(String lockToken)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateDelete" );
        }

        Headers = null;
        Body = null;

        if ( secondTime )
        {
            Node = Node2;
            ResourceName= ResourceName2;
        }
        else
        {
            Node2 = Node;
            ResourceName2 = ResourceName;
        }

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        Method = "DELETE";
        Body = null;
        if (lockToken != null)
        {
            Headers = new NVPair[2];
            Headers[1] = new NVPair("If","(<" + lockToken + ">)");
        }
        else
        {
            Headers = new NVPair[1];
        }
        if (Port == 0 || Port == DEFAULT_PORT)
            Headers[0] = new NVPair("Host",HostName);
        else
            Headers[0] = new NVPair("Host",HostName + ":" + Port);
        return true;
    }


    /**
     * Returns the parent Node
     * This is used to indicate which
     * Node is beeing writen to by WebDAVResponseInterpreter:parsePut
     * Parent in case of selection of collection in file view
     * and the collection itself in the case of nothing selected in
     * file view window
     * 
     * @return  parent node  
     */
    public WebDAVTreeNode getPossibleParentOfSelectedCollectionNode()
    {
        return parentNode;
    }


    /**
     * 
     */
    public void resetParentNode()
    {
        parentNode= null;
    }


    /**
     * Generate a PUT request
     * @see     "RFC 2518"
     * @param fileName
     * @param destDir
     * @param lockToken
     * @param selectedCollection
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GeneratePut( String fileName, String destDir,
                                             String lockToken, WebDAVTreeNode selectedCollection )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GeneratePut" );
        }

        Headers = null;
        Body = null;

        parentNode = selectedCollection;

        ResourceName = destDir;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        // strip any directory info from the destination name
        String dest = fileName;
        int pos = dest.lastIndexOf( File.separatorChar );
        if( pos >= 0 )
            dest = dest.substring( pos + 1 );

        // get the collection part of the resource
        pos = StrippedResource.lastIndexOf( "/" );
        if( pos >= 0 )
            StrippedResource = StrippedResource.substring( 0, pos + 1 );
        StrippedResource = StrippedResource + dest;

        if ( (fileName == null) || (fileName.equals("")) )
        {
            GlobalData.getGlobalData().errorMsg("DAV Generator:\nFile not found!\n");
            return false;
        }
        File file = new File(fileName);
        if (!file.exists())
        {
            GlobalData.getGlobalData().errorMsg("Invalid File.");
            return false;
        }

        try
        {
            long fileSize = file.length();
            Method = "PUT";
            extendedData = fileName;

            if (lockToken != null)
            {
                Headers = new NVPair[4];
                Headers[3] = new NVPair("If","(<" + lockToken + ">)");
            }
            else
            {
                Headers = new NVPair[3];
            }

            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair("Host",HostName);
            else
                Headers[0] = new NVPair("Host",HostName + ":" + Port);

            Headers[1] = new NVPair( "Content-Type", getContentType(fileName) );
            Headers[2] = new NVPair( "Content-Length", new Long(fileSize).toString() );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "Error generating PUT\n" + e );
            return false;
        }
        return true;
    }


    /**
     * Generate a COPY request
     * @see     "RFC 2518"
     * @param Dest
     * @param Overwrite
     * @param KeepAlive
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateCopy( String Dest, boolean Overwrite,
                                              boolean KeepAlive )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateCopy" );
        }

        Headers = null;
        Body = null;
        extendedCode = WebDAVResponseEvent.COPY;

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        String ow = (Overwrite) ? "T" : "F";

        if (Dest == null)
        {
            if( StrippedResource.endsWith( "/" ) )
                Dest = StrippedResource.substring( 0, StrippedResource.length()-1 );
            else
                Dest = StrippedResource;
            Dest = Dest + "_copy";
        }

        if( Port==0 || Port==DEFAULT_PORT )
            Dest = HostName + Dest;
        else
            Dest = HostName + ":" + Port + Dest ;


        if( !Dest.startsWith(GlobalData.WebDAVPrefix) && !Dest.startsWith(GlobalData.WebDAVPrefixSSL) )
        {
            if( GlobalData.getGlobalData().getSSL() )
                Dest = GlobalData.WebDAVPrefixSSL + Dest;
            else
                Dest = GlobalData.WebDAVPrefix + Dest;
        }

        Method = "COPY";
        Body = null;
        if (KeepAlive)
        {
            Document miniDoc = new Document();
            miniDoc.setVersion("1.0");
            miniDoc.addChild(WebDAVXML.elemNewline,null);

            AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
            if( asgen == null )
                asgen = WebDAVXML.createNamespace( new AsGen(), null );
            Element propBehavior = WebDAVXML.createElement( WebDAVXML.ELEM_PROPERTY_BEHAVIOR, Element.ELEMENT, null, asgen );
            propBehavior.addChild( WebDAVXML.elemNewline, null );

            Element keepAlv = WebDAVXML.createElement( WebDAVXML.ELEM_KEEP_ALIVE, Element.ELEMENT, propBehavior, asgen );
            Element val = WebDAVXML.createElement( null, Element.PCDATA, keepAlv, asgen );
            val.setText("*");
            // keep on same line without whitespace
            addChild( keepAlv, val, 0, 0, false, false );
            addChild( propBehavior, keepAlv, 1, 0, false, true );

            miniDoc.addChild(propBehavior, null);
            miniDoc.addChild(WebDAVXML.elemNewline, null);

            ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
            XMLOutputStream xml_out = new XMLOutputStream(byte_str);

            try
            {
                miniDoc.save(xml_out);

                Body = byte_str.toByteArray();

                Headers = new NVPair[5];
                if (Port == 0 || Port == DEFAULT_PORT)
                    Headers[0] = new NVPair("Host", HostName);
                else
                    Headers[0] = new NVPair("Host", HostName + ":" + Port);
                Headers[1] = new NVPair("Destination", Dest);
                Headers[2] = new NVPair("Content-Type", "text/xml");
                Headers[3] = new NVPair("Content-Length", new Long(Body.length).toString());
                Headers[4] = new NVPair("Overwrite", ow);

                printXML( miniDoc );
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg("XML Generator Error: \n" + e);
                return false;
            }
        }
        else
        {
            Headers = new NVPair[3];
            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair("Host", HostName);
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
            Headers[1] = new NVPair("Destination", Dest);
            Headers[2] = new NVPair("Overwrite", ow);
        }
        return true;
    }


    /**
     * Generate a RENAME request
     * @see     "RFC 2518"
     * @param Dest
     * @param dir
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateRename( String Dest, String dir )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateRename" );
        }
        return DiscoverLock( WebDAVResponseEvent.RENAME, Dest + ":" + dir );
    }


    /**
     * Generate a MOVE request
     * @see     "RFC 2518"
     * @param Dest
     * @param dir
     * @param Overwrite
     * @param KeepAlive
     * @param lockToken
     * @param code
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateMove( String Dest, String dir, boolean Overwrite,
                                              boolean KeepAlive, String lockToken,
                                              int code )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateMove" );
        }

        Headers = null;
        Body = null;
        extendedCode = code;
        extendedData = Dest;

        if( secondTime)
        {
            Node = Node2;
            ResourceName = ResourceName2;
            Dest = Dest2;
            dir = dir2;
            Overwrite = Overwrite2;
            KeepAlive = KeepAlive2;
        }
        else
        {
            Node2 = Node;
            ResourceName2 = ResourceName;
            Dest2 = Dest;
            if( dir != null )
                dir2 = dir;
            else dir2 = null;
            Overwrite2 = Overwrite;
            KeepAlive2 = KeepAlive;
        }

        String srcFile = ResourceName;
        ResourceName = dir;

        ResourceName = srcFile;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        String ow = (Overwrite) ? "T" : "F";
        if (Dest == null)
        {
            GlobalData.getGlobalData().errorMsg( "Invalid Destination" );
            return false;
        }

        // may be null if invoked from menu
        if( dir == null )
        {
            if( GlobalData.getGlobalData().getSSL() )
                dir = GlobalData.WebDAVPrefixSSL;
            else
                dir = GlobalData.WebDAVPrefix;

            if( Port==0 || Port==DEFAULT_PORT )
                dir += HostName;
            else
                dir += HostName + ":" + Port;

        }
        Dest = dir + Dest;

        Method = "MOVE";
        Body = null;
        if (KeepAlive)
        {
            Document miniDoc = new Document();
            miniDoc.setVersion("1.0");
            miniDoc.addChild(WebDAVXML.elemNewline,null);

            AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
            if( asgen == null )
                asgen = WebDAVXML.createNamespace( new AsGen(), null );
            Element propBehavior = WebDAVXML.createElement( WebDAVXML.ELEM_PROPERTY_BEHAVIOR, Element.ELEMENT, null, asgen );
            propBehavior.addChild( WebDAVXML.elemNewline, null );
            Element keepAlv = WebDAVXML.createElement( WebDAVXML.ELEM_KEEP_ALIVE, Element.ELEMENT, propBehavior, asgen );
            Element val = WebDAVXML.createElement( null, Element.PCDATA, keepAlv, asgen );
            val.setText("*");
            // keep on same line without whitespace
            addChild( keepAlv, val, 0, 0, false, false );
            addChild( propBehavior, keepAlv, 1, 0, false, true );

            miniDoc.addChild(propBehavior, null);
            miniDoc.addChild(WebDAVXML.elemNewline, null);

            ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
            XMLOutputStream xml_out = new XMLOutputStream(byte_str);

            try
            {
                miniDoc.save(xml_out);
                Body = byte_str.toByteArray();
                if (lockToken != null)
                {
                    Headers = new NVPair[6];
                    Headers[5] = new NVPair("If","(<" + lockToken + ">)");
                }
                else
                {
                    Headers = new NVPair[5];
                }

                if (Port == 0 || Port == DEFAULT_PORT)
                    Headers[0] = new NVPair("Host", HostName);
                else
                    Headers[0] = new NVPair("Host", HostName + ":" + Port);
                Headers[1] = new NVPair("Destination", Dest);
                Headers[2] = new NVPair("Content-Type", "text/xml");
                Headers[3] = new NVPair("Content-Length", new Long(Body.length).toString());
                Headers[4] = new NVPair("Overwrite", ow);

                printXML( miniDoc );
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg("XML Generator Error: \n" + e);
                return false;
            }
        }
        else
        {
            Headers = new NVPair[3];
            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair("Host", HostName);
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
            Headers[1] = new NVPair("Destination", Dest);
            Headers[2] = new NVPair("Overwrite", ow);
        }
        return true;
    }


    /**
     * Generate a LOCK request
     * @see     "RFC 2518"
     * @param OwnerInfo
     * @param lockToken
     * @param exclusive
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateLock( String OwnerInfo, String lockToken,
                                              boolean exclusive )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateLock" );
        }

        Headers = null;
        Body = null;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        Method = "LOCK";
        Body = null;
        extendedData = lockToken;

        if (lockToken == null)
        {
            // new lock
            Document miniDoc = new Document();
            miniDoc.setVersion("1.0");
            miniDoc.addChild(WebDAVXML.elemNewline,null);

            AsGen asgen = WebDAVXML.findNamespace( new AsGen(), null );
            if( asgen == null )
                asgen = WebDAVXML.createNamespace( new AsGen(), null );
            Element lockInfoElem = WebDAVXML.createElement( WebDAVXML.ELEM_LOCK_INFO, Element.ELEMENT, null, asgen );

            Element lockTypeElem = WebDAVXML.createElement( WebDAVXML.ELEM_LOCK_TYPE, Element.ELEMENT, lockInfoElem, asgen );
            Element scopeElem = WebDAVXML.createElement( WebDAVXML.ELEM_LOCK_SCOPE, Element.ELEMENT, lockInfoElem, asgen );
            Element ownerElem = WebDAVXML.createElement( WebDAVXML.ELEM_OWNER, Element.ELEMENT, lockInfoElem, asgen );

            Element typeValue = WebDAVXML.createElement( WebDAVXML.ELEM_WRITE, Element.ELEMENT, lockTypeElem, asgen );
            Element scopeVal;
            if( exclusive )
                scopeVal = WebDAVXML.createElement( WebDAVXML.ELEM_EXCLUSIVE, Element.ELEMENT, scopeElem, asgen );
            else
                scopeVal = WebDAVXML.createElement( WebDAVXML.ELEM_SHARED, Element.ELEMENT, scopeElem, asgen );
            Element ownerHref = WebDAVXML.createElement( WebDAVXML.ELEM_HREF, Element.ELEMENT, ownerElem, asgen );
            Element ownerVal = WebDAVXML.createElement( null, Element.PCDATA, ownerElem, asgen );
            ownerVal.setText(OwnerInfo);
            // keep on same line without whitespace
            addChild( ownerHref, ownerVal, 0, 0, false, false );
            addChild( ownerElem, ownerHref, 2, 0, true, true );
            addChild( lockTypeElem, typeValue, 2, true );
            addChild( scopeElem, scopeVal, 2, true );
            addChild( lockInfoElem, lockTypeElem, 1, true );
            addChild( lockInfoElem, scopeElem, 1, false );
            addChild( lockInfoElem, ownerElem, 1, false );

            miniDoc.addChild(lockInfoElem,null);
            miniDoc.addChild(WebDAVXML.elemNewline, null);

            ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
            XMLOutputStream xml_out = new XMLOutputStream(byte_str);

            try
            {
                miniDoc.save(xml_out);
                Body = byte_str.toByteArray();

                Headers = new NVPair[5];
                if (Port == 0 || Port == DEFAULT_PORT)
                    Headers[0] = new NVPair("Host", HostName);
                else
                    Headers[0] = new NVPair("Host", HostName + ":" + Port);
                Headers[1] = new NVPair("Timeout", "Second-86400"); // 1 day
                Headers[2] = new NVPair("Content-Type", "text/xml");
                Headers[3] = new NVPair("Content-Length", new Long(Body.length).toString());
                Headers[4] = new NVPair("Depth", "infinity" );

                printXML( miniDoc );
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg("XML Generator Error: \n" + e);
                return false;
            }
        }
        else
        {
            // refresh the lock
            try
            {
                String token = "(<" + lockToken + ">)";

                Headers = new NVPair[3];
                if (Port == 0 || Port == DEFAULT_PORT)
                    Headers[0] = new NVPair("Host", HostName);
                else
                    Headers[0] = new NVPair("Host", HostName + ":" + Port);

                Headers[1] = new NVPair("Timeout", "Second-86400"); // 1 day
                Headers[2] = new NVPair("If", token);
            }
            catch (Exception e)
            {
                GlobalData.getGlobalData().errorMsg(e.toString());
                return false;
            }
        }
        return true;
    }


    /**
     * Generate a UNLOCK request
     * @see     "RFC 2518"
     * @param lockToken
     * 
     * @return  true if successful, false else  
     */
    public synchronized boolean GenerateUnlock( String lockToken )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateUnlock" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            return false;

        try
        {
            Method = "UNLOCK";
            Body = null;
            Headers = new NVPair[2];
            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair("Host",HostName);
            else
                Headers[0] = new NVPair("Host",HostName + ":" + Port);
            Headers[1] = new NVPair("Lock-Token", "<" + lockToken + ">");
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating UNLOCK\n" + e );
            return false;
        }
        return true;
    }


    /**
     * 
     * @param info
     */
    public synchronized void setExtendedInfo( int code, String data )
    {
        extendedCode = code;
        extendedData = data;
    }


    /**
     * 
     * @param file
     * 
     * @return
     */
    protected String getContentType( String file )
    {
        String content = "application/octet-stream";

        int pos = file.lastIndexOf( "." );
        if( pos >= 0 )
        {
            for( int i=0; i<extensions.length; i+=2 )
            {
                String extension = file.substring( pos+1 ).toLowerCase();
                if( extension.equals(extensions[i]) )
                {
                    content = extensions[i+1];
                    break;
                }
            }
        }
        return content;
    }


    /**
     * When debugging is enabled, print the generated XML
     * @param miniDoc   The XML document (pre-DOM)
     */
    protected void printXML( Document miniDoc )
    {
        String debugOutput = System.getProperty( "debug", "false" );
        if( debugOutput.equals( "true" ) || debugXML )
        {
            System.out.println("generated xml: " );
            XMLOutputStream out = new XMLOutputStream(System.out);
            try
            {
                miniDoc.save(out);
            }
            catch (Exception e)
            {
            }
        }
    }


    protected String[] extensions = { "htm", "text/html",
                                      "html", "text/html",
                                      "gif", "image/gif",
                                      "jpg", "image/jpeg",
                                      "jpeg", "image/jpeg",
                                      "css", "text/css",
                                      "pdf", "application/pdf",
                                      "doc", "application/msword",
                                      "ppt", "application/vnd.ms-powerpoint",
                                      "xls", "application/vnd.ms-excel",
                                      "ps", "application/postscript",
                                      "zip", "application/zip",
                                      "fm", "application/vnd.framemaker",
                                      "mif", "application/vnd.mif",
                                      "png", "image/png",
                                      "tif", "image/tiff",
                                      "tiff", "image/tiff",
                                      "rtf", "text/rtf",
                                      "xml", "text/xml",
                                      "mpg", "video/mpeg",
                                      "mpeg", "video/mpeg",
                                      "mov", "video/quicktime",
                                      "hqx", "application/mac-binhex40",
                                      "au", "audio/basic",
                                      "vrm", "model/vrml",
                                      "vrml", "model/vrml",
                                      "txt", "text/plain",
                                      "c", "text/plain",
                                      "cc", "text/plain",
                                      "cpp", "text/plain",
                                      "h", "text/plain",
                                      "sh", "text/plain",
                                      "bat", "text/plain",
                                      "ada", "text/plain",
                                      "java", "text/plain",
                                      "rc", "text/plain"
                                  };
}
