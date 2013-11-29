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
	
	JalanParse(def threads, def execute, def baseName, def control, def gui,
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
			if (!control) { 
				def threadMapper = new MappingHandler(baseName)
				def mapIn =
					SAXParserFactory.newInstance().newSAXParser().XMLReader
				mapIn.setContentHandler(threadMapper)
				mapIn.parse(new InputSource(new FileInputStream(fileName)))
			} else {
				println "Using control file ${fileName}"
				def executioner = new ExecutionTimer(fileName, gui)
			}
		}
	}
}



def cliJalan = new CliBuilder
	(usage: 'Jalan [options] XMLFile\nJalan [options]c ControlFile')
cliJalan.t(longOpt: 'threads', 'Count threads')
cliJalan.x(longOpt: 'execution', 'Time execution model')
cliJalan.b(longOpt: 'basename', args:1, argName:'filename',
	optionalArg:true, 'Name for thread records')
cliJalan.c(longOpt: 'control', 'Use control file (supercedes \'b\' option)')
cliJalan.g(longOpt: 'gui', "Display GUI output (default is text only)")

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
	def control = false
	def gui = false
	
	if (jArgs.g) {
		gui = true
	}
	
	if (jArgs.x) {
		execute = true
	}
	if (jArgs.c) {
		control = true
		execute = true
	}
	else if (jArgs.b) {
		execute = true
		basename = jArgs.b
	}
	
	new JalanParse(threads, execute, basename, control, gui,
		args[args.size() - 1])
	
}