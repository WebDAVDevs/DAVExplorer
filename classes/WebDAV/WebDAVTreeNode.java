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

// This is where we handle tree loading.
// Each time a folder is double clicked, getChildCount()
// will be called. This will in turn either form PROPFIND
// or itterate a local directory.
//
//  Version: 0.3
//  Author:  Robert Emmery
//  Date:    4/2/98
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


package WebDAV;

import com.sun.java.swing.tree.DefaultMutableTreeNode;
import java.util.*;
import java.io.*;

import com.ms.xml.om.*;
import com.ms.xml.parser.*;
import com.ms.xml.util.*;

public class WebDAVTreeNode extends DefaultMutableTreeNode {

  protected boolean hasLoaded = false;
  protected final static String WebDAVPrefix = "http://";
  protected final static String HTTPPrefix = "http://";
  protected final static String WebDAVRoot = "WebDAV Explorer";
  protected DataNode dataNode;
  protected static WebDAVRequestGenerator generator = new WebDAVRequestGenerator();
  protected static WebDAVResponseInterpreter interpreter = new WebDAVResponseInterpreter();

    protected boolean childrenLoaded = false;

  public WebDAVTreeNode (Object o) {
    super(o);
System.out.println("%%child " + o.toString() + " created..");
    hasLoaded = true;
  }

  public WebDAVTreeNode (Object o, boolean isRoot) {
    super(o);
    hasLoaded = true;
    childrenLoaded = true;
System.out.println("%%%%child " + o.toString() + " created..");
    dataNode = new DataNode(true,false,null, o.toString(),"WebDAV Root Node","",0,"",null);
  }
  
  public DataNode getDataNode() {
    return dataNode;
  }
  public void setDataNode(DataNode newNode) {
    dataNode = newNode;
  }

  public boolean isLeaf() {
    return false;
  }


// Yuzo
    public boolean hasLoadedChildren(){
	return childrenLoaded;
    }

    public void setHasLoadedChildren( boolean b){
System.out.println("Setting setHasLoadedChildren to=" + b + " for node =" + this);
	childrenLoaded = b;
    }




// Yuzo Note They did this function wrong 
  public void removeChildren() {

System.out.println("removeChildren() calling getChildCount()");
    int count = super.getChildCount();
    for (int c=0;c<count;c++) {
      remove(0);
    }
  }

  public int getChildCount() {
System.out.println("$$getChildCount =" + super.getChildCount());
    return super.getChildCount();
  }





  protected void loadRemote(byte[] byte_xml) {

System.out.println("loadRemote called");
/*
if ((byte_xml != null) && (byte_xml.length > 0)){
for (int i = 0; i < byte_xml.length; i++){
System.out.print((char)byte_xml[i]);
}
System.out.println();
}
*/
    Vector nodesChildren = new Vector();
    Document xml_doc = null; 
    Element multiElem = null;
    Element respElem = null;
    boolean found = false;
    String ResourceName = interpreter.getResource();

    if ((ResourceName.startsWith("/")) && (ResourceName.length() > 1) )
      ResourceName = ResourceName.substring(1);


    try {
System.out.println("$1:");
      ByteArrayInputStream byte_in = new ByteArrayInputStream(byte_xml);
System.out.println("$2:");
      EscapeInputStream iStream = new EscapeInputStream( byte_in, true );
      XMLInputStream xml_in = new XMLInputStream( iStream );
System.out.println("$3:");
      xml_doc = new Document();
System.out.println("$4:");
      xml_doc.load(xml_in);
    }
    catch (Exception inEx) {
System.out.println("Exception:" + inEx);
      dataNode = null;
System.out.println("SETTING hasLoaded to FALSE!!!!!!!");
      hasLoaded = false;
      interpreter.clearStream();
      return;
    }

    Enumeration docEnum = xml_doc.getElements();
    while (docEnum.hasMoreElements()) {
System.out.print("*");
      multiElem = (Element) docEnum.nextElement();
      Name multiTag = multiElem.getTagName();
      if (multiTag == null)
        continue;
      if (!multiTag.getName().equals(WebDAVXML.ELEM_MULTISTATUS))
        continue;
      found = true; // case: multiTag.getName() == WebDAVXML.ELEM_MULTISTATUS
      break;
    } 
    if (found) { 
System.out.println("*loadRemote path  FOUND");
      Enumeration multiEnum = multiElem.getElements(); 
      while (multiEnum.hasMoreElements()) {
System.out.print("*");
        respElem = (Element) multiEnum.nextElement();
        Name respTag = respElem.getTagName();
        if (respTag == null)
          continue;
        if (!respTag.getName().equals(WebDAVXML.ELEM_RESPONSE))
          continue;
	parseResponse(respElem, ResourceName, nodesChildren);
      } 
System.out.println();
    }
    else {
System.out.println("*loadRemote path NOT  FOUND");
      dataNode = null;
System.out.println("SETTING hasLoaded to FALSE!!!!!!!");
      hasLoaded = false;
    }
    interpreter.clearStream();
    interpreter.ResetRefresh();
    if (dataNode != null) {
      dataNode.setSubNodes(nodesChildren);
      hasLoaded = true;
    }
	//Yuzo added:
      hasLoaded = true;

int s= nodesChildren.size();
System.out.println("dataNodesChildren:" + s);
for (int j = 0; j < s ; j++){
	System.out.println("   NodesChildren=" + nodesChildren.elementAt(j) );
}
System.out.println("NodesChildren:" + s);
Enumeration childNodes = this.children();
while ( childNodes.hasMoreElements()){
    int count = 1;
    WebDAVTreeNode tn = (WebDAVTreeNode)childNodes.nextElement();
    System.out.println("   " + count++ + ": " + (String)tn.getUserObject() );
}
System.out.println("At end of loadRemote, hasLoaded=" + hasLoaded);

  }


