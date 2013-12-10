
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
	long instructionCount
	long perThreadFault
	def tickOn
	
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
		tickOn = 0
		master.activeThreads++
		instructionCount = 0
	}
	
	//wait for next global clock tick
	void waitForTick()
	{
		if (++tickOn >= master.synchCount) {
			waitOne = new Semaphore(0)
			master.signalTick()
			waitOne.acquire()
			tickOn = 0
		}
	}
	
	//check if we have a processor and attempt to assign one if we don't
	def getProcessor()
	{
		master.activeThreads--
		for (i in 0 .. processorList.size() - 1) {
			if (processorList[i].matchThread(threadNumber)) {
				myProcessor = i
				waitState = false
				master.activeThreads++
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
					master.activeThreads++
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
	
	synchronized long getInstructionCount()
	{
		long oldInstructionCount = instructionCount
		instructionCount = 0
		return oldInstructionCount
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
			instructionCount++
			break;
			
			case 'modify':
			//have to do it twice
			def address = Long.parseLong(attrs.getValue('address'), 16)
			addressRead(address)
			addressRead(address)
			instructionCount++
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
		activeThreads--
		myProcessor.deassignThread()
	}
	
}
