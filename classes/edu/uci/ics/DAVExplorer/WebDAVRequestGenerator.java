/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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
 * Title:       WebDAVRequest Generator
 * Description: This is where all of the requests are formed. The class contains
 *              static information needed to form all WebDAV requests. When GUI
 *              sends an event indicating that another resource has been
 *              selected it is properly handled by either
 *              tableSelectionChanged() or treeSelectionChanged()
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
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
 */

package edu.uci.ics.DAVExplorer;

import java.util.Vector;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.io.StringReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import HTTPClient.NVPair;
import com.ms.xml.om.Element;
import com.ms.xml.om.Document;
import com.ms.xml.util.XMLOutputStream;
import com.ms.xml.util.Name;

public class WebDAVRequestGenerator implements Runnable
{
    private static String WebDAVPrefix = "http://";
    private static String WebDAVPrefixSSL = "https://";
    private static final int DEFAULT_PORT = 80;
    private static String HostName = "";
    private static int Port = 0;
    private static String Method;
    private static String Path = "";
    private static String ResourceName = "";
    private static String tableResource = "";
    private static String StrippedResource = "";
    private static NVPair[] Headers = null;
    private static byte[] Body = null;
    private static String Extra = new String();
    private static String User = "";
    private static String Password = "";

    private WebDAVTreeNode Node = null;
    private WebDAVTreeNode parentNode = null;

    private static Vector listeners = new Vector();

    private boolean debugXML = false;

    private String userAgent = null;

    // Need to save the following values for the second
    // go around for the Move and Delete when trying
    private WebDAVTreeNode Node2;
    private String ResourceName2;
    private String Dest2;
    private String dir2;
    private boolean Overwrite2;
    private boolean KeepAlive2;
    private boolean secondTime = false;


    public WebDAVRequestGenerator()
    {
        super();
    }

    public void setUser(String username)
    {
        User = username;
    }

    public void setPass(String pass)
    {
        Password = pass;
    }

    public void setUserAgent( String ua )
    {
        userAgent = ua;
    }

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

    public void setSecondTime(boolean b)
    {
    secondTime = b;
    }

    //Yuzo
    //SetResourceName to the value
    public void setResource(String name, WebDAVTreeNode node)
    {
        if( name == null )
            return;

        tableResource = name;
        if (Path.length() == 0)
        {
            ResourceName = new String(name);
        }
        else
        {
            ResourceName = Path + new String(name);
        }

        Node = node;
    }

    public void setNode( WebDAVTreeNode node )
    {
        Node = node;
    }

    public void treeSelectionChanged(ViewSelectionEvent e)
    {
        String Item;
        Path = (String)e.getPath().toString();
        ResourceName = Path + "/";
    }

    public String parseResourceName( boolean escape )
    {
        if (ResourceName.equals(""))
        {
            GlobalData.getGlobalData().errorMsg("No resource selected!");
            return null;
        }
        if( !ResourceName.startsWith(WebDAVPrefix) && !ResourceName.startsWith(WebDAVPrefixSSL) )
        {
            GlobalData.getGlobalData().errorMsg("This operation cannot be executed\non a local resource.");
            return null;
        }
        String stripped;
        if( ResourceName.startsWith(WebDAVPrefix) )
            stripped = ResourceName.substring(WebDAVPrefix.length());
        else
            stripped = ResourceName.substring(WebDAVPrefixSSL.length());
        return parseStripped( stripped, escape );
    }


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


    public String getDefaultName( String appendix )
    {
        String defaultName = parseResourceName( false );
        if( defaultName == null )
            return null;

        if( defaultName.endsWith( "/" ) )
            defaultName = defaultName.substring( 0, defaultName.length()-1 );
        defaultName += appendix;

        return defaultName;
    }


    public void execute()
    {
        AsGen.clear();
        Thread th = new Thread(this);
        th.start();
    }

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

        // add our own user-agent header
        if( userAgent != null )
        {
            NVPair[] newHeaders = new NVPair[Headers.length+1];
            for( int i=0; i<Headers.length; i++ )
                newHeaders[i] = Headers[i];
            newHeaders[Headers.length] = new NVPair( "User-Agent", userAgent );
            Headers = newHeaders;
        }

