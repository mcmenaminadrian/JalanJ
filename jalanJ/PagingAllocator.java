package jalanJ;

public interface PagingAllocator {

	boolean havePage(long address);
	boolean allocatePage(long address);
	void setPageOffset(int bits);
	void setMaxMemory(long bytes);
	
}
