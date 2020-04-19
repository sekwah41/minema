package info.ata4.minecraft.minema;

import info.ata4.minecraft.minema.client.util.MinemaException;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.lang.reflect.Field;

public class Utils {

	public static void print(final String msg, final TextFormatting format) {
		final TextComponentString text = new TextComponentString(msg == null ? "null" : msg);
		text.getStyle().setColor(format);
		Minecraft.getMinecraft().player.sendMessage(text);
	}

	public static void printError(Throwable throwable) {
		print(throwable.getClass().getName(), TextFormatting.RED);
		print(throwable.getMessage(), TextFormatting.RED);
		Throwable cause = throwable.getCause();
		if (cause != null) {
			print("Cause:", TextFormatting.RED);
			print(cause.getClass().getName(), TextFormatting.RED);
			print(cause.getMessage(), TextFormatting.RED);
		}
		throwable.printStackTrace();
		print("See log for full stacktrace", TextFormatting.RED);
	}

	public static void printPrettyError(Throwable throwable) {
		Throwable cause = throwable.getCause();

		if (cause == null && throwable instanceof MinemaException) {
			cause = throwable;
		}

		if (cause != null) {
			print(cause.getMessage() + "\n", TextFormatting.RED);
		}

		throwable.printStackTrace();
		print("See log for full stacktrace", TextFormatting.RED);
	}

	public static Field getField(Class clazz, String mcp, String srg) {
		Field field = null;

		try {
			field = clazz.getDeclaredField(mcp);
		} catch (Exception e) {}

		if (field == null) {
			try {
				field = clazz.getDeclaredField(srg);
			} catch (Exception e) {}
		}

		if (field != null) {
			field.setAccessible(true);
		}

		return field;
	}

}
