package jalanJ

class LocalMemoryAllocator implements PagingAllocator {
	
	def memoryPool = [:]
	def TIMEOUT = 2048000
	def PAGESHIFT
	def maxMem
	
	LocalMemoryAllocator(def pageShift, def poolSize)
	{
		setPageOffset(pageShift)
		setMaxMemory(poolSize * 16)
	}

	synchronized setNewMax(long bytes)
	{
		def oldMax = maxMem
		setMaxMemory(bytes)
		//brutally dump pages if needed
		if (maxMem < oldMax) {
			//sort and dump
			def excessPages = memoryPool.size() - maxMem
			if (excessPages > 0) {
				def sortedPool = memoryPool.sort{a, b -> a.value <=> b.value}
				def removals = sortedPool.take((int)excessPages)
				memoryPool = memoryPool - removals
			}
		}
	}
	
	synchronized void zeroMemory()
	{
		maxMem = 0
		memoryPool=[:]
	}
	
	@Override
	public boolean havePage(long address, Object debug) {
		if (maxMem == 0)
			return false
		if (memoryPool[address >> PAGESHIFT])
			return true
		return false
	}

	@Override
	synchronized public boolean allocatePage(long address, Object debug) {
		if (memoryPool.size() >= maxMem) {
			def purgePages = [:]
			memoryPool.each{key, value ->
				if (debug - value > TIMEOUT)
					purgePages[key] = value
			}
			if (purgePages.size() == 0) {
				if (!memoryPool){
					println "HELP!"
				}
				memoryPool.remove(memoryPool.min{it.value}.key)
			}
			else
				memoryPool = memoryPool - purgePages
		}
		memoryPool[address >> PAGESHIFT] = debug
		return false
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
