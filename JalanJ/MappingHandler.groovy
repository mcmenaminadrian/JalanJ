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
	long threadBitmap
	def threadList = []
	def currentThread

	MappingHandler()
	{
		super()
		threadCount = 0
		threadBitmap = 0L
		currentThread = new PosixThreadTID(0, 0, 0)
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		if (qName == 'thread') {
			
			def threadNumberStr = attrs.getValue('tid')
			def threadNumberInt = Integer.parseInt(threadNumberStr, 16)
			if (threadNumberInt != currentThread) {
				def nextThread
				def testThread = 1 << (threadNumberInt - 1)
				if (!(threadBitMap & testThread)) {
					threadBitMap = threadBitMap | testThread
					nextThread =
						new PosixThreadTID(threadNumberInt, currentThread)
					threadList << nextThread
					println "Thread $threadNumberInt begins after "
					"$currentThread.number instruction"
					" $currentThread.instructionCount"
				} else {
					nextThread = threadList.find {it.number = threadNumberInt}
				}
				currentThread = nextThread
			}
		} else {
			if (qName == 'instruction'||qName == 'store' ||
				qName == 'load' || qName == 'modify') {
				currentThread++
			}
		}
	}
}
