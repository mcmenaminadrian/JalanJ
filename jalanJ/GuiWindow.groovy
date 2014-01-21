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

class GuiWindow extends SwingBuilder {

	def controlObject
	def frame
	def tickCounter
	def faultCounter
	FileWriter writer
	FileWriter writerIC
	long previousCount
	Closure countUp
	Closure firstCount
	def COUNT = 1000000
	
	GuiWindow(def controller)
	{
		super()
		previousCount = 0
		controlObject = controller
		frame = this.frame(title:'JalanJ') {
			panel {
				tickCounter = textField(columns:15, text:'0',
					editable: false){}
				faultCounter = textField(columns:3, text: '0',
					editable: false){}
			}
		}
		def datum = new Date()
		writer = new FileWriter("FAULTS${datum.time.toString()}.txt")
		writerIC = new FileWriter("IC${datum.time.toString()}.txt")
		writer.write("Count, Faults")
		writerIC.write("Count")
		for (i in 1..18) {
			writer.write(", Thread${i}")
			writerIC.write(", Thread${i}")
		}
		writer.write("\n")
		writerIC.write("\n")
		writer.flush()
		writerIC.flush()
		frame.pack()
		frame.show()
		
		
		countUp = {
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
				writer.write("${newCount}, ${faultCount}")
				writerIC.write("${newCount}")
				for (i in 1..controlObject.handlers.size())
				{
					writer.write(", ${handlerFR[i - 1]}")
					writerIC.write(", ${handlerIC[i - 1]}")
				}
				writer.write("\n")
				writerIC.write("\n")
				writer.flush()
				writerIC.flush()
				previousCount = newCount
				tickCounter.text = newCount
				faultCounter.text = faultRate
				tickCounter.repaint()
				faultCounter.repaint()
			}
			new Timer().runAfter(5000, countUp)
		}
		
		firstCount = {	
			new Timer().runAfter(3000, countUp)
		}
		
		firstCount()
		
	}
}
