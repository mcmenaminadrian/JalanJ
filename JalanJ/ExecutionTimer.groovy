/**
 * 
 */
package JalanJ

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
	def threadMap = [:]
	def PROCESSORS = 16
	def firstThread
	def activeThreads
	def signalledThreads
	def guiOutput
	def guiWindow
	
	ExecutionTimer(def fileStr, def gui)
	{
		guiOutput = gui
		controlFile = fileStr
		timeElapsed = 0
		for (i in 1 .. PROCESSORS)
		{
			processors << new ProcessorState()
		}
		if (gui) {
			guiWindow = new GuiWindow(this)
		}
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
	
	void tickOver()
	{
		processors.each {
			it.clockTick()
		}
		
		handlers.each {
			if (it.waitOne) {
				it.waitOne.release(1)
			}
		}
	}

	
	synchronized def handleThread(def threadStr, def procs){
		Thread.start {
		def threadNo = Integer.parseInt(threadStr, 16)
		def threadHandler = new ThreadHandler(procs, threadNo, this)
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
		if (++signalledThreads == activeThreads) {
			signalledThreads = 0
			timeElapsed++
			tickOver()
		}
	}
		
	void beginExecution()
	{
		activeThreads = 1
		signalledThreads = 0
		//this is the first thread, so we just allocate a processor
		processors[0].activeThread = firstThread.toInteger()
		handleThread(firstThread, processors)
	
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
