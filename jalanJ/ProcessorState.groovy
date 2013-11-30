/**
 * 
 */
package jalanJ

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
	def localMemory
	def MAXSIZE = 32 * 1024
	def PAGESHIFT = 4
	def PAGESIZE = 1 << PAGESHIFT
	def everyOther
	
	ProcessorState() {
		countdownTimer = 0
		waitState = false
		activeThread = -1
		instructionCount = 0L
		everyOther = 0
		localMemory = new StackAllocator(PAGESHIFT, MAXSIZE)
	}
	
	
	synchronized void setThread(def thread)
	{
		activeThread = thread
	}
	
	synchronized def gotPage(long address)
	{
		return localMemory.havePage(address)
	}
	
	synchronized void clockTick()
	{
		
	}
	
	synchronized def addPage(long address)
	{
		return localMemory.allocatePage(address)
	}
	
	def matchThread(def threadNo)
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
