/*
 ** 2012 January 3
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.engine;

import java.lang.reflect.Field;

import net.minecraft.util.Timer;

/**
 * Extension of Minecraft's default timer for fixed framerate rendering.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de> / Shader part: daipenger
 */
public class FixedTimer extends Timer {

	private static Field shader_frameTimeCounter;

	private final float ticksPerSecond;
	private final float framesPerSecond;

	/*
	 * Timespan between frames is 1/framesPerSecond (same as frequency and
	 * period in physics) -> the shader mod just measures the time between
	 * frames, in this context it is a constant time
	 */
	private final float frameTimeCounter_step;
	private float fixedFrameTimeCounter;

	public FixedTimer(float tps, float fps, float speed) {
		super(tps);

		// Not doing this with a static constructor because at a static point we
		// cannot say for sure if shaders are already loaded
		if (shader_frameTimeCounter == null) {
			// Java 1.6 level and its catchy catch bloat up
			Field frameTimeCounter = null;
			try {
				frameTimeCounter = Class.forName("shadersmod.client.Shaders").getDeclaredField("frameTimeCounter");
				frameTimeCounter.setAccessible(true);
			} catch (NoSuchFieldException e) {
			} catch (SecurityException e) {
			} catch (ClassNotFoundException e) {
			}
			shader_frameTimeCounter = frameTimeCounter;
		}

		ticksPerSecond = tps;
		framesPerSecond = fps;
		timerSpeed = speed;
		fixedFrameTimeCounter = getFrameTimeCounter();
		frameTimeCounter_step = speed / fps;
	}

	@Override
	public void updateTimer() {
		elapsedPartialTicks += timerSpeed * (ticksPerSecond / framesPerSecond);
		elapsedTicks = (int) elapsedPartialTicks;
		elapsedPartialTicks -= elapsedTicks;
		renderPartialTicks = elapsedPartialTicks;

		if (shader_frameTimeCounter == null)
			return;
		// Shader mod analog code
		fixedFrameTimeCounter += frameTimeCounter_step;
		fixedFrameTimeCounter %= 3600.0F;
	}

	private float getFrameTimeCounter() {
		if (shader_frameTimeCounter == null)
			return 0;
		// this field is static, just using null as the object
		try {
			return shader_frameTimeCounter.getFloat(null);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return 0;
		}
	}

	public void setFrameTimeCounter() {
		if (shader_frameTimeCounter == null)
			return;
		// this field is static, just using null as the object
		try {
			shader_frameTimeCounter.setFloat(null, fixedFrameTimeCounter);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

}
