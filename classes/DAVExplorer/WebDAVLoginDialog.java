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

/////////////////////////////////////////////////////////////////////////
//This is the class that will display a dialog box prompting the user
//for authentication information.
//
//Written by: Gerair D. Balian (Elite 5)
//On: 3/3/98
//For ICS126B (WebDAV Project)
/////////////////////////////////////////////////////////////////////////
// This class causes a login dialog box to
// appear.  The purpose of this Login box is
// to authenticate users when they attempt to
// connect to a DAV site through the action of
// connecting to it.
//
// This class DOES NOT authenticate users at this
// time.  It is in place as a UI component which
// may be fully integrated in an authetication scheme
// at some future point.
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
// Note: No authenication check is executed in the Action Listener
// when "okay" is clicked.

package DAVExplorer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JDialog.*;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.util.*;
import java.awt.*;
import java.awt.event.*;


public class WebDAVLoginDialog extends Dialog implements ActionListener
{
/*-----------------------------------------------------------------------
Public methods and attributes section
-----------------------------------------------------------------------*/

    Vector listeners = new Vector();

    //Construction
    public WebDAVLoginDialog(JFrame parent, ActionListener l, String strCaption, boolean isModal)
    {
        super( parent, strCaption, isModal );

        Rectangle recthDimensions = getParent().getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width - 350)/ 2,
             recthDimensions.y + (recthDimensions.height - 110)/2, 350, 110 );
        addListener(l);
        JPanel groupPanel = new JPanel(new GridLayout( 4, 1 ));
        groupPanel.add(new JLabel("Login name:"));
        groupPanel.add(txtUsername = new JTextField(40));
        groupPanel.add(new JLabel("Password:"));
        txtPassword = new JPasswordField("", 40);
        groupPanel.add(txtPassword);
        add(OKbutton = new JButton("OK"), BorderLayout.SOUTH);
        OKbutton.addActionListener(this);
        add(groupPanel, BorderLayout.CENTER);
        pack();
        setVisible( true );
    }

    //Handling the events that happen in the dialog
    public synchronized void addListener(ActionListener l) {
        listeners.addElement(l);
    }

    public synchronized void removeListener(ActionListener l) {
        listeners.removeElement(l);
    }

    public void actionPerformed(ActionEvent e)
    {
    if(e.getActionCommand().equals("OK"))
        {
            String user = txtUsername.getText();
            String pass = String.valueOf( txtPassword.getPassword() );

            if ( ( user.length() > 0  ) && (pass.equals("") ) )
                return;
            ActionEvent evt = new ActionEvent(this, 0,user + ":" + pass);
            Vector v;
            synchronized(this)
            {
                v = (Vector)listeners.clone();
            }
            for (int i=0; i< v.size(); i++)
            {
                ActionListener client = (ActionListener)v.elementAt(i);
                client.actionPerformed(evt);
            }
        }
        setVisible( false );
        dispose();
    }

    //Set the name of the user in the protected data member
    public void setUsername(String strUsername)
    {
        m_strUsername = strUsername;
    }

    //Set the password the user entered.  This is data gotten from the edit component
    public void setUserPassword(String strUserPassword)
    {
        m_strUserPassword = strUserPassword;
    }

    //Get the user name to be sent through the wire
    public String getUsername()
    {
    return m_strUsername;
    }

    //Get the user password to be sent through the wire.
    public String getUserPassword()
    {
        return m_strUserPassword;
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
