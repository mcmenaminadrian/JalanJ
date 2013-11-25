/**
 * 
 */
package JalanJ

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.*



/**
 * @author adrian mcmenamin
 *
 */
class JalanParse {
	
	JalanParse(def threads, def execute, def baseName, def controlName,
		def fileName)
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
			if (!controlName) { 
				def threadMapper = new MappingHandler(baseName)
				def mapIn =
					SAXParserFactory.newInstance().newSAXParser().XMLReader
				mapIn.setContentHandler(threadMapper)
				mapIn.parse(new InputSource(new FileInputStream(fileName)))
			} else {
				println "Using control file ${controlName}"
				def executioner = new ExecutionTimer(controlName)
			}
		}
	}
}



def cliJalan = new CliBuilder
	(usage: 'Jalan [options] XMLFile')
cliJalan.t(longOpt: 'threads', 'Count threads')
cliJalan.x(longOpt: 'execution', 'Time execution model')
cliJalan.b(longOpt: 'basename', args:1, argName:'filename',
	optionalArg:true, 'Name for thread records')
cliJalan.c(longOpt: 'controlname', args:1, argName: 'controlname',
	optionalArg: false, 'Name for control flag (supercedes \'b\' option)')

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
	def controlname = ""
	
	if (jArgs.x) {
		execute = true
	}
	if (jArgs.c) {
		execute = true
		controlname = jArgs.c
	}
	else if (jArgs.b) {
		execute = true
		basename = jArgs.b
	}
	
	new JalanParse(threads, execute, basename, controlname,
		args[args.size() - 1])
	
}