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
 * Title:       WebDAVLockInfo
 * Description: Dialog to enter the lock owner info
 * Copyright:   Copyright (c) 1998-2001 Regents of the University of California. All rights reserved.
 * @author      Robert Emmery (dav-exp@ics.uci.edu)
 * @date        2 April 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * @date        17 March 1999
 * Changes:     Note: This code was not tested at this time (3/17/99) as
 *              the current Apache server does not support locking.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        12 January 2001
 * Changes:     Added support for https (SSL)
 */

package DAVExplorer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.util.Vector;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


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
