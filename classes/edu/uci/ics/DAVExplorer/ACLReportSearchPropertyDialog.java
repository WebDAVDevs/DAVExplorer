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
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;

/**
 * Title:       Report search property dialog
 * Description: Dialog to select data for some ACL reports
 * Copyright:   Copyright (c) 2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         11 Feb 2005
 */
public class ACLReportSearchPropertyDialog extends ACLReportPropertiesDialog
{
    /**
     * Constructor
     * 
     * @param resource
     *      the resource the privileges are applied to
     */
    public ACLReportSearchPropertyDialog( String resource )
    {
        super( resource, "Select Search Criteria", true );
    }


    /**
     * Constructor
     * 
     * @param resource
     *      the resource the privileges are applied to
     * @param match
     *      true if showing the match edit box, false if showing the self checkbox
     */
    public ACLReportSearchPropertyDialog( String resource, boolean match )
    {
        super( resource, "Select Search Criteria", match );
    }


    /**
     * Initialization
     * Creates the dialog panel.
     * 
     * @param resource
     *      the resource the privileges are applied to
     * @param hostname
     *      the server name
     * @param reserved
     *      unused
     * @param title
     *      the dialog title
     * @param match
     *      true if showing the match edit box, false if showing the self checkbox
     */
    protected void init( String _resource, String _hostname, Vector reserved, String title, boolean _match )
    {
        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        this.resource = _resource;
        this.match = _match;
        this.self = !_match;
        available = new Vector();
        selected = new Vector();
        setTitle( title );
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
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton( cancelButton );
        cancelButton.grabFocus();

        getContentPane().add( "South", buttonPanel );
        setBackground(Color.lightGray);

        JTabbedPane tabbedPane = new JTabbedPane();
        JPanel panel1 = makeCriteriaPanel();
        tabbedPane.addTab( "Search Criteria", panel1 );
        tabbedPane.setMnemonicAt( 0, KeyEvent.VK_S );
        JPanel panel2 = makePanel();
        tabbedPane.addTab( "Properties", panel2 );
        tabbedPane.setMnemonicAt( 1, KeyEvent.VK_P );
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
     * Create the search criteria panel.
     *  
     * @return
     *      the search criteria panel
     */
    protected JPanel makeCriteriaPanel()
    {
        JLabel label = new JLabel( "Search Criteria" );
        JPanel panel = new JPanel(false);
        label.setForeground(Color.black);
        label.setHorizontalAlignment( JLabel.CENTER );
        BorderLayout layout = new BorderLayout();
        panel.setLayout( new BorderLayout() );
        panel.add( label, BorderLayout.NORTH );

        propTable = new JTable( new ACLPropertySearchModel( match ) );
        propTable.getSelectionModel().addListSelectionListener(this);
        propTable.setPreferredScrollableViewportSize( new Dimension( 400, 100 ) );
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( propTable );
        if( self )
        {
            JPanel subPanel = new JPanel( false );
            GridBagLayout gridbag = new GridBagLayout();
            GridBagConstraints c = new GridBagConstraints();
            subPanel.setLayout( gridbag );
            c.fill = GridBagConstraints.BOTH;
            c.weighty = 5.0;
            c.gridwidth = GridBagConstraints.REMAINDER;
            gridbag.setConstraints( scrollpane, c );
            subPanel.add( scrollpane );
            selfBox = new JCheckBox( "Self" );
            selfBox.addActionListener( this );
            selfBox.setMnemonic( KeyEvent.VK_S );
            c.weighty = 1.0;
            gridbag.setConstraints( selfBox, c );
            subPanel.add( selfBox );
            panel.add( subPanel, BorderLayout.CENTER );
        }
        else
            panel.add( scrollpane, BorderLayout.CENTER );
        addButton = new JButton("Add");
        addButton.addActionListener(this);
        addButton.setMnemonic( KeyEvent.VK_A );
        deleteButton  = new JButton("Delete");
        deleteButton.addActionListener(this);
        deleteButton.setMnemonic( KeyEvent.VK_D );
        deleteButton.setEnabled( false );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add( addButton );
        buttonPanel.add( deleteButton );
        panel.add( buttonPanel, BorderLayout.SOUTH );

        return panel;
    }


    /**
     * Returns the panel name.
     *
     * @return
     *      the panel name 
     */
    protected String getPanelTitle()
    {
        return  "Properties";
    }


    /**
     * Act on changes to the dialog, i.e., enable or disable the Save button
     */
    public void setChanged()
    {
        changed = true;
        boolean okEnable = (((ACLPropertySearchModel)propTable.getModel()).getRealRowCount()>0) && changed;
        if( self )
            okEnable |= selfBox.isSelected();
        okButton.setEnabled( okEnable );
    }


    /**
     * From the ActionListener interface.
     * Handles user actions, i.e., button clicks.
     * 
     * @param e
     *      the event describing the action
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Add") )
        {
            ACLReportChangeSearchPropertiesDialog dlg = new ACLReportChangeSearchPropertiesDialog( resource, match );
            if( !dlg.isCanceled() )
            {
                ((ACLPropertySearchModel)propTable.getModel()).addRow( dlg.getSelected(), dlg.getMatch() );
                setChanged();
            }
        }
        else if( e.getActionCommand().equals("Delete") )
        {
            int pos = propTable.getSelectedRow();
            ((ACLPropertySearchModel)propTable.getModel()).removeRow( pos );
            deleteButton.setEnabled( false );
            setChanged();
        }
        else if( e.getActionCommand().equals("Self") )
        {
            addButton.setEnabled( !selfBox.isSelected() );
            propTable.setEnabled( !selfBox.isSelected() );
            if( selfBox.isSelected() )
                deleteButton.setEnabled( false );
            else
            {
                if( ((ACLPropertySearchModel)propTable.getModel()).getRealRowCount() > 0 )
                    deleteButton.setEnabled( true );
            }
            setChanged();
        }
        else
            super.actionPerformed( e );
    }


    /**
     * From the ListSelectionListener.
     * Enable the buttons appropriately.
     *  
     * @param e
     *      the selection event
     */
    public void valueChanged(ListSelectionEvent e)
    {
        if( propTable.isShowing() )
        {
            if( ((ACLPropertySearchModel)propTable.getModel()).getRealRowCount() > 0 )
                deleteButton.setEnabled( true );
        }
        else
            super.valueChanged( e );
    }


    /**
     * Returns the search criteria.
     *  
     * @return
     *      A vector of the search criteria
     */
    public Vector getSearchCriteria()
    {
        Vector criteria = new Vector();
        ACLPropertySearchModel model = (ACLPropertySearchModel)propTable.getModel();
        for( int i=0; i<model.getRealRowCount(); i++ )
            criteria.add( model.getRow(i) );
        return criteria;
    }


    /**
     * Check if the self-box is selected.
     * 
     * @return
     *      true if the self-box is selected, false else
     */
    public boolean isSelf()
    {
        if( self )
            return selfBox.isSelected();
        return false;
    }


    protected JTable propTable;
    protected JButton addButton;
    protected JButton deleteButton;
    protected JCheckBox selfBox;
    protected boolean match;
    protected boolean self;
}
