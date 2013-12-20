/**
 * 
 */
package jalanJ

import javax.xml.parsers.SAXParserFactory
import org.xml.sax.*



/**
 * @author adrian mcmenamin
 *
 */
class JalanParse {
	
	JalanParse(def threads, def execute, def baseName, def control, def gui,
		def memModel, def pageOffset, def maxSize, def fileName)
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
				def threadMapper = new MappingHandler(baseName, gui, memModel)
				def mapIn =
					SAXParserFactory.newInstance().newSAXParser().XMLReader
				mapIn.setContentHandler(threadMapper)
				mapIn.parse(new InputSource(new FileInputStream(fileName)))
			} else {
				println "Using control file ${fileName}"
				println "Memory model: $memModel"
				println "Page size (bytes): ${1 << pageOffset}"
				println "Per processor memory (kB): $maxSize"
				def executioner = new ExecutionTimer(fileName, gui, memModel,
					maxSize, pageOffset)
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
cliJalan.m(longOpt: 'memorymodel', args:1, argName:'type', optionalArg:false,
	'Memory allocation model: q for queue (default), l for LRU')
cliJalan.p(longOpt: 'pageoffset', args:1, argName:'bits', optionalArg:false,
	'Bit offset for page size (default is 12 - 4k page)')
cliJalan.z(longOpt: 'perprocessormem', args:1, argName:'mx', optionalArg:false,
	'Maximum (kB) local memory per processor (default is 32)')

def jArgs = cliJalan.parse(args)
def pageOffset = 12
def maxSize = 32

if (jArgs.p) {
	pageOffset = jArgs.p as Integer
}
if (jArgs.z) {
	maxSize = jArgs.z as Integer
}

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
	def memModel = "q"
	
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
	
	if (jArgs.m) {
		memModel = jArgs.m
	}
	
	new JalanParse(threads, execute, basename, control, gui, memModel,
		pageOffset, maxSize, args[args.size() - 1])
	
}