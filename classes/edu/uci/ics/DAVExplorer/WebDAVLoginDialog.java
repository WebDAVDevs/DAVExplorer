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

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import java.util.Vector;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Title:       Login Dialog
 * Description: This class causes a login dialog box to appear.  The purpose
 *              of this Login box is to authenticate users when they attempt to
 *              connect to a DAV site through the action of connecting to it.
 *              This class DOES NOT authenticate users at this time. It is in
 *              place as a UI component which may be fully integrated in an
 *              authentication scheme at some future point.
 * Copyright:   Copyright (c) 1998-2005 Regents of the University of California. All rights reserved.
 * @author      Gerair D. Balian (dav-exp@ics.uci.edu)
 * date         3 March 1998
 * @author      Yuzo Kanomata, Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 1999
 * Changes:     Note: No authenication check is executed in the Action Listener
 *              when "okay" is clicked.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         3 December 1999
 * Changes:     Now invoked from Authentication handler in HTTPClient
 *              when "OK" is clicked.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         29 September 2001
 * Changes:     The return key now triggers a programmatic click on the OK
 *              button.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 December 2001
 * Changes:     Better handling of ok button enable/disable
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 March 2003
 * Changes:     Integrated Brian Johnson's applet changes.
 *              Added better error reporting.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 November 2003
 * Changes:     Explicitly resetting the cursor.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 February 2004
 * Changes:     Added Javadoc templates
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 February 2005
 * Changes:     Some refactoring
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         18 April 2005
 * Changes:     Handle case where scheme/realm are not passed in
 */
public class WebDAVLoginDialog extends JDialog implements ActionListener, DocumentListener
{
/*-----------------------------------------------------------------------
Public methods and attributes section
-----------------------------------------------------------------------*/

    Vector listeners = new Vector();

    /**
     * Constructor
     * @param strCaption
     * @param realm
     * @param scheme
     * @param isModal
     */
    public WebDAVLoginDialog( String strCaption, String realm, String scheme, boolean isModal )
    {
        super( GlobalData.getGlobalData().getMainFrame(), strCaption, isModal );

        GridBagLayout gridbag = new GridBagLayout();
        JPanel groupPanel = new JPanel( gridbag );
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        if( realm != null )
        {
            JLabel l = new JLabel( "Realm: " + realm, JLabel.CENTER );
            l.setForeground(Color.black);
            gridbag.setConstraints( l, constraints );
            groupPanel.add( l );
        }
        if( scheme != null )
        {
            JLabel l = new JLabel( "Scheme: " + scheme, JLabel.CENTER );
            l.setForeground(Color.black);
            gridbag.setConstraints( l, constraints );
            groupPanel.add( l );
        }
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        JLabel l = new JLabel( "Login name: ", JLabel.LEFT );
        l.setForeground(Color.black);
        gridbag.setConstraints( l, constraints );
        groupPanel.add( l );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        txtUsername = new JTextField(30);
        txtUsername.getDocument().addDocumentListener( this );
        txtUsername.addActionListener( this );
        gridbag.setConstraints( txtUsername, constraints );
        groupPanel.add( txtUsername );
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        l = new JLabel( "Password: ", JLabel.LEFT );
        l.setForeground(Color.black);
        gridbag.setConstraints( l, constraints );
        groupPanel.add( l );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        txtPassword = new JPasswordField( "", 30 );
        txtPassword.getDocument().addDocumentListener( this );
        txtPassword.addActionListener( this );
        gridbag.setConstraints( txtPassword, constraints );
        groupPanel.add( txtPassword );
        JPanel p = new JPanel();
        p.add( okButton = new JButton( "OK" ) );
        okButton.addActionListener( this );
        p.add( cancelButton = new JButton( "Cancel" ) );
        cancelButton.addActionListener( this );
        gridbag.setConstraints( p, constraints );
        groupPanel.add( p );
        getRootPane().setDefaultButton( okButton );
        okButton.setEnabled( false );
        txtUsername.requestFocus();

        getContentPane().add( groupPanel );
        pack();
        GlobalData.getGlobalData().center( this );
        setResizable( false );
        setVisible( true );
        GlobalData.getGlobalData().getMainFrame().setCursor( Cursor.getDefaultCursor() );
    }


    /**
     * 
     * @param l
     */
    public synchronized void addListener( ActionListener l )
    {
        listeners.addElement(l);
    }


    /**
     * 
     * @param l
     */
    public synchronized void removeListener( ActionListener l )
    {
        listeners.removeElement(l);
    }


    /**
     * ActionListener interface
     * @param e
     */
    public void actionPerformed( ActionEvent e )
    {
        if( e.getActionCommand().equals("OK") )
        {
            m_strUsername = txtUsername.getText();
            m_strUserPassword = String.valueOf( txtPassword.getPassword() );
            if ( (m_strUsername.length()>0) && (m_strUserPassword.length()>0) )
            {
                setVisible( false );
                dispose();
            }
        }
        else if( e.getActionCommand().equals( "Cancel" ) )
        {
            m_strUsername = "";
            m_strUserPassword = "";
            setVisible( false );
            dispose();
        }
        else
        {
            /*
             * Simulate click on default button
             * JTextFields intercept the return button
             * Ideally, this would be modified by code like this:
             * static {
             *   JTextField f = new JTextField();
             *   KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
             *   Keymap map = f.getKeymap();
             *   map.removeKeyStrokeBinding(enter);
             * }
             * However, this changes the keymap for *all* JTextFields, and we
             * need the original mapping for the URI box
             */
            if ( okButton.isEnabled() )
                okButton.doClick();
        }
    }


    /**
     * DocumentListener interface
     * @param e
     */
    public void insertUpdate( DocumentEvent e )
    {
        checkEnableOk();
    }


    /**
     * DocumentListener interface
     * @param e
     */
    public void removeUpdate( DocumentEvent e )
    {
        checkEnableOk();
    }


    /**
     * DocumentListener interface
     * @param e
     */
    public void changedUpdate( DocumentEvent e )
    {
        checkEnableOk();
    }


    /**
     * Get the user name to be sent through the wire
     * 
     * @return
     */
    public String getUsername()
    {
        return m_strUsername;
    }


    /**
     * Get the user password to be sent through the wire
     * 
     * @return
     */
    public String getUserPassword()
    {
        return m_strUserPassword;
    }


    /**
     *
     */
    public void clearData()
    {
    	// try to get rid of any unencoded passwords in dialog
        m_strUserPassword = null;
        m_strUsername = null;
        System.gc();
    }

/*-----------------------------------------------------------------------
Protected methods and attributes section
-----------------------------------------------------------------------*/
    /**
     * 
     */
    protected void checkEnableOk()
    {
        if( (txtUsername.getText().length()>0) && (String.valueOf(txtPassword.getPassword()).length()>0) )
        {
            okButton.setEnabled( true );
            getRootPane().setDefaultButton( okButton );
        }
        else
            okButton.setEnabled( false );
    }


    protected String m_strUsername;
    protected String m_strUserPassword;
    protected JTextField txtUsername;
    protected JPasswordField txtPassword;
    protected JButton okButton;
    protected JButton cancelButton;
}

