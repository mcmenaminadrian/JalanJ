package jalanJ;

public interface PagingAllocator {

	boolean havePage(long address, def debug);
	boolean allocatePage(long address, def debug);
	void setPageOffset(int bits);
	void setMaxMemory(long bytes);
	
}
