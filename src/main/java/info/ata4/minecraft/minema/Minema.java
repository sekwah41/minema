package info.ata4.minecraft.minema;

import org.lwjgl.input.Keyboard;

import info.ata4.minecraft.minema.client.config.MinemaConfig;
import info.ata4.minecraft.minema.client.modules.CaptureSession;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;

/**
 * Most of the files in this repo do have the old copyright notice about
 * Barracuda even though I have touched most of it, in some cases substantially.
 * Few classes do not contain the notice, these are the ones that I have written
 * completely myself or some of the class with substantial changes.
 * 
 * @author Gregosteros (minecraftforum) / daipenger (github)
 */
@Mod(modid = Minema.ID, name = Minema.NAME, version = Minema.VERSION, guiFactory = "info.ata4.minecraft.minema.client.config.MinemaConfigGuiFactory")
public class Minema {

	public static final String NAME = "Minema";
	/*
	 * in 1.11 Forge will only accept a lower case id and due to annotations
	 * this has to be a constant expression
	 */
	public static final String ID = "minema";
	public static final String VERSION = "1.11.2";

	private static final String category = "key.categories.minema";
	private static final KeyBinding KEY_CAPTURE = new KeyBinding("key.minema.capture", Keyboard.KEY_F4, category);

	@Instance(ID)
	public static Minema instance;
	public static final EventBus EVENT_BUS = new EventBus();

	private MinemaConfig config;
	private CaptureSession session;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		config = new MinemaConfig(new Configuration(e.getSuggestedConfigurationFile()));
	}

	@EventHandler
	public void onInit(FMLInitializationEvent evt) {
		ClientCommandHandler.instance.registerCommand(new CommandMinema(this));
		ClientRegistry.registerKeyBinding(KEY_CAPTURE);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent e) {
		if (e.getModID().equals(ID)) {
			if (config.getConfigForge().hasChanged()) {
				config.getConfigForge().save();
			}
		}
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event) {
		if (KEY_CAPTURE.isPressed()) {
			if (!enable())
				disable();
		}
	}

	public boolean enable() {
		if (session == null || !session.isEnabled()) {
			session = new CaptureSession(config);
			session.enable();
			return true;
		}
		return false;
	}

	public boolean disable() {
		if (session == null || !session.isEnabled()) {
			return false;
		}
		session.disable();
		session = null;
		return true;
	}

	public MinemaConfig getConfig() {
		return config;
	}

}
