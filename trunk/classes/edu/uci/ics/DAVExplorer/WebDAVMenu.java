/*
 * Copyright (c) 1998-2005 Regents of the University of California.
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


package edu.uci.ics.DAVExplorer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;


/**
 * Title:       WebDAVMenu
 * Description: This class creates the menus to be used in the application
 * as well as the event handling for each of the items built in.
 * Copyright:   Copyright (c) 1998-2005 Regents of the University of California. All rights reserved.
 * @author      Undergraduate project team ICS 126B 1998
 * date         1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 1999
 * Changes:     Added Create Folder menu
 *              Added enable/disable functionality to menu entries
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * date         31 March 1999
 * Changes:     Changed Application menu to View menu
 *              Consolidated view functionality in View menu
 *              Added lock info view
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         2 August 2001
 * Changes:     Added Move menu entry
 *              Renamed Duplicate to Copy
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         29 September 2001
 * Changes:     Changed View Properties menu to reflect modify functionality
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         27 April 2003
 * Changes:     Added shared lock functionality.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         23 September 2003
 * Changes:     Integrated the DeltaV code from the Spring 2003 ICS125 team.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         10 February 2005
 * Changes:     Added ACL menus
 */
public class WebDAVMenu extends JMenuBar implements ActionListener
{
    // menu entry magic values
    public static final int GET_FILE = 0;
    public static final int WRITE_FILE = 1;
    public static final int EXCLUSIVE_LOCK = 2;
    public static final int SHARED_LOCK = 3;
    public static final int UNLOCK = 4;
    public static final int COPY = 5;
    public static final int MOVE = 6;
    public static final int DELETE = 7;
    public static final int CREATE_COLLECTION = 8;
    public static final int EXIT = 9;
    public static final int EDIT_LOCK_INFO = 10;
    public static final int EDIT_PROXY_INFO = 11;
    public static final int CLEAR_AUTH_BUFFER = 12;
    public static final int HTTP_LOGGING = 13;
    public static final int VIEW_LOCK_PROPS = 14;
    public static final int VIEW_MODIFY_PROPS = 15;
    public static final int REFRESH = 16;
    public static final int ABOUT = 17;
    public static final int USE_SSL = 18;
    // versioning menu entries
    public static final int INIT_VERSION_CONTROL = 100;
    public static final int VERSION_REPORT = 101;
    public static final int CHECKOUT = 102;
    public static final int UNCHECKOUT = 103;
    public static final int CHECKIN = 104;
    public static final int MAKE_ACTIVITY = 105;
    public static final int MERGE = 106;
    // ACL menu entries
    public static final int VIEW_OWNER = 200;
    public static final int VIEW_GROUP = 202;
    public static final int GET_SUPPORTED_PRIVILEGES = 204;
    public static final int GET_USER_PRIVILEGES = 205;
    public static final int VIEW_ACL = 206;
    public static final int GET_SUPPORTED_ACL = 207;
    public static final int GET_INHERITED_ACL = 209;
    public static final int GET_PRINCIPALS = 210;
    public static final int ACL_PRINCIPAL_PROP_SET_REPORT = 211;
    public static final int PRINCIPAL_MATCH_REPORT = 212;
    public static final int PRINCIPAL_PROPERTY_SEARCH_REPORT = 213;
    public static final int PRINCIPAL_SEARCH_PROPERTY_SET_REPORT = 214;


    /**
     * Internal class that takes care of adding listeners to the
     * menu items.
     */
    class WebDAVMenuItem extends JMenuItem
    {
        /**
         * Constructor
         * @param strMenuTag
         * @param aL
         * @param enabled
         */
        public WebDAVMenuItem( String strMenuTag, int id, ActionListener aL, boolean enabled )
        {
            super( strMenuTag );
            addActionListener( aL );
            setEnabled( enabled );
            this.id = id;
        }

        /**
         * Constructor
         * 
         * @param strMenuTag
         * @param id
         * @param aL
         */
        public WebDAVMenuItem( String strMenuTag, int id, ActionListener aL )
        {
            super( strMenuTag );
            addActionListener( aL );
            setEnabled( true );
            this.id = id;
        }

        /**
         * 
         * @return
         */
        public int getId()
        {
            return id;
        }
        
        private int id;
    }


    /**
     * 
     */
    class WebDAVCheckBoxMenuItem extends JCheckBoxMenuItem
    {
        /**
         * 
         * @param strMenuTag
         * @param aL
         * @param enabled
         */
        public WebDAVCheckBoxMenuItem( String strMenuTag, int id, ActionListener aL, boolean enabled )
        {
            super( strMenuTag );
            addActionListener( aL );
            setEnabled( enabled );
            this.id = id;
        }

        /**
         * Constructor
         * 
         * @param strMenuTag
         * @param id
         * @param aL
         */
        public WebDAVCheckBoxMenuItem( String strMenuTag, int id, ActionListener aL )
        {
            super( strMenuTag );
            addActionListener( aL );
            setEnabled( true );
            this.id = id;
        }

