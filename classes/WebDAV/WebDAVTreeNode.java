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
  public WebDAVTreeNode (Object o) {
    super(o);
//    System.out.println("child " + o.toString() + " created..");
  }
  public WebDAVTreeNode (Object o, boolean isRoot) {
    super(o);
    hasLoaded = true;
    dataNode = new DataNode(true,false,o.toString(),"WebDAV Root Node","",0,"",null);
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


  public void removeChildren() {

    int count = super.getChildCount();
    for (int c=0;c<count;c++) {
      remove(0);
    }
  }

  public int getChildCount() {
     
     if ( (hasLoaded) && (interpreter.Refreshing()) ) {
       Object[] full_path = getPath();
       if (full_path.length > 1) {      
         removeChildren();
         dataNode = null;
         hasLoaded = false;
         interpreter.clearStream();
       }
       interpreter.ResetRefresh();
     }
     else if (hasLoaded){
       return super.getChildCount();
     }

    if( !hasLoaded )
    {
      	loadChildren();
    }

    return super.getChildCount();
  }

  protected void loadRemote(byte[] byte_xml) {

    Vector nodesChildren = new Vector();
    Document xml_doc = null; 
    Element multiElem = null;
    Element respElem = null;
    boolean found = false;
    String ResourceName = interpreter.getResource();

    if ((ResourceName.startsWith("/")) && (ResourceName.length() > 1) )
      ResourceName = ResourceName.substring(1);


    try {
      ByteArrayInputStream byte_in = new ByteArrayInputStream(byte_xml);
      XMLInputStream xml_in = new XMLInputStream(byte_in);
      xml_doc = new Document();
      xml_doc.load(xml_in);
    }
    catch (Exception inEx) {
      dataNode = null;
      hasLoaded = false;
      interpreter.clearStream();
      return;
    }

    Enumeration docEnum = xml_doc.getElements();
    while (docEnum.hasMoreElements()) {
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
      Enumeration multiEnum = multiElem.getElements(); 
      while (multiEnum.hasMoreElements()) {
        respElem = (Element) multiEnum.nextElement();
        Name respTag = respElem.getTagName();
        if (respTag == null)
          continue;
        if (!respTag.getName().equals(WebDAVXML.ELEM_RESPONSE))
          continue;
	parseResponse(respElem, ResourceName, nodesChildren);
      } 
    }
    else {
      dataNode = null;
      hasLoaded = false;
    }
    interpreter.clearStream();
    interpreter.ResetRefresh();
    if (dataNode != null) {
      dataNode.setSubNodes(nodesChildren);
      hasLoaded = true;
    }
  }


  public void parseResponse(Element respElem, String ResourceName, Vector nodesChildren) {


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


  public void parseProps(Element propElem, String ResourceName, String resName, String fullName, Vector nodesChildren) {

      boolean isColl, isLocked, done;
      int leftToFind;
      String  resDisplay, resType, resLength, resDate;
      isColl = false;
      isLocked = false;
      resDisplay = new String("");
      resType = new String("");
      resLength = new String("0");
      resDate = new String("");
      done = false;
      leftToFind = 6;

          Enumeration enumProps = propElem.getElements();
	  
          while ( (enumProps.hasMoreElements()) && (!done) ) {
            Element propTypeElem = (Element) enumProps.nextElement();
            Name prop = propTypeElem.getTagName();
            if (prop == null)
              continue;
            if (prop.getName().equals(WebDAVProp.PROP_DISPLAYNAME.getName())) {
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
            else if (prop.getName().equals(WebDAVProp.PROP_LOCKDISCOVERY.getName())) {
              Enumeration valEnum = propTypeElem.getElements();
	      if (valEnum.hasMoreElements()) {
		isLocked = true;
	      }
              leftToFind--;
            }
            else if (prop.getName().equals(WebDAVProp.PROP_RESOURCETYPE.getName())) {
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
            else if (prop.getName().equals(WebDAVProp.PROP_GETCONTENTTYPE.getName())) {
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
            else if (prop.getName().equals(WebDAVProp.PROP_GETCONTENTLENGTH.getName())) {
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
            else if (prop.getName().equals(WebDAVProp.PROP_GETLASTMODIFIED.getName())) {
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
      /*      
	System.out.println("--------------------------");
        System.out.println("isColl: " + isColl);
        System.out.println("isLocked: " + isLocked);
        System.out.println("resName: " + resName);
        System.out.println("resDisplay: " + resDisplay);
        System.out.println("resType: " + resType);
        System.out.println("size: " + size);
        System.out.println("resDate: " + resDate);
        System.out.println("ResourceName: " + ResourceName);
        System.out.println("fullName: " + fullName);
        System.out.println("++++++++++++++++++++++++++++++++");
	*/
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
          hostName = WebDAVPrefix + interpreter.getHost() + ResourceName;
        dataNode = new DataNode(isColl,isLocked,hostName, resDisplay, resType, size, resDate, null);
//        System.out.println("this data node created.");
      }
      else {
        DataNode newNode = new DataNode(isColl,
                                          isLocked,
                                          resName,
                                          resDisplay,
                                          resType,
                                          size,
                                          resDate,null);

        if (isColl) {
            WebDAVTreeNode childNode = new WebDAVTreeNode(resName);
            childNode.setDataNode(newNode);
            insert(childNode,0);
//            System.out.println("inserting collection: " + resName);
        }
        else {
            nodesChildren.addElement(newNode);
//            System.out.println("inserting non-collection: " + resName);
        }
      }
  }
  protected void loadLocal(String name, Object[] full_path) {

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
        dataNode = new DataNode(true,false,name,"Local File","",
                                            f.length(),fileDate.toLocaleString(), nodesChildren);
     }
     else {
	hasLoaded = false;
        //System.out.println("ERROR: invalid directory");
	dataNode = null;
	return;
     }
     hasLoaded = true;
  }

  protected void loadChildren() {

    Object[] full_path = getPath();
/*
    System.out.println("loadChildren: getPath() = ");
    for (int p=0;p<full_path.length;p++)
      System.out.println("  full_path[" + p + "] = " + full_path[p].toString());
*/
    if( full_path == null || full_path.length <= 1 )
        return;
        
    String name = full_path[1].toString();
    if (name.startsWith(WebDAVPrefix)) {
      byte[] byte_xml = interpreter.getXML();
      if (byte_xml == null)  {
    	hasLoaded = false;
    	dataNode = null;
        interpreter.ResetRefresh();
        generator.setExtraInfo("index");
        generator.GeneratePropFind(null,"allprop","one",null,null);
        generator.execute();
        byte_xml = interpreter.getXML();
        loadRemote(byte_xml);
        return;
      }	 
      else {
        loadRemote(byte_xml);
    	interpreter.clearStream();
    	interpreter.ResetRefresh();
      }
    }    
    else {
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

}
