/*
 * @(#)CookieModule.java				0.3 30/01/1998
 *
 *  This file is part of the HTTPClient package
 *  Copyright (C) 1996-1998  Ronald Tschalaer
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public
 *  License as published by the Free Software Foundation; either
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 *  MA 02111-1307, USA
 *
 *  For questions, suggestions, bug-reports, enhancement-requests etc.
 *  I may be contacted at:
 *
 *  ronald@innovation.ch
 *  Ronald.Tschalaer@psi.ch
 *
 */

package HTTPClient;

import java.io.File;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;

import java.awt.Frame;
import java.awt.Panel;
import java.awt.Label;
import java.awt.Color;
import java.awt.Button;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;


/**
 * This module handles Netscape cookies (also called Version 0 cookies)
 * and Version 1 cookies. Specifically is reads the <var>Set-Cookie</var>
 * and <var>Set-Cookie2</var> response headers and sets the <var>Cookie</var>
 * and <var>Cookie2</var> headers as neccessary.
 *
 * <P>The accepting and sending of cookies is controlled by a
 * <var>CookiePolicyHandler</var>. This allows you to fine tune your privacy
 * preferences. A cookie is only added to the cookie jar if the handler
 * allows it, and a cookie from the cookie jar is only sent if the handler
 * allows it.
 *
 * <P>The cookie jar is not yet persistent, i.e. all cookies are discarded
 * at exit. Persistence will be added in the future.
 *
 * @see <a href="http://home.netscape.com/newsref/std/cookie_spec.html">Netscape's cookie spec</a>
 * @see <a href="ftp://ds.internic.net/internet-drafts/draft-ietf-http-state-man-mec-06.txt">HTTP State Management Mechanism spec</a>
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 * @since	V0.3
 */

public class CookieModule implements HTTPClientModule, GlobalConstants
{
    /** the list of known cookies */
    private static Hashtable cookie_cntxt_list = new Hashtable();

    /** the file to use for persistent cookie storage */
    private static File cookie_jar = null;

    /** the cookie policy handler */
    private static CookiePolicyHandler cookie_handler =
					    new DefaultCookiePolicyHandler();



    // read in cookies from disk at startup

    static
    {
	boolean persist;
	try
	{
	    // don't use Boolean.getBoolean, as netscape won't throw a
	    // security exception for that...
	    String dont = System.getProperty("HTTPClient.cookies.dont_persist");
	    persist = !Boolean.valueOf(dont).booleanValue();
	}
	catch (Exception e)
	    { persist = false; }

	if (persist)
	{
	    cookie_jar = new File(getCookieJarName());
	    Hashtable cookie_list = Util.getList(cookie_cntxt_list,
					 HTTPConnection.getDefaultContext());
	    Cookie.readFromFile(cookie_jar, cookie_list);

	    try
		{ System.runFinalizersOnExit(true); }
	    catch (Throwable t)
		{ }
	}
    }


    static void classFinalize()
    {
	if (cookie_jar != null)
	{
	    Hashtable cookie_list = Util.getList(cookie_cntxt_list,
					 HTTPConnection.getDefaultContext());
	    Cookie.saveToFile(cookie_jar, cookie_list);
	}
    }


