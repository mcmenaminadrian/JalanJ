/**
 * 
 */
package jalanJ

/**
 * @author adrian
 *
 */
class ProcessorState {

	def waitState
	def activeThread
	def startProcessor
	static def localMemory
	def MAXSIZE = 32 * 1024
	def PAGESHIFT = 12
	def PAGESIZE = 1 << PAGESHIFT
	
	ProcessorState(def memoryModel) {
		waitState = false
		activeThread = -1
		if (!localMemory) {
			if (memoryModel == "q")
				localMemory = new QueueAllocator(PAGESHIFT, MAXSIZE * 16)
			else if (memoryModel == "l")
				localMemory = new SimpleLRUAllocator(PAGESHIFT, MAXSIZE * 16)
		}
	}
	
	synchronized void setThread(def thread)
	{
		activeThread = thread
	}
	
	synchronized def gotPage(long address)
	{
		return localMemory.havePage(address)
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
	
	synchronized void deassignThread()
	{
		activeThread = -1
	}
}
