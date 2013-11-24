/**
 * 
 */
package JalanJ

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler
import java.util.concurrent.Semaphore

/**
 * @author adrian
 *
 */
class ThreadHandler extends DefaultHandler {
	
	def processorList
	def threadNumber
	def waitOne
	def waitCount
	def master
	
	
	ThreadHandler(def processors, def threadNo, def callback)
	{
		super()
		processorList = processors
		threadNumber = threadNo
		master = callback
	}
	
	def findPage = {processor, address ->
		processor.gotPage(address)
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		
		def progress = false
		
		while (progress == false) {
			for (i in 0 .. processorList.size() - 1) {
				if (processorList[i].matchThread(threadNumber)) {
					progress = true
					break;
				}
			}
			if (progress == false){
				for (i in 0 .. processorList.size() - 1) {
					if (processorList[i].matchThread(-1)) {
						progress = true
						break
					} else {
						master.signalTick()
						waitOne = new Semaphore(0)
						waitOne.aquire()
					}
				}
			}
		}
		
		switch (qName) {
			
			case 'instruction':
			def address = Long.parseLong(attrs.getValue('address'), 16)
			/* FIX ME: we assume for now that allocations are aligned */
			/* FIX ME: do not account for processor locality yet */
			if (!processorList.find(findPage(it, address)))
			{
				master.signalTick()
				waitCount = new Semaphore(-100)
				waitCount.aquire()
			}
			
		}
		
	}
	
}
