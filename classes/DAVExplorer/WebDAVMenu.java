/*
 * Copyright (c) 1998-2001 Regents of the University of California.
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
 * Title:       WebDAVMenu
 * Description: Main menu class
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * @date        1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * Changes:     Added Create Folder menu
 *              Added enable/disable functionality to menu entries
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        31 March 1999
 * Changes:     Changed Application menu to View menu
 *              Consolidated view functionality in View menu
 *              Added lock info view
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        2 August 2001
 * Changes:     Added Move menu entry
 *              Renamed Duplicate to Copy
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 September 2001
 * Changes:     Changed View Properties menu to reflect modify functionality
 */

package DAVExplorer;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

/*--------------------------------------------------------
This class creates the menus to be used in the application
as well as the event handling for each of the items
built in.
--------------------------------------------------------*/

public class WebDAVMenu extends JMenuBar implements ActionListener
{
    /*--------------------------------------------------------
    Internal class that takes care of adding listeners to the
    menu items.
    --------------------------------------------------------*/

    class WebDAVMenuItem extends JMenuItem
    {
        public WebDAVMenuItem( String strMenuTag, ActionListener aL, boolean enabled )
    {
            super( strMenuTag );
        addActionListener( aL );
        setEnabled( enabled );
    }
    }

    class WebDAVCheckBoxMenuItem extends JCheckBoxMenuItem
    {
        public WebDAVCheckBoxMenuItem( String strMenuTag, ActionListener aL, boolean enabled )
    {
            super( strMenuTag );
        addActionListener( aL );
        setEnabled( enabled );
    }
    }

    /*--------------------------------------------------------
    Public attributes section
    --------------------------------------------------------*/

    /*--------------------------------------------------------
    Public methods section
    --------------------------------------------------------*/

    /*
    The constructor
    */
    public WebDAVMenu()
    {
        this.add(generateFileMenu());
        this.add(generateEditMenu());
        this.add(generateViewMenu());
        this.add(generateHelpMenu());
        menuListeners = new Vector();
    }

    /*
    This method will take care of catching the events created
    by the menuitems.
    */
    public void actionPerformed(ActionEvent Event)
    {
        Vector ls;
        synchronized (this)
        {
            ls = (Vector) menuListeners.clone();
        }
        for (int i=0; i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(Event);
        }
    }

    /*
    Add new menu event listeners to the vector
    */
    public synchronized void addWebDAVMenuListener(ActionListener MenuListener)
    {
        menuListeners.addElement(MenuListener);
    }

    /*
    Remove a menu event listener from the vector
    */
    public synchronized void removeWebDAVMenuListener(ActionListener MenuListener)
    {
        menuListeners.removeElement(MenuListener);
    }

    public void setLogging( boolean newState )
    {
        logging.setState( newState );
    }

    public boolean getLogging()
    {
        return logging.getState();
    }

    /*--------------------------------------------------------
    Protected attributes section
    --------------------------------------------------------*/

    protected Vector menuListeners;

    /*--------------------------------------------------------
    Protected methods section
    --------------------------------------------------------*/

    /*
    Generate the File menu
    */
    protected JMenu generateFileMenu()
    {
        JMenu mnu_FileMenu = new JMenu( "File", true );

        mnu_FileMenu.add(new WebDAVMenuItem( "Get File", this, true ));
        mnu_FileMenu.add(new WebDAVMenuItem( "Write File",this, true ));
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add(new WebDAVMenuItem( "Lock", this, true ));
        mnu_FileMenu.add(new WebDAVMenuItem( "Unlock", this, true ));
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add(new WebDAVMenuItem( "Copy", this, true ));
        mnu_FileMenu.add(new WebDAVMenuItem( "Move", this, true ));
        mnu_FileMenu.add(new WebDAVMenuItem( "Delete", this, true ));
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add(new WebDAVMenuItem( "Create Collection", this, true ));
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add(new WebDAVMenuItem( "Exit", this, true ));

        return mnu_FileMenu;
    }

    /*
    Generate the Edit menu
    */
    protected JMenu generateEditMenu()
    {
        JMenu mnu_EditMenu = new JMenu( "Edit", true );

        mnu_EditMenu.add(new WebDAVMenuItem( "Edit Proxy Info",this, true ));
        mnu_EditMenu.add(new WebDAVMenuItem( "Edit Lock Info",this, true ));
        mnu_EditMenu.addSeparator();
        mnu_EditMenu.add(new WebDAVMenuItem( "Clear Auth Buffer", this, true ));
        mnu_EditMenu.addSeparator();
        logging = new WebDAVCheckBoxMenuItem( "HTTP Logging", this, true );
        mnu_EditMenu.add( logging );
        return mnu_EditMenu;
    }

    /*
    Generate the View menu
    */
    protected JMenu generateViewMenu()
    {
        JMenu mnu_ViewMenu = new JMenu( "View", true );

        mnu_ViewMenu.add(new WebDAVMenuItem( "View Lock Properties", this, true ));
        mnu_ViewMenu.addSeparator();
        mnu_ViewMenu.add(new WebDAVMenuItem( "View/Modify Properties", this, true ));
        mnu_ViewMenu.addSeparator();
        mnu_ViewMenu.add(new WebDAVMenuItem( "Refresh",this, true ));

        return mnu_ViewMenu;
    }

    /*
    Generate the Help Menu
    */
    protected JMenu generateHelpMenu()
    {
        JMenu mnu_HelpMenu = new JMenu("Help", true);

        mnu_HelpMenu.add(new WebDAVMenuItem("About DAV Explorer...", this, true ));

        return mnu_HelpMenu;
    }

    private WebDAVCheckBoxMenuItem logging;
}
