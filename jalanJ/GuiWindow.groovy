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
			if (newCount - previousCount > 100000) {
				Long faultCount = controlObject.getFaultCount()
				def normalizer = (1000000/(newCount - previousCount))
				Integer faultRate = faultCount * normalizer
				previousCount = newCount
				tickCounter.text = newCount
				faultCounter.text = faultRate
				tickCounter.repaint()
				faultCounter.repaint()

				writer.write("${newCount}, ${faultRate}")
				for (i in 1..controlObject.handlers.size())
				{
					Integer normalizedPerThreadFR = controlObject.
						handlers[i - 1].getPerThreadFaults() * normalizer
					writer.write(", $normalizedPerThreadFR")
				}
				writer.write("\n")
				writer.flush()
			}
			new Timer().runAfter(3000, countUp)
		}
		
		firstCount = {	
			new Timer().runAfter(3000, countUp)
		}
		
		firstCount()
		
	}
}