  protected void parseResponse(Element respElem, String ResourceName, Vector nodesChildren) {

System.out.println("parseResponse");

    Enumeration respEnum = respElem.getElements();
    
    
    while (respEnum.hasMoreElements()) {     


      String resName = new String("");
      String fullName = new String("");
      Element e = (Element) respEnum.nextElement();
      Name tag = e.getTagName();
      if (tag == null)
	continue;
      if (!tag.getName().equals(WebDAVXML.ELEM_HREF))
	continue;

      Enumeration enumHref = e.getElements();
      while (enumHref.hasMoreElements()) {
        Element hrefElem = (Element) enumHref.nextElement();
        if (hrefElem.getType() != Element.PCDATA)
          continue;
        resName = new String(truncateResource(hrefElem.getText()));
        fullName = new String(getFullResource(hrefElem.getText()));
	break;
      }
      while (respEnum.hasMoreElements()) {
        Element propstatElem = (Element) respEnum.nextElement();
        Name propstatTag = propstatElem.getTagName();
        if (propstatTag == null) 
          continue;
        if (!propstatTag.getName().equals(WebDAVXML.ELEM_PROPSTAT))
	  continue;
        Enumeration enumPropstat = propstatElem.getElements();
        while (enumPropstat.hasMoreElements()) {
          Element propElem = (Element) enumPropstat.nextElement();
          Name propTag = propElem.getTagName();
          if (propTag == null)
            continue;
          if (!propTag.getName().equals(WebDAVXML.ELEM_PROP))
            continue;
          parseProps(propElem,ResourceName, resName, fullName,nodesChildren);
          break;
        }
        break;
      }
    }
  }


