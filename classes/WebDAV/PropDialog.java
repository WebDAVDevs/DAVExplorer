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

package WebDAV;

import java.awt.*;
import java.awt.event.*;
import com.sun.java.swing.*;
import com.sun.java.swing.border.*;
import java.util.*;

public class PropDialog {

      protected String data;
      protected String initial_data;
      String host;
      String resource;
      GUI gui;
      private Vector PropDialogListeners = new Vector();
      public PropDialog(String Resource, String Host, String xml) {

           data = new String(xml);
           initial_data = new String(xml);
           host = Host;
           resource = Resource;
           gui = new GUI(this);
           gui.setVisible(true);

       }
       public static final void main(String[] argv) {
         PropDialog d = new PropDialog("Hostname","Resource","Text..");
       }
       public String getHost() {
         return host;
       }
       public void setData(String newData) {
         this.data = newData;
       }
       public String getResource() {
         return resource;
       }
       public void ok() {
           notifyPropDialogChange();
           gui.setVisible(false);
           gui = null;
       }
       public void cancel() {
           gui.setVisible(false);
           gui = null;
       }

       public void addPropDialogListener(PropDialogListener pl) {
           if (!PropDialogListeners.contains(pl))
           {
                PropDialogListeners.addElement(pl);
           }
       }

       public void removePropDialogListener(PropDialogListener pl) {
            if (PropDialogListeners.contains(pl))
                PropDialogListeners.removeElement(pl);

       }

       public void notifyPropDialogChange() {

           PropDialogEvent evt = new PropDialogEvent(this, host, resource, initial_data,data);
           Vector v;
           synchronized(this)
           {
                v = (Vector)PropDialogListeners.clone();
           }

           int cnt = v.size();
           for (int i = 0; i < cnt; i++)
           {
                PropDialogListener client = (PropDialogListener)v.elementAt(i);
                client.propDialog(evt);
           }

       }
}

class GUI extends Frame implements ActionListener {

    PropDialog pd = null;
    JTextArea textArea = null;

    public GUI(PropDialog pd) {
          super("Modify Properties");
          this.pd = (PropDialog) pd;
          setSize(400,400);
          Label label = new Label(pd.getResource() + " (" + pd.getHost() + ")",Label.CENTER);
          label.setFont(new Font("Dialog", Font.PLAIN, 14));
          add("North",label);

          Panel buttonPanel=new Panel();
          textArea = new JTextArea(pd.data);
          Button okButton = new Button("Submit");

          okButton.addActionListener(this);

          Button cancelButton  = new Button("Cancel");
          cancelButton.addActionListener(this);

   
           buttonPanel.add(okButton);
           buttonPanel.add(cancelButton);
           add("South",buttonPanel);
           setBackground(Color.lightGray);

        JPanel textAreaPanel = new JPanel();

	textAreaPanel.add(Box.createRigidArea(new Dimension(1,10)));

	JPanel textWrapper = new JPanel(new BorderLayout());
	textWrapper.setAlignmentX(LEFT_ALIGNMENT);
 	textWrapper.setBorder(new SoftBevelBorder(BevelBorder.LOWERED));

	textAreaPanel.add(textWrapper);

 
	JScrollPane scroller = new JScrollPane() {
            public Dimension getPreferredSize() {
		return new Dimension(300,300);
	    }
	    public float getAlignmentX() {
		return LEFT_ALIGNMENT;
	    }
	};

	scroller.getViewport().add(textArea);
        textArea.setFont(new Font("Dialog", Font.PLAIN, 12));
	textWrapper.add(scroller, BorderLayout.CENTER);

	add(Box.createRigidArea(new Dimension(10,1)));
        add("Center",textWrapper);

        addWindowListener(new WindowAdapter() {public void windowClosing(
                          WindowEvent we_Event) {System.exit(0);} });
    }
    public void actionPerformed(ActionEvent e) {
          if (e.getActionCommand().equals("Submit")) {
            pd.setData(textArea.getText());
            pd.ok();
          }
          else
            pd.cancel();
    }

}
