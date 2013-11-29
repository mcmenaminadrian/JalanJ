/**
 * 
 */
package JalanJ

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
	Closure countUp
	
	GuiWindow(def controller)
	{
		super()
		controlObject = controller
		frame = this.frame(title:'JalanJ') {
			panel {
				tickCounter = textField(columns:15, text:'0',
					editable: false){}
				textField(id:'activeThreads', columns:3, text: '0',
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
			tickCounter.text = controlObject.timeElapsed
			tickCounter.repaint()
			new Timer().runAfter(500, countUp)
		}
		
		countUp()
		
	}
}