        /**
         * 
         * @return
         */
        public int getId()
        {
            return id;
        }
        
        private int id;
    }

    /*--------------------------------------------------------
    Public methods section
    --------------------------------------------------------*/

    /**
     * Constructor
     */
    public WebDAVMenu()
    {
        this.add(generateFileMenu());
        this.add(generateEditMenu());
        this.add(generateVersionMenu());
        this.add(generateACLMenu());
        this.add(generateViewMenu());
        this.add(generateHelpMenu());
        menuListeners = new Vector();
    }


    /**
     * This method will take care of catching the events created
     * by the menuitems.
     * @param Event
     */
    public void actionPerformed(ActionEvent Event)
    {
        Vector ls;
        synchronized (this)
        {
            ls = (Vector) menuListeners.clone();
        }
        ActionEvent e = Event;
        JMenuItem source = (JMenuItem)Event.getSource();
        if( source instanceof WebDAVMenuItem )
            e = new ActionEvent( source, ((WebDAVMenuItem)source).getId(), Event.getActionCommand() );
        else if( source instanceof WebDAVCheckBoxMenuItem )
            e = new ActionEvent( source, ((WebDAVCheckBoxMenuItem)source).getId(), Event.getActionCommand() );
        for (int i=0; i<ls.size();i++)
        {
            ActionListener l = (ActionListener) ls.elementAt(i);
            l.actionPerformed(e);
        }
    }

 
    /**
     * Add new menu event listeners to the vector
     * @param MenuListener
     */
    public synchronized void addWebDAVMenuListener(ActionListener MenuListener)
    {
        menuListeners.addElement(MenuListener);
    }

 
    /**
     * Remove a menu event listener from the vector
     * @param MenuListener
     */
    public synchronized void removeWebDAVMenuListener(ActionListener MenuListener)
    {
        menuListeners.removeElement(MenuListener);
    }


    /**
     * Changes the state of the logging menu entry.
     *  
     * @param newState
     * 		the new state
     */
    public void setLogging( boolean newState )
    {
        logging.setState( newState );
    }


    /**
     * Returns the state of the logging menu entry.
     * 
     * @return
     * 		true if logging is selected, false else
     */
    public boolean getLogging()
    {
        return logging.getState();
    }

    
    /**
     * Changes the state of the ssl menu entry.
     *  
     * @param newState
     * 		the new state
     */
    public void setSSL( boolean newState )
    {
        ssl.setState( newState );
    }


    /**
     * Returns the state of the ssl menu entry
     * 
     * @return
     * 		true if use of ssl is selected, false else
     */
    public boolean getSSL()
    {
        return ssl.getState();
    }

    /*--------------------------------------------------------
    Protected attributes section
    --------------------------------------------------------*/

    protected Vector menuListeners;

    /*--------------------------------------------------------
    Protected methods section
    --------------------------------------------------------*/

    /**
     * Generate the File menu
     * 
     * @return
     */
    protected JMenu generateFileMenu()
    {
        JMenu mnu_FileMenu = new JMenu( "File", true );

        mnu_FileMenu.add( new WebDAVMenuItem( "Get File", GET_FILE, this ) );
        mnu_FileMenu.add( new WebDAVMenuItem( "Write File", WRITE_FILE, this ) );
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add( new WebDAVMenuItem( "Exclusive Lock", EXCLUSIVE_LOCK, this ) );
        mnu_FileMenu.add( new WebDAVMenuItem( "Shared Lock", SHARED_LOCK, this ) );
        mnu_FileMenu.add( new WebDAVMenuItem( "Unlock", UNLOCK, this ) );
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add( new WebDAVMenuItem( "Copy", COPY, this ) );
        mnu_FileMenu.add( new WebDAVMenuItem( "Move", MOVE, this ) );
        mnu_FileMenu.add( new WebDAVMenuItem( "Delete", DELETE, this ) );
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add( new WebDAVMenuItem( "Create Collection", CREATE_COLLECTION, this ) );

        if (!GlobalData.getGlobalData().isAppletMode()) {
            mnu_FileMenu.addSeparator();
            mnu_FileMenu.add(new WebDAVMenuItem( "Exit", EXIT, this ));
        }

        return mnu_FileMenu;
    }

 
    /**
     * Generate the Edit menu
     * 
     * @return
     */
    protected JMenu generateEditMenu()
    {
        JMenu mnu_EditMenu = new JMenu( "Edit", true );

        mnu_EditMenu.add(new WebDAVMenuItem( "Edit Lock Info", EDIT_LOCK_INFO, this ));
        mnu_EditMenu.add( new WebDAVMenuItem( "Edit Proxy Info", EDIT_PROXY_INFO, this ) );
        mnu_EditMenu.addSeparator();
        mnu_EditMenu.add( new WebDAVMenuItem( "Clear Auth Buffer", CLEAR_AUTH_BUFFER, this ) );
        mnu_EditMenu.addSeparator();
        ssl = new WebDAVCheckBoxMenuItem( "Use SSL", USE_SSL, this );
        setSSL( GlobalData.getGlobalData().getSSL() );
        mnu_EditMenu.add( ssl );
        mnu_EditMenu.addSeparator();
        logging = new WebDAVCheckBoxMenuItem( "HTTP Logging", HTTP_LOGGING, this );
        mnu_EditMenu.add( logging );

        return mnu_EditMenu;
    }


