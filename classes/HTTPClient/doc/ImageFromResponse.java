
import java.awt.Image;
import java.awt.Component;
import java.io.InputStream;
import java.io.IOException;
import java.net.URLConnection;
import sun.awt.image.URLImageSource;
import HTTPClient.HTTPResponse;
import HTTPClient.ModuleException;


/**
 * This uses sun.awt.image.URLImageSource to produce an image from an
 * HTTPResponse. The idea was taken from David Erb:
 * http://www.dtack.com/java/dtack/hinweis/imagedecode/imagedecode.html
 */
public class ImageFromResponse extends URLConnection
{
    HTTPResponse resp;

    private ImageFromResponse(HTTPResponse resp)
    {
	super(null);
	this.resp = resp;
    }

    public static Image create(HTTPResponse resp, Component comp)
    {
	return comp.createImage(new URLImageSource(new ImageFromResponse(resp)));
    }

    public void connect()  { }

    public String getContentType()
    {
	try
	    { return resp.getHeader("Content-type"); }
	catch (Exception e)
	    { return null; }
    }

    public InputStream getInputStream()  throws IOException
    {
	try
	    { return resp.getInputStream(); }
	catch (ModuleException me)
	    { throw new IOException(me.getMessage()); }
    }
}

