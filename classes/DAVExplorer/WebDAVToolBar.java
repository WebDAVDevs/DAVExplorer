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
// This code was originally written by an undergraduate project
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


package DAVExplorer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.text.*;
import javax.swing.*;

public class WebDAVToolBar extends JPanel implements ActionListener
{
    private JToolBar toolbar;
    private Vector toolbarListener;
    private static String jarPath = null;
    private static final String jarExtension =".jar";
    private static final String WebDAVClassName = "DAVExplorer";
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

        JButton b = null;
        if( jarPath == null )
            b = new JButton(loadImageIcon(iconPath + File.separatorChar + name + ".gif", name));
        else
            b = new JButton(loadImageIcon(iconPath + name + ".gif", name));
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
        toolbar.addSeparator();
        addTool( toolbar, "lock", "Lock" );
        addTool( toolbar, "unlock", "Unlock" );
        addTool( toolbar, "propfind", "View Properties" );
        return toolbar;
    }

    private static String getIconPath()
    {
        String icons = WebDAVClassName + File.separatorChar + IconDir;
        String classPath = System.getProperty("java.class.path");
        if (classPath == null)
        {
            errorMsg("Toolbar:\nNo Classpath set." );
            return null;
        }

        StringTokenizer paths = new StringTokenizer(classPath,":;");
        while (paths.hasMoreTokens())
        {
            String nextPath = paths.nextToken();
            String lowerPath = nextPath.toLowerCase();
            if( lowerPath.endsWith( jarExtension ) )
            {
                jarPath = nextPath;
                int pos = lowerPath.indexOf( jarExtension );
                nextPath = nextPath.substring( 0, pos );
            }
            if (!nextPath.endsWith(new Character(File.separatorChar).toString()))
                nextPath += File.separatorChar;
            nextPath += icons;
            File iconDirFile = new File(nextPath);
            if (iconDirFile.exists())
                return nextPath;
            if( jarPath != null )
            {
                try
                {
                    ZipFile zfile = new ZipFile( jarPath );
                    icons = WebDAVClassName + "/" + IconDir + "/";
                    ZipEntry entry = zfile.getEntry( icons  );
                    if( entry != null )
                    {
                        return icons;
                    }
                    else
                        jarPath = null;
                }
                catch( IOException e )
                {
                }
            }
        }
        errorMsg("Toolbar:\nPath to icons not found." );
        return null;
    }

    private ImageIcon loadImageIcon(String filename, String description)
    {
        if( jarPath == null )
            return new ImageIcon(filename, description);
        else
        {
            try
            {
                ZipFile file = new ZipFile( jarPath );
                ZipEntry entry = file.getEntry( filename );
                InputStream is = file.getInputStream( entry );
                int len = (int)entry.getSize();
                if( len != -1 )
		{
                    byte[] ba = new byte[len];
                    is.read( ba, 0, len );
                    return new ImageIcon( ba, description );
		}
            }
            catch( IOException e )
            {
                errorMsg("Toolbar:\nIcon load error." );
                return null;
            }
        }
        return null;
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

    private static void errorMsg(String str)
    {
        JOptionPane pane = new JOptionPane();
        Object[] options = { "OK" };
        pane.showOptionDialog( null, str,"Error Message", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
    }
}
