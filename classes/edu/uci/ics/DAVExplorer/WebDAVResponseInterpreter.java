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

// This is the interpreter module that parses WebDAV responses.
// Some of the methods are not parsed, and the functions are left
// empty intentinally.
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
//  1. Fixed parseGet to save the retrieved file properly
//  2. parseMkCol now has functionality to refresh the display

package DAVExplorer;

import HTTPClient.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import com.ms.xml.om.*;
import com.ms.xml.parser.*;
import com.ms.xml.util.*;

public class WebDAVResponseInterpreter
{
    private final static String  HTTPPrefix = "http://";
    private static WebDAVRequestGenerator generator;
    private static byte[] stream = null;
    private static String Method;
    private static String Extra;
    private static HTTPResponse res;
    private static String HostName;
    private static int Port;
    private static String Resource;
    private static Vector listeners = new Vector();
    private static Vector moveListeners = new Vector();
    private static Vector lockListeners = new Vector();
    private final static String EditDir = "Edit";
    private final static String WebDAVClassName = "DAVExplorer";
    private final static String lockInfoFilename = "lockinfo.dat";
    private final static String WebDAVLockDir = "";
    private static String WebDAVEditDir = null;
    private static boolean refresh = false;
    private static boolean inProg = false;
    private static JFrame mainFrame;
    private static String userPathDir;

    private WebDAVTreeNode Node;
    private static CopyResponseListener copyListener;

    private boolean debugXML = false;

    public WebDAVResponseInterpreter()
    { }

    public WebDAVResponseInterpreter(JFrame mainFrame, WebDAVRequestGenerator rg)
    {
        super();
        this.mainFrame = mainFrame;
        //generator = new WebDAVRequestGenerator(mainFrame);
        generator = rg;
        String classPath = System.getProperty("java.class.path");
        if (classPath == null)
        {
            WebDAVEditDir = null;
            return;
        }
        StringTokenizer paths = new StringTokenizer(classPath,":;");
        boolean found = false;
        while (paths.hasMoreTokens())
        {
            String nextPath = paths.nextToken();
            if (!nextPath.endsWith(new Character(File.separatorChar).toString()))
                nextPath += File.separatorChar;
            nextPath += WebDAVClassName + File.separatorChar;
            File classDir = new File(nextPath + "icons");
            if (!classDir.exists())
                continue;
            File editDir = new File(nextPath + EditDir);
            if (!editDir.exists())
                editDir.mkdir();
            WebDAVEditDir = nextPath + EditDir;
        }
        userPathDir = System.getProperty( "user.home" );
        if( userPathDir == null )
            userPathDir = "";
        else
            userPathDir += File.separatorChar;
    }

