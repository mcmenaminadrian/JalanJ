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
		handlers.each {
			if (it.waitOne)
				it.waitOne.release(1)
		}
	}
	
	synchronized void closeoutThread()
	{
		signalTick()
		removeActiveThread()
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
	
	synchronized void signalTick()
	{
		if (++signalledThreads >= activeThreads) {
			signalledThreads = 0
			tickOver()
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
