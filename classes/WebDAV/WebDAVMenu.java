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

package WebDAV;

import java.awt.event.*;
import java.util.*;
import java.awt.*;
import com.sun.java.swing.JFrame;
import com.sun.java.swing.JMenu;
import com.sun.java.swing.JMenuItem;
import com.sun.java.swing.JMenuBar;

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
		public WebDAVMenuItem(String strMenuTag, ActionListener aL)
		{
			super(strMenuTag);
			addActionListener(aL);
		}
	}

	/*--------------------------------------------------------
	Public attributes section
	--------------------------------------------------------*/

	/*--------------------------------------------------------
	Public methods section
	--------------------------------------------------------*/

	/*________________________________________________________
	The constructor
	________________________________________________________*/

	public WebDAVMenu()
	{
		this.add(generateFileMenu());
		this.add(generateEditMenu());
		this.add(generateApplicationMenu());
		this.add(generateHelpMenu());
		menuListeners = new Vector();
	}

	/*________________________________________________________
	This method will take care of catching the events created
	by the menuitems.
	________________________________________________________*/

	public void actionPerformed(ActionEvent Event) {
	
          Vector ls;
          synchronized (this) {
            ls = (Vector) menuListeners.clone();
	  }
          for (int i=0; i<ls.size();i++) {
            WebDAVMenuListener l = (WebDAVMenuListener) ls.elementAt(i);
            l.actionPerformed(Event);
          }
        }
	
	/*________________________________________________________
	Add new menu event listeners to the vector
	________________________________________________________*/

	public synchronized void addWebDAVMenuListener(WebDAVMenuListener MenuListener)
	{
		menuListeners.addElement(MenuListener);
	}

	/*________________________________________________________
	Remove a menu event listener from the vector
	________________________________________________________*/

	public synchronized void removeWebDAVMenuListener(WebDAVMenuListener MenuListener)
	{
		menuListeners.removeElement(MenuListener);
	
	}



	/*--------------------------------------------------------
	Protected attributes section
	--------------------------------------------------------*/

	protected Vector menuListeners;

	/*--------------------------------------------------------
	Protected methods section
	--------------------------------------------------------*/

	/*________________________________________________________
	Generate the File menu
	________________________________________________________*/

    protected JMenu generateFileMenu()
    {
        JMenu mnu_FileMenu = new JMenu("File", true);

        mnu_FileMenu.add(new WebDAVMenuItem("View", this));
	mnu_FileMenu.add(new WebDAVMenuItem("Save", this));
        mnu_FileMenu.add(new WebDAVMenuItem("Save As...", this));
        mnu_FileMenu.add(new WebDAVMenuItem("Export File...",this));
        mnu_FileMenu.add(new WebDAVMenuItem("Lock", this));
        mnu_FileMenu.add(new WebDAVMenuItem("Unlock", this));
        mnu_FileMenu.add(new WebDAVMenuItem("Duplicate", this));
	mnu_FileMenu.add(new WebDAVMenuItem("Delete", this));
        mnu_FileMenu.addSeparator();
        mnu_FileMenu.add(new WebDAVMenuItem("Exit", this));

        return mnu_FileMenu;
    }

    /*________________________________________________________
	Generate the Edit menu
	________________________________________________________*/

    protected JMenu generateEditMenu()
    {
        JMenu mnu_EditMenu = new JMenu("Edit", true);

        mnu_EditMenu.add(new WebDAVMenuItem("Edit Resource", this));
//        mnu_EditMenu.add(new WebDAVMenuItem("UnEdit Resource", this));
        mnu_EditMenu.add(new WebDAVMenuItem("Commit Changes", this));
        mnu_EditMenu.add(new WebDAVMenuItem("View Properties", this));
        mnu_EditMenu.addSeparator();
        mnu_EditMenu.add(new WebDAVMenuItem("Clear Auth Buffer", this));
        mnu_EditMenu.addSeparator();
        mnu_EditMenu.add(new WebDAVMenuItem("Lock Info...",this));
        mnu_EditMenu.addSeparator();
	mnu_EditMenu.add(new WebDAVMenuItem("Refresh",this));
        return mnu_EditMenu;
    }

    /*________________________________________________________
	Generate the Application menu
	________________________________________________________*/

    protected JMenu generateApplicationMenu()
    {
        JMenu mnu_ApplicationMenu = new JMenu("Application", true);

        mnu_ApplicationMenu.add(new WebDAVMenuItem("View Extension", this));

        return mnu_ApplicationMenu;
    }

    /*________________________________________________________
	Generate the Help Menu
	________________________________________________________*/

    protected JMenu generateHelpMenu()
    {
        JMenu mnu_HelpMenu = new JMenu("Help", true);


        mnu_HelpMenu.add(new WebDAVMenuItem("WebDAV Help", this));
        mnu_HelpMenu.add(new WebDAVMenuItem("About WebDAV...", this));

        return mnu_HelpMenu;
	}
}
