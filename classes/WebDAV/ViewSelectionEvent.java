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

import java.util.*;

public class ViewSelectionEvent extends EventObject {
   
        Object strPath;
        Object Node;

  public ViewSelectionEvent(Object module, Object Node, Object strPath) {

    super(module);
    this.strPath = strPath;
    this.Node = Node;
  }

  public String getPath() {
    return new String (strPath.toString());
  }
  public Object getNode() {
    return Node;
  }
}
