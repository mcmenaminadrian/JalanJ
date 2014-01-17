/**
 * 
 */
package jalanJ

/**
 * @author adrian
 *
 */
class ThreeQAllocator implements PagingAllocator {
	
	def totalPages
	def lowSize
	def middleSize
	def highSize
	def PAGESHIFT
	def memorySize
	def lowPriority = [:]
	def middlePriority = [:]
	def highPriority = [:]
	def LOWLIMIT = 327680
	def MIDDLELIMIT = 655350
	def HIGHLIMIT = 2560000
	
	
	ThreeQAllocator(def pageOff, def memSize)
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
		if (highPriority[page]) {
			highPriority[page] = debug
			return true
		}
		if (middlePriority[page])
		{
			middlePriority.remove(page)
			highPriority[page] = debug
			return true
		}
		if (lowPriority[page]) {
			lowPriority.remove(page)
			middlePriority[page] = debug
			return true
		}
		return false;
	}
	
	// anything older than certain number of ticks gets pushed down
	void flushOldPages(def ticks)
	{
		def highRemovals = [:]
		def middleRemovals = [:]
		def lowRemovals = [:]
		highPriority.each{key, value ->
			if (ticks - value > HIGHLIMIT) {
				highRemovals[key] = value
				
			}
		}
		highPriority = highPriority - highRemovals
		lowPriority.each{key, value ->
			if (ticks - value > LOWLIMIT) {
				lowRemovals[key] = value
			}
		}
		lowPriority = lowPriority - lowRemovals
		middlePriority.each{key, value ->
			if (ticks - value > MIDDLELIMIT) {
				middleRemovals[key] = value
			}
		}
		middlePriority = middlePriority - middleRemovals

	}
	
	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address, def debug) {
		flushOldPages(debug)
		if (lowPriority.size() + middlePriority.size() +
				highPriority.size() >= totalPages) {
				if (highPriority.size())
					highPriority.remove((highPriority.min{it.value}).key)
				else if (middlePriority.size())
					middlePriority.remove((middlePriority.min{it.value}).key)
				else
					lowPriority.remove(lowPriority.min{it.value})
		}
		lowPriority[address >> PAGESHIFT] = debug
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
		totalPages = bytes/(1 << PAGESHIFT)
		highSize = (totalPages * 0.4) as Integer
		middleSize = (totalPages * 0.3) as Integer
		lowSize = totalPages - (highSize + middleSize)
		println (
		"Pages: $totalPages High: $highSize Middle: $middleSize  Low: $lowSize"
		)
	}

}