    private static String getCookieJarName()
    {
	String file = null;

	try
	    { file = System.getProperty("HTTPClient.cookies.file"); }
	catch (Exception e)
	    { }

	if (file == null)
	{
	    String os = System.getProperty("os.name");
	    String hj_cf, nn_cf, ie_cf;

	    if (os.equalsIgnoreCase("Windows 95"))
	    {
		hj_cf = System.getProperty("java.home") +
		        File.separator + ".hotjava" +
		        File.separator + "cookies.txt";
		nn_cf = "C:" + File.separator + "program files" +
			File.separator + "netscape" +
			File.separator + "cookies.txt";
		ie_cf = "C:" + File.separator + "windows" +
			File.separator + "cookies" + File.separator;
	    }
	    else if (os.equalsIgnoreCase("Windows NT"))
	    {
		hj_cf = System.getProperty("user.home") +
		        File.separator + ".hotjava" +
		        File.separator + "cookies.txt";
		nn_cf = "C:" + File.separator + "program files" +
			File.separator + "netscape" +
			File.separator + "cookies.txt";
		ie_cf = "C:" + File.separator + "winnt" +
			File.separator + "cookies" + File.separator;
	    }
	    else if (os.equalsIgnoreCase("Mac OS"))
	    {
		hj_cf = null;
		nn_cf = "System Folder" + File.separator +
			"Preferences" + File.separator +
			"Netscape" + File.separator +
			"MagicCookie";
		ie_cf = null;
	    }
	    else		// it's probably U*IX
	    {
		hj_cf = System.getProperty("user.home") +
			File.separator + ".hotjava" +
			File.separator + "cookies";
		nn_cf = System.getProperty("user.home") +
			File.separator + ".netscape" +
			File.separator + "cookies";
		ie_cf = null;
	    }

	    File tmp = new File(nn_cf);
	    if (tmp.isFile()  &&  tmp.canRead())
		file = nn_cf;

	    tmp = new File(hj_cf);
	    if (tmp.isFile()  &&  tmp.canRead())
		file = hj_cf;

	    if (file == null)
		file = hj_cf;
	}

	return file;
    }


    // Constructors

    CookieModule()
    {
    }


    // Methods

    /**
     * Invoked by the HTTPClient.
     */
    public int requestHandler(Request req, Response[] resp)
    {
	// First remove any Cookie headers we might have set for a previous
	// request

	NVPair[] hdrs = req.getHeaders();
	int length = hdrs.length;
	for (int idx=0; idx<hdrs.length; idx++)
	{
	    int beg = idx;
	    while (idx < hdrs.length  &&
		   hdrs[idx].getName().equalsIgnoreCase("Cookie"))
		idx++;

	    if (idx-beg > 0)
	    {
		length -= idx-beg;
		System.arraycopy(hdrs, idx, hdrs, beg, length-beg);
	    }
	}
	if (length < hdrs.length)
	{
	    hdrs = Util.resizeArray(hdrs, length);
	    req.setHeaders(hdrs);
	}


	// Now set any new cookie headers

	Vector names = new Vector();
	Vector lens  = new Vector();

	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, req.getConnection().getContext());
	Enumeration list = cookie_list.elements();
	boolean cookie2 = false;
	while (list.hasMoreElements())
	{
	    Cookie cookie = (Cookie) list.nextElement();

	    if (cookie.hasExpired())
	    {
		cookie_list.remove(cookie);
		continue;
	    }

	    if (cookie.sendWith(req)  &&  (cookie_handler == null  ||
		cookie_handler.sendCookie(cookie, req)))
	    {
		int len = cookie.getPath().length();
		int idx;

		// insert in correct position
		for (idx=0; idx<lens.size(); idx++)
		    if (((Integer) lens.elementAt(idx)).intValue() < len) break;

		names.insertElementAt(cookie.toExternalForm(), idx);
		lens.insertElementAt(new Integer(len), idx);

		if (cookie instanceof Cookie2)  cookie2 = true;
	    }
	}

	if (names.size() > 0)
	{
	    StringBuffer value = new StringBuffer();

	    if (cookie2)
		value.append("$Version=\"1\"");

	    value.append((String) names.elementAt(0));
	    for (int idx=1; idx<names.size(); idx++)
	    {
		value.append("; ");
		value.append((String) names.elementAt(idx));
	    }
	    hdrs = Util.resizeArray(hdrs, hdrs.length+1);
	    hdrs[hdrs.length-1] = new NVPair("Cookie", value.toString());

	    // add Cookie2 header if necessary
	    if (!cookie2)
	    {
		int idx;
		for (idx=0; idx<hdrs.length; idx++)
		    if (hdrs[idx].getName().equalsIgnoreCase("Cookie2"))
			break;
		if (idx == hdrs.length)
		{
		    hdrs = Util.resizeArray(hdrs, hdrs.length+1);
		    hdrs[hdrs.length-1] =
				    new NVPair("Cookie2", "$Version=\"1\"");
		}
	    }

	    req.setHeaders(hdrs);

	    if (DebugMods)
		System.err.println("CookM: Sending cookies '" + value + "'");
	}

