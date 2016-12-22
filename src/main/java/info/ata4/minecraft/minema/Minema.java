/*
 ** 2013 April 09
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
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
 * Main control class for Forge.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Mod(modid = Minema.ID, name = Minema.NAME, version = Minema.VERSION, guiFactory = "info.ata4.minecraft.minema.client.config.MinemaConfigGuiFactory")
public class Minema {

	public static final String NAME = "Minema";
	/*
	 * in 1.11 Forge will only accept a lower case id and due to annotations
	 * this has to be a constant expression
	 */
	public static final String ID = "minema";
	public static final String VERSION = "1.11";

	private static final String category = "key.categories.minema";
	private static final KeyBinding KEY_CAPTURE = new KeyBinding("key.minema.capture", Keyboard.KEY_F4, category);

	@Instance(ID)
	public static Minema instance;
	public static final EventBus EVENT_BUS = new EventBus();

	private Configuration configForge;
	private MinemaConfig config;
	private CaptureSession session;

	@EventHandler
	public void onPreInit(FMLPreInitializationEvent e) {
		configForge = new Configuration(e.getSuggestedConfigurationFile());
		config = new MinemaConfig(configForge);
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
			if (configForge.hasChanged()) {
				configForge.save();
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

	public Configuration getConfigForge() {
		return configForge;
	}

	public MinemaConfig getConfig() {
		return config;
	}

}
