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
// This code was originally written by a undergraduate project
// team at UCI.
//
// This class creates an event object which carries the path
// and node to the recieving listener.
//
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
// Note: This code was not tested at this time (3/17/99) as 
// the current Apache server does not support locking.


package WebDAV;


import com.sun.java.swing.JDialog;
import com.sun.java.swing.JFrame;
import com.sun.java.swing.JDialog.*;
import com.sun.java.swing.JPanel;
import com.sun.java.swing.JLabel;
import com.sun.java.swing.JTextField;
import com.sun.java.swing.JPasswordField;
import com.sun.java.swing.JButton;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class WebDAVLockInfo extends JDialog implements ActionListener
{
	
/*-----------------------------------------------------------------------
Public methods and attributes section
-----------------------------------------------------------------------*/
        


         Vector listeners = new Vector();
         static final String WebDAVClassName = "WebDAV";
	 static final String fileName = "lockinfo.dat";
         String lockInfo;
	 String classPath;
         String filePath;

	//Construction

        public WebDAVLockInfo(JFrame parent, String strCaption, boolean isModal)
	{
                super(parent, strCaption, isModal);

            
                classPath = getClassPath();
                if (classPath == null)
  		  return;
                File theFile = new File(classPath + fileName);
	        if (theFile.exists())
                  filePath = classPath + fileName;
		if (filePath != null) {
                  try {
                    FileInputStream fin = new FileInputStream(filePath);
		    BufferedReader in = new BufferedReader(new InputStreamReader(fin));
		    lockInfo = in.readLine();
	            in.close();
  		  } catch (Exception fileEx) { }                               
		}

//                addListener(l);
		JPanel groupPanel = new JPanel(new GridLayout(4,1));
                groupPanel.add(new JLabel("Lock Info:"));
                groupPanel.add(txtUsername = new JTextField(80));
	        if (lockInfo != null)
                  txtUsername.setText(lockInfo);
                else
                  txtUsername.setText("http://");
                getContentPane().add(OKbutton = new JButton("OK"), BorderLayout.SOUTH);
                OKbutton.addActionListener(this);
                getContentPane().add(groupPanel, BorderLayout.CENTER);
		pack();
		show();
	}

	//Handling the events that happen in the dialog


  private static String getClassPath() {
    String classPath = System.getProperty("java.class.path");
    if (classPath == null)
      return null;

    StringTokenizer paths = new StringTokenizer(classPath,":;");

    while (paths.hasMoreTokens()) {
      String nextPath = paths.nextToken();
      if (!nextPath.endsWith(new Character(File.separatorChar).toString()))
        nextPath += File.separatorChar;
      nextPath += WebDAVClassName + File.separatorChar;
      File theFile = new File(nextPath + "icons");
      if (theFile.exists())
        return nextPath;
    }
    return null;
  }
  

   public synchronized void addListener(ActionListener l) {
        listeners.addElement(l);
    }

    public synchronized void removeListener(ActionListener l) {
        listeners.removeElement(l);
    }


	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("OK")) {
                  String user = txtUsername.getText();

                  if ( user.length() == 0  )  
		    return;
                  try {
		    FileOutputStream fout = new FileOutputStream(classPath + fileName);
		    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(fout));
                    out.write(user,0,user.length());
                    out.newLine();
		    out.close();

		  } catch (Exception exc) { }

                }
	  dispose();
	}

	//Set the name of the user in the protected data member

	public void setUsername(String strUsername)
	{
		m_strUsername = strUsername;
	}




        public void show()
        {
                Rectangle recthDimensions = getParent().bounds();
                Rectangle rectvDimensions = bounds();

                move(recthDimensions.x + (recthDimensions.width - rectvDimensions.width)/ 2,
                         recthDimensions.y + (recthDimensions.height - rectvDimensions.height)/2);

                super.show();
        }

/*-----------------------------------------------------------------------
Private methods and attributes section
-----------------------------------------------------------------------*/

/*-----------------------------------------------------------------------
Protected methods and attributes section
-----------------------------------------------------------------------*/

	protected String m_strUsername;

	protected String m_strUserPassword;

	protected JTextField txtUsername;

	protected JPasswordField txtPassword;

	protected JButton OKbutton;

}

