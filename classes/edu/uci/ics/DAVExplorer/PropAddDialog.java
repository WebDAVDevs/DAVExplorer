/*
 * Copyright (c) 2001 Regents of the University of California.
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
 * Title:       Property Add Dialog
 * Description: Dialog for adding WebDAV properties
 * Copyright:   Copyright (c) 2001 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        29 September 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * @date        1 October 2001
 * Changes:     Change of package name
 */

package edu.uci.ics.DAVExplorer;

import java.awt.Font;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class PropAddDialog extends JDialog implements ActionListener, FocusListener
{

    public PropAddDialog( String resource, boolean selected )
    {
        super( GlobalData.getGlobalData().getMainFrame(), "Add Property", true );
        JLabel label = new JLabel( resource, JLabel.CENTER );
        //label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.black);
        getContentPane().add( "North", label );

        GridBagLayout gridbag = new GridBagLayout();
        JPanel groupPanel = new JPanel( gridbag );
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        label = new JLabel( "Tag:", JLabel.LEFT );
        label.setForeground(Color.black);
        gridbag.setConstraints( label, constraints );
        groupPanel.add( label );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        tagField = new JTextField(30);
        tagField.addFocusListener( this );
        gridbag.setConstraints( tagField, constraints );
        groupPanel.add( tagField );
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        label = new JLabel( "Namespace:", JLabel.LEFT );
        label.setForeground(Color.black);
        gridbag.setConstraints( label, constraints );
        groupPanel.add( label );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        NSField = new JTextField( 30 );
        NSField.addFocusListener( this );
        gridbag.setConstraints( NSField, constraints );
        groupPanel.add( NSField );
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        label = new JLabel( "Value:", JLabel.LEFT );
        label.setForeground(Color.black);
        gridbag.setConstraints( label, constraints );
        groupPanel.add( label );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        valueField = new JTextField( 30 );
        gridbag.setConstraints( valueField, constraints );
        groupPanel.add( valueField );
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        label = new JLabel( "", JLabel.LEFT );
        gridbag.setConstraints( label, constraints );
        groupPanel.add( label );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        addToRoot = new JRadioButton( "Add to Root Node", !selected );
        gridbag.setConstraints( addToRoot, constraints );
        ButtonGroup addGroup = new ButtonGroup();
        addGroup.add( addToRoot );
        groupPanel.add( addToRoot );
        constraints.gridwidth = 1;
        constraints.weightx = 1.0;
        label = new JLabel( "", JLabel.LEFT );
        gridbag.setConstraints( label, constraints );
        groupPanel.add( label );
        constraints.weightx = 3.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        addToSelected = new JRadioButton( "Add to Selected Node", selected );
        gridbag.setConstraints( addToSelected, constraints );
        addGroup.add( addToSelected );
        groupPanel.add( addToSelected );
        if( !selected )
            addToSelected.setEnabled( false );

        getContentPane().add( "Center", groupPanel );

        okButton = new JButton( "OK" );
        okButton.addActionListener( this );
        cancelButton  = new JButton( "Cancel" );
        cancelButton.addActionListener( this );
        JPanel buttonPanel = new JPanel();
        buttonPanel.add( okButton );
        buttonPanel.add( cancelButton );
        //getRootPane().setDefaultButton( cancelButton );
        //cancelButton.grabFocus();
        okButton.setEnabled( false );
        getContentPane().add( "South", buttonPanel );

        setBackground(Color.lightGray);
        addWindowListener(
            new WindowAdapter()
            {
                public void windowClosing(WindowEvent we_Event)
                {
                    cancel();
                }
            });

        pack();
        setSize( getPreferredSize() );
        center();
        show();
    }

    public void actionPerformed(ActionEvent e)
    {
        if( e.getActionCommand().equals("OK") )
        {
            Ok();
        }
        else if( e.getActionCommand().equals("Cancel") )
        {
            cancel();
        }
    }

    public void focusGained( FocusEvent e )
    {
        checkEnableOk();
    }

    public void focusLost( FocusEvent e )
    {
        checkEnableOk();
    }

    public void Ok()
    {
        setVisible( false );
    }

    public void cancel()
    {
        setVisible( false );
        cancel = true;
    }

    public boolean isCanceled()
    {
        return cancel;
    }

    public String getTag()
    {
        return tagField.getText();
    }

    public String getNamespace()
    {
        return NSField.getText();
    }

    public String getValue()
    {
        return valueField.getText();
    }

    public boolean isAddToRoot()
    {
        return addToRoot.isSelected();
    }

    protected void checkEnableOk()
    {
        if( (tagField.getText().length()>0) && (NSField.getText().length()>0) )
            okButton.setEnabled( true );
        else
            okButton.setEnabled( false );
    }

    protected void center()
    {
        Rectangle recthDimensions = getParent().getBounds();
        Rectangle bounds = getBounds();
        setBounds(recthDimensions.x + (recthDimensions.width-bounds.width)/2,
             recthDimensions.y + (recthDimensions.height - bounds.height)/2, bounds.width, bounds.height );
    }

    private JTextField tagField;
    private JTextField NSField;
    private JTextField valueField;
    private JButton okButton;
    private JButton cancelButton;
    private JRadioButton addToRoot;
    private JRadioButton addToSelected;
    private boolean cancel = false;
}