package info.ata4.minecraft.minema.client.util;

public class MinemaException extends RuntimeException {
	public MinemaException(String message) {
		super(message);
	}

	public MinemaException(String message, Throwable cause) {
		super(message, cause);
	}
}