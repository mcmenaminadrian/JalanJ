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

class ControlFileHandler extends DefaultHandler {

	def callbackClass
	def setFirst
	
	ControlFileHandler()
	{
		super()
		setFirst = false
	}
	
	void setCallbackClass(def masterClass)
	{
		callbackClass = masterClass
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		if (qName == 'file') {
			def threadNumber = attrs.getValue("thread")
			def filePath = attrs.getValue("path")
			callbackClass.addThreadMap(threadNumber, filePath)
			if (!setFirst)
			{
				callbackClass.setFirstThread(threadNumber)
				setFirst = true
			}
		}
	}
	
}
