/**
 * 
 */
package JalanJ

/**
 * @author adrian
 *
 */
class DefaultMemoryModel {

	def localSize = 16 * 24 * 1024 //local memory
	def pageShift = 7 //256 byte pages
	def workingSet = 100 //instruction count per processor
	def faultHit = 1000 //cycles lost to service page fault
	def readTime = 4 // cycles lost to local read
	def writeTime = 8 // cycles lost to local write
	
	
}
