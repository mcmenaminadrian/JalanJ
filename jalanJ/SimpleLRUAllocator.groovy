/**
 * 
 */
package jalanJ



/**
 * @author adrian
 *
 */
class SimpleLRUAllocator implements PagingAllocator {

	def LRUQueue = [:]
	def PAGESHIFT
	def memorySize
	
	SimpleLRUAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
	}
	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long)
	 */
	@Override
	public boolean havePage(long address) {
		if (LRUQueue[address >> PAGESHIFT])
			return true
		return false;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	public boolean allocatePage(long address) {
		LRUQueue[address >> PAGESHIFT] = getTime(new Date())
		if (LRUQueue.size() > memorySize)
			LRUQueue.minus(LRUQueue.min{it.value})
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

	}

}