  protected void parseProps(Element propElem, String ResourceName, String resName, String fullName, Vector nodesChildren) {

      boolean isColl, isLocked, done;
      int leftToFind;
      String  resDisplay, resType, resLength, resDate;
      String lockToken = null;
      isColl = false;
      isLocked = false;
      resDisplay = new String("");
      resType = new String("");
      resLength = new String("0");
      resDate = new String("");
      done = false;
      leftToFind = 6;

System.out.println("**&&parseProps called");

          Enumeration enumProps = propElem.getElements();
	  
          while ( (enumProps.hasMoreElements()) && (!done) ) {
            Element propTypeElem = (Element) enumProps.nextElement();
            Name prop = propTypeElem.getTagName();
            if (prop == null)
              continue;
            if (prop.getName().equals(WebDAVProp.PROP_DISPLAYNAME)) {
              Enumeration valEnum = propTypeElem.getElements();
              while (valEnum.hasMoreElements()) {
                Element value = (Element) valEnum.nextElement();
                if (value.getType() != Element.PCDATA)
                  continue;
                resDisplay = new String(value.getText());
                leftToFind--;
	        break;
              }
            }
            else if (prop.getName().equals(WebDAVProp.PROP_LOCKDISCOVERY)) {
              Enumeration valEnum = propTypeElem.getElements();
	        if (valEnum.hasMoreElements())
	        {
                String[] token = new String[1];
                token[0] = new String( WebDAVXML.ELEM_ACTIVE_LOCK );
                int index = 0;
                TreeEnumeration enumTree =  new TreeEnumeration( propTypeElem );
                while( enumTree.hasMoreElements() )
                {
                    Element current = (Element)enumTree.nextElement();
                    Name currentTag = current.getTagName();
                    if( index >= 0 )
                    {
                        if( (currentTag != null) && (currentTag.getName().equals( token[index] )) )
                        {
                            // we only care about the subtree from this point on
                            enumTree = new TreeEnumeration( current );
                            index++;
                        }
                        if( index >= token.length )
                            index = -1;
                    }
                    else if( currentTag != null )
                    {
                        if( currentTag.getName().equals( WebDAVXML.ELEM_LOCK_TOKEN ) )
                        {
                            lockToken = getLockToken( current );
                        }
                    }
                }
		        isLocked = true;
	        }
              leftToFind--;
            }
            else if (prop.getName().equals(WebDAVProp.PROP_RESOURCETYPE)) {
              Enumeration valEnum = propTypeElem.getElements();
              while (valEnum.hasMoreElements()) {
                Element value = (Element) valEnum.nextElement();
                Name valueName = value.getTagName();
                if (valueName == null)
                  continue;
                if (valueName.getName().equals(WebDAVXML.ELEM_COLLECTION))
                  isColl = true;
                break;
              }              
              leftToFind--;
            }
            else if (prop.getName().equals(WebDAVProp.PROP_GETCONTENTTYPE)) {
              Enumeration valEnum = propTypeElem.getElements();
              while (valEnum.hasMoreElements()) {
                Element value = (Element) valEnum.nextElement();
                if (value.getType() != Element.PCDATA)
                  continue;
                resType = new String(value.getText());        
                leftToFind--;
		break;
              }
            }
            else if (prop.getName().equals(WebDAVProp.PROP_GETCONTENTLENGTH)) {
              Enumeration valEnum = propTypeElem.getElements();
              while (valEnum.hasMoreElements()) {
                Element value = (Element) valEnum.nextElement();
                if (value.getType() != Element.PCDATA)
                  continue;
                resLength = new String(value.getText());        
                leftToFind--;
                break;
              }
	    }
            else if (prop.getName().equals(WebDAVProp.PROP_GETLASTMODIFIED)) {
              Enumeration valEnum = propTypeElem.getElements();
              while (valEnum.hasMoreElements()) {
                Element value = (Element) valEnum.nextElement();
               	if (value.getType() != Element.PCDATA)
               	  continue;
                resDate = new String(value.getText());        
                leftToFind--;
	        break;
              } 
            }
            if (leftToFind == 0)
              done = true;
          }  // while more in prop 
     
      // This is where we fill out the data node
      long size = 0;
      try {
          size = Long.parseLong(resLength);
      } catch (Exception parseEx) { 
	  System.out.println(parseEx);
          return;
	}
        if (resName.equals(""))
          return;

      String ResourceNameStrp = "";
      if (ResourceName.endsWith("/"))
        ResourceNameStrp = ResourceName.substring(0,ResourceName.length() - 1);            
      if ( (fullName.equals(ResourceName)) || (fullName.equals(ResourceNameStrp)) ) {
      // this is the container
        int pathLen = getPath().length;
        String hostName = resName;
        if (pathLen == 2)
          hostName = WebDAVPrefix + interpreter.getHost() + "/" + ResourceName;
        dataNode = new DataNode(isColl,isLocked, lockToken, hostName, resDisplay, resType, size, resDate, null);
      }
      else {
        DataNode newNode = new DataNode(isColl,
                                          isLocked,
                                          lockToken,
                                          resName,
                                          resDisplay,
                                          resType,
                                          size,
                                          resDate,null);

        if (isColl) {
            WebDAVTreeNode childNode = new WebDAVTreeNode(resName);
            childNode.setDataNode(newNode);
            insert(childNode,0);
        }
        else {
            nodesChildren.addElement(newNode);
        }
      }
  }
  protected void loadLocal(String name, Object[] full_path) {
System.out.println("loadLocal called");

     String fileName = name;
     for (int i=2;i<full_path.length;i++)
       fileName += File.separator + full_path[i];


     name = full_path[full_path.length - 1].toString();
     File f = new File(fileName);
     if ((f != null) && (f.exists()) && (f.isDirectory()) ) {
	Vector nodesChildren = new Vector();
// Yuzo bug fix for empty sub dir
try{
        String[] fileList = f.list();
        int len = fileList.length;
        for (int i=0;i<len;i++) {
          File aFile = new File(fileName + File.separatorChar + fileList[i]);
          boolean isDir = aFile.isDirectory();
	  Date newDate = new Date(aFile.lastModified());
          DataNode newNode = new DataNode(isDir,
                                    false,
                                    null,
                                    fileList[i],
                                    "Local File",
                                    "",
                                    aFile.length(),
                                    newDate.toLocaleString(),null);

          if (isDir) {
            WebDAVTreeNode childNode = new WebDAVTreeNode(newNode.getName());
            childNode.setDataNode(newNode);
            insert(childNode,0);
          }
          else {
            nodesChildren.addElement(newNode);
          } 
	}
}catch( Exception e) {
System.out.println(e);
}
        Date fileDate = new Date(f.lastModified());
        dataNode = new DataNode(true,false,null, name,"Local File","",
                                            f.length(),fileDate.toLocaleString(), nodesChildren);
     }
     else {
System.out.println("SETTING hasLoaded to FALSE!!!!!!!");
	hasLoaded = false;
        //System.out.println("ERROR: invalid directory");
	dataNode = null;
	return;
     }
     hasLoaded = true;
  }

