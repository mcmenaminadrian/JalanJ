package JalanJ

class PosixThreadTID {
	
	def number
	long instructionCount
	def threadStarter
	long threadStarterInstruction
	
	PosixThreadTID(def numb, def previousThread, long previousThreadIC)
	{
		number = numb
		instructionCount = 0L
		threadStarter = previousThread
		threadStarterInstruction = previousThreadIC
	}
	
	PosixThreadTID(def numb, PosixThreadTID oldThread)
	{
		number = numb
		instructionCount = 0L
		threadStarter = oldThread.number
		threadStarterInstruction = oldThread.instructionCount
	}
	
	long next()
	{
		return ++instructionCount
	}
}
