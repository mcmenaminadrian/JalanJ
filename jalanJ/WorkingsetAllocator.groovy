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
	def oldTimeStamp = 0
	
	WorkingsetAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
		//assume 16 processors of 32k here
		MAXTICKS = 2048000
	}
	
	boolean cleanPageSet(def timeStamp)
	{
		if (oldTimeStamp == timeStamp)
			return false
		def outOfDatePages = [:]
		pageSet.each { key, value ->
			if (timeStamp - value > MAXTICKS)
				outOfDatePages[key] = value
		}
		if (outOfDatePages.size() == 0)
			return false
		pageSet = pageSet - outOfDatePages
		oldTimeStamp = timeStamp
		return true
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long, java.lang.Object)
	 */
	@Override
	synchronized public boolean havePage(long address, def debug) {
		def page = address >> PAGESHIFT
		if (pageSet[page]) {
			pageSet[page] = debug
			return true
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long, java.lang.Object)
	 */
	@Override
	synchronized public boolean allocatePage(long address, def debug) {
		if (pageSet.size() == MAXSIZE) {
			if (!cleanPageSet(debug))
				pageSet.remove((pageSet.min{it.value}).key)
		}
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
