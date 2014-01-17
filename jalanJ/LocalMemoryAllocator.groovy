package jalanJ

class LocalMemoryAllocator implements PagingAllocator {
	
	def memoryPool = [:]
	def TIMEOUT = 2048000
	def PAGESHIFT
	def maxMem
	
	LocalMemoryAllocator(def pageShift, def poolSize)
	{
		setPageOffset(pageShift)
		setMaxMemory(poolSize)
	}

	@Override
	public boolean havePage(long address, Object debug) {
		if (memoryPool[address >> PAGESHIFT])
			return true
		return false;
	}

	@Override
	synchronized public boolean allocatePage(long address, Object debug) {
		if (memoryPool.size() >= maxMem) {
			def purgePages = [:]
			memoryPool.each{key, value ->
				if (debug - value > TIMEOUT)
					prugePages[key] = value
			}
			if (purgePages.size() == 0) {
				memoryPool = memoryPool.remove(memoryPool.min{it.value})
			}
			else
				memoryPool = memoryPool - purgePages
		}
		memoryPool[address >> PAGESHIFT] = debug
		return false;
	}

	@Override
	public void setPageOffset(int bits) {
		PAGESHIFT = bits
	}

	@Override
	public void setMaxMemory(long bytes) {
		maxMem = bytes >> PAGESHIFT
	}

}
