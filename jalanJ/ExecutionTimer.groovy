/**
 * 
 */
package jalanJ

import org.xml.sax.Attributes
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.*

/**
 * @author adrian
 *
 */



class ExecutionTimer {
	
	def controlFile
	long timeElapsed
	def handlers = []
	def processors = []
	def threads = []
	def threadMap = [:]
	def PROCESSORS = 16
	def firstThread
	volatile int activeThreads
	def signalledThreads
	def guiOutput
	def guiWindow
	def faultCount
	def synchCount = 10
	def latchKey
	def resetMemory
	
	ExecutionTimer(def fileStr, def gui, def memModel, def maxSize,
		def pageOffset)
	{
		guiOutput = gui
		controlFile = fileStr
		timeElapsed = 0
		for (i in 1 .. PROCESSORS)
		{
			processors << new ProcessorState(memModel, pageOffset, maxSize,
				this)
		}
		if (gui) {
			guiWindow = new GuiWindow(this)
		}
		faultCount = 0
		activeThreads = 0
		resetMemory = false
		timeExecution()
	}
	
	void addThreadMap(def number, def path)
	{
		threadMap[number] = path
	}
	
	void setFirstThread(def number)
	{
		firstThread = number
	}
	
	synchronized void addActiveThread()
	{
		activeThreads++
	}
	
	synchronized void removeActiveThread() 
	{
		activeThreads--
	}
	
	synchronized void tickOver()
	{
		timeElapsed += synchCount
		//reset the memory here to avoid race conditions - 
		//all threads have stopped
		if (resetMemory) {
			fixMemory()
			resetMemory = false
		}
		handlers.each {
			if (it.waitOne)
				it.waitOne.release(1)
		}
	}
	
	synchronized void signalTick()
	{
		if (++signalledThreads >= activeThreads) {
			signalledThreads = 0
			tickOver()
		}
	}
	
	synchronized void cutMemory()
	{
		resetMemory = true
		signalTick()
	}
	
	synchronized void fixMemory()
	{
		for (i in 1 .. PROCESSORS)
			processors[i - 1].activeThread = -1
		def maxProcessors = activeThreads
		if (maxProcessors > PROCESSORS)
			maxProcessors = PROCESSORS
		for (i in 1 .. maxProcessors)
		{
			processors[i - 1].lessMemory(maxProcessors)
		}
		if (activeThreads < PROCESSORS) {
			for (i in activeThreads + 1 .. PROCESSORS)
			{
				processors[i - 1].zeroMemory()
			}
		} 
	}
	
	synchronized void closeoutThread()
	{
		signalTick()
		removeActiveThread()
		resetMemory = true
	}
	
	synchronized def handleFirstThread(def threadStr, def procs){
		def firstThread = Thread.start {
		def threadNo = Integer.parseInt(threadStr, 16)
		def threadHandler = new FirstThreadHandler(procs, threadNo, this)
		def threadIn =
			SAXParserFactory.newInstance().newSAXParser().XMLReader
		threadIn.setContentHandler(threadHandler)
		handlers << threadHandler
		threadIn.parse(
			new InputSource(new FileInputStream(threadMap[threadStr]))
			)
		}
	}
		
	void beginExecution()
	{
		signalledThreads = 0
		handleFirstThread(firstThread, processors)	
	}
	
	synchronized void incrementFaultCount()
	{
		faultCount++
	}
	
	synchronized def getFaultCount()
	{
		def oldFaultCount = faultCount
		faultCount = 0
		return oldFaultCount
	}
	
	def timeExecution()
	{
		//map the threads
		def controlHandler = new ControlFileHandler()
		controlHandler.setCallbackClass(this)
		def controlIn = 
			SAXParserFactory.newInstance().newSAXParser().XMLReader
		controlIn.setContentHandler(controlHandler)
		controlIn.parse(new InputSource(new FileInputStream(controlFile)))
		beginExecution()
		return timeElapsed
	}
}