	return REQ_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase1Handler(Response resp, RoRequest req)
	    throws IOException
    {
	String set_cookie  = resp.getHeader("Set-Cookie");
	String set_cookie2 = resp.getHeader("Set-Cookie2");
	if (set_cookie == null  &&  set_cookie2 == null)
	    return;

	resp.deleteHeader("Set-Cookie");
	resp.deleteHeader("Set-Cookie2");

	if (set_cookie != null)
	    handleCookie(set_cookie, false, req, resp);
	if (set_cookie2 != null)
	    handleCookie(set_cookie2, true, req, resp);
    }


    /**
     * Invoked by the HTTPClient.
     */
    public int responsePhase2Handler(Response resp, Request req)
    {
	return RSP_CONTINUE;
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void responsePhase3Handler(Response resp, RoRequest req)
    {
    }


    /**
     * Invoked by the HTTPClient.
     */
    public void trailerHandler(Response resp, RoRequest req)  throws IOException
    {
	String set_cookie = resp.getTrailer("Set-Cookie");
	String set_cookie2 = resp.getHeader("Set-Cookie2");
	if (set_cookie == null  &&  set_cookie2 == null)
	    return;

	resp.deleteTrailer("Set-Cookie");
	resp.deleteTrailer("Set-Cookie2");

	if (set_cookie != null)
	    handleCookie(set_cookie, false, req, resp);
	if (set_cookie2 != null)
	    handleCookie(set_cookie2, true, req, resp);
    }


    private void handleCookie(String set_cookie, boolean cookie2, RoRequest req,
			      Response resp)
	    throws ProtocolException
    {
	Cookie[] cookies;
	if (cookie2)
	    cookies = Cookie2.parse(set_cookie, req);
	else
	    cookies = Cookie.parse(set_cookie, req);

	if (DebugMods)
	{
	    System.err.println("CookM: Received and parsed " + cookies.length +
			       " cookies:");
	    for (int idx=0; idx<cookies.length; idx++)
		System.err.println("CookM: Cookie " + idx + ": " +cookies[idx]);
	}

	Hashtable cookie_list =
	    Util.getList(cookie_cntxt_list, req.getConnection().getContext());
	for (int idx=0; idx<cookies.length; idx++)
	{
	    Cookie cookie = (Cookie) cookie_list.get(cookies[idx]);
	    if (cookie != null  &&  cookies[idx].hasExpired())
		cookie_list.remove(cookie);		// expired, so remove
	    else  					// new or replaced
	    {
		if (cookie_handler == null  ||
		    cookie_handler.acceptCookie(cookies[idx], req, resp))
		    cookie_list.put(cookies[idx], cookies[idx]);
	    }
	}
    }


    /**
     * Discard all cookies for all contexts. Cookies stored in persistent
     * storage are not affected.
     */
    public static void discardAllCookies()
    {
	cookie_cntxt_list = new Hashtable();
    }


    /**
     * Discard all cookies for the given context. Cookies stored in persistent
     * storage are not affected.
     *
     * @param context the context Object
     */
    public static void discardAllCookies(Object context)
    {
	cookie_cntxt_list.put(context, new Hashtable());
    }


    /**
     * Sets a new cookie policy handler. This handler will be called for each
     * cookie that a server wishes to set and for each cookie that this
     * module wishes to send with a request. In either case the handler may
     * allow or reject the operation.
     *
     * <P>At initialization time a default handler is installed. This
     * handler allows all cookies to be sent. For any cookie that a server
     * wishes to be set two lists are consulted. If the server matches any
     * host or domain in the reject list then the cookie is rejected; if
     * the server matches any host or domain in the accept list then the
     * cookie is accepted. If no host or domain match is found in either of
     * these two lists and user interaction is allowed then a dialog box is
     * poped up to ask the user whether to accept or reject the cookie; if
     * user interaction is not allowed the cookie is accepted.
     *
     * <P>The accept and reject lists in the default handler are initialized
     * at startup from the two properties
     * <var>HTTPClient.cookies.hosts.accept</var> and
     * <var>HTTPClient.cookies.hosts.reject</var>. These properties must
     * contain a "|" separated list of host and domain names. All names
     * beginning with a "." are treated as domain names, all others as host
     * names. An empty string which will match all hosts. The two lists are
     * further expanded if the user chooses one of the "Accept All from Domain"
     * or "Reject All from Domain" buttons in the dialog box.
     *
     * @param the new policy handler
     * @return the previous policy handler
     */
    public static CookiePolicyHandler
			    setCookiePolicyHandler(CookiePolicyHandler handler)
    {
	CookiePolicyHandler old = cookie_handler;
	cookie_handler = handler;
	return old;
    }
}


/**
 * A simple cookie policy handler.
 */

class DefaultCookiePolicyHandler implements CookiePolicyHandler
{
    /** a list of all hosts and domains from which to silently accept cookies */
    private String[] accept_domains;

