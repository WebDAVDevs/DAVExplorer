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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JButton;
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
 * @date        11 Feb 2005
 */
public class ACLReportSearchPropertyDialog extends ACLReportPropertiesDialog
{
    /**
     * Constructor
     * 
     * @param resource
     */
    public ACLReportSearchPropertyDialog( String resource )
    {
        super( resource, "Select Search Criteria" );
        this.match = true;
    }


    /**
     * Constructor
     * 
     * @param resource
     * @param match
     */
    public ACLReportSearchPropertyDialog( String resource, boolean match )
    {
        super( resource, "Select Search Criteria" );
        this.match = match;
    }


    /**
     * Initialization
     * 
     * @param resource
     * @param node
     */
    protected void init( String resource, String hostname, Vector reserved, String title )
    {
        GlobalData.getGlobalData().setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );
        this.resource = resource;
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
     * 
     * @return
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

        propTable = new JTable( new ACLPropertySearchModel() );
        propTable.getSelectionModel().addListSelectionListener(this);
        propTable.setPreferredScrollableViewportSize( new Dimension( 400, 100 ) );
        JScrollPane scrollpane = new JScrollPane();
        scrollpane.setViewportView( propTable );
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
     * 
     */
    protected String getPanelTitle()
    {
        return  "Properties";
    }


    /**
     * 
     * @param enable
     */
    public void setChanged( boolean enable )
    {
        changed = enable;
        okButton.setEnabled( (((ACLPropertySearchModel)propTable.getModel()).getRealRowCount()>0) && changed );
    }


    /**
     * 
     * @param e
     */
    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("Add") )
        {
            ACLReportChangeSearchPropertiesDialog dlg = new ACLReportChangeSearchPropertiesDialog( resource, match );
            if( !dlg.isCanceled() )
            {
                ((ACLPropertySearchModel)propTable.getModel()).addRow( dlg.getSelected(), dlg.getMatch() );
                setChanged( true );
            }
        }
        else if( e.getActionCommand().equals("Delete") )
        {
            int pos = propTable.getSelectedRow();
            ((ACLPropertySearchModel)propTable.getModel()).removeRow( pos );
            deleteButton.setEnabled( false );
            setChanged( true );
        }
        else
            super.actionPerformed( e );
    }


    /**
     * 
     * @param e
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
     * 
     * @return
     */
    public Vector getSearchCriteria()
    {
        Vector criteria = new Vector();
        ACLPropertySearchModel model = (ACLPropertySearchModel)propTable.getModel();
        for( int i=0; i<model.getRealRowCount(); i++ )
            criteria.add( model.getRow(i) );
        return criteria;
    }


    protected JTable propTable;
    protected JButton addButton;
    protected JButton deleteButton;
    protected boolean match;
}
