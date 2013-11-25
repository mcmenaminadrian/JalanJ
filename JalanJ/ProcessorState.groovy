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
		long pageNumber = address >> PAGESHIFT
		if (localMemory[pageNumber]) {
			localMemory[pageNumber] = 1
			return true
		} else {
			return false
		}
	}
	
	synchronized void clockTick()
	{
		localMemory.each { key, value ->
			if (value > 0) {
				localMemory[key] = value - 1
			}
		}
	}
	
	synchronized def allocateToFree(long address)
	{
		if (localMemory.size() * pageSize < MAXSIZE) {
			localMemory[address >> PAGESHIFT] = 1
			return true
		}
		return false
	}
	
	synchronized void dumpPage()
	{
		//find minimum value and remove first that matches
		def minReference = localMemory.min{it.value}
		localMemory.remove(minReference.key)
	}
	
	synchronized void addPage(long address)
	{
		def pageSize = 1 << PAGESHIFT
		if (localMemory.size() * pageSize >= MAXSIZE) {
			dumpPage()
		}
		if (!gotPage(address)) {
			localMemory[address >> PAGESHIFT] = 1
		}
	}
	
	synchronized def matchThread(def threadNo)
	{
		return (threadNo == activeThread)
	}
	
	synchronized def assignThread(def threadNo)
	{
		if (activeThread == -1) {
			activeThread = threadNo
			return true
		}
		return false
	}
}
