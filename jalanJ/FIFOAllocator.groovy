/**
 * 
 */
package jalanJ

/**
 * @author adrian
 * Crude stack based memory allocator
 */
class FIFOAllocator implements PagingAllocator {

	def topOfStack
	def memorySize
	def PAGESHIFT
	def memoryStack = [:]
	
	FIFOAllocator(def pageOff, def memSize)
	{
		setPageOffset(pageOff)
		setMaxMemory(memSize)
		topOfStack = -1
	}
	
	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#havePage(long)
	 */
	@Override
	public boolean havePage(long address) {
		if (topOfStack < 0)
			return false
		if (memoryStack[address >> PAGESHIFT])
			return true
		else
			return false
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address) {
		if (havePage(address))
			return true
		topOfStack++
		memoryStack[address >> PAGESHIFT] = topOfStack
		if (memoryStack.size() > memorySize >> PAGESHIFT)
		{
			memoryStack.remove(memoryStack.min{it.value})
		}
		return true
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
