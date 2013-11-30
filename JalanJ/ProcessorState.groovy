/**
 * 
 */
package jalanJ

import java.util.TreeMap.PrivateEntryIterator;

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
	def PAGESIZE = 1 << PAGESHIFT
	def everyOther
	
	ProcessorState() {
		countdownTimer = 0
		waitState = false
		activeThread = -1
		instructionCount = 0L
		everyOther = 0
	}
	
	
	synchronized void setThread(def thread)
	{
		activeThread = thread
	}
	
	synchronized def gotPage(long address)
	{
		long pageNumber = address >> PAGESHIFT
		if (localMemory[pageNumber]) {
			if (localMemory[pageNumber] < 2)
				localMemory[pageNumber] += 1
			return true
		} else
			return false
	}
	
	synchronized void clockTick()
	{
		if (everyOther) {
			localMemory.each { key, value ->
				if (value > 0)
					localMemory[key] -= 1	
			}
			everyOther = 0
		} else
			everyOther = 1
	}
	
	synchronized def allocateToFree(long address)
	{
		if (localMemory.size() * PAGESIZE < MAXSIZE) {
			long pageNumber = address >> PAGESHIFT
			localMemory[pageNumber] = 1
			return true
		}
		if (everyOther)
			dumpPage()
		return false
	}
	
	private void dumpPage()
	{
		//find minimum value and remove first that matches
		def minReference = localMemory.min{it.value}
		localMemory.remove(minReference.key)
	}
	
	synchronized void addPage(long address)
	{
		if (localMemory.size() * PAGESIZE >= MAXSIZE) {
			dumpPage()
		}
		long pageNumber = address >> PAGESHIFT
		localMemory[pageNumber] = 1
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
