/*
 * Copyright (c) 1998-2003 Regents of the University of California.
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
 * Title:       WebDAV Toolbar
 * Description: Implements the main toolbar
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
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
 * @author      Joachim Feise (dav-exp@ics.uci.edu), Karen Schuchardt
 * @date        2 April 2002
 * Changes:     Incorporated Karen Schuchardt's changes to improve the loading of
 *              images. Thanks!
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        27 April 2003
 * Changes:     separated button name from icon name.
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


public class WebDAVToolBar extends JPanel implements ActionListener
{
    private JToolBar toolbar;
    private Vector toolbarListener;

    public WebDAVToolBar()
    {
        super();
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        toolbar = new JToolBar();

        add(createToolbar());
        toolbarListener = new Vector();
    }

    private void addTool( JToolBar tb, String name, String description )
    {
        addTool( tb, name, name, description );
    }


    private void addTool( JToolBar tb, String name, String iconName, String description )
    {
        JButton b = new JButton(GlobalData.getGlobalData().getImageIcon(iconName + ".gif", name));
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
        addTool( toolbar, "exclusiveLock", "lock", "Exclusive Lock" );
        addTool( toolbar, "unlock", "Unlock" );
        addTool( toolbar, "propfind", "View/Modify Properties" );
        return toolbar;
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
