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
	long previousCount
	Closure countUp
	Closure firstCount
	
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
		writer = new FileWriter("DATA${new Date().time.toString()}.txt")
		writer.write("Count, Rate")
		for (i in 1..18)
			writer.write(", Thread${i}")
		writer.write("\n")
		writer.flush()
		frame.pack()
		frame.show()
		
		
		countUp = {
			long newCount = controlObject.timeElapsed
			if (newCount - previousCount > 1000000) {
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
				def normalizer = (1000000/(newCount - previousCount))
				Integer faultRate = faultCount * normalizer
				//output data
				writer.write("${newCount}, ${faultRate}")
				for (i in 1..controlObject.handlers.size())
				{
					Integer normalizedPerThreadFR =
						handlerFR[i - 1] * normalizer
					writer.write(", $normalizedPerThreadFR")
					print "$i ---> ${handlerIC[i - 1]} -->"
					print "$normalizedPerThreadFR --> ${handlerFR[i - 1]}, "
				}
				print "\n"
				writer.write("\n")
				writer.flush()
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