        WebDAVRequestEvent e = new WebDAVRequestEvent(this, Method,HostName,Port,StrippedResource, Headers, Body, Extra, User, Password, Node );
        Node = null;
        for (int i=0;i<ls.size();i++)
        {
            WebDAVRequestListener l = (WebDAVRequestListener) ls.elementAt(i);
            l.requestFormed(e);
        }
    }


    public synchronized void addRequestListener(WebDAVRequestListener l)
    {
        listeners.addElement(l);
    }


    public synchronized void removeRequestListener(WebDAVRequestListener l)
    {
        listeners.removeElement(l);
    }


    public synchronized boolean DiscoverLock(String method)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::DiscoverLock" );
        }

        Extra = new String(method);
        String[] prop = new String[1];
        String[] schema = new String[1];

        // 1999-June-08, Joachim Feise (jfeise@ics.uci.edu):
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
            prop[0] = new String("lockdiscovery");
            schema[0] = new String(WebDAVProp.DAV_SCHEMA);
            retval = GeneratePropFind(null, "prop", "zero", prop, schema, false );
    }
        if( retval )
        {
            execute();
    }
        return retval;
    }


    public synchronized boolean GeneratePropFindForNode( String FullPath, String command,
                                                         String Depth, String[] props,
                                                         String[] schemas, boolean flag,
                                                         WebDAVTreeNode n )
    {
        Node = n;
        return GeneratePropFind( FullPath, command, Depth, props, schemas, flag);
    }


    public synchronized boolean GenerateOptions(String FullPath  )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateOptions" );
        }

        Headers = null;
        Body = null;

        if (FullPath != null)
        {
            StrippedResource = parseStripped( FullPath, true );
        }
        else
        {
            StrippedResource = parseResourceName( true );
        }
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating OPTIONS Method for " + StrippedResource );
            return false;
        }

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


    public synchronized boolean GeneratePropFind(String FullPath, String command, String Depth, String[] props, String[] schemas, boolean flagGetFilesBelow )
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
            if (Extra.equals("select"))
            {
            // Skip on a selection
            }
            else if (ResourceName.equals(FullPath))
            {
                Extra = "index";
            }
            else
            {
                Extra = "expand";
            }
            ResourceName = FullPath;
            StrippedResource = parseResourceName( true );
        }
        else if (FullPath != null)
        {
            StrippedResource = parseStripped( FullPath, true );
        }
        else
        {
            StrippedResource = parseResourceName( true );
        }
        ok = (StrippedResource != null);

        if (!ok)
        {
            //GlobalData.getGlobalData().errorMsg( "Error Generating PROPFIND Method for " + StrippedResource );
            return false;
        }
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
            Element propElem = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, propFind, asgen );
            propElem.addChild( WebDAVXML.elemNewline, null );
            for (int i=0;i<props.length;i++)
            {
                Element prop = WebDAVXML.createElement( props[i], Element.ELEMENT, propElem, asgen );
                addChild( propElem, prop, 2, false );
            }
            addChild( propFind, propElem, 1, true );
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

    private void addChild( Element parent, Element child, int tabcount, boolean leadingCR )
    {
        addChild( parent, child, tabcount, tabcount, leadingCR, true );
    }

    private void addChild( Element parent, Element child, int leadingTabcount, int trailingTabcount, boolean leadingCR, boolean trailingCR )
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

    private static boolean docContains(Document doc, Element e)
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

    public synchronized boolean GeneratePropPatch(String FullPath, Element addProps, Element removeProps, String locktoken )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GeneratePropPatch" );
        }

        if (FullPath != null)
        {
            StrippedResource = parseStripped( FullPath, true );
        }
        else
        {
            StrippedResource = parseResourceName( true );
        }
        boolean ok = (StrippedResource != null);

        if (!ok)
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating PROPPATCH Method for " + StrippedResource );
            return false;
        }

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
                if( apache.equalsIgnoreCase("no") )
                    Headers[0] = new NVPair( "Host", HostName + ":" + Port );
                else
                    Headers[0] = new NVPair( "Host", HostName );
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
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating MKCOL Method" );
            return false;
        }
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

    public synchronized boolean GenerateGet(String localName)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateGet" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating GET Method" );
            return false;
        }

        Extra = localName;
        Method = "GET";
        Body = null;
        Headers = new NVPair[1];
        if (Port == 0 || Port == DEFAULT_PORT)
            Headers[0] = new NVPair("Host",HostName);
        else
            Headers[0] = new NVPair("Host",HostName + ":" + Port);
        return true;
    }

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
            ResourceName2 = new String(ResourceName);
        }

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating DELETE Method" );
            return false;
        }

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

    // Returns the parent Node , this is used to indicate which
    // Node is beeing writen to by WebDAVResponseInterpreter:parsePut
    // Parent in case of selection of collection in file view
    // and the collection itself in the case of nothing selected in
    // file view window
    public WebDAVTreeNode getPossibleParentOfSelectedCollectionNode()
    {
        return parentNode;
    }

    public void resetParentNode()
    {
        parentNode= null;
    }

    public synchronized boolean GeneratePut(String fileName, String destDir, String lockToken, WebDAVTreeNode selectedCollection)
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
        {
            GlobalData.getGlobalData().errorMsg( "File is not local" );
            return false;
        }

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
            FileInputStream file_in = new FileInputStream(file);
            DataInputStream in = new DataInputStream(file_in);
            Method = "PUT";

            int off = 0;
            int fileSize = (int) file.length();
            Body = new byte[fileSize];
            int rcvd = 0;
            do
            {
                off += rcvd;
                rcvd = file_in.read(Body, off, fileSize-off);
            }
            while (rcvd != -1 && off+rcvd < fileSize);

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
            Headers[2] = new NVPair( "Content-Length", new Integer(fileSize).toString() );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg( "Error generating PUT\n" + e );
            return false;
        }
        return true;
    }

    public synchronized boolean GenerateCopy(String Dest, boolean Overwrite, boolean KeepAlive)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateCopy" );
        }

        Headers = null;
        Body = null;
        Extra = "copy"; //Yuzo added

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating COPY Method" );
            return false;
        }

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


        if( !Dest.startsWith(WebDAVPrefix) && !Dest.startsWith(WebDAVPrefixSSL) )
        {
            if( GlobalData.getGlobalData().doSSL() )
                Dest = WebDAVPrefixSSL + Dest;
            else
                Dest = WebDAVPrefix + Dest;
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

    public synchronized boolean GenerateRename( String Dest, String dir )
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateRename" );
        }
        // Why have the below when the DIscoverLock puts something else
        // the Extra field
        Extra = new String(tableResource);

        return DiscoverLock("rename:" + Dest + ":" + dir );
    }

    public synchronized boolean GenerateMove(String Dest, String dir, boolean Overwrite, boolean KeepAlive, String lockToken, String extraPrefix)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateMove" );
        }

        Headers = null;
        Body = null;
        Extra = extraPrefix +  Dest;

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
        ResourceName2 = new String(ResourceName);
        Dest2 = new String(Dest);
        if( dir != null )
            dir2 = new String(dir);
        else dir2 = null;
        Overwrite2 = Overwrite;
        KeepAlive2 = KeepAlive;
    }


        String srcFile = ResourceName;
        ResourceName = dir;

        ResourceName = srcFile;
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating MOVE Method" );
            return false;
        }
        String ow = (Overwrite) ? "T" : "F";
        if (Dest == null)
        {
            GlobalData.getGlobalData().errorMsg( "Invalid Destination" );
            return false;
        }

        // may be null if invoked from menu
        if( dir == null )
        {
            if( GlobalData.getGlobalData().doSSL() )
                dir = WebDAVPrefixSSL;
            else
                dir = WebDAVPrefix;

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


    public synchronized boolean GenerateLock(String OwnerInfo, String lockToken)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateLock" );
        }

        Headers = null;
        Body = null;
        // Only exclusive write lock is supported at the time
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating LOCK Method for " + StrippedResource );
            return false;
        }

        Method = "LOCK";
        Body = null;
        Extra = lockToken;

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
            Element scopeVal = WebDAVXML.createElement( WebDAVXML.ELEM_EXCLUSIVE, Element.ELEMENT, scopeElem, asgen );
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

    public synchronized boolean GenerateUnlock(String lockToken)
    {
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GenerateUnlock" );
        }

        Headers = null;
        Body = null;

        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
        {
            GlobalData.getGlobalData().errorMsg( "Error Generating UNLOCK Method" );
            return false;
        }

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

    public synchronized void setExtraInfo(String info)
    {
        Extra = info;
    }


    private String getContentType( String file )
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

    private void printXML( Document miniDoc )
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

    private String[] extensions = { "htm", "text/html",
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
