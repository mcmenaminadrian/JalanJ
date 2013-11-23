/**
 * 
 */
package JalanJ

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler

/**
 * @author adrian
 *
 */
class ThreadHandler extends DefaultHandler {
	
	def waitState
	
	
	ThreadHandler()
	{
		super()
		waitState = 0
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		
		
	}
	
}
