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
// Author:  Robert Emmery  <memmery@earthlink.net>
// Date:    4/2/98
//////////////////////////////////////////////////////////////


package WebDAV;

import WebDAV.WebDAVManager;
import HTTPClient.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import com.sun.java.swing.*;
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
    private final static String WebDAVClassName = "WebDAV";
    private final static String lockInfoFilename = "lockinfo.dat";
    private final static String WebDAVLockDir = "";
    private static String WebDAVEditDir = null;
    private static boolean refresh = false;
    private static boolean inProg = false;
    private static JFrame mainFrame;
    private static String classPathDir;

    public WebDAVResponseInterpreter()
    { }

    public WebDAVResponseInterpreter(JFrame mainFrame)
    {
        super();
        this.mainFrame = mainFrame;
        generator = new WebDAVRequestGenerator(mainFrame);
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
            classPathDir = nextPath;
            File editDir = new File(nextPath + EditDir);
            if (!editDir.exists())
                editDir.mkdir();
            WebDAVEditDir = nextPath + EditDir;
        }
    }

    public void handleResponse(WebDAVResponseEvent e)
    {
        res = e.getResponse();
        Method = e.getMethodName();
        Extra = e.getExtraInfo();
        HostName = e.getHost();
        Port = e.getPort();
        Resource = e.getResource();
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
                errorMsg("WebDAV Interpreter:\n\n" + res.getStatusCode() + " " + res.getReasonLine());
                return;
            }
        }
        catch (Exception ex)
        { }
   
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
            parseCopy();   
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

    public void saveProps(Element parent, Element fileProp, int tabs)
    {
        Element newProp = null;
        if (fileProp.getType()  == Element.PCDATA)
        {
            newProp = new ElementImpl(null,Element.PCDATA);
            newProp.setText(fileProp.getText());
        }
        else if  (fileProp.getType() == Element.ELEMENT)
        {
            newProp = new ElementImpl(fileProp.getTagName(), Element.ELEMENT);
        }
        else
        {
            return;
        }

        parent.addChild(WebDAVXML.elemDSpace,null);
        parent.addChild(newProp,null);
        parent.addChild(WebDAVXML.elemNewline,null);

        for (int t=0;t<tabs;t++)
            parent.addChild(WebDAVXML.elemDSpace,null);

        if (newProp.getType() == Element.PCDATA)
        {
            newProp.addChild(WebDAVXML.elemNewline,null);
            for (int t=0;t<tabs + 1;t++)
                newProp.addChild(WebDAVXML.elemDSpace,null);
            return;
        }
        Enumeration propEnum = fileProp.getElements();
        while (propEnum.hasMoreElements())
        {
            Element propEl = (Element) propEnum.nextElement();

            if ( (propEl.getType() == Element.ELEMENT) || (propEl.getType() == Element.PCDATA) )
            {
                newProp.addChild(WebDAVXML.elemNewline,null);
                for (int t=0;t<tabs + 1;t++)
                    newProp.addChild(WebDAVXML.elemDSpace,null);
                saveProps(newProp, propEl, ++tabs);
            }
        }
    }

    public void parsePropFind()
    {
        try
        {
            byte[] body = res.getData(); 
            stream = body;
            if (body == null)
            {
                errorMsg("WebDAV Interpreter:\n\nMissing XML body in\nPROPFIND response.");
                return;
            }
            ByteArrayInputStream byte_in = new ByteArrayInputStream(body);
            Document xml_doc = new Document();
            xml_doc.load(byte_in);

            System.out.println("Received xml:");
            XMLOutputStream out = new XMLOutputStream(System.out);
            ByteArrayInputStream tmpIn = new ByteArrayInputStream(body);
            Document tmpDoc = new Document();
            tmpDoc.load(tmpIn);
            tmpDoc.save(out);

            if (Extra.equals("uribox"))
            {
                if( Port > 0 )
                {
                    System.out.println("HostName + Port + Resource: " + HostName + Port + Resource);
                    fireInsertionEvent(HostName + ":" + Port + Resource);
                }
                else
                {
                    System.out.println("HostName + Resource: " + HostName + Resource);
                    fireInsertionEvent(HostName + Resource);
                }
            }
            else if (Extra.equals("lock") || Extra.equals("unlock") || Extra.equals("delete") || Extra.startsWith("rename:") || Extra.equals("display")
               || Extra.equals("commit") )
            {
                String lockToken = null;
                String ownerInfo = "";
                String lockType = "";
                String lockScope = "";
                String lockTimeout = "";
                String lockDepth = "";

                System.out.println("In parsePropFind");
                Element rootElem = (Element) xml_doc.getRoot();
                Enumeration enumRoot = rootElem.getElements();
                while (enumRoot.hasMoreElements())
                {
                    Element respElem = (Element) enumRoot.nextElement();
                    Name respTag = respElem.getTagName();
                    if (respTag == null)
                        continue;
                    if (!respTag.getName().equals(WebDAVXML.ELEM_RESPONSE))
                        continue;
                    Enumeration enumResp = respElem.getElements();
                    while (enumResp.hasMoreElements())
                    {
                        Element e = (Element) enumResp.nextElement();
                        Name propstatTag = e.getTagName();
                        if (propstatTag == null)
                            continue;
                        if (!propstatTag.getName().equals(WebDAVXML.ELEM_PROPSTAT))
                            continue;
                        Enumeration propEnum = e.getElements();
                        while (propEnum.hasMoreElements())
                        {
                            Element propElem = (Element) propEnum.nextElement();
                            Name propTag = propElem.getTagName();
                            if (propTag == null)
                                continue;
                            if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
                                continue;
                            Enumeration propNameEnum = propElem.getElements();
                            while (propNameEnum.hasMoreElements())
                            {
                                Element propEl = (Element) propNameEnum.nextElement();
                                Name propNam = propEl.getTagName();
                                if (propNam == null)
                                    continue;
                                if (!propNam.getName().equals(WebDAVProp.PROP_LOCKDISCOVERY))
                                    continue;
                                Enumeration lockEnum = propEl.getElements();
                                while (lockEnum.hasMoreElements())
                                {
                                    Element activeEl = (Element) lockEnum.nextElement();
                                    Name activeTag = activeEl.getTagName();
                                    if (activeTag == null)
                                        continue;
                                    if (!activeTag.getName().equals(WebDAVXML.ELEM_ACTIVE_LOCK))
                                        continue;
                                    Enumeration activeEnum = activeEl.getElements();
                                    while (activeEnum.hasMoreElements())
                                    {
                                        Element tokenEl = (Element) activeEnum.nextElement();
                                        Name tokenTag = tokenEl.getTagName();
                                        if (tokenTag == null)
                                            continue;
                                        if (tokenTag.getName().equals(WebDAVXML.ELEM_LOCK_TOKEN))
                                        {                    
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element hrefEl = (Element) tokenEnum.nextElement();
                                                Name hrefTag = hrefEl.getTagName();
                                                if (hrefTag == null)
                                                    continue;
                                                if (!hrefTag.getName().equals(WebDAVXML.ELEM_HREF))
                                                    continue;
                                                Enumeration hrefEnum = hrefEl.getElements();
                                                while (hrefEnum.hasMoreElements())
                                                {
                                                    Element el = (Element) hrefEnum.nextElement();
                                                    if (el.getType() != Element.PCDATA)
                                                        continue;
                                                    lockToken = el.getText();
                                                    System.out.println("lockToken set to" + lockToken);
                                                    break;
                                                }
                                            }
                                        } // locktocken
                                        else if (tokenTag.getName().equals(WebDAVXML.ELEM_LOCK_TYPE))
                                        {
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element val = (Element) tokenEnum.nextElement();
                                                Name valTag = val.getTagName();
                                                if (valTag == null)
                                                    continue;
                                                lockType = valTag.getName();
                                                break;
                                            }
                                        } // locktype
                                        else if (tokenTag.getName().equals(WebDAVXML.ELEM_LOCK_SCOPE))
                                        {                    
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element val = (Element) tokenEnum.nextElement();
                                                Name valTag = val.getTagName();
                                                if (valTag == null)
                                                    continue;
                                                lockScope = valTag.getName();
                                                break;
                                            }
                                        } // lockscope
                                        else if (tokenTag.getName().equals(WebDAVXML.ELEM_OWNER))
                                        {                    
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element hrefEl = (Element) tokenEnum.nextElement();
                                                if (hrefEl.getType() == Element.PCDATA)
                                                {
                                                    ownerInfo = hrefEl.getText();
                                                    break;
                                                }  
                                                Name hrefTag = hrefEl.getTagName();
                                                if (hrefTag == null)
                                                    continue;
                                                if (!hrefTag.getName().equals(WebDAVXML.ELEM_HREF))
                                                    continue;
                                                Enumeration hrefEnum = hrefEl.getElements();
                                                while (hrefEnum.hasMoreElements())
                                                {
                                                    Element el = (Element) hrefEnum.nextElement();
                                                    if (el.getType() != Element.PCDATA)
                                                        continue;
                                                    ownerInfo = el.getText();
                                                    break;
                                                }
                                            }
                                        } // owner
                                        else if (tokenTag.getName().equals(WebDAVXML.ELEM_TIMEOUT))
                                        {                    
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element val = (Element) tokenEnum.nextElement();
                                                if (val.getType() != Element.PCDATA)
                                                    continue;
                                                lockTimeout = val.getText();
                                                break;
                                            }
                                        } // timeout
                                        else if (tokenTag.getName().equals(WebDAVXML.ELEM_LOCK_DEPTH))
                                        {                    
                                            Enumeration tokenEnum = tokenEl.getElements();
                                            while (tokenEnum.hasMoreElements())
                                            {
                                                Element val = (Element) tokenEnum.nextElement();
                                                if (val.getType() != Element.PCDATA)
                                                    continue;
                                                lockDepth = val.getText();
                                                break;
                                            }
                                        } // timeout
                                    } // while elements in activelock
                                } // while elements in lockdiscovery
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
                    generator.GenerateDelete(lockToken);
                    generator.execute();
                }
                else if (Extra.startsWith("rename:"))
                {
                    int pos = Extra.indexOf(":");
                    String dest = Extra.substring(pos + 1);
                    generator.GenerateMove(dest, false, true, lockToken);
                    generator.execute();
                }
                else if (Extra.equals("display"))
                {
                    if (lockToken != null)
                        displayLock(lockType, lockScope, lockDepth, lockToken, lockTimeout, ownerInfo);
                    else
                    {
                        setRefresh();
                        fireInsertionEvent(null);          
                    }
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
                        generator.GeneratePut(fileName,lockToken);
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
                System.out.println("getting properties for " + Resource);
                Element root = xml_doc.getRoot();
                if (root == null)
                    return;
                Enumeration enumRoot = root.getElements();
                while (enumRoot.hasMoreElements())
                {
                    Element responseElem = (Element) enumRoot.nextElement();
                    Name resptag = responseElem.getTagName();
                    if (resptag == null)
                        continue;
                    if (!resptag.getName().equals(WebDAVXML.ELEM_RESPONSE))
                        continue;

                    Enumeration enumResp = responseElem.getElements();
                    while (enumResp.hasMoreElements())
                    {
                        Element e = (Element) enumResp.nextElement();
                        Name hrefTag = e.getTagName();
                        if (hrefTag == null)
                            continue;
                        if (!hrefTag.getName().equals(WebDAVXML.ELEM_HREF))
	                        continue;
                        Enumeration hrefEnum = e.getElements();
                        while (hrefEnum.hasMoreElements())
                        {
                            Element valEl = (Element) hrefEnum.nextElement(); 
                            if (valEl.getType() != Element.PCDATA)
		                        continue;
                            String HrefValue = valEl.getText();
                            HrefValue = HrefValue.substring(HTTPPrefix.length());
                            int pos = HrefValue.indexOf("/");
                            HrefValue = HrefValue.substring(pos);
                            if (HrefValue.endsWith("/"))
                                HrefValue = HrefValue.substring(0,HrefValue.length()-1);
                            if (HrefValue.length() == 0)
                                HrefValue = "/";
	                        if (!HrefValue.equals(Resource))
    		                    continue;
                            while (enumResp.hasMoreElements())
                            {
                                Element propstatElem = (Element) enumResp.nextElement();
                                Name propstatTag = propstatElem.getTagName();
                                if (propstatTag == null)
		                            continue;
                                if (!propstatTag.getName().equals(WebDAVXML.ELEM_PROPSTAT))
                                    continue;
                                Enumeration enumProp = propstatElem.getElements();
                                while (enumProp.hasMoreElements())
                                {
                                    Element propElem = (Element) enumProp.nextElement();
                                    Name propTag = propElem.getTagName();
                                    if (propTag == null)
                                        continue;
                                    if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
                                        continue;
                                    AsGen alias = new AsGen();
                                    Element outProp = WebDAVXML.createElement( WebDAVXML.ELEM_PROP, Element.ELEMENT, null, alias );
                                    outProp.addChild(WebDAVXML.elemNewline,null); 
                                    Enumeration propValEnum = propElem.getElements();
                                    while (propValEnum.hasMoreElements())
                                    {
                                        Element propValEl = (Element) propValEnum.nextElement();
                                        if (propValEl.getType() != Element.ELEMENT)
			                                continue;
		                                saveProps(outProp,propValEl,0);
		                            }
                                    ByteArrayOutputStream byte_prop = new ByteArrayOutputStream();
                                    XMLOutputStream  xml_prop = new XMLOutputStream(byte_prop);
                                    ppatchDoc.save(xml_prop);
                                    outProp.save(xml_prop);
                                    byte[] prop_out = byte_prop.toByteArray();
                                    String host = HostName;
                                    if (Port > 0)
                                        host = HostName + ":" + Port;
                                    PropDialog pd = new PropDialog(Resource,host,new String(prop_out));
                                    pd.addPropDialogListener(new propDialogListener());
                                } 
                            }  
                        }
                    }          
                }
            }
            else
            {
                //  "refresh"
                setRefresh();
                fireInsertionEvent(null);
            }
        } catch (Exception e)
        {
            errorMsg("WebDAV Interpreter:\n\nError encountered \nwhile parsing PROPFIND Response.\n" + e);
                            stream = null; }
    }

    public String getLockInfo()
    {
        File lockFile = new File(classPathDir + lockInfoFilename);  
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
        // inform the user
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
                fileName = fd.getFile();  
                System.out.println("dialog select: " + fileName);
            }
            else
            {     
                fileName =  WebDAVEditDir + File.separatorChar + newRes;
            }
            File theFile = new File(fileName);
            if (theFile.exists())
            {
                if (!replaceFile(newRes))
                {
                    if ( (Extra.equals("view")) || (Extra.equals("edit")) )
                    {
                        if (launchAnyway())
                        {
                            System.out.println("launching..");
                            return;
                        }
                        else
                            return;
                    } 
                }
            }
            body = res.getData();
            fout = new FileOutputStream(fileName);
            if (fout == null)
                return;
            fout.write(body);
            fout.close();
//     String str = File.separatorChar + "Program Files" + File.separatorChar + "Microsoft Office" + File.separatorChar + "Office" + File.separatorChar + "winword.exe " + fileName;
//     Runtime rt = Runtime.getRuntime();
//     rt.exec(str);
        }
        catch (Exception exc)
        {
            return;
        }
    }
  
    public void parsePut()
    {
        // inform the user
        setRefresh();
        fireInsertionEvent(null);
    }

    public void parseDelete()
    {
        // inform the user
        setRefresh();
        fireInsertionEvent(null);
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
                fireMoveUpdate(Extra);
                errorMsg("WebDAV Interpreter:\n\n" + res.getStatusCode() + " " + res.getReasonLine());
                return;
            }
            else
                fireMoveUpdate(null);
        }
        catch (Exception e)
        {
            fireMoveUpdate(Extra);
        }
    }
  
    public void parseLock()
    {
        fireLockEvent("true");
    }
  
    public void parseUnlock()
    {
        // inform the user
        fireLockEvent("false");
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
  
    public void fireLockEvent(String str)
    {
        Vector ls;

        synchronized (this)
        {
            ls = (Vector) lockListeners.clone();
        }
        ActionEvent e = new ActionEvent(this,0,str);
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
}
