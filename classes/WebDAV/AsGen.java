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

// We Use this class to generate namespace aliases within
// an xml document.
//
// Version: 0.1
// Author:  Robert Emmery  <memmery@earthlink.net>
// Date:    2/28/98
////////////////////////////////////////////////////

package WebDAV;

public class AsGen {

  String _lastGenerated;
  public static final String DAV_AS = "D";

  public AsGen() {
    _lastGenerated = DAV_AS;
  }

  public String getNextAs() {
 
  String str = _lastGenerated;

  int len = str.length();
  byte[] byte_str = str.getBytes();
  if (str.endsWith("Z")) {
    byte_str[len-1] = 'A';
    boolean found = false;
    boolean append = true;
    int i = len-2;
    while( (i>=0) && (!found) ) {
      if (byte_str[i] != 'Z') {
        append = false;
        found = true;
        byte_str[i]++;
      }
      else
        byte_str[i] = 'A';

       i--;
     }
    str = new String(byte_str);
    if (append)
      str += 'A';
  }
  else {
    byte_str[len-1]++;
    str = new String(byte_str);
  }
  _lastGenerated = str;
  return str;

  }

}
