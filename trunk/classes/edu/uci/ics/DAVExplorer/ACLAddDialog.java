/*
 * Copyright (C) 2005 Regents of the University of California.
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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Title:       ACL Add Dialog
 * Description: Dialog to specify new ACLs to add to a resource and to change existing ACLs.
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         11 Feb 2005  
 */
public class ACLAddDialog extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    /**
     * Constructor
     *      Create a new ACL
     * 
     * @param resource
     *      The resource the ACL applies to
     * @param hostname
     *      The server hostname 
     */
    public ACLAddDialog( String resource, String hostname )
    {
        super( GlobalData.getGlobalData().getMainFrame(), true );
        init( resource, hostname, null );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * Constructor
     * 
     *      Modify an existing ACL
     * 
     * @param resource
     *      The resource the ACL applies to
     * @param hostname
     *      The server hostname 
     * @param node
     *      The ACL to change
     */
    public ACLAddDialog( String resource, String hostname, ACLNode node )
    {
        super( GlobalData.getGlobalData().getMainFrame(), true );
        init( resource, hostname, node );
        pack();
        setSize( getPreferredSize() );
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * Initialization
     *      Create the dialog panel, a multi-tabbed panel
     * 
     * @param resource
     *      The resource the ACL applies to
     * @param hostname
     *      The server hostname 
     * @param node
     *      The ACL to change, null if adding a new ACL
     */
    protected void init( String resource, String hostname, ACLNode node )
    {
        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        this.hostname = hostname;
        this.resource = resource;
        if( node != null )
        {
            setTitle("Edit ACL");
            addACL = false;
            // make copies
            principal = new String[2];
            principal[0] = new String( node.getPrincipal()[0] );
            principal[1] = new String( node.getPrincipal()[1] );
            principalType = node.getPrincipalType();
            privileges = new Vector( node.getPrivileges() );
            grant = node.getGrant();
            this.node = node;
        }
        else
        {
            setTitle("Add ACL");
            addACL = true;
        }
        ((Main)GlobalData.getGlobalData().getMainFrame()).addWebDAVCompletionListener(this);

        JLabel label = new JLabel( this.resource, JLabel.CENTER );
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        okButton = new JButton("OK");
        okButton.addActionListener(this);
        okButton.setMnemonic( KeyEvent.VK_O );
        okButton.setEnabled( false );
        cancelButton  = new JButton("Cancel");
        cancelButton.addActionListener(this);
        cancelButton.setMnemonic( KeyEvent.VK_C );
        buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton( cancelButton );
        cancelButton.grabFocus();

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panel1 = makePrincipalPanel();
        tabbedPane.addTab( "Principal", panel1 );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_P );
        JPanel panel2 = makePrivilegesPanel();
        tabbedPane.addTab( "Privileges", panel2 );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_R );
        JPanel panel3 = makeGrantPanel();
        tabbedPane.addTab( "Grant/Deny", panel3 );
        tabbedPane.setMnemonicAt( 2, KeyEvent.VK_G );
        getContentPane().add( "Center", tabbedPane );

        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    close( true );
                }
            });
        GlobalData.getGlobalData().resetCursor();
    }


    /**
     * Check if the dialog was canceled.
     * 
     * @return
     *      true if the dialog was canceled, false else 
     */
    public boolean isCanceled()
    {
        return canceled;
    }


    /**
     * Get the selected principal.
     *  
     * @return
     *      the selected principal
     */
    public String[] getPrincipal()
    {
        return principal;
    }


    /**
     * Get the type of the selected principal.
     * The type can be ACLNode.GENERAL, ACLNode.PROPERTY, and ACLNode.HREF.
     * Currently, all ACLs created here are of type ACLNode.HREF.
     *  @see edu.uci.ics.DAVExplorer.ACLNode
     * 
     * @return
     *      the type of the selected principal
     */
    public int getPrincipalType()
    {
        return principalType;
    }


    /**
     * Get the selected privileges.
     * 
     * @return
     *      the selected privileges
     */
    public Vector getPrivileges()
    {
        return privileges;
    }


    /**
     * Get the selected grant or deny privileges value.
     *  
     * @return
     *      true if privileges are granted, false if they are denied
     */
    public boolean getGrant()
    {
        return grant;
    }


    /**
     * From the ChangeListener interface.
     * 
     * @param e
     *      the change event
     */
    public void stateChanged( ChangeEvent e )
    {
        setChanged();
    }


    /**
     * Act on changes to the dialog, i.e., enable or disable the OK button
     */
    protected void setChanged()
    {
        changed = true;
        okButton.setEnabled( privileges!=null );
    }


    /**
     * From the ActionListener interface.
     * Handles user actions, i.e., button clicks and combobox selections.
     * 
     * @param e
     *      the event describing the action
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("OK") )
        {
            close( false );
        }
        else if( e.getActionCommand().equals("Change") )
        {
            ACLChangePrivilegesDialog dlg = new ACLChangePrivilegesDialog( resource, hostname, privileges, true );
            if( !dlg.isCanceled() )
            {
                privileges = dlg.getSelected();
                privilegesList.setListData( privileges );
                setChanged();
            }
        }
        else if( e.getActionCommand().equals("Cancel") )
        {
            close( true );
        }
        else if( e.getActionCommand().equals("Principal") )
        {
            // handle events from principal combobox
            JComboBox cb = (JComboBox)e.getSource();
            principal = (String[])(cb.getSelectedItem());
            principalType = ACLNode.HREF;
            href.setText( principal[0] );
            setChanged();
        }
        else if( e.getActionCommand().equals("Grant") )
        {
            grant = true;
            setChanged();
        }
        else if( e.getActionCommand().equals("Deny") )
        {
            grant = false;
            setChanged();
        }
    }


    /**
     * From the ListSelectionListener.
     *  
     * @param e
     *      the selection event
     */
    public void valueChanged(ListSelectionEvent e)
    {
    }


    /**
     * From the WebDAVCompletionListener.
     * Inform the waiting thread that data from the server is available
     * 
     * @param e
     *      the event
     */
    public void completion( WebDAVCompletionEvent e )
    {
        if( waiting )
        {
            interpreter = (ACLResponseInterpreter)e.getSource();
            synchronized( this )
            {
                waiting = false;
                notify();
            }
        }
    }


    /**
     * Create the panel for the principal selection.
     *   
     * @return
     *      the panel created
     */
    protected JPanel makePrincipalPanel()
    {
        Vector entries = new Vector();
        JPanel panel = new JPanel(false);
        if( addACL )
        {
            String prefix;
            if( GlobalData.getGlobalData().getSSL() )
                prefix =  GlobalData.WebDAVPrefixSSL;
            else
                prefix = GlobalData.WebDAVPrefix;
            // get the available principals from the server, wait for it
            // two-step method:
            // 1. get the principal collections
            // 2. for each collection, get the principals
            ACLRequestGenerator generator = (ACLRequestGenerator)ACLResponseInterpreter.getGenerator();
            generator.setResource( prefix+resource, null );
            waiting = true;
            generator.GetPrincipalCollections();
            try
            {
                synchronized( this )
                {
                    wait(30000);
                }
            }
            catch( InterruptedException e )
            {
            }
            if( interpreter != null )
            {
                Vector principals = interpreter.getPrincipalCollectionSet();
                if( principals != null )
                {
                    for( int i=0; i < principals.size(); i++ )
                    {
                        interpreter = null;
                        generator.setResource( prefix+hostname+(String)principals.get(i), null );
                        waiting = true;
                        generator.GetPrincipalNames();
                        try
                        {
                            synchronized( this )
                            {
                                wait(30000);
                            }
                        }
                        catch( InterruptedException e )
                        {
                        }
                        if( interpreter != null && interpreter.getPrincipalNames() != null )
                            entries.addAll( interpreter.getPrincipalNames() );
                    }
                }
            }
        }
        else
        {
            entries.add( principal );
        }
        JComboBox combo = new JComboBox( entries );
        combo.setRenderer( 
            new DefaultListCellRenderer()
            {
                public Component getListCellRendererComponent(
                        JList list,
                        Object value,
                        int index,
                        boolean isSelected,
                        boolean cellHasFocus)
                    {
                        setText( ((String[])value)[1] );
                        setBackground(isSelected ? Color.gray : Color.lightGray);
                        setForeground(Color.black);
                        return this;
                    }
            });

        combo.setEditable( false );
        combo.setActionCommand( "Principal" );
        combo.addActionListener( this );
        principal = (String[])combo.getSelectedItem();
        principalType = ACLNode.HREF;

        JLabel label = new JLabel( "Principal: " );
        label.setHorizontalAlignment( JLabel.RIGHT );
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout( gridbag );
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints( label, c );
        panel.add( label );
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( combo, c );
        panel.add( combo );
        JLabel hrefLabel = new JLabel( "href: " );
        hrefLabel.setHorizontalAlignment( JLabel.RIGHT );
        c.gridwidth = 1;
        c.ipady = 10;
        c.anchor = GridBagConstraints.CENTER;
        gridbag.setConstraints( hrefLabel, c );
        panel.add( hrefLabel );
        href = new JLabel( principal[0] );
        href.setHorizontalAlignment( JLabel.LEFT );
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( href, c );
        panel.add( href );
        return panel;
    }


    /**
     * Create the panel for the privilege selection.
     *  
     * @return
     *      the panel for the principal selection
     */
    protected JPanel makePrivilegesPanel()
    {
        Vector entries = new Vector();
        JPanel panel = new JPanel(false);
        if( !addACL )
            entries.addAll( privileges );
        privilegesList = new JList( entries ); 
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( privilegesList );
        JLabel label = new JLabel( "Privileges" );
        label.setHorizontalAlignment( JLabel.CENTER );
        JButton changeButton = new JButton( "Change" );
        changeButton.setMnemonic( KeyEvent.VK_H );
        changeButton.setHorizontalAlignment( JButton.CENTER );
        changeButton.addActionListener(this);
        scrollpane.setPreferredSize( new Dimension(10,10) );
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout( gridbag );
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1.0;
        c.gridwidth = 1;
        gridbag.setConstraints( label, c );
        panel.add( label );
        c.gridx = 0;
        c.gridy = GridBagConstraints.RELATIVE;
        gridbag.setConstraints( changeButton, c );
        panel.add( changeButton );
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( scrollpane, c );
        panel.add( scrollpane );
        return panel;
    }


    /**
     * Create the panel for the grant/deny selection.
     *  
     * @return
     *      the panel for the grant/deny selection
     */
    protected JPanel makeGrantPanel()
    {
        JPanel panel = new JPanel(false);
        ButtonGroup group = new ButtonGroup();
        JLabel label = new JLabel( "Grant or Deny Privileges" );
        label.setHorizontalAlignment( JLabel.CENTER );
        JRadioButton grantButton = new JRadioButton( "Grant" );
        JRadioButton denyButton = new JRadioButton( "Deny" );
        grantButton.setMnemonic( KeyEvent.VK_T );
        denyButton.setMnemonic( KeyEvent.VK_D );
        grantButton.setActionCommand( "Grant" );
        denyButton.setActionCommand( "Deny" );
        grantButton.addActionListener( this );
        denyButton.addActionListener( this );
        group.add( grantButton );
        group.add( denyButton );
        // default to deny
        if( addACL )
            grant = false;
        if( grant )
            grantButton.setSelected( grant );
        else
            denyButton.setSelected( !grant );

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout( gridbag );
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints( label, c );
        panel.add( label );
        c.anchor = GridBagConstraints.EAST;
        c.gridwidth = 1;
        gridbag.setConstraints( grantButton, c );
        panel.add( grantButton );
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.anchor = GridBagConstraints.WEST;
        gridbag.setConstraints( denyButton, c );
        panel.add( denyButton );
        return panel;
    }


    /**
     * Close the dialog.
     *  
     * @param cancel
     *      true if the dialog was canceled, false else
     */
    protected void close( boolean cancel )
    {
        setVisible(false);
        canceled = cancel;
    }


    protected String hostname;
    protected String resource;
    protected ACLNode node;
    protected JPanel buttonPanel;
    protected JButton okButton;
    protected JButton cancelButton;
    protected JList privilegesList;
    protected JLabel href;
    protected boolean addACL;
    protected boolean changed = false;
    protected boolean waiting;
    protected boolean canceled;
    protected String[] principal;
    protected int principalType;
    protected Vector privileges;
    protected boolean grant;
    protected ACLResponseInterpreter interpreter;
}
