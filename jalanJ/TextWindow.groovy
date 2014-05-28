/**
 * 
 */
package jalanJ

import groovy.swing.SwingBuilder
import java.util.timer.*

/**
 * @author adrian
 *
 */

class TextWindow {

	def controlObject
	def tickCounter
	def faultCounter
	FileWriter writer
	FileWriter writerIC
	long previousCount
	Closure countUp
	Closure firstCount
	def COUNT = 1000000
	
	TextWindow(def controller)
	{
		previousCount = 0
		controlObject = controller

		def datum = new Date()
		writer = new FileWriter("FAULTS${datum.time.toString()}.txt")
		writerIC = new FileWriter("IC${datum.time.toString()}.txt")
		writer.write("Count, Rate")
		writerIC.write("Count")
		for (i in 1..18) {
			writer.write(", Thread${i}")
			writerIC.write(", Thread${i}")
		}
		writer.write("\n")
		writerIC.write("\n")
		writer.flush()
		writerIC.flush()
		
		countUp = {
			while (true) {
				Thread.sleep 7000
				long newCount = controlObject.timeElapsed
				if (newCount - previousCount > COUNT) {
					def handlerFR = []
					def handlerIC = []
					//collect the data
					Long faultCount = controlObject.getFaultCount()
					for (i in 1 .. controlObject.handlers.size())
					{
						handlerFR <<
							controlObject.handlers[i - 1].getPerThreadFaults()
						handlerIC <<
							controlObject.handlers[i - 1].getInstructionCount()
					}
					//process the data
					def normalizer = (COUNT/(newCount - previousCount))
					Integer faultRate = faultCount * normalizer
					//output data
					writer.write("${newCount}, ${faultRate}")
					writerIC.write("${newCount}")
					for (i in 1..controlObject.handlers.size())
					{
						Integer normalizedPerThreadFR =
							handlerFR[i - 1] * normalizer
						writer.write(", $normalizedPerThreadFR")
						writerIC.write(", ${handlerIC[i - 1]}")
					}
					writer.write("\n")
					writerIC.write("\n")
					writer.flush()
					writerIC.flush()
					previousCount = newCount
				}
			}
		}
		
		Thread.start(countUp)
	}
}
