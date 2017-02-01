/*
** 2016 June 03
**
** The author disclaims copyright to this source code. In place of
** a legal notice, here is a blessing:
**    May you do good and not evil.
**    May you find forgiveness for yourself and forgive others.
**    May you share freely, never taking more than you give.
 */
package info.ata4.minecraft.minema.client.event;

import java.nio.file.Path;

import info.ata4.minecraft.minema.client.util.CaptureFrame;
import info.ata4.minecraft.minema.client.util.CaptureTime;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Cancelable
public abstract class FrameEvent extends Event {

	public final CaptureFrame frame;
	public final CaptureTime time;

	public final Path captureDir;
	public final String movieName;

	public FrameEvent(CaptureFrame frame, CaptureTime time, Path captureDir, String movieName) {
		this.frame = frame;
		this.time = time;
		this.captureDir = captureDir;
		this.movieName = movieName;
	}

}
