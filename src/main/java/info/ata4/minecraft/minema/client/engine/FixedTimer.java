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
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class FixedTimer extends Timer {

	private static Field shader_frameTimeCounter;

	private final float ticksPerSecond;
	private final float framesPerSecond;

	private static float frameTimeCounter;

	public FixedTimer(float tps, float fps, float speed) {
		super(tps);
		ticksPerSecond = tps;
		framesPerSecond = fps;
		timerSpeed = speed;
		frameTimeCounter = 0;

		if (shader_frameTimeCounter != null)
			return;

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

	@Override
	public void updateTimer() {
		elapsedPartialTicks += timerSpeed * (ticksPerSecond / framesPerSecond);
		elapsedTicks = (int) elapsedPartialTicks;
		elapsedPartialTicks -= elapsedTicks;
		renderPartialTicks = elapsedPartialTicks;

		// Timespan between frames is 1/framesPerSecond -> the shaders mod just
		// measures the time between frames, in this context it is the constant
		// time
		if (shader_frameTimeCounter != null) {
			frameTimeCounter += 1 / (framesPerSecond * timerSpeed);
			frameTimeCounter %= 3600;
		}
	}

	public static void setFrameTimeCounter() {
		if (shader_frameTimeCounter == null)
			return;
		// this field is static, just using null as the object
		try {
			shader_frameTimeCounter.setFloat(null, frameTimeCounter);
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
	}

}
