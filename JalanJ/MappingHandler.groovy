/**
 * 
 */
package JalanJ

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler

/**
 * @author adrian
 *
 * Mapping Handler extracts individual thread execution patterns
 */
class MappingHandler extends DefaultHandler {
	
	def threadCount
	long threadBitMap
	def threadList = []
	def currentThread
	def groupingName

	MappingHandler(def baseName)
	{
		super()
		threadCount = 0
		threadBitMap = 0L
		currentThread = new PosixThreadTID(0, 0, 0)
		groupingName = baseName
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		if (qName == 'thread') {
			
			def threadNumberStr = attrs.getValue('tid')
			def threadNumberInt = Integer.parseInt(threadNumberStr, 16)
			if (threadNumberInt != currentThread.number) {
				def nextThread
				def testThread = 1 << (threadNumberInt - 1)
				if (!(threadBitMap & testThread)) {
					threadBitMap = threadBitMap | testThread
					nextThread =
						new PosixThreadTID(threadNumberInt, currentThread)
					threadList << nextThread
					nextThread.openXML(groupingName)
					if (currentThread.number > 0) {
						currentThread.outputSpawn(threadNumberInt)
					}
					
					print("Thread $threadNumberInt begins after thread ")
					print("$currentThread.number reference")
					println(" $currentThread.referenceCount")
				} else {
					nextThread = threadList.find {it.number == threadNumberInt}
				}
				currentThread = nextThread
			}
		} else {
			switch (qName) {
				case 'instruction':
				currentThread.
					outputInstruction(attrs.getValue('address'),
						attrs.getValue('size'))
				break
				
				case 'modify':
				currentThread.
					outputModify(attrs.getValue('address'),
						attrs.getValue('size'))
				break
				
				case 'load':
				currentThread.
					outputLoad(attrs.getValue('address'),
						attrs.getValue('size'))
				break
				
				case 'store':
				currentThread.
					outputStore(attrs.getValue('address'),
						attrs.getValue('size'))		
				
				default:
				break
			}
			currentThread.increment()
		}
	}
		
	void endDocument()
	{
		threadList.each() {
			it.closeXML()
		} 
	}
}
