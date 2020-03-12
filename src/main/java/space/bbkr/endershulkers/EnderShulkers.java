package space.bbkr.endershulkers;

import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.LevelComponentCallback;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import space.bbkr.endershulkers.component.EnderShulkerComponent;

public class EnderShulkers implements ModInitializer {
	public static final String MODID = "endershulkers";

	public static final Logger logger = LogManager.getLogger();

	public static final ComponentType<EnderShulkerComponent> ENDER_SHULKER_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier(MODID, "ender_shulkers"), EnderShulkerComponent.class);

	public static final Block ENDER_SHULKER_BLOCK;
	public static final BlockEntity ENDER_SHULKER_BLOCK_ENTITY;

	@Override
	public void onInitialize() {
		LevelComponentCallback.EVENT.register(((levelProperties, container) -> container.put(ENDER_SHULKER_COMPONENT, EnderShulkerComponent.INSTANCE)));
	}


}
