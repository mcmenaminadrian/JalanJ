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
	
	synchronized void setThread(def thread)
	{
		activeThread = thread
	}
	
	synchronized def gotPage(long address)
	{
		long pageNnumber = address >> PAGESHIFT
		if (localMemory[pageNumber]) {
			localMemory[pageNumber] = 1
			return true
		} else {
			return false
		}
	}
	
	synchronized def clockTick()
	{
		localMemory.each { key, value ->
			if (value > 0) {
				localMemory[key] = value - 1
			}
		}
	}
	
	synchronized def dumpPage()
	{
		//find minimum value and remove first that matches
		def minReference = localMemory.min{it.value}
		localMemory.remove(minReference.key)
	}
	
	synchronized def addPage(long address)
	{
		def pageSize = 1 << PAGESHIFT
		if (localMemory.size() * pageSize == MAXSIZE) {
			dumpPage()
		}
		if (!gotPage(address)) {
			localMemory[address >> PAGESHIFT] = 1
		}
	}
	
	synchronized def matchThread(def threadNo)
	{
		return (threadNo = activeThread)
	}
}
