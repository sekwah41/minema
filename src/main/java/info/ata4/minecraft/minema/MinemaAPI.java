package info.ata4.minecraft.minema;

import info.ata4.minecraft.minema.client.engine.FixedTimer;
import info.ata4.minecraft.minema.client.modules.modifiers.TimerModifier;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Minema API which can be used through reflection or by adding the
 * mod to dependency list
 */
@SideOnly(Side.CLIENT)
public class MinemaAPI {

	/**
	 * Get Minema's version
	 */
	public static String getVersion() {
		return Minema.VERSION;
	}

	/**
	 * Is Minema currently recording
	 */
	public static boolean isRecording() {
		return CaptureSession.singleton.isEnabled();
	}

	/**
	 * Set engine speed
	 *
	 * This could be used for keyframing engine speed for timelapses
	 * and other stuff
	 */
	public static void setEngineSpeed(float speed) {
		FixedTimer timer = TimerModifier.getTimer();

		if (timer != null) {
			timer.setSpeed(speed);
		}
	}

	/**
	 * Toggle Minema's recording
	 */
	public static boolean toggleRecording(boolean start) throws Exception {
		if (start == isRecording()) {
			return false;
		}

		if (start) {
			CaptureSession.singleton.start();
		} else {
			CaptureSession.singleton.stopCapture();
		}

		return true;
	}

}