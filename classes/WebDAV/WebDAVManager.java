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

//
// This is the WebDAV method wrapper class. It is implemented as
// a bean, which listens for a request event. Once the event
// occurs, appropriate Method from the WebDAV class library is
// called.
//
// Version: 0.2
// Author:  Robert Emmery
// Date:    3/25/98
///////////////////////////////////////////////////////////////////////


package WebDAV;

import java.io.*;
import HTTPClient.*;
import WebDAV.WebDAVConnection.*;
import java.util.*;
import java.awt.event.*;
import com.sun.java.swing.*;

public class WebDAVManager {

  public HTTPResponse Response;
  private WebDAVConnection Con ;
  private String HostName = null;
  private int Port;
  private String MethodName;
  private String ResourceName;
  private NVPair[] Headers;
  private byte[] Body;
  private String ExtraInfo;
  private Vector Listeners = new Vector();
  private JFrame mainFrame;

  public WebDAVManager(JFrame mainFrame) {
    this.mainFrame = mainFrame;
  }

  public void sendRequest(WebDAVRequestEvent e) {

    String TempHost = e.getHost();
    int TempPort = e.getPort();

    if ((TempHost != null) && (TempHost.length() > 0) && (!TempHost.equals(HostName))) {
      HostName = TempHost;
      if (TempPort != 0) {
        Port = TempPort;
        Con = new WebDAVConnection(HostName, Port);
      }
      else {
        Port = 0;
        Con = new WebDAVConnection(HostName);
      }
    }
    String user = e.getUser();
    String pass = e.getPass();
    
    if (user.length() > 0) {
      try {
        Con.addDigestAuthorization(HostName,user, pass);
        Con.addBasicAuthorization(HostName,user,pass);
      } catch (Exception exc) { System.out.println(e); }
    }
    
    MethodName = e.getMethod();
    ResourceName = e.getResource();
    Headers = e.getHeaders();
    Body = e.getBody();
    ExtraInfo = e.getExtraInfo();
    try {
      Response = Con.Generic(MethodName, ResourceName, Body, Headers);
      WebDAVResponseEvent webdavResponse  = GenerateWebDAVResponse(Response);
      fireResponse(webdavResponse); 
    }
    catch (IOException E) { }
    catch (ModuleException E) { }
  }

  public synchronized void addResponseListener(WebDAVResponseListener l) {
    Listeners.addElement(l);
  }
  public synchronized void removeResponseListener(WebDAVResponseListener l) {
    Listeners.removeElement(l);
  }
  public WebDAVResponseEvent GenerateWebDAVResponse(HTTPResponse Response) {

      WebDAVResponseEvent e = new WebDAVResponseEvent(this,HostName, Port, ResourceName,MethodName,Response,ExtraInfo);
      return e; 
  }

  public void fireResponse(WebDAVResponseEvent e) {
    
    Vector ls;
    synchronized (this) {
      ls = (Vector) Listeners.clone();
    }
    for (int i=0; i<ls.size();i++) {
       WebDAVResponseListener l = (WebDAVResponseListener) ls.elementAt(i);
      l.responseFormed(e);
    }
  }
}
