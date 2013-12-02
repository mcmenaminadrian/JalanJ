
package jalanJ

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
	def memoryWidth
	def instructionCount
	long perThreadFault
	
	ThreadHandler(def processors, def threadNo, def callback)
	{
		super()
		processorList = processors
		threadNumber = threadNo
		master = callback
		waitState = false
		myProcessor = -1
		memoryWidth = processors[0].PAGESIZE/16
		perThreadFault = 0
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
		for (i in 0 .. processorList.size() - 1) {
			if (processorList[i].matchThread(threadNumber)) {
				myProcessor = i
				waitState = false
				return i
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
		def havePage = false
		def countDown = 100 * memoryWidth
		getProcessor()
		while (countDown > 0) {
		//	if (!processorList.find {it.gotPage(address)}) {
			if (!processorList[0].gotPage(address)) {
				waitState = true
				waitForTick()
				countDown--
			} else {
				havePage = true
				break
			}
		}
		if (!havePage) {
			master.incrementFaultCount()
			perThreadFault++ 
			processorList.find{ it.addPage(address)}
		}
		waitForTick()
	}
	
	synchronized long getPerThreadFaults()
	{
		long oldFaultCount = perThreadFault
		perThreadFault = 0
		return oldFaultCount
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
			master.handleThread(nextThread, processorList)
			println "Have spawned thread ${nextThread} after ${master.timeElapsed} ticks"
			break
		}
		
	}
		
	void endDocument()
	{
		println "Thread $threadNumber finished at tick ${master.timeElapsed}"
	}
	
}