    /** a list of all hosts and domains from which to silently reject cookies */
    private String[] reject_domains;

    /** the query popup */
    private BasicCookieBox popup = null;


    DefaultCookiePolicyHandler()
    {
	// have all cookies been accepted or rejected?
	String list;

	try
	    { list = System.getProperty("HTTPClient.cookies.hosts.accept"); }
	catch (Exception e)
	    { list = null; }
	accept_domains = Util.splitProperty(list);
	for (int idx=0; idx<accept_domains.length; idx++)
	    accept_domains[idx] = accept_domains[idx].toLowerCase();

	try
	    { list = System.getProperty("HTTPClient.cookies.hosts.reject"); }
	catch (Exception e)
	    { list = null; }
	reject_domains = Util.splitProperty(list);
	for (int idx=0; idx<reject_domains.length; idx++)
	    reject_domains[idx] = reject_domains[idx].toLowerCase();
    }


    /**
     * returns whether this cookie should be accepted. First checks the
     * stored lists of accept and reject domains, and if it is neither
     * accepted nor rejected by these then query the user via a popup.
     *
     * @param cookie   the cookie in question
     * @param req      the request
     * @param resp     the response
     * @return true if we accept this cookie.
     */
    public boolean acceptCookie(Cookie cookie, RoRequest req, RoResponse resp)
    {
	String server = req.getConnection().getHost().toLowerCase();


	// Check lists. Reject takes priority over accept

	for (int idx=0; idx<reject_domains.length; idx++)
	{
	    if (reject_domains[idx].length() == 0  ||
		reject_domains[idx].charAt(0) == '.'  &&
		server.endsWith(reject_domains[idx])  ||
		reject_domains[idx].charAt(0) != '.'  &&
		server.equals(reject_domains[idx]))
		    return false;
	}

	for (int idx=0; idx<accept_domains.length; idx++)
	{
	    if (accept_domains[idx].length() == 0  ||
		accept_domains[idx].charAt(0) == '.'  &&
		server.endsWith(accept_domains[idx])  ||
		accept_domains[idx].charAt(0) != '.'  &&
		server.equals(accept_domains[idx]))
		    return true;
	}


	// Ok, not in any list, so ask the user (if allowed).

	if (!req.allowUI())  return true;

	if (popup == null)
	    popup = new BasicCookieBox();

	return popup.accept(cookie, this, server);
    }


    void addAcceptDomain(String domain)
    {
	for (int idx=0; idx<accept_domains.length; idx++)
	{
	    if (domain.endsWith(accept_domains[idx]))
		return;
	    if (accept_domains[idx].endsWith(domain))
	    {
		accept_domains[idx] = domain;
		return;
	    }
	}
	accept_domains =
		    Util.resizeArray(accept_domains, accept_domains.length+1);
	accept_domains[accept_domains.length-1] = domain;
    }

