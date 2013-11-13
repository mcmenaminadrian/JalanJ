package JalanJ

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler

class ThreadCounter extends DefaultHandler {
	
	
	def threadCount = 0
	def switches = 0L
	def threadBitMap = 0
	def currentThread = 0
	def instructionCount = 0L
	
	ThreadCounter()
	{
		super()
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		if (qName == 'thread') {
			def threadNumberStr = attrs.getValue('tid')
			def threadNumberInt = Integer.parseInt(threadNumberStr, 16)
			if (threadNumberInt != currentThread) {
				currentThread = threadNumberInt
				switches++
				def testThread = 1 << (threadNumberInt - 1)
				if (!(threadBitMap & testThread)) {
					threadBitMap = threadBitMap | testThread
					threadCount++
				}
				print("Now made $switches switches and in thread $threadNumberInt of $threadCount. ")
				println("Previous thread had $instructionCount instructions.")
				instructionCount = 0
			}
		} else {
			if (qName == 'instruction') {
				instructionCount++
			}
		}
	}
}
