/*
 * Copyright (c) 1999-2001 Regents of the University of California.
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
// This class creates an event object which carries the path
// and node to the recieving listener.
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
// Note: This code was not tested at this time (3/17/99) as
// the current Apache server does not support locking.
//
// Date: 2001-Jan-12
// Joe Feise: Added support for https (SSL)

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
import java.io.*;

public class WebDAVLockInfo extends Dialog implements ActionListener
{
/*-----------------------------------------------------------------------
Public methods and attributes section
-----------------------------------------------------------------------*/
    Vector listeners = new Vector();

    //Construction
    public WebDAVLockInfo(JFrame parent, String strCaption, boolean isModal)
    {
        super(parent, strCaption, isModal);

        JPanel groupPanel = new JPanel(new GridLayout( 2, 1 ));
        groupPanel.add(new JLabel("Lock Info:"));
        groupPanel.add(txtUsername = new JTextField(40));
        txtUsername.setText( GlobalData.getGlobalData().ReadConfigEntry("lockinfo") );
        add(OKbutton = new JButton("OK"), BorderLayout.SOUTH);
        OKbutton.addActionListener(this);
        add(groupPanel, BorderLayout.CENTER);
        pack();
        center();
        setVisible( true );
    }

    public synchronized void addListener(ActionListener l)
    {
        listeners.addElement(l);
    }

    public synchronized void removeListener(ActionListener l)
    {
        listeners.removeElement(l);
    }

    public void actionPerformed(ActionEvent e)
    {
        if(e.getActionCommand().equals("OK"))
        {
            String user = txtUsername.getText();

            if ( user.length() == 0  )
                return;
            GlobalData.getGlobalData().WriteConfigEntry( "lockinfo", user );
        }
        setVisible( false );
        dispose();
    }
/*-----------------------------------------------------------------------
Private methods and attributes section
-----------------------------------------------------------------------*/

/*-----------------------------------------------------------------------
Protected methods and attributes section
-----------------------------------------------------------------------*/
    protected void center()
    {
        Rectangle recthDimensions = getParent().getBounds();
        Rectangle bounds = getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width-bounds.width)/2,
             recthDimensions.y + (recthDimensions.height - bounds.height)/2, bounds.width, bounds.height );
    }

    protected JTextField txtUsername;
    protected JButton OKbutton;
}
