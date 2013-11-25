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
	def master
	def waitState
	def myProcessor
	
	
	ThreadHandler(def processors, def threadNo, def callback)
	{
		super()
		processorList = processors
		threadNumber = threadNo
		master = callback
		waitState = false
		myProcessor = -1
	}
	
	def allocatePage = {processor, address ->
		processor.allocateToFree(address)
	}
	
	//wait for next global clock tick
	void waitForTick()
	{
		waitOne = new Semaphore(0)
		master.signalTick()
		waitOne.acquire()
	}
	
	//check if we have a processor and attempt to assign one if we don't
	def getProcessor()
	{
		while (true) {
			for (i in 0 .. processorList.size() - 1) {
				if (processorList[i].matchThread(threadNumber)) {
					myProcessor = i
					waitState = false
					return i
				}
			}
		}
		while (true) {
			waitState = true
			myProcessor = -1
			for (i in 0 .. processorList.size() - 1) {
				if (processorList[i].assignThread(threadNumber)) {
					myProcessor = i
					waitState = false
					return i
				}
			}
			waitForTick()
		}
	}
	
	void addressRead(long address)
	{
		if (!processorList.find{it.gotPage(address)})
		{
			def countDown = 100
			waitState = true
			while (countDown > 0) {
				waitForTick()
				countDown--
			}
			
			//Do we still hold the processor?
			getProcessor()
			//we might have the page by now
			if (processorList.find{it.gotPage(address)}) {
				waitForTick()
				return
			} else {
				if (processorList.find{it.gotPage(address)}) {
				waitForTick()
				return
				}
				//cannot allocate to free page, so dump a page
				processorList[myProcessor].addPage(address)
			}
		}
		waitForTick()
	}
	
	void startElement(String ns, String localName, String qName,
		Attributes attrs) {
		
		getProcessor()
		
		switch (qName) {
			
			case 'instruction':
			case 'load':
			case 'store':
			def address = Long.parseLong(attrs.getValue('address'), 16)
			/* FIX ME: we assume for now that allocations are aligned */
			/* FIX ME: do not account for processor locality yet */
			addressRead(address)
			break;
			
			case 'modify':
			//have to do it twice
			def address = Long.parseLong(attrs.getValue('address'), 16)
			addressRead(address)
			addressRead(address)
			break
			
			case 'spawn':
			//start a new thread
			def nextThread = attrs.getValue('thread')
			master.handleThread(nextThread, master.processorList)
			println "Have spawned thread ${nextThread} after ${master.timeElapsed} ticks"
			break
		}
		
	}
	
}
