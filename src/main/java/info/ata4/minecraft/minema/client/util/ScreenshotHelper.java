package info.ata4.minecraft.minema.client.util;

import info.ata4.minecraft.minema.Minema;

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
}
