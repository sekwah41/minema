package info.ata4.minecraft.minema;

import info.ata4.minecraft.minema.client.engine.FixedTimer;
import info.ata4.minecraft.minema.client.modules.modifiers.TimerModifier;

/**
 * Minema API which can be used through reflection or by adding the
 * mod to dependency list
 */
public class MinemaAPI {

	public static String getVersion() {
		return Minema.VERSION;
	}

	public static boolean isRecording() {
		return CaptureSession.singleton.isEnabled();
	}

	public static void setEngineSpeed(float speed) {
		FixedTimer timer = TimerModifier.getTimer();

		if (timer != null) {
			timer.setSpeed(speed);
		}
	}

	public static boolean toggleRecording(boolean start) {
		if (start == isRecording()) {
			return false;
		}

		if (start) {
			CaptureSession.singleton.startCapture();
		} else {
			CaptureSession.singleton.stopCapture();
		}

		return true;
	}

}