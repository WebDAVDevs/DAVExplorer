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

// This is where all of the requests are formed. The class contains
// static information needed to form all WebDAV requests. When GUI
// sends an event indicating that another resource has been
// selected it is properly handled by either
// tableSelectionChanged() or treeSelectionChanged()
//
// Version: 0.3
// Author:  Robert Emmery
// Date:    4/2/98
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
//
// Date: 2001-Jan-12
// Joe Feise: Added support for https (SSL)

package DAVExplorer;

import HTTPClient.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;
import com.ms.xml.om.*;
import com.ms.xml.parser.*;
import com.ms.xml.util.*;

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


    public synchronized boolean GeneratePropFindForNode(   String FullPath,
                            String command,
                            String Depth,
                            String[] props,
                            String[] schemas,
                            boolean flag,
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

        AsGen asgen = new AsGen();
        WebDAVXML.createNamespace( asgen, null );
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

    public synchronized boolean GeneratePropPatch(String Host, int port, String Res, Document old_xml, Document new_xml)
    {
        // need to determine here the patches (if any)
        if( GlobalData.getGlobalData().getDebugRequest() )
        {
            System.err.println( "WebDAVRequestGenerator::GeneratePropPatch" );
        }

        Headers = null;
        Body = null;
        boolean setUsed = false;
        boolean removeUsed = false;
        Method = "PROPPATCH";
        Document miniDoc = new Document();
        miniDoc.setVersion("1.0");
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        AsGen asgen = new AsGen();
        WebDAVXML.createNamespace( asgen, null );
        Element propUpdate = WebDAVXML.createElement( WebDAVXML.ELEM_PROPERTY_UPDATE, Element.ELEMENT, null, asgen );

        Element setEl = WebDAVXML.createElement( WebDAVXML.ELEM_SET, Element.ELEMENT, propUpdate, asgen );
        Element removeEl = WebDAVXML.createElement( WebDAVXML.ELEM_REMOVE, Element.ELEMENT, propUpdate, asgen );
        Element setProp = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, setEl, asgen );
        Element removeProp = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, removeEl, asgen );
        addChild( setEl, setProp, 2, true );
        addChild( removeEl, removeProp, 2, true );

        Enumeration namesEnum = new_xml.getElements();
        while (namesEnum.hasMoreElements())
        {
            Element nameEl = (Element) namesEnum.nextElement();
            if (nameEl.getType() != Element.NAMESPACE)
                continue;
            miniDoc.addChild(nameEl,null);
            miniDoc.addChild(WebDAVXML.elemNewline,null);
        }

        // if any of the properties were added, insert them into set elem
        miniDoc.addChild(propUpdate,null);
        miniDoc.addChild(WebDAVXML.elemNewline,null);

        Enumeration newEnum = new_xml.getElements();
        while (newEnum.hasMoreElements())
        {
            Element propElem = (Element) newEnum.nextElement();
            Name propTag = propElem.getTagName();
            if (propTag == null)
                continue;
            if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
                continue;
            Enumeration propEnum = propElem.getElements();
            while (propEnum.hasMoreElements())
            {
                Element prop = (Element) propEnum.nextElement();
                if (prop.getType() != Element.ELEMENT)
                    continue;
                if (!docContains(old_xml,prop))
                {
                    setUsed = true;
                    setProp.addChild(WebDAVXML.elemTab,null);
                    setProp.addChild(prop,null);
                    setProp.addChild(WebDAVXML.elemNewline,null);
                    setProp.addChild(WebDAVXML.elemTab,null);
                    setProp.addChild(WebDAVXML.elemTab,null);
                }
            }
        }

        Enumeration OldEnum = old_xml.getElements();
        while (OldEnum.hasMoreElements())
        {
            Element propElem = (Element) OldEnum.nextElement();
            Name propTag = propElem.getTagName();
            if (propTag == null)
                continue;
            if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
                continue;
            Enumeration propEnum = propElem.getElements();
            while (propEnum.hasMoreElements())
            {
                Element prop = (Element) propEnum.nextElement();
                if (prop.getType() != Element.ELEMENT)
                    continue;
                if (!docContains(new_xml,prop))
                {
                    removeUsed = true;
                    removeProp.addChild(WebDAVXML.elemTab,null);
                    Element remEl = new ElementImpl(prop.getTagName(),Element.ELEMENT);
                    removeProp.addChild(remEl,null);
                    removeProp.addChild(WebDAVXML.elemNewline,null);
                    removeProp.addChild(WebDAVXML.elemTab,null);
                    removeProp.addChild(WebDAVXML.elemTab,null);
                }
            }
        }
        if ( (!setUsed) && (!removeUsed))
        {
            return false;
        }

        if (setUsed)
        {
            propUpdate.addChild(WebDAVXML.elemTab,null);
            propUpdate.addChild(setEl,null);
            propUpdate.addChild(WebDAVXML.elemNewline,null);
        }
        if (removeUsed)
        {
            propUpdate.addChild(WebDAVXML.elemTab,null);
            propUpdate.addChild(removeEl,null);
            propUpdate.addChild(WebDAVXML.elemNewline,null);
        }

        StrippedResource = Res;
        HostName = Host;
        Port = port;

        ByteArrayOutputStream byte_str = new ByteArrayOutputStream();
        XMLOutputStream xml_out = new XMLOutputStream(byte_str);

        try
        {
            miniDoc.save(xml_out);
            Body = byte_str.toByteArray();

            Headers = new NVPair[3];
            if (Port == 0 || Port == DEFAULT_PORT)
                Headers[0] = new NVPair("Host", HostName);
            else
                Headers[0] = new NVPair("Host", HostName + ":" + Port);
            Headers[1] = new NVPair("Content-Type", "text/xml");
            Headers[2] = new NVPair("Content-Length", new Long(Body.length).toString());

            printXML( miniDoc );
        }
        catch (Exception e)
        {
            GlobalData.getGlobalData().errorMsg("XML Generator Error: \n" + e);
            return false;
        }

        execute();
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
    public WebDAVTreeNode getPossibleParentOfSelectedCollectionNode(){
    return parentNode;
    }

    public void resetParentNode(){
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
            //Dest =  HostName + ":" + Port + StrippedResource + "/" + Dest + "_copy" ;


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

            AsGen asgen = new AsGen();
            WebDAVXML.createNamespace( asgen, null );
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

    /*
        StrippedResource = parseResourceName( true );
        if( StrippedResource == null )
            dir = "/";
        else
            dir = StrippedResource;
    */

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


    /*
    if( Port==0 || Port==DEFAULT_PORT )
            Dest = HostName + Dest;
        else
            Dest = HostName + ":" + Port + Dest ;

        if( !Dest.startsWith(WebDAVPrefix) )
            Dest = WebDAVPrefix + Dest;
    */

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

            AsGen asgen = new AsGen();
            WebDAVXML.createNamespace( asgen, null );
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

            AsGen asgen = new AsGen();
            WebDAVXML.createNamespace( asgen, null );
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
//                Headers[1] = new NVPair("Timeout", "Infinite");
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

    public void handlePropPatch(PropDialogEvent e)
    {
        String hostname = e.getHost();
        String host = null;
        String res = e.getResource();
        byte[] old_bytes = e.getInitialData().getBytes();
        byte[] new_bytes = e.getData().getBytes();

        int pos = hostname.indexOf(":");
        int port = 0;
        if (pos > 0)
        {
            try
            {
                host = hostname.substring(0,pos);
                port = Integer.parseInt(hostname.substring(pos+1));
            }
            catch (Exception exception)
            {
                return;
            }
        }

        // Generate XML
        Document oldProp = null;
        Document newProp = null;
        try
        {
            ByteArrayInputStream old_b = new ByteArrayInputStream(old_bytes);
            XMLInputStream old_in = new XMLInputStream(old_b);
            oldProp = new Document();
            oldProp.load(old_in);

            ByteArrayInputStream new_b = new ByteArrayInputStream(new_bytes);
            XMLInputStream new_in = new XMLInputStream(new_b);
            newProp = new Document();
            newProp.load(new_in);
        }
        catch (Exception ex)
        {
            GlobalData.getGlobalData().errorMsg("PROPPATCH Failed! \nXML Parsing Error: \n\n" + ex);
            return;
        }
        GeneratePropPatch(host,port,res,oldProp,newProp);
    }


    private String getContentType( String file )
    {
        String content = "application/octet-stream";

        int pos = file.lastIndexOf( "." );
        if( pos >= 0 )
        {
            String extension = file.substring( pos+1 ).toLowerCase();
            if( extension.equals( "htm" ) || extension.equals( "html" ) )
                content = "text/html";
            else if( extension.equals( "gif" ) )
                content = "image/gif";
            else if( extension.equals( "jpg" ) || extension.equals( "jpeg" ) )
                content = "image/jpeg";
            else if( extension.equals( "css" ) )
                content = "text/css";
            else if( extension.equals( "pdf" ) )
                content = "application/pdf";
            else if( extension.equals( "doc" ) )
                content = "application/msword";
            else if( extension.equals( "ppt" ) )
                content = "application/vnd.ms-powerpoint";
            else if( extension.equals( "xls" ) )
                content = "application/vnd.ms-excel";
            else if( extension.equals( "ps" ) )
                content = "application/postscript";
            else if( extension.equals( "zip" ) )
                content = "application/zip";
            else if( extension.equals( "fm" ) )
                content = "application/vnd.framemaker";
            else if( extension.equals( "mif" ) )
                content = "application/vnd.mif";
            else if( extension.equals( "png" ) )
                content = "image/png";
            else if( extension.equals( "tif" ) || extension.equals( "tiff" ) )
                content = "image/tiff";
            else if( extension.equals( "rtf" ) )
                content = "text/rtf";
            else if( extension.equals( "xml" ) )
                content = "text/xml";
            else if( extension.equals( "mpg" ) || extension.equals( "mpeg" ) )
                content = "video/mpeg";
            else if( extension.equals( "mov" ) )
                content = "video/quicktime";
            else if( extension.equals( "hqx" ) )
                content = "application/mac-binhex40";
            else if( extension.equals( "au" ) )
                content = "audio/basic";
            else if( extension.equals( "vrm" ) || extension.equals( "vrml" ) )
                content = "model/vrml";
            else if( extension.equals( "txt" ) || extension.equals( "c" ) || extension.equals( "cc" ) || extension.equals( "cpp" ) ||
                extension.equals( "h" ) || extension.equals( "sh" ) || extension.equals( "bat" ) || extension.equals( "ada" ) ||
                extension.equals( "java" ) || extension.equals( "rc" ) )
                content = "text/plain";
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
}
