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
		if (LRUQueue[address >> PAGESHIFT]) {
			LRUQueue[address >> PAGESHIFT] = new Date()
			return true
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	public boolean allocatePage(long address) {
		LRUQueue[address >> PAGESHIFT] = new Date()
		if (LRUQueue.size() > memorySize >> PAGESHIFT)
			LRUQueue.remove(LRUQueue.min{it.value}.key)
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
