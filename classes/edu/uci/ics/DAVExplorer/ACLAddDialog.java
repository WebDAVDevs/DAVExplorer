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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Rectangle;
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Title:       
 * Description: 
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        28 January 2005  
 */
public class ACLAddDialog extends JDialog
    implements ActionListener, ChangeListener, ListSelectionListener, WebDAVCompletionListener
{
    /**
     * Constructor
     * 
     * @param resource
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
     * @param resource
     * @param node
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
     * 
     * @param resource
     * @param node
     */
    protected void init( String resource, String hostname, ACLNode node )
    {
        this.hostname = hostname;
        this.resource = resource;
        if( node != null )
        {
            setTitle("Edit ACL");
            addACL = false;
            principal = node.getPrincipal();
            principalType = node.getPrincipalType();
            privileges = node.getPrivileges();
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
    }


    /**
     * 
     * @return
     */
    public boolean isCanceled()
    {
        return canceled;
    }


    /**
     * 
     * @return
     */
    public String getPrincipal()
    {
        return principal;
    }


    /**
     * 
     * @return
     */
    public int getPrincipalType()
    {
        return principalType;
    }


    /**
     * 
     * @return
     */
    public Vector getPrivileges()
    {
        return privileges;
    }


    /**
     * 
     * @return
     */
    public boolean getGrant()
    {
        return grant;
    }


    /**
     * 
     * @param e
     */
    public void stateChanged( ChangeEvent e )
    {
        setChanged( true );
    }


    /**
     * 
     * @param enable
     */
    public void setChanged( boolean enable )
    {
        changed = enable;
        okButton.setEnabled( (privileges!=null) && changed );
    }


    /**
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("OK") )
        {
            close( false );
        }
        else if( e.getActionCommand().equals("Change") )
        {
            // TODO: get privileges
            ACLChangePrivilegesDialog dlg = new ACLChangePrivilegesDialog( resource, hostname, privileges );
            if( !dlg.isCanceled() )
            {
                privileges = dlg.getCurrentPrivileges();
                privilegesList.setListData( privileges );
                setChanged( true );
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
            principal = (String)cb.getSelectedItem();
            setChanged( true );
        }
        else if( e.getActionCommand().equals("Grant") )
        {
            grant = true;
            setChanged( true );
        }
        else if( e.getActionCommand().equals("Deny") )
        {
            grant = false;
            setChanged( true );
        }
    }


    /**
     * 
     * @param e
     */
    public void valueChanged(ListSelectionEvent e)
    {
    }


    /**
     * 
     * @param e
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
     * 
     * @return
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
        combo.setEditable( false );
        combo.setActionCommand( "Principal" );
        combo.addActionListener( this );
        principal = (String)combo.getSelectedItem();
        
        JLabel label = new JLabel( "Principal:  " );
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
        return panel;
    }


    /**
     * 
     * @return
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
     * 
     * @return
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
    *
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
    protected boolean addACL;
    protected boolean changed = false;
    protected boolean waiting;
    protected boolean canceled;
    protected String principal;
    protected int principalType;
    protected Vector privileges;
    protected boolean grant;
    protected ACLResponseInterpreter interpreter;
}
