/**
 * 
 */
package jalanJ

/**
 * @author adrian
 *
 */
class AdaptiveLRUAllocator implements PagingAllocator {
	
	def totalPages
	def lowSize
	def highSize
	def PAGESHIFT
	def memorySize
	def lowPriority = [:]
	def highPriority = [:]
	def TICKLIMIT = 512000
	
	
	AdaptiveLRUAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long)
	 */
	@Override
	synchronized public boolean havePage(long address, def debug) {
		def page = address >> PAGESHIFT
		if (lowPriority[page]) {
			lowPriority.remove(page)
			highPriority[page] = debug
			return true
		} else if (highPriority[page]) {
			highPriority[page] = debug
			return true
		}
		return false;
	}
	
	// anything older than certain number of ticks gets pushed down
	void rotateHigh(def ticks)
	{
		if (ticks < TICKLIMIT)
			return
		def removals = [:]
		highPriority.each{key, value ->
			if (ticks - value > TICKLIMIT) {
				removals[key] = value
				lowPriority[key] = value
			}
		}
		highPriority = highPriority - removals
	}
	
	//have to push away more 
	void flushHigh(def num)
	{
		def sortedPriority = highPriority.sort{a, b -> a.value <=> b.value}
		def removals = sortedPriority.take(num)
		highPriority = highPriority - removals
		lowPriority = lowPriority + removals
	}
	
	void flushLow()
	{
		def toGo = lowPriority.min{it.value}
		lowPriority.remove(toGo.key)
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address, def debug) {
		rotateHigh(debug)
		if (lowPriority.size() == 0) {
			if (highPriority.size() > highSize) {
				flushHigh(highPriority.size() - highSize)
			}
		}
		if (lowPriority.size() + highPriority.size() == totalPages) {
			flushLow()
		}
		lowPriority[address >> PAGESHIFT] = debug
		return true;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#setPageOffset(int)
	 */
	@Override
	public void setPageOffset(int bits) {
		PAGESHIFT = bits
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#setMaxMemory(long)
	 */
	@Override
	public void setMaxMemory(long bytes) {
			memorySize = bytes
		//low guide is 30%, high 70%
		totalPages = bytes/(1 << PAGESHIFT)
		highSize = (totalPages * 0.7) as Integer
		lowSize = totalPages - highSize
		println (
		"Total pages: $totalPages High pages: $highSize  Low pages: $lowSize"
		)
	}

}
