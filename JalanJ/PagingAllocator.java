package jalanJ;

public interface PagingAllocator {

	boolean havePage(long address);
	void allocatePage(long address);
	
}
