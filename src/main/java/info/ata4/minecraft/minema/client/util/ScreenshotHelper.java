package info.ata4.minecraft.minema.client.util;

import info.ata4.minecraft.minema.Minema;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.image.BufferedImage;

public class ScreenshotHelper
{
	/**
	 * CALLED BY ASM INJECTED CODE! (COREMOD) DO NOT MODIFY METHOD SIGNATURE!
	 */
	public static int getType()
	{
		return Minema.instance.getConfig().useAlphaScreenshot.get() ? BufferedImage.TYPE_4BYTE_ABGR : BufferedImage.TYPE_INT_RGB;
	}

	/**
	 * Replace the call for normal blend functions
	 */
	public static void  replaceBlendFunc(int srcFactor, int dstFactor) {
		if(srcFactor == GL11.GL_SRC_ALPHA && dstFactor == GL11.GL_ONE_MINUS_SRC_ALPHA) {

		}
		GL11.glBlendFunc(srcFactor, dstFactor);
		GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
				GL11.GL_ONE_MINUS_DST_ALPHA, GL11.GL_ONE);
	}
}
