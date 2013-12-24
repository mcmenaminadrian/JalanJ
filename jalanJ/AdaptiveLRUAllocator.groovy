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
	
	
	AdaptiveLRUAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long)
	 */
	@Override
	synchronized public boolean havePage(long address) {
		def page = address >> PAGESHIFT
		if (lowPriority[page]) {
			lowPriority.remove(page)
			highPriority[page] = new Date()
			return true
		} else if (highPriority[page]) {
			highPriority[page] = new Date()
			return true
		}
		return false;
	}
	
	// anything older than a second gets pushed to low queue
	void rotateHigh()
	{
		def removals = [:]
		highPriority.each{key, value ->
			def now = (new Date().toTimestamp()).getTime()
			if (now - (value.toTimestamp()).getTime() > 999) {
				removals[key] = value
				lowPriority[key] = value
			}
		}
		highPriority = highPriority - removals
	}
	
	//have to push away more 
	void flushHigh(def num)
	{
		highPriority.sort{it.value}
		def removals = highPriority.take(num)
		highPriority = highPriority - removals
		lowPriority = lowPriority + removals
	}
	
	void flushLow()
	{
		lowPriority.remove(lowPriority.min{it.value}.key)
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address) {
		rotateHigh()
		if (highPriority.size() > highSize) {
			flushHigh(highSize - highPriority.size())
		}
		if (lowPriority.size() + highSize >= totalPages) {
			flushLow()
		}
		lowPriority[address >> PAGESHIFT] = new Date()
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
		//low guide is 33%, high 67%
		totalPages = bytes/(1 << PAGESHIFT)
		highSize = (totalPages * 0.4) as Integer
		lowSize = totalPages - highSize
		println (
		"Total pages: $totalPages High pages: $highSize  Low pages: $lowSize"
		)
	}

}
