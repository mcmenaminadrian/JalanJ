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

	
	def handleThread = Thread.start {
		def threadHandler = new ThreadHandler()
		def threadIn =
			SAXParserFactory.newInstance().newSAXParser().XMLReader
		threadIn.setContentHandler(threadHandler)
		parsers << threadIn
		threadIn.parse(
			new InputSource(new FileInputStream(threadMap[it]))
			)
	}
		
	void beginExecution()
	{
		activeThreads = 1
		
		handleThread(firstThread)
	
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
