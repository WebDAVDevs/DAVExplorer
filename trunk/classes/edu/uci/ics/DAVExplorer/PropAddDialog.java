/*
 * Copyright (c) 2001-2005 Regents of the University of California.
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Title:       Property Add Dialog
 * Description: Dialog for adding WebDAV properties
 * Copyright:   Copyright (c) 2001-2005 Regents of the University of California. All rights reserved.
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         29 September 2001
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         1 October 2001
 * Changes:     Change of package name
 * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         17 December 2001
 * Changes:     Better handling of ok button enable/disable
  * @author      Joachim Feise (dav-exp@ics.uci.edu)
 * date         8 February 2005
 * Changes:     Some refactoring
*/
public class PropAddDialog extends JDialog implements ActionListener, DocumentListener
{
    /**
     * Constructor
     * @param resource
     * @param selected
     */
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
        tagField.getDocument().addDocumentListener( this );
        tagField.addActionListener( this );
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
        NSField.getDocument().addDocumentListener( this );
        NSField.addActionListener( this );
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
        valueField.addActionListener( this );
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
        getRootPane().setDefaultButton( okButton );
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
        GlobalData.getGlobalData().center( this );
        show();
    }


    /**
     * ActionListener interface
     * @param e
     */
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
     *
     */
    public void Ok()
    {
        setVisible( false );
    }


    /**
     *
     */
    public void cancel()
    {
        setVisible( false );
        cancel = true;
    }


    /**
     * 
     * @return
     */
    public boolean isCanceled()
    {
        return cancel;
    }


    /**
     * 
     * @return
     */
    public String getTag()
    {
        return tagField.getText();
    }


    /**
     * 
     * @return
     */
    public String getNamespace()
    {
        return NSField.getText();
    }


    /**
     * 
     * @return
     */
    public String getValue()
    {
        return valueField.getText();
    }


    /**
     * 
     * @return
     */
    public boolean isAddToRoot()
    {
        return addToRoot.isSelected();
    }


    /**
     *
     */
    protected void checkEnableOk()
    {
        if( (tagField.getText().length()>0) && (NSField.getText().length()>0) )
        {
            okButton.setEnabled( true );
            getRootPane().setDefaultButton( okButton );
        }
        else
            okButton.setEnabled( false );
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
