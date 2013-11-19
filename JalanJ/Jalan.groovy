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
	
	JalanParse(def threads, def execute, def baseName, def fileName)
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
			def threadMapper = new MappingHandler(baseName)
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
cliJalan.b(longOpt: 'basename', args:1, argName:'filename',
	optionalArg:true, 'Name for thread records')

def jArgs = cliJalan.parse(args)

if (jArgs.u || args.size()==0) {
	cliJalan.usage()
} else {
	def threads = false
	if (jArgs.t) {
		threads = true
	}
	def execute = false
	def basename = "${new Date().time.toString()}"
	
	if (jArgs.x) {
		execute = true
	}
	
	if (jArgs.b) {
		execute = true
		basename = jArgs.b
	}
	
	new JalanParse(threads, execute, basename, args[args.size() - 1])
	
}