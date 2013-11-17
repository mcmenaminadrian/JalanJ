/**
 * 
 */
package JalanJ

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.*
import java.util.concurrent.*


/**
 * @author adrian mcmenamin
 *
 */
class JalanParse {
	
	JalanParse(def threads, def execute, def fileName)
	{
		if (threads) {
			println "Counting threads in $fileName"
			def threadHandler = new ThreadCounter()
			def readIn =
				SAXParserFactory.newInstance().newSAXParser().XMLReader
			readIn.setContentHandler(threadHandler)
			readIn.parse(new InputSource(new FileInputStream(fileName)))
		}
		if (execute) {
			println "Timing execution"
			def threadMapper = new MappingHandler()
			def mapIn = SAXParserFactory.newInstance().newSAXParser().XMLReader
			mapIn.setContentHandler(threadMapper)
			mapIn.parse(new InputSource(new FileInputStream(fileName)))
		}
	}
}

def cliJalan = new CliBuilder
	(usage: 'Jalan [options] XMLFile')
cliJalan.t(longOpt: 'threads', 'Count threads')
cliJalan.x(longOpt: 'execution', 'Time execution model')

def jArgs = cliJalan.parse(args)

if (jArgs.u || args.size()==0) {
	cliJalan.usage()
} else {
	def threads = false
	if (jArgs.t) {
		threads = true
	}
	def execute = false
	if (jArgs.x) {
		execute = true
	}
	
	new JalanParse(threads, execute, args[args.size() - 1])
	
}