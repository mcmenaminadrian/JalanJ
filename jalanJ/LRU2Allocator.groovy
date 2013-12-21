/**
 * 
 */
package jalanJ

/**
 * @author adrian
 *
 */
class LRU2Allocator implements PagingAllocator {

	def lowPriority = [:]
	def highPriority = [:]
	def memorySize
	def highSize
	def lowSize
	def totalPages
	def PAGESHIFT
	
	
	LRU2Allocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
	}
	
	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long)
	 */
	@Override
	public boolean havePage(long address) {
		if (lowPriority[address >> PAGESHIFT]) {
			allocatePage(address >> PAGESHIFT)
			return true
		} else if (highPriority[address >> PAGESHIFT]) {
			highPriority[address >> PAGESHIFT] = new Date()
			return true
		}
		else return false
	}

	void reallocatePage(def page)
	{
		lowPriority.remove(page)
		highPriority[page] = new Date()
		if (highPriority.size() > highSize)
		{
			def oldPage = highPriority.min{it.value}
			def oldDate = highPriority.remove(oldPage.key)
			lowPriority[oldPage] = oldDate
		}
	}
	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address) {
		def page = address >> PAGESHIFT
		if (lowPriority[page]) {
			reallocatePage(page)
		} else if (highPriority[page]) {
			println "have ${highPriority.size()} pages now"
			highPriority[page] = new Date()
			return true
		} else
			lowPriority[page] = new Date()
		if (lowPriority.size() > lowSize) {
			lowPriority.remove((lowPriority.min{it.value}).key)
		}
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
		//low takes 75%, high 25%
		totalPages = bytes/(1 << PAGESHIFT)
		highSize = (totalPages * 0.25) as Integer
		lowSize = totalPages - highSize
		println "Total pages: $totalPages High pages: $highSize  Low pages: $lowSize"
	}

}
