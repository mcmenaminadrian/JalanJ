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
				for (i in 0 .. controlObject.PROCESSORS) {
					textField(id:'processor_${i}', columns:3, text:"$i",
						editable: false){}
				}
			}
		}
		frame.pack()
		frame.show()
		
		
		countUp = {
			long newCount = controlObject.timeElapsed
			Integer faultRate = controlObject.getFaultCount() * 
					(1000000/(newCount - previousCount))
			previousCount = newCount
			tickCounter.text = newCount
			faultCounter.text = faultRate
			tickCounter.repaint()
			faultCounter.repaint()
			new Timer().runAfter(5000, countUp)
		}
		
		firstCount = {
			new Timer().runAfter(5000, countUp)
		}
		
		firstCount()
		
	}
}
