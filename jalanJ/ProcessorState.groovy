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
	def MAXSIZE
	def PAGESHIFT
	def PAGESIZE
	
	ProcessorState(def memoryModel, def pageOffset, def maxSize) {
		waitState = false
		activeThread = -1
		PAGESHIFT = pageOffset
		PAGESIZE = 1 << PAGESHIFT
		MAXSIZE = maxSize * 1024
		if (!localMemory) {
			switch (memoryModel) {
				case "q":
				localMemory = new FIFOAllocator(PAGESHIFT, MAXSIZE * 16)
				break
				case "l":
				localMemory = new SimpleLRUAllocator(PAGESHIFT, MAXSIZE * 16)
				break
				case "k":
				localMemory = new LRU2Allocator(PAGESHIFT, MAXSIZE * 16)
				break
				case "a":
				localMemory = new AdaptiveLRUAllocator(PAGESHIFT, MAXSIZE * 16)
				break
			}
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
