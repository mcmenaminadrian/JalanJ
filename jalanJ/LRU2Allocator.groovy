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
	synchronized public boolean havePage(long address, def debug=null) {
		if (lowPriority[address >> PAGESHIFT] ||
			highPriority[address >> PAGESHIFT]) {
			return allocatePage(address)
		}
		return false
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
	synchronized public boolean allocatePage(long address, def debug=null) {
		def page = address >> PAGESHIFT
		if (lowPriority[page]) {
			reallocatePage(page)
			return true
		} else if (highPriority[page]) {
			highPriority[page] = new Date()
			return true
		}
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
		//low takes 5%, high 95%
		def totalPages = bytes/(1 << PAGESHIFT)
		highSize = (totalPages * 0.95) as Integer
		lowSize = totalPages - highSize
		println (
		"Total pages: $totalPages High pages: $highSize  Low pages: $lowSize")
	}

}
