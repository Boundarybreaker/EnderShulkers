package space.bbkr.endershulkers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.Identifier;
import space.bbkr.endershulkers.client.render.EnderShulkerRenderer;

public class EnderShulkersClient implements ClientModInitializer {
	public static final Identifier ENDER_SHULKER_ID = new Identifier(EnderShulkers.MODID, "entity/shulker/ender_shulker");
	public static final Identifier PRIVATE_ENDER_SHULKER_ID = new Identifier(EnderShulkers.MODID, "entity/shulker/ender_shulker_private");

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(EnderShulkers.ENDER_SHULKER_BLOCK, RenderLayer.getCutout());
		BlockEntityRendererRegistry.INSTANCE.register(EnderShulkers.ENDER_SHULKER_BLOCK_ENTITY, EnderShulkerRenderer::new);
		ClientSpriteRegistryCallback.event(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE).register((texture, registry) -> {
			registry.register(ENDER_SHULKER_ID);
			registry.register(PRIVATE_ENDER_SHULKER_ID);
		});
	}
}
