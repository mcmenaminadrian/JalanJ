/**
 * 
 */
package JalanJ

/**
 * @author adrian
 *
 */
class ProcessorState {
	
	def countdownTimer
	def waitState
	def activeThread
	long instructionCount
	def startProcessor
	long startProcessorInstructionCount
	
	ProcessorState() {
		countdownTimer = 0
		waitState = false
		activeThread = -1
		instructionCount = 0L
	}
	
	void setThread(def thread)
	{
		activeThread = thread
	}

}