    public void handleResponse(WebDAVResponseEvent e)
    {
        res = e.getResponse();
        Method = e.getMethodName();
        Extra = e.getExtraInfo();
        HostName = e.getHost();
        Port = e.getPort();

    ByteArrayInputStream ar = new ByteArrayInputStream(e.getResource().getBytes());
        EscapeInputStream iStream = new EscapeInputStream( ar, true );
    DataInputStream dis = new DataInputStream( iStream );
    try{
            Resource = dis.readLine();
    } catch(Exception exc) {
        System.out.println(exc);
    }

        Node = e.getNode();
        try
        {
            if (Method.equals("MOVE"))
            {
                parseMove();
                resetInProgress();
                return;
            }
            if (res.getStatusCode() >= 300)
            {
                resetInProgress();
                errorMsg("DAV Interpreter:\n\n" + res.getStatusCode() + " " + res.getReasonLine());
                return;
            }
        }
        catch (Exception ex)
        {
            System.out.println(ex);
        }

        if (Method.equals("PROPFIND"))
            parsePropFind();
        else if (Method.equals("PROPPATCH"))
            parsePropPatch();
        else if (Method.equals("MKCOL"))
            parseMkCol();
        else if (Method.equals("GET"))
            parseGet();
        else if (Method.equals("PUT"))
            parsePut();
        else if (Method.equals("DELETE"))
            parseDelete();
        else if (Method.equals("COPY"))
        {
            //Original
            //parseCopy();
            try
            {
                if (res.getStatusCode() == 201)
                {
                    executeCopy();
                }
                else
                {
                }
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
        }
        else if (Method.equals("LOCK"))
            parseLock();
        else if (Method.equals("UNLOCK"))
            parseUnlock();
        else
        {
            System.out.println("unsupported method.. cannot parse");
            return;
        }
    }

    protected void saveProps( Element parent, Element prop, int tabs )
    {
        Element newProp = null;
        if( prop.getType()  == Element.PCDATA )
        {
            newProp = new ElementImpl( null,Element.PCDATA );
            newProp.setText( prop.getText() );
        }
        else if( prop.getType() == Element.ELEMENT )
        {
            newProp = new ElementImpl( prop.getTagName(), Element.ELEMENT );
        }
        else
        {
            return;
        }

        parent.addChild( WebDAVXML.elemNewline,null );
        for( int t=0; t<tabs; t++ )
            parent.addChild( WebDAVXML.elemDSpace,null );
        parent.addChild( newProp,null );

        if( prop.numElements() > 0 )
        {
            Enumeration propEnum = prop.getElements();
            while (propEnum.hasMoreElements())
            {
                Element propEl = (Element) propEnum.nextElement();
                if ( (propEl.getType() == Element.ELEMENT) || (propEl.getType() == Element.PCDATA) )
                {
                    saveProps( newProp, propEl, tabs+1 );
                }
            }
            newProp.addChild( WebDAVXML.elemNewline,null );
            for( int t=0; t<tabs; t++ )
                newProp.addChild( WebDAVXML.elemDSpace,null );
        }
    }


    public void parsePropFind()
    {
        byte[] body = null;
        Document xml_doc = null;

        try
        {
            body = res.getData();
            stream = body;
            if (body == null)
            {
                errorMsg("DAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            EscapeInputStream iStream = new EscapeInputStream( byte_in, true );
            xml_doc = new Document();
            xml_doc.load( iStream );
        }
        catch (Exception e)
        {
            errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                            stream = null;
            return;
        }

        printXML( body );

        if (Extra.equals("uribox"))
        {
            if( Port > 0 )
            {
                fireInsertionEvent(HostName + ":" + Port + Resource);
            }
            else
            {
                fireInsertionEvent(HostName + Resource);
            }
        }
        else if (Extra.equals("lock") || Extra.equals("unlock") || Extra.equals("delete") || Extra.startsWith("rename:") || Extra.equals("display")
           || Extra.equals("commit") )
        {
            // get lock information out of XML tree
            String lockToken = null;
            String ownerInfo = "";
            String lockType = "";
            String lockScope = "";
            String lockTimeout = "";
            String lockDepth = "";

            String[] token = new String[5];
            token[0] = new String( WebDAVXML.ELEM_RESPONSE );
            token[1] = new String( WebDAVXML.ELEM_PROPSTAT );
            token[2] = new String( WebDAVXML.ELEM_PROP );
            token[3] = new String( WebDAVProp.PROP_LOCKDISCOVERY );
            token[4] = new String( WebDAVXML.ELEM_ACTIVE_LOCK );

            Element rootElem = skipElements( xml_doc, token );
            if( rootElem != null )
            {
                TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
                while( enumTree.hasMoreElements() )
                {
                    Element current = (Element)enumTree.nextElement();
                    Name currentTag = current.getTagName();
                    if( currentTag != null )
                    {
                        if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_TOKEN ) )
                        {
                            lockToken = getLockToken( current );
                        }
                        else if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_TYPE ) )
                        {
                            lockType = getLockType( current );
                        }
                        else if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_SCOPE ) )
                        {
                            lockScope = getLockScope( current );
                        }
                        else if( currentTag.getName().equals( WebDAVXML.ELEM_OWNER ) )
                        {
                            ownerInfo = getOwnerInfo( current );
                        }
                        else if( currentTag.getName().equals( WebDAVXML.ELEM_TIMEOUT ) )
                        {
                            lockTimeout = getLockTimeout( current );
                        }
                        else if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_DEPTH ) )
                        {
                            lockDepth = getLockDepth( current );
                        }
                    }
                }
            }
            if (lockToken != null)
            {
                lockToken.trim();
                int pos = lockToken.indexOf("opaque");
                lockToken = lockToken.substring(pos);
            }
            if (Extra.equals("lock"))
            {
                String lockInfo = getLockInfo();
                generator.GenerateLock(lockInfo,lockToken);
                generator.execute();
            }
            else if (Extra.equals("unlock"))
            {
                if (lockToken != null)
                {
                    generator.GenerateUnlock(lockToken);
                    generator.execute();
                }
            }
            else if (Extra.equals("delete"))
            {
                generator.setNode(Node); // sets the Node which will be operated
                generator.GenerateDelete(lockToken);
                generator.execute();
            }
            else if (Extra.startsWith("rename:"))
            {
                int pos = Extra.indexOf(":");
                String tmp = Extra.substring(pos + 1);
                pos = tmp.indexOf( ":" );
                String dest = null;
                String dir = null;
                if( pos >= 0 )
                {
                    dest = tmp.substring( 0, pos );
                    dir = tmp.substring( pos + 1 );
                }
                else
                    dest = tmp;

        clearStream();
        //Old
        generator.setNode(Node);
                generator.GenerateMove(dest, dir, false, true, lockToken);
                generator.execute();
            }
            else if (Extra.equals("display"))
            {
                displayLock(lockType, lockScope, lockDepth, lockToken, lockTimeout, ownerInfo);
            }
            else if (Extra.equals("commit"))
            {
                String newRes = Resource.substring(1);
                String fileName =  WebDAVEditDir + File.separatorChar + newRes;
                File theFile = new File(fileName);
                if (!theFile.exists())
                {
                    errorMsg("File not found!\n");
                    return;
                }
                else
                {
                    generator.GeneratePut(fileName, newRes, lockToken);
                    generator.execute();
                }
            }
        }
        else if (Extra.equals("properties"))
        {
            Document ppatchDoc = new Document();
            Enumeration docEnum = xml_doc.getElements();
            while (docEnum.hasMoreElements())
            {
                Element nameEl = (Element) docEnum.nextElement();
                if (nameEl.getType() == Element.ELEMENT)
                    break;
                ppatchDoc.addChild(nameEl,null);
            }
            ppatchDoc.addChild(WebDAVXML.elemNewline,null);

            // write header
            ByteArrayOutputStream byte_prop = new ByteArrayOutputStream();
            XMLOutputStream  xml_prop = new XMLOutputStream(byte_prop);
            byte[] prop_out = null;
            try
            {
                ppatchDoc.save(xml_prop);
            }
            catch( Exception e )
            {
                errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                                stream = null;
                return;
            }

            String[] token = new String[1];
            token[0] = new String( WebDAVXML.ELEM_RESPONSE );
            Element rootElem = skipElements( xml_doc, token );
            if( rootElem != null )
            {
                TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
                while( enumTree.hasMoreElements() )
                {
                    Element current = (Element)enumTree.nextElement();
                    Name currentTag = current.getTagName();
                    if( currentTag != null )
                    {
                        if( currentTag.getName().equals( WebDAVXML.ELEM_PROPSTAT ) )
                        {
                            parseProperties( current, xml_prop );
                        }
                    }
                }

                prop_out = byte_prop.toByteArray();
                String host = HostName;
                if (Port > 0)
                    host = HostName + ":" + Port;
                PropDialog pd = new PropDialog(Resource,host,new String(prop_out), false);
                pd.addPropDialogListener(new propDialogListener());
            }
        }
        else if(Extra.equals("expand"))
        {
        }
        else if(Extra.equals("index"))
        {
        }
        else
        {
            //  "refresh"
            setRefresh();
            fireInsertionEvent(null);
        }
    }

    public String getLockInfo()
    {
        File lockFile = new File(userPathDir + lockInfoFilename);
        if (!lockFile.exists())
            return new String("");
        String lockInfo = null;
        try
        {
            FileInputStream fin = new FileInputStream(lockFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(fin));
            lockInfo = in.readLine();
            in.close();
        }
        catch (Exception fileEx)
        { }
        if (lockInfo == null)
            return new String("");
        else
        return lockInfo;
    }

    public boolean Refreshing()
    {
        return refresh;
    }

    public void ResetRefresh()
    {
        refresh = false;
    }

    public void setRefresh()
    {
        refresh = true;
    }

    public void parsePropPatch()
    {
        // inform the user
        setRefresh();
        fireInsertionEvent(null);
    }

    public void parseMkCol()
    {
    //old
        // inform the user
        //setRefresh();
        //fireInsertionEvent(null);

        clearStream();
        CopyResponseEvent e = new CopyResponseEvent( this, Node);
        copyListener.CopyEventResponse(e);
    }

    public void parseGet()
    {
        // inform the user
        byte[] body = null;
        String fileName = "";
        try
        {
            FileOutputStream fout = null;
            String newRes = Resource.substring(1);
            if (Extra.equals("saveas"))
            {
                FileDialog fd = new FileDialog(mainFrame, "Save As" , FileDialog.SAVE);
                fd.setVisible(true);
                fileName = fd.getDirectory() + fd.getFile();
            }
            else
            {
                fileName =  WebDAVEditDir + File.separatorChar + newRes;
                // write the proper separator
                StringBuffer fName = new StringBuffer( fileName );
                for( int pos = 0; pos < fName.length(); pos++ )
                {
                    if( (fName.charAt(pos) == '/') || (fName.charAt(pos) == '\\') )
                        fName.setCharAt( pos, File.separatorChar );
                }
                fileName = fName.toString();
            }

            // create all subdirectories as necessary
            String dir = fileName.substring( 0, fileName.lastIndexOf( File.separatorChar ) );
            File theDir = new File( dir );
            theDir.mkdirs();

            File theFile = new File(fileName);
            boolean bSave = true;
            if (theFile.exists())
            {
                //if (!replaceFile(newRes))
                if (!replaceFile(fileName))
                {
                    bSave = false;
                    if ( (Extra.equals("view")) || (Extra.equals("edit")) )
                    {
                        if( !launchAnyway() )
                        {
                            return;
                        }
                    }
                }
            }
            if( bSave )
            {
                body = res.getData();
                fout = new FileOutputStream(fileName);
                if (fout == null)
                    return;
                fout.write(body);
                fout.close();
            }

            if( Extra.equals("view") || Extra.equals("edit") )
            {
                String app = selectApplication();
                if( (app != null) && (app != "") )
                {
                    Runtime rt = Runtime.getRuntime();
                    String[] cmdarray =  new String[2];
                    cmdarray[0] = app;
                    cmdarray[1] = fileName;
                    rt.exec( cmdarray );
                }
            }
        }
        catch (Exception exc)
        {
            return;
        }
    }

    public void parsePut()
    {
    //Old
        // inform the user
        //setRefresh();
        //fireInsertionEvent(null);

    // Piggy back on the Copy Response stuff
    clearStream();
    CopyResponseEvent e = new CopyResponseEvent( this, Node);
    copyListener.CopyEventResponse(e);
    }

    public void parseDelete()
    {
    //Old
        // inform the user
        //setRefresh();
        //fireInsertionEvent(null);

        // Piggy back on the Copy Response stuff
        clearStream();
        CopyResponseEvent e = new CopyResponseEvent( this, Node);
        copyListener.CopyEventResponse(e);
    }

    public void addCopyResponseListener( CopyResponseListener l)
    {
        // Add only one for now
        copyListener = l;
    }

    public void executeCopy()
    {
        CopyResponseEvent e = new CopyResponseEvent( this, Node);

        copyListener.CopyEventResponse(e);
    }

    public void parseCopy()
    {
        // inform the user
        setRefresh();
        fireInsertionEvent(null);
    }

    public void parseMove()
    {
        try
        {
            if (res.getStatusCode() >= 300)
            {
                errorMsg("DAV Interpreter:\n\n" + res.getStatusCode() + " " + res.getReasonLine());
            }
        }
        catch( Exception e )
        {
        System.out.println(e);
        }

        clearStream();
        CopyResponseEvent e = new CopyResponseEvent( this, Node);
        copyListener.CopyEventResponse(e);
    }

    public void parseLock()
    {
        byte[] body = null;
        Document xml_doc = null;
        try
        {
            body = res.getData();
            stream = body;
            if (body == null)
            {
                errorMsg("DAV Interpreter:\n\nMissing XML body in\nLOCK response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            EscapeInputStream iStream = new EscapeInputStream( byte_in, true );
            xml_doc = new Document();
            xml_doc.load( iStream );
        }
        catch (Exception e)
        {
            errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing LOCK Response.\n" + e);
                            stream = null;
            return;
        }

        printXML( body );

        String lockToken = null;
        String[] token = new String[2];
        token[0] = new String( WebDAVProp.PROP_LOCKDISCOVERY );
        token[1] = new String( WebDAVXML.ELEM_ACTIVE_LOCK );

        Element rootElem = skipElements( xml_doc, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_TOKEN ) )
                    {
                        lockToken = getLockToken( current );
                        lockToken.trim();
                        int pos = lockToken.indexOf("opaque");
                        lockToken = lockToken.substring(pos);
                        break;
                    }
                }
            }
        }
        fireLockEvent( 0, lockToken );
    }

    public void parseUnlock()
    {
        // inform the user
        fireLockEvent( 1, null );
    }

    public void clearStream()
    {
        stream = null;
    }

    public byte[] getXML()
    {
        return stream;
    }

    public synchronized void addInsertionListener(ActionListener l)
    {
        listeners.addElement(l);
    }

    public synchronized void removeInsertionListener(ActionListener l)
    {
        listeners.removeElement(l);
    }

    public synchronized void addMoveUpdateListener(ActionListener l)
    {
        moveListeners.addElement(l);
    }

    public synchronized void removeMoveUpdateListener(ActionListener l)
    {
        moveListeners.removeElement(l);
    }

    public synchronized void addLockListener(ActionListener l)
    {
        lockListeners.addElement(l);
    }

    public synchronized void removeLockListener(ActionListener l)
    {
        lockListeners.removeElement(l);
    }

    public void fireInsertionEvent(String str)
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) listeners.clone();
        }
        ActionEvent e = new ActionEvent(this,0,str);
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }

    public void fireMoveUpdate(String str)
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) moveListeners.clone();
        }
        ActionEvent e = new ActionEvent(this,0,str);
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }

    public void fireLockEvent(int id, String str)
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) lockListeners.clone();
        }
        ActionEvent e = new ActionEvent( this, id, str );
        for (int i=0;i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }

    public boolean inProgress()
    {
        return inProg;
    }

    public void setInProgress()
    {
        inProg = true;
    }

    public void resetInProgress()
    {
        inProg = false;
    }

    public String getResource()
    {
        return Resource;
    }

    public String getHost()
    {
        return HostName;
    }

    class propDialogListener implements PropDialogListener
    {
        public void propDialog(PropDialogEvent e)
        {
            generator.handlePropPatch(e);
        }
    }

    public void errorMsg(String str)
    {
        JOptionPane pane = new JOptionPane();
        Object[] options = { "OK" };
        pane.showOptionDialog(mainFrame,str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }

    public boolean replaceFile(String fileName)
    {
        JOptionPane pane = new JOptionPane();
        String str = new String(fileName + " exists.\nReplace?\n");
        int opt = pane.showConfirmDialog(null,str,"File Exists",JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }

    public boolean launchAnyway()
    {
        JOptionPane pane = new JOptionPane();
        String str = new String("View in application?");
        int opt = pane.showConfirmDialog(null,str,"Launch Application?",JOptionPane.YES_NO_OPTION);
        if (opt == JOptionPane.YES_OPTION)
            return true;
        else
            return false;
    }

    public String selectApplication()
    {
        JOptionPane pane = new JOptionPane();
        String str = new String("Select the application to show this file");
        String ret = pane.showInputDialog(null,str,"Select Application",JOptionPane.QUESTION_MESSAGE);
        return ret;
    }

    public void displayLock(String LockType, String LockScope, String LockDepth, String LockToken, String LockTimeout, String LockOwner )
    {
        JOptionPane pane = new JOptionPane();
        Object [] options = { "OK" };
        if (LockToken == null)
            LockToken = "";
        String str = new String("Lock Type:  " + LockType +
                          "\nLock Scope: " + LockScope +
                          "\nLock Depth: " + LockDepth +
                          "\nLock Owner: " + LockOwner +
                          "\nLock Token: " + LockToken+
                          "\nTimeout:    " + LockTimeout+"\n");
        pane.showOptionDialog(mainFrame,str, "Lock Information", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,options, options[0]);
    }


    private String getLockToken( Element locktoken )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( locktoken );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && tag.getName().equals( WebDAVXML.ELEM_HREF ) )
            {
                Element token = (Element)treeEnum.nextElement();
                if( (token != null) && (token.getType() == Element.PCDATA) )
                    return token.getText();
            }
        }
        return null;
    }


    private String getLockType( Element locktype )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( locktype );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && !tag.getName().equals( WebDAVXML.ELEM_LOCK_TYPE ) )
                return tag.getName();
        }
        return "";
    }


    private String getLockScope( Element lockscope )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( lockscope );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && !tag.getName().equals( WebDAVXML.ELEM_LOCK_SCOPE ) )
                return tag.getName();
        }
        return "";
    }


    private String getOwnerInfo( Element ownerinfo )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( ownerinfo );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( (tag != null) && tag.getName().equals( WebDAVXML.ELEM_OWNER ) )
            {
                current = (Element)treeEnum.nextElement();
                if( current != null )
                {
                    tag = current.getTagName();
                    if( (tag != null) && tag.getName().equals( WebDAVXML.ELEM_HREF ) )
                    {
                        Element token = (Element)treeEnum.nextElement();
                        if( (token != null) && token.getType() == Element.PCDATA )
                            return token.getText();
                    }
                }
            }
        }
        return "";
    }


    private String getLockTimeout( Element locktimeout )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( locktimeout );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            if( (current != null) && (current.getType() == Element.PCDATA) )
                return current.getText();
        }
        return "";
    }


    private String getLockDepth( Element lockdepth )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( lockdepth );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            if( (current != null) && (current.getType() == Element.PCDATA) )
                return current.getText();
        }
        return "";
    }


    private boolean checkHrefValue( Element el )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( el );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( tag.getName().equals( WebDAVXML.ELEM_HREF ) )
            {
                Element token = (Element)treeEnum.nextElement();
                if( token.getType() == Element.PCDATA )
                {
                    String HrefValue = token.getText();
                    int pos = HrefValue.indexOf( HTTPPrefix );
                    if( pos >= 0 )
                        HrefValue = HrefValue.substring( pos+HTTPPrefix.length() );
                    pos = HrefValue.indexOf( "/" );
                    if( pos >= 0 )
                        HrefValue = HrefValue.substring( pos );
                    if (HrefValue.length() == 0)
                        HrefValue = "/";
                    if (HrefValue.equals(Resource))
                        return true;
                }
            }
        }
        return false;
    }

    private Element skipElements( Document xml_doc, String[] token )
    {
        Element rootElem = (Element)xml_doc.getRoot();
        return skipElements( rootElem, token );
    }

    private Element skipElements( Element rootElem, String[] token )
    {
        int index = 0;
        TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
        while( enumTree.hasMoreElements() )
        {
            Element current = (Element)enumTree.nextElement();
            Name currentTag = current.getTagName();
            if( index >= 0 )
            {
                if( (currentTag != null) && (currentTag.getName().equals( token[index] )) )
                {
                    if( currentTag.getName().equals( WebDAVXML.ELEM_HREF ) )
                    {
                        if( !checkHrefValue( current ) )
                            break;
                    }
                    else
                    {
                        // we only care about the subtree from this point on
                        // NOTE: do not get the href subtree, since the href tree
                        // is a sibling to the tree we need
                        enumTree = new TreeEnumeration( current );
                    }
                    index++;
                }
                if( index >= token.length )
                    return current;
            }
        }
        return null;
    }


    private void parseProperties( Element properties, XMLOutputStream xml_prop )
    {
        String[] token = new String[2];
        token[0] = new String( WebDAVXML.ELEM_PROPSTAT );
        token[1] = new String( WebDAVXML.ELEM_PROP );
        Element rootElem = skipElements( properties, token );
        if( rootElem != null )
        {
            TreeEnumeration enumTree =  new TreeEnumeration( rootElem );
            while( enumTree.hasMoreElements() )
            {
                Element current = (Element)enumTree.nextElement();
                Name currentTag = current.getTagName();
                if( currentTag != null )
                {
                    try
                    {
                        // create a tree of all property tags, nicely formatted
                        AsGen alias = new AsGen();
                        Element outProp = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, null, alias );
                        Enumeration propValEnum = current.getElements();
                        while (propValEnum.hasMoreElements())
                        {
                            Element propValEl = (Element) propValEnum.nextElement();
                            if (propValEl.getType() != Element.ELEMENT)
                                continue;
                            saveProps( outProp, propValEl, 1 );
                        }
                        outProp.addChild(WebDAVXML.elemNewline,null);
                        outProp.save(xml_prop);
                        return;
                    }
                    catch( Exception e )
                    {
                        errorMsg("DAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                                        stream = null;
                        return;
                    }
                }
            }
        }
    }

    private void printXML( byte[] body )
    {
        String debugOutput = System.getProperty( "debug", "false" );
        if( debugOutput.equals( "true" ) || debugXML )
        {
            System.out.println("Received xml:");
            XMLOutputStream out = new XMLOutputStream(System.out);
            ByteArrayInputStream tmpIn = new ByteArrayInputStream(body);
            Document tmpDoc = new Document();
            try
            {
                tmpDoc.load(tmpIn);
                tmpDoc.save(out);
            }
            catch (Exception e)
            {
            }
        }
    }
}