    /**
     * Generate the Version menu
     * 
     * @return
     */
    protected JMenu generateVersionMenu()
    {
        JMenu mnu_VersionMenu = new JMenu( "Versioning", true );

        mnu_VersionMenu.add( new WebDAVMenuItem( "Put Under Version Control", INIT_VERSION_CONTROL, this ) );
        mnu_VersionMenu.addSeparator();
        mnu_VersionMenu.add( new WebDAVMenuItem( "Version Report", VERSION_REPORT, this ) );
        mnu_VersionMenu.addSeparator();
        mnu_VersionMenu.add( new WebDAVMenuItem( "Check Out", CHECKOUT, this ) );
        mnu_VersionMenu.add( new WebDAVMenuItem( "Uncheckout", UNCHECKOUT, this ) );
        mnu_VersionMenu.add( new WebDAVMenuItem( "Check In", CHECKIN, this ) );
        //mnu_VersionMenu.addSeparator();
        //mnu_VersionMenu.add( new WebDAVMenuItem( "Make Activity", MAKE_ACTIVITY, this ) );
        //mnu_VersionMenu.add( new WebDAVMenuItem( "Merge", MERGE, this ) );

        return mnu_VersionMenu;
    }


    /**
     * Generate the menus to handle Access control requests
     * 
     * @return
     */
    protected JMenu generateACLMenu()
    {
        JMenu mnu_ACLMenu = new JMenu( "Access Control", true );

        mnu_ACLMenu.add( new WebDAVMenuItem( "View/Modify Owner", VIEW_OWNER, this ) );
        mnu_ACLMenu.add( new WebDAVMenuItem( "View/Modify Group", VIEW_GROUP, this ) );
        mnu_ACLMenu.addSeparator();
        mnu_ACLMenu.add( new WebDAVMenuItem( "Get User's Privileges", GET_USER_PRIVILEGES, this ) );
        mnu_ACLMenu.addSeparator();
        mnu_ACLMenu.add( new WebDAVMenuItem( "Get ACL Restrictions", GET_SUPPORTED_ACL, this ) );
        mnu_ACLMenu.add( new WebDAVMenuItem( "Get Inherited ACLs", GET_INHERITED_ACL, this ) );
        mnu_ACLMenu.addSeparator();
        mnu_ACLMenu.add( new WebDAVMenuItem( "View/Modify ACLs", VIEW_ACL, this ) );
        mnu_ACLMenu.addSeparator();
        mnu_ACLMenu.add( new WebDAVMenuItem( "Principal-Property-Set Report", ACL_PRINCIPAL_PROP_SET_REPORT, this ) );
        mnu_ACLMenu.add( new WebDAVMenuItem( "Principal-Match Report", PRINCIPAL_MATCH_REPORT, this ) );
        mnu_ACLMenu.add( new WebDAVMenuItem( "Principal-Property-Search Report", PRINCIPAL_PROPERTY_SEARCH_REPORT, this ) );
        mnu_ACLMenu.add( new WebDAVMenuItem( "Principal-Search-Property-Set Report", PRINCIPAL_SEARCH_PROPERTY_SET_REPORT, this ) );

        return mnu_ACLMenu;
    }


    /**
     * Generate the View menu
     * 
     * @return
     */
    protected JMenu generateViewMenu()
    {
        JMenu mnu_ViewMenu = new JMenu( "View", true );

        mnu_ViewMenu.add( new WebDAVMenuItem( "View Lock Properties", VIEW_LOCK_PROPS, this ) );
        mnu_ViewMenu.addSeparator();
        mnu_ViewMenu.add( new WebDAVMenuItem( "View/Modify Properties", VIEW_MODIFY_PROPS, this ) );
        mnu_ViewMenu.addSeparator();
        mnu_ViewMenu.add( new WebDAVMenuItem( "Refresh", REFRESH, this ) );

        return mnu_ViewMenu;
    }

    /**
     * Generate the Help Menu
     * 
     * @return
     */
    protected JMenu generateHelpMenu()
    {
        JMenu mnu_HelpMenu = new JMenu("Help", true);

        mnu_HelpMenu.add( new WebDAVMenuItem("About DAV Explorer...", ABOUT, this ) );

        return mnu_HelpMenu;
    }


    private WebDAVCheckBoxMenuItem logging;
    private WebDAVCheckBoxMenuItem ssl;
}