    void addRejectDomain(String domain)
    {
	for (int idx=0; idx<reject_domains.length; idx++)
	{
	    if (domain.endsWith(reject_domains[idx]))
		return;
	    if (reject_domains[idx].endsWith(domain))
	    {
		reject_domains[idx] = domain;
		return;
	    }
	}

	reject_domains =
		    Util.resizeArray(reject_domains, reject_domains.length+1);
	reject_domains[reject_domains.length-1] = domain;
    }


    /**
     * This handler just allows all cookies to be sent which were accepted
     * (i.e. no further restrictions are placed on the sending of cookies).
     *
     * @return true
     */
    public boolean sendCookie(Cookie cookie, RoRequest req)
    {
	return true;
    }
}


/**
 * A simple popup that asks whether the cookie should be accepted or rejected,
 * or if cookies from whole domains should be silently accepted or rejected.
 *
 * @version	0.3  30/01/1998
 * @author	Ronald Tschal&auml;r
 */
class BasicCookieBox extends Frame
{
    private final static String title = "Set Cookie Request";
    private Dimension           screen;
    private Label		name_value_label;
    private Label		domain_label;
    private Label		path_label;
    private Label		expires_label;
    private Label		secure_label;
    private TextField		domain;
    private Button		default_focus;
    private boolean             accept;
    private boolean             accept_domain;


    /**
     * Constructs the popup.
     */
    BasicCookieBox()
    {
	super(title);

	screen = getToolkit().getScreenSize();

	addNotify();
	addWindowListener(new Close());

	GridBagLayout layout;
	setLayout(layout = new GridBagLayout());
	GridBagConstraints constr = new GridBagConstraints();

	Label header_label =
		new Label("The server would like to set the following cookie:");
	add(header_label);
	constr.gridwidth = GridBagConstraints.REMAINDER;
	constr.anchor = GridBagConstraints.WEST;
	layout.setConstraints(header_label, constr);

	Panel p = new Panel();
	Panel pp = new Panel();
	pp.setLayout(new GridLayout(4,1));
	pp.add(new Label("Name=Value:"));
	pp.add(new Label("Domain:"));
	pp.add(new Label("Path:"));
	pp.add(new Label("Expires:"));
	p.add(pp);

	pp = new Panel();
	pp.setLayout(new GridLayout(4,1));
	pp.add(name_value_label = new Label());
	pp.add(domain_label = new Label());
	pp.add(path_label = new Label());
	pp.add(expires_label = new Label());
	p.add(pp);
	add(p);
	layout.setConstraints(p, constr);
	add(secure_label = new Label(""));
	layout.setConstraints(secure_label, constr);

	add(default_focus = new Button("Accept"));
	default_focus.addActionListener(new Accept());
	constr.gridwidth = 1;
	constr.anchor = GridBagConstraints.CENTER;
	constr.weightx = 1.0;
	layout.setConstraints(default_focus, constr);

	Button b;
	add(b= new Button("Reject"));
	b.addActionListener(new Reject());
	constr.gridwidth = GridBagConstraints.REMAINDER;
	layout.setConstraints(b, constr);

	constr.weightx = 0.0;
	p = new Separator();
	add(p);
	constr.fill = GridBagConstraints.HORIZONTAL;
	layout.setConstraints(p, constr);

	header_label =
	    new Label("Accept/Reject all cookies from a host or domain:");
	add(header_label);
	constr.fill   = GridBagConstraints.NONE;
	constr.anchor = GridBagConstraints.WEST;
	layout.setConstraints(header_label, constr);

	p = new Panel();
	p.add(new Label("Host/Domain:"));
	p.add(domain = new TextField(30));
	add(p);
	layout.setConstraints(p, constr);

        header_label =
	    new Label("domains are characterized by a leading dot (`.')");
	add(header_label);
	layout.setConstraints(header_label, constr);
	header_label =	new Label("(an empty string matches all hosts)");
	add(header_label);
	layout.setConstraints(header_label, constr);

	add(b = new Button("Accept All"));
	b.addActionListener(new AcceptDomain());
	constr.anchor    = GridBagConstraints.CENTER;
	constr.gridwidth = 1;
	constr.weightx   = 1.0;
	layout.setConstraints(b, constr);

	add(b = new Button("Reject All"));
	b.addActionListener(new RejectDomain());
	constr.gridwidth = GridBagConstraints.REMAINDER;
	layout.setConstraints(b, constr);

	pack();
	setResizable(false);
    }


