package info.ata4.minecraft.minema.client.config;

import info.ata4.minecraft.minema.Minema;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;

public class MinemaConfigGui extends GuiConfig {

	public MinemaConfigGui(GuiScreen parentScreen) {
		super(parentScreen, Minema.instance.getConfig().getCategoryElements(), Minema.ID, false, false,
				GuiConfig.getAbridgedConfigPath(Minema.instance.getConfig().getConfigForge().toString()));
	}

}
