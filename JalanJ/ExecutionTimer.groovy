/**
 * 
 */
package JalanJ

import org.xml.sax.Attributes;
import org.xml.sax.*

/**
 * @author adrian
 *
 */



class ExecutionTimer {
	
	def controlFile
	long timeElapsed
	def parsers = []
	def processors = []
	def threadMap = [:]
	def PROCESSORS = 16
	def firstThread
	def activeThreads
	def signalledThreads
	
	ExecutionTimer(def fileStr)
	{
		controlFile = fileStr
		timeElapsed = 0
		(1 .. PROCESSORS)
		{
			processors << new ProcessorState()
		}
	}
	
	void addThreadMap(def number, def path)
	{
		threadMap.put(number, path)
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
		
		parsers.each {
			if (it.waitOne) {
				it.waitOne.release(1)
			}
		}
	}

	
	def handleThread = Thread.start {threadStr, processorList ->
		def threadNo = Integer.parseInt(threadStr, 16)
		def threadHandler = new ThreadHandler(processorList, threadNo, this)
		def threadIn =
			SAXParserFactory.newInstance().newSAXParser().XMLReader
		threadIn.setContentHandler(threadHandler)
		parsers << threadIn
		threadIn.parse(
			new InputSource(new FileInputStream(threadMap[threadStr]))
			)
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
		ProcessorState[0].activeThread = firstThread.toInteger()
		
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
