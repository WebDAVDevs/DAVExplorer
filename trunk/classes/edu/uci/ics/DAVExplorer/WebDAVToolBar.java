/*
 * Copyright (c) 1998-2004 Regents of the University of California.
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
 * Copyright:   Copyright (c) 1998-2004 Regents of the University of California. All rights reserved.
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
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        23 September 2003
 * Changes:     Added DeltaV buttons.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        08 February 2004
 * Changes:     Added Javadoc templates
 */

package edu.uci.ics.DAVExplorer;

import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * 
 */
public class WebDAVToolBar extends JPanel implements ActionListener
{
    private JToolBar toolbar;
    private Vector toolbarListener;


    class WebDAVTBButton extends JButton
    {
        public WebDAVTBButton( Icon icon, int id )
        {
            super( icon );
            this.id = id;
        }

        public int getId()
        {
            return id;
        }

        private int id;
    }
    
    
    /**
     * Constructor
     */
    public WebDAVToolBar()
    {
        super();
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new BorderLayout());
        toolbar = new JToolBar();

        add(createToolbar());
        toolbarListener = new Vector();
    }


    /**
     * 
     * @param tb
     * @param name
     * @param description
     */
    private void addTool( JToolBar tb, String name, String description, int id )
    {
        addTool( tb, name, name, description, id );
    }


    /**
     * 
     * @param tb
     * @param name
     * @param iconName
     * @param description
     * @param id
     */
    private void addTool( JToolBar tb, String name, String iconName, String description, int id )
    {
        WebDAVTBButton b = new WebDAVTBButton( GlobalData.getGlobalData().getImageIcon(iconName + ".gif", name), id );
        b.setActionCommand( description );
        b.addActionListener(this);
        b.setToolTipText( description );
        b.setMargin(new Insets(1,1,1,1));
        tb.add(b);
    }


    /**
     * 
     * @return
     */
    private Component createToolbar()
    {
        addTool( toolbar, "open", "Get File", WebDAVMenu.GET_FILE );
        addTool( toolbar, "save", "Write File", WebDAVMenu.WRITE_FILE );
        addTool( toolbar, "copy", "Copy", WebDAVMenu.COPY );
        addTool( toolbar, "delete", "Delete", WebDAVMenu.DELETE );
        toolbar.addSeparator();
        addTool( toolbar, "exclusiveLock", "lock", "Exclusive Lock", WebDAVMenu.EXCLUSIVE_LOCK );
        addTool( toolbar, "unlock", "Unlock", WebDAVMenu.UNLOCK );
        addTool( toolbar, "propfind", "View/Modify Properties", WebDAVMenu.VIEW_MODIFY_PROPS );
        // DeltaV support
        toolbar.addSeparator();
        addTool( toolbar, "versioning", "Put Under Version Control", WebDAVMenu.INIT_VERSION_CONTROL );
        addTool( toolbar, "checkout", "Check Out", WebDAVMenu.CHECKOUT );
        addTool( toolbar, "uncheckout", "Uncheckout", WebDAVMenu.UNCHECKOUT );
        addTool( toolbar, "checkin", "Check In", WebDAVMenu.CHECKIN );
        addTool( toolbar, "versions", "Version Report", WebDAVMenu.VERSION_REPORT );
        return toolbar;
    }


    /**
     * 
     * @param l
     */
    public synchronized void addActionListener(ActionListener l)
    {
        toolbarListener.addElement(l);
    }


    /**
     * 
     * @param l
     */
    public synchronized void removeActionListener(ActionListener l)
    {
        toolbarListener.removeElement(l);
    }


    /**
     * 
     */
    public void actionPerformed(ActionEvent evt)
    {
        notifyListener(evt);
    }


    /**
     * 
     * @param e
     */
    protected void notifyListener(ActionEvent e)
    {
        WebDAVTBButton b = (WebDAVTBButton)e.getSource();
        ActionEvent evt = new ActionEvent(this, b.getId(), e.getActionCommand());
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
