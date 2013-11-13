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

	def threadHandler
	
	JalanParse(def threads, def fileName)
	{
		if (threads) {
			println "Counting threads in $fileName"
			def threadHandler = new ThreadCounter()
			def readIn = SAXParserFactory.newInstance().newSAXParser().XMLReader
			readIn.setContentHandler(threadHandler)
			readIn.parse(new InputSource(new FileInputStream(fileName)))
		}
	}
}

def cliJalan = new CliBuilder
	(usage: 'Jalan [options] XMLFile')
cliJalan.t(longOpt: 'threads', 'Count threads')

def jArgs = cliJalan.parse(args)

if (jArgs.u || args.size()==0) {
	cliJalan.usage()
} else {
	def threads = false
	if (jArgs.t) {
		threads = true
	}
	
	new JalanParse(threads, args[args.size() - 1])
	
}