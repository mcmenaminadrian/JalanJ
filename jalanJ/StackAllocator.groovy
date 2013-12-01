/**
 * 
 */
package jalanJ

/**
 * @author adrian
 * Crude stack based memory allocator
 */
class StackAllocator implements PagingAllocator {

	def topOfStack
	def memorySize
	def PAGESHIFT
	def memoryStack = [] as Queue
	
	StackAllocator(def pageOff, def memSize)
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
		long pageNumber = address >> PAGESHIFT
		for (i in 0 .. topOfStack) {
			if (pageNumber == memoryStack[i])
				return true
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#allocatePage(long)
	 */
	@Override
	synchronized public boolean allocatePage(long address) {
		if (havePage(address))
			return true
		if (topOfStack + 1 >= memorySize >> PAGESHIFT) {
			memoryStack.poll()
			topOfStack--
		}
		Long pageNumber = address >> PAGESHIFT
		memoryStack[++topOfStack] = pageNumber
		return true
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#setPageOffset(int)
	 */
	@Override
	public void setPageOffset(int bits) {
		// TODO Auto-generated method stub
		PAGESHIFT = bits
	}

	/* (non-Javadoc)
	 * @see jalanJ.PagingAllocator#setMaxMemory(long)
	 */
	@Override
	public void setMaxMemory(long bytes) {
		// TODO Auto-generated method stub
		memorySize = bytes
	}

}