    // This finishes the Load Children when a call is made to a 
    // DAV server
    public void finishLoadChildren(){



System.out.println("CONTINUING in finishLoadChildren:1");
	byte[] byte_xml = interpreter.getXML();
System.out.println("CONTINUING in finishLoadChildren:2");
        loadRemote(byte_xml);
System.out.println("CONTINUING in finishLoadChildren:3");
    	interpreter.ResetRefresh();
    }

  public void loadChildren() {

    Object[] full_path = getPath();
System.out.println("loadChildren called");
    if( full_path == null || full_path.length <= 1 )
        return;
        
    String name = full_path[1].toString();
    if (name.startsWith(WebDAVPrefix)) {
System.out.println("Path, loadChildren name has WebDAVprefix");
      byte[] byte_xml = interpreter.getXML();
      if (byte_xml == null)  {
System.out.println("Path, loadChildren byte_xml == null");
System.out.println("SETTING hasLoaded to FALSE!!!!!!!");
    	hasLoaded = false;
    	dataNode = null;
        interpreter.ResetRefresh();
        generator.setExtraInfo("index");

	String pathToResource = name;
	for (int i=2; i < full_path.length; i++){
	    pathToResource = pathToResource + "/" + full_path[i].toString(); 
	}
	pathToResource = pathToResource + "/";
	
        generator.GeneratePropFindForNode(pathToResource,"allprop","one",null,null, true, this);
        //generator.execute();
        generator.run();

        return;
      }	 else {
System.out.println("Path, loadChildren byte_xml NOT null");

	// This is that case of the Select/Expand being called to a new DAV Server.
	// The buffer should have data in it via the response.
	// This should change in the future, as processing here is 
	// unsafe -- the thread that gets the buffer may not be finished yet.

    	interpreter.clearStream();  // Added to finish after lock/unlock

    	hasLoaded = false;
    	dataNode = null;
        interpreter.ResetRefresh();
        generator.setExtraInfo("index");

	String pathToResource = name;
	for (int i=2; i < full_path.length; i++){
	    pathToResource = pathToResource + "/" + full_path[i].toString(); 
	}
	pathToResource = pathToResource + "/";
	
        generator.GeneratePropFindForNode(pathToResource,"allprop","one",null,null, true, this);
        //generator.execute();
        generator.run();
	/*
        loadRemote(byte_xml);
    	interpreter.clearStream();
    	interpreter.ResetRefresh();
	*/
      }
    }    
    else {
System.out.println("Path, loadChildren name NOT WebDAVprefix");
      loadLocal(name,full_path);
    }
  }

  public String truncateResource(String res)
  {
    int pos = res.indexOf(HTTPPrefix);
    if (pos >= 0)
        res = res.substring(HTTPPrefix.length());
    pos = res.indexOf("/");
    res = res.substring(pos);

    if (res.endsWith("/"))
        res = res.substring(0, res.length() - 1);
    pos = res.lastIndexOf("/");
    if (pos >= 0)
        res = res.substring(pos);
    if ((res.startsWith("/")) && (res.length() > 1))
        res = res.substring(1);
    if (res.length() == 0)
        res = "/";
    return res;
  }
  
  public String getFullResource(String res) {

    int pos = res.indexOf(HTTPPrefix);
    if (pos >= 0)
        res = res.substring(HTTPPrefix.length());
    pos = res.indexOf("/");
    res = res.substring(pos);
    if (res.endsWith("/"))
        res = res.substring(0,res.length() - 1);
    if (res.length() == 0)
        res = "/";
    if ( (res.startsWith("/")) && (res.length() > 1) )
        res = res.substring(1);
    return res;
  }

    private String getLockToken( Element locktoken )
    {
        TreeEnumeration treeEnum = new TreeEnumeration( locktoken );
        while(treeEnum.hasMoreElements() )
        {
            Element current = (Element)treeEnum.nextElement();
            Name tag = current.getTagName();
            if( tag.getName().equals( WebDAVXML.ELEM_HREF ) )
            {
                Element token = (Element)treeEnum.nextElement();
                if( token.getType() == Element.PCDATA )
                    return token.getText();
            }
        }
        return null;
    }
}
