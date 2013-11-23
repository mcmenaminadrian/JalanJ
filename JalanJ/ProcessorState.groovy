/**
 * 
 */
package JalanJ

/**
 * @author adrian
 *
 */
class ProcessorState {
	
	def countdownTimer
	def waitState
	def activeThread
	long instructionCount
	def startProcessor
	long startProcessorInstructionCount
	def localMemory =[:]
	def MAXSIZE = 32 * 1024
	def PAGESHIFT = 4
	def PAGEMASK = PAGESHIFT - 1
	
	ProcessorState() {
		countdownTimer = 0
		waitState = false
		activeThread = -1
		instructionCount = 0L
	}
	
	void setThread(def thread)
	{
		activeThread = thread
	}
	
	def gotPage(long address)
	{
		long pageNnumber = address >> PAGESHIFT
		if (localMemory[pageNumber]) {
			localMemory[pageNumber] = 1
			return true
		} else {
			return false
		}
	}
	
	def clockTick()
	{
		localMemory.each { key, value ->
			if (value > 0) {
				localMemory[key] = value - 1
			}
		}
	}
	
	def dumpPage()
	{
		//find minimum value and remove first that matches
		def minReference = localMemory.min{it.value}
		localMemory.remove(minReference.key)
	}
	
	def addPage(long address)
	{
		def pageSize = 1 << PAGESHIFT
		if (localMemory.size() * pageSize == MAXSIZE) {
			dumpPage()
		}
		if (!gotPage(address)) {
			localMemory[address >> PAGESHIFT] = 1
		}
	}

}
