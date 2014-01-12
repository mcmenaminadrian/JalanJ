/**
 * 
 */
package jalanJ

/**
 * @author adrian
 *
 */
class WorkingsetAllocator implements PagingAllocator {
	
	def MAXTICKS //working set parameter
	def totalPages
	def PAGESHIFT
	def MAXSIZE
	def memorySize
	def pageSet = [:]
	
	WorkingsetAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
		//assume 16 processors of 32k here
		MAXTICKS = 2048000
	}
	
	void cleanPageSet(def timeStamp)
	{
		def outOfDatePages = [:]
		pageSet.each { key, value ->
			if (timeStamp - value > MAXTICKS)
				outOfDatePages[key] = value
		}
		pageSet = pageSet - outOfDatePages
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long, java.lang.Object)
	 */
	@Override
	synchronized public boolean havePage(long address, def debug) {
		def page = address >> PAGESHIFT
		if (pageSet[page]) {
			pageSet[page] = debug
			cleanPageSet(debug)
			return true
		}
		cleanPageSet(debug)
		return false;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long, java.lang.Object)
	 */
	@Override
	synchronized public boolean allocatePage(long address, def debug) {
		cleanPageSet(debug)
		if (pageSet.size() == MAXSIZE)
			pageSet.remove((pageSet.min{it.value}).key)
		pageSet[address >> PAGESHIFT] = debug
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
		MAXSIZE = memorySize >> PAGESHIFT
	}

}