    public Dimension getMaximumSize()
    {
	return new Dimension(screen.width*3/4, screen.height*3/4);
    }


    /**
     * our event handlers
     */
    class Accept implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
	    accept = true;
	    accept_domain = false;
            synchronized (BasicCookieBox.this)
		{ BasicCookieBox.this.notifyAll(); }
        }
    }

    class Reject implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
	{
	    accept = false;
	    accept_domain = false;
            synchronized (BasicCookieBox.this)
		{ BasicCookieBox.this.notifyAll(); }
	}
    }

    class AcceptDomain implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
	    accept = true;
	    accept_domain = true;
            synchronized (BasicCookieBox.this)
		{ BasicCookieBox.this.notifyAll(); }
	}
    }

    class RejectDomain implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
	{
	    accept = false;
	    accept_domain = true;
            synchronized (BasicCookieBox.this)
		{ BasicCookieBox.this.notifyAll(); }
	}
    }


    class Close extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    new Reject().actionPerformed(null);
	}
    }


    /**
     * the method called by the DefaultCookiePolicyHandler.
     *
     * @return the username/password pair
     */
    synchronized boolean accept(Cookie cookie, DefaultCookiePolicyHandler h,
				String server)
    {
	// set the new values

	name_value_label.setText(cookie.getName() + "=" + cookie.getValue());
	domain_label.setText(cookie.getDomain());
	path_label.setText(cookie.getPath());
	if (cookie.expires() == null)
	    expires_label.setText("at end of session");
	else
	    expires_label.setText(cookie.expires().toString());
	if (cookie.isSecure())
	    secure_label.setText("This cookie will only be sent over secure connections");
	else
	    secure_label.setText("");


	// invalidate all labels, so that new values are displayed correctly

	name_value_label.invalidate();
	domain_label.invalidate();
	path_label.invalidate();
	expires_label.invalidate();
	secure_label.invalidate();
	invalidate();


	// set default domain test

	domain.setText(cookie.getDomain());


	// display

	pack();
	setLocation((screen.width-getPreferredSize().width)/2,
		    (int) ((screen.height-getPreferredSize().height)/2*.7));
	default_focus.requestFocus();
	setVisible(true);


	// wait for user input

	try { wait(); } catch (InterruptedException e) { }

	setVisible(false);


	// handle accept/reject domain buttons

	if (accept_domain)
	{
	    String dom = domain.getText().trim().toLowerCase();

	    if (accept)
		h.addAcceptDomain(dom);
	    else
		h.addRejectDomain(dom);

	    accept =
		accept != (dom.length() == 0  ||
			   dom.charAt(0) == '.'  &&  server.endsWith(dom)  ||
			   dom.charAt(0) != '.'  &&  server.equals(dom));
	}

	return accept;
    }
}


/**
 * A simple separator element.
 */
class Separator extends Panel
{
    public void paint(Graphics g)
    {
	int w = getSize().width,
	    h = getSize().height/2;

	g.setColor(Color.darkGray);
	g.drawLine(2, h-1, w-2, h-1);
	g.setColor(Color.white);
	g.drawLine(2, h, w-2, h);
    }

    public Dimension getMinimumSize()
    {
	return new Dimension(4, 2);
    }
}

