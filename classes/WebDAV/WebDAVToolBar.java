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

public class WebDAVToolBar extends JPanel implements ActionListener
{
    private JToolBar toolbar;
    private Vector toolbarListener;
    private static final String WebDAVClassName = "WebDAV";
    private static final String IconDir = "icons";

    public WebDAVToolBar()
    {
        super();
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        toolbar = new JToolBar();

        add(createToolbar());
        toolbarListener = new Vector();
    }

    public void addTool( JToolBar tb, String name, String description )
    {
        String iconPath = getIconPath();
        if (iconPath == null)
            System.exit(0);

        JButton b = new JButton(loadImageIcon(iconPath + File.separatorChar + name + ".gif", name));
        b.setActionCommand( description );
        b.addActionListener(this);
        b.setToolTipText( description );
        b.setMargin(new Insets(1,1,1,1));
        tb.add(b);
    }

    private Component createToolbar()
    {
        addTool( toolbar, "open", "Get File" );
        addTool( toolbar, "save", "Write File" );
        addTool( toolbar, "copy", "Duplicate" );
        addTool( toolbar, "delete", "Delete" );
//        addTool( toolbar, "delete", "Create Folder" );
        toolbar.addSeparator();
        addTool( toolbar, "lock", "Lock" );
        addTool( toolbar, "unlock", "Unlock" );
//        addTool( toolbar, "launch", "View Lock Properties" );
        addTool( toolbar, "propfind", "View Properties" );
//        addTool( toolbar, "launch", "Refresh" );
        return toolbar;
    }

    private static String getIconPath()
    {
        String classPath = System.getProperty("java.class.path");
        if (classPath == null)
            return null;

        StringTokenizer paths = new StringTokenizer(classPath,":;");
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

    public synchronized void addActionListener(ActionListener l)
    {
        toolbarListener.addElement(l);
    }

    public synchronized void removeActionListener(ActionListener l)
    {
        toolbarListener.removeElement(l);
    }

    public void actionPerformed(ActionEvent evt)
    {
        notifyListener(evt);
    }

    protected void notifyListener(ActionEvent e)
    {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, e.getActionCommand());
        Vector v;
        synchronized(this)
        {
            v = (Vector)toolbarListener.clone();
        }

        for (int i=0; i< v.size(); i++)
        {
            ActionListener client = (ActionListener)v.elementAt(i);
            client.actionPerformed(evt);
        }
    }
}
