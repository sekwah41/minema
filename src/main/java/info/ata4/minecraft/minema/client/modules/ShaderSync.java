package info.ata4.minecraft.minema.client.modules;

import info.ata4.minecraft.minema.Minema;
import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.util.reflection.PrivateAccessor;

public class ShaderSync extends CaptureModule {

	private static ShaderSync instance = null;
	private static boolean freezeServer;

	/*
	 * Timespan between frames is 1/framesPerSecond (same as frequency and period in
	 * physics) -> the shader mod just measures the time between frames, in this
	 * context it is a constant time
	 */
	private float frameTimeCounter_step;
	private float fixedFrameTimeCounter;

	public static void freeze(boolean freeze) {
		ShaderSync.freezeServer = freeze;
	}

	public static boolean noHolding() {
		return Minema.instance.getConfig().heldFrames.get() <= 1;
	}

	@Override
	protected void doEnable() {
		MinemaConfig cfg = Minema.instance.getConfig();

		float fps = cfg.frameRate.get().floatValue();
		float speed = cfg.engineSpeed.get().floatValue();

		fixedFrameTimeCounter = PrivateAccessor.getFrameTimeCounter();
		frameTimeCounter_step = speed / fps;

		instance = this;
		ShaderSync.freeze(true);
	}

	@Override
	protected boolean checkEnable() {
		return Minema.instance.getConfig().syncEngine.get();
	}

	@Override
	protected void doDisable() {
		instance = null;
		ShaderSync.freeze(false);
	}

	private void sync() {
		fixedFrameTimeCounter += frameTimeCounter_step;
		fixedFrameTimeCounter %= 3600.0F;
		PrivateAccessor.setFrameTimeCounter(fixedFrameTimeCounter);
	}

	/**
	 * CALLED BY ASM INJECTED CODE! (COREMOD) DO NOT MODIFY METHOD SIGNATURE!
	 */
	public static void setFrameTimeCounter() {
		// This spot is right here because I can choose to only synchronize when
		// recording right here
		if (instance == null)
			return;
		instance.sync();
	}

	/**
	 * CALLED BY ASM INJECTED CODE! (COREMOD) DO NOT MODIFY METHOD SIGNATURE!
	 */
	public static long correctServerTick(long tick) {
		if (freezeServer) {
			// During ffmpeg initialization phase, it takes a while to launch the encoder
			// so this supposed to freeze server thread completely until the first frame
			// export
			return 0L;
		}

		return tick;
	}

}
