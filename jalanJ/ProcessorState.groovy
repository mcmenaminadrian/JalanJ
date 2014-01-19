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
	def exeTimer
	
	ProcessorState(def memoryModel, def pageOffset, def maxSize, def master) {
		exeTimer = master
		waitState = false
		activeThread = -1
		PAGESHIFT = pageOffset
		PAGESIZE = 1 << PAGESHIFT
		MAXSIZE = maxSize * 1024
		goingThreads = 1
		switch (memoryModel) {
			case "z":
			localMemory = new LocalMemoryAllocator(PAGESHIFT, MAXSIZE)
			break
			
			default:
			println "ERROR: This is local memory allocator code"
			throw(new Exception("Wrong memory model specified"))
		}
	}
	
	synchronized void moreMemory(def threadCount)
	{
		localMemory.setNewMax(MAXSIZE * (16/threadCount).toInteger())
	}
	
	synchronized void lessMemory(def threadCount)
	{
	
		localMemory.setNewMax(MAXSIZE * (16/threadCount).toInteger())
	}
	
	synchronized void zeroMemory()
	{
		localMemory.zeroMemory()
	}
	
	synchronized void setThread(def thread)
	{
		activeThread = thread
	}
	
	synchronized def gotPage(long address)
	{
		return localMemory.havePage(address, exeTimer.timeElapsed)
	}
	
	synchronized def addPage(long address)
	{
		return localMemory.allocatePage(address, exeTimer.timeElapsed)
	}
	
	def matchThread(def threadNo)
	{
		if (threadNo == activeThread)
			return true
		else return assignThread(threadNo)
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
