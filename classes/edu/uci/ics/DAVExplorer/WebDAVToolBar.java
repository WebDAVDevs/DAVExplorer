/*
 * Copyright (c) 1998-2002 Regents of the University of California.
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

/**
 * Title:       WebDAV Toolbae
 * Description: Implements the main toolbar
 * Copyright:   Copyright (c) 1998-2002 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * Changes:     Loading the icons from the jar file
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 August 2001
 * Changes:     Renamed Duplicate to Copy
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.ImageIcon;
import javax.swing.BorderFactory;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;


public class WebDAVToolBar extends JPanel implements ActionListener
{
    private JToolBar toolbar;
    private Vector toolbarListener;
    private static String jarPath = null;
    private static final String jarExtension =".jar";
    private static final String WebDAVClassName = "edu/uci/ics/DAVExplorer";
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
        addTool( toolbar, "copy", "Copy" );
        addTool( toolbar, "delete", "Delete" );
        toolbar.addSeparator();
        addTool( toolbar, "lock", "Lock" );
        addTool( toolbar, "unlock", "Unlock" );
        addTool( toolbar, "propfind", "View/Modify Properties" );
        return toolbar;
    }

    private static String getIconPath()
    {
        String icons = WebDAVClassName;
        int pos = icons.indexOf("/");
        while( pos >=0 )
        {
            icons = icons.substring( 0, pos) + File.separatorChar + icons.substring( pos+1 );
            pos = icons.indexOf( "/" );
        }
        icons += File.separatorChar + IconDir;
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
                pos = lowerPath.indexOf( jarExtension );
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
                    ZipEntry entry = zfile.getEntry( icons + "connect.gif" );
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
                if( entry != null )
                {
                    InputStream is = file.getInputStream( entry );
                    int len = (int)entry.getSize();
                    if( len != -1 )
                    {
                        byte[] ba = new byte[len];
                        is.read( ba, 0, len );
                        return new ImageIcon( ba, description );
                    }
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
