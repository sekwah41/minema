package info.ata4.minecraft.minema.client.capture;

import static org.lwjgl.opengl.ARBBufferObject.glBindBufferARB;
import static org.lwjgl.opengl.ARBBufferObject.glBufferDataARB;
import static org.lwjgl.opengl.ARBBufferObject.glDeleteBuffersARB;
import static org.lwjgl.opengl.ARBBufferObject.glGenBuffersARB;
import static org.lwjgl.opengl.ARBBufferObject.glMapBufferARB;
import static org.lwjgl.opengl.ARBBufferObject.glUnmapBufferARB;
import static org.lwjgl.opengl.GL11.GL_PACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNPACK_ALIGNMENT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glPixelStorei;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.ARBPixelBufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.Util;

import net.minecraft.client.shader.Framebuffer;

public class PBOCapturer extends ACapturer {

	public static final boolean isSupported = GLContext.getCapabilities().GL_ARB_pixel_buffer_object;

	private static final int PACK_MODE = ARBPixelBufferObject.GL_PIXEL_PACK_BUFFER_ARB;
	private static final int STREAM_READ = ARBPixelBufferObject.GL_STREAM_READ_ARB;
	private static final int READ_ONLY_ACCESS = ARBPixelBufferObject.GL_READ_ONLY_ARB;

	private int frontAddress;
	private int backAddress;
	private ByteBuffer frontCache;
	private ByteBuffer backCache;

	public PBOCapturer() {
		this.frontAddress = glGenBuffersARB();
		glBindBufferARB(PACK_MODE, this.frontAddress);
		glBufferDataARB(PACK_MODE, this.bufferSize, STREAM_READ);

		this.backAddress = glGenBuffersARB();
		glBindBufferARB(PACK_MODE, this.backAddress);
		glBufferDataARB(PACK_MODE, this.bufferSize, STREAM_READ);

		glBindBufferARB(PACK_MODE, 0);
	}

	private void swapPBOs() {
		final int swapAddress = this.frontAddress;
		this.frontAddress = this.backAddress;
		this.backAddress = swapAddress;
		final ByteBuffer swapGlBuffer = this.frontCache;
		this.frontCache = this.backCache;
		this.backCache = swapGlBuffer;
	}

	@Override
	public void capture() {
		glBindBufferARB(PACK_MODE, this.frontAddress);

		// Calling into event queue

		// set alignment flags (has to be inside event queue)
		glPixelStorei(GL_PACK_ALIGNMENT, 1);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

		// use faster framebuffer access if enabled
		if (isFramebufferEnabled) {
			final Framebuffer buffer = MC.getFramebuffer();
			buffer.bindFramebufferTexture();
			GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, this.colorFormat, GL_UNSIGNED_BYTE, 0L);
			buffer.unbindFramebufferTexture();
		} else {
			GL11.glReadPixels(0, 0, this.start.getWidth(), this.start.getHeight(), this.colorFormat, GL_UNSIGNED_BYTE,
					0);
		}

		// Not calling into event queue

		glBindBufferARB(PACK_MODE, 0);

		swapPBOs();

		glBindBufferARB(PACK_MODE, this.frontAddress);

		this.frontCache = glMapBufferARB(PACK_MODE, READ_ONLY_ACCESS, this.bufferSize, this.frontCache);
		// If mapping threw an error -> crash immediately please
		Util.checkGLError();
		this.buffer.put(this.frontCache);
		// Recycling native buffers also needs rewinding! Not doing so would
		// result in fast line flipping of the first frame (or not if you do not
		// use PipeExporter) -> a symptom of not writing due to not rewinding
		this.frontCache.rewind();
		glUnmapBufferARB(PACK_MODE);

		glBindBufferARB(PACK_MODE, 0);
	}

	@Override
	public void close() {
		glDeleteBuffersARB(this.frontAddress);
		glDeleteBuffersARB(this.backAddress);
	}

}
