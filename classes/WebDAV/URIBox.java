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
// This class creates an extension of JPanel which creates the
// URI entry box on the WebDAVExplorer.  This box contains
// the text field in which the user enters the dav server's
// URI.  
// 
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


package WebDAV;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import com.sun.java.swing.text.*;
import com.sun.java.swing.*;

public class URIBox extends JPanel implements ActionListener
{
    private Vector URIBoxListener;
    private final static String WebDAVClassName = "WebDAV";
    private final static String IconDir = "icons";

    public URIBox()
    {
        super();
        //setLayout(new BorderLayout());

        JPanel panel = new JPanel();

        String iconPath = getIconPath();
        if (iconPath == null)
            System.exit(0);

        okButton = new JButton(loadImageIcon(iconPath + File.separatorChar + "connect.gif", "Connect"));
        //okButton.setMargin(new Insets(1,1,1,1));
        okButton.setActionCommand("Connect");
        okButton.addActionListener(this);
        okButton.setToolTipText("Connect");

        panel.add(okButton);

        textField1 = new JTextField(30);
        textField1.addActionListener(new EnterPressedListener());
        label1 = new JLabel();
        label1.setText("http:// ");

        panel.add(label1);
        panel.add(textField1);

        add("Center", panel);
        URIBoxListener = new Vector();
    }

    JTextField textField1;
    JLabel label1;
    JButton okButton;

    private static String getIconPath()
    {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null)
            return null;

        StringTokenizer paths = new StringTokenizer(classPath, ":;");

        while (paths.hasMoreTokens())
        {
            String nextPath = paths.nextToken();
            if (!nextPath.endsWith(new Character(File.separatorChar).toString()))
                nextPath += File.separatorChar;
            nextPath += WebDAVClassName + File.separatorChar + IconDir;
            File iconDirFile = new File(nextPath);
            if (iconDirFile.exists())
                return nextPath;
        }
        return null;
    }

    private ImageIcon loadImageIcon(String filename, String description)
    {
        return new ImageIcon(filename, description);
    }

    public void actionPerformed(ActionEvent evt)
    {
        notifyListener(evt);
    }

    public String getText()
    {
        return textField1.getText().trim();
    }

    public synchronized void addActionListener(ActionListener l)
    {
        URIBoxListener.addElement(l);
    }

    public synchronized void removeActionListener(ActionListener l)
    {
        URIBoxListener.removeElement(l);
    }

    protected void notifyListener(ActionEvent e)
    {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getText());
        Vector v;
        synchronized(this)
        {
            v = (Vector)URIBoxListener.clone();
        }

        for (int i=0; i< v.size(); i++)
        {
            WebDAVURIBoxListener client = (WebDAVURIBoxListener)v.elementAt(i);
            client.actionPerformed(evt);
        }
    }

    class EnterPressedListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            notifyListener(e);
        }
    }
}
