/**
 * 
 */
package jalanJ

import org.xml.sax.Attributes;
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.*




/**
 * @author adrian
 *
 */
class FirstThreadHandler extends ThreadHandler {
	
	def noSpawns
	
	
	FirstThreadHandler(def processors, def threadNo, def callback){
		super(processors, threadNo, callback)
		noSpawns = true
		myProcessor = getProcessor()
	}
	
	void addressRead(long address)
	{
		if (!processorList[0].gotPage(address)) {
				master.timeElapsed += 100 * memoryWidth
				master.incrementFaultCount()
				perThreadFault++
				processorList[0].addPage(address)
			}
		master.timeElapsed++
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		
		if (!noSpawns)
			myProcessor = getProcessor()
		
		switch (qName) {
			
			case 'instruction':
			case 'load':
			case 'store':
			def address = Long.parseLong(attrs.getValue('address'), 16)
			/* FIX ME: we assume for now that allocations are aligned */
			/* FIX ME: do not account for processor locality yet */
			if (noSpawns)
				addressRead(address)
			else
				super.addressRead(address)
			instructionCount++
			break;
			
			case 'modify':
			//have to do it twice
			def address = Long.parseLong(attrs.getValue('address'), 16)
			if (noSpawns) {
				addressRead(address)
				addressRead(address)
			} else {
				super.addressRead(address)
				super.addressRead(address)
			}
			instructionCount++
			break
			
			case 'spawn':
			//start a new thread
			noSpawns = false
			def nextThread = attrs.getValue('thread')
			def addedThread = Thread.start {
				def threadNo = Integer.parseInt(nextThread, 16)
				def threadHandler =
					new ThreadHandler(processorList, threadNo, master)
				def threadIn =
					SAXParserFactory.newInstance().newSAXParser().XMLReader
				threadIn.setContentHandler(threadHandler)
				master.handlers << threadHandler
				threadIn.parse(
					new InputSource(new FileInputStream(master.threadMap[nextThread]))
					)
				}
			addedThread.join()
			println "Have spawned thread ${nextThread} after ${master.timeElapsed} ticks"
			break
		}
		
	}

}
