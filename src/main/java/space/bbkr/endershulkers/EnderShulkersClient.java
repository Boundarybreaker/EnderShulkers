package space.bbkr.endershulkers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.minecraft.client.render.RenderLayer;
import space.bbkr.endershulkers.client.render.EnderShulkerRenderer;

public class EnderShulkersClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(EnderShulkers.ENDER_SHULKER_BLOCK, RenderLayer.getCutout());
		BlockEntityRendererRegistry.INSTANCE.register(EnderShulkers.ENDER_SHULKER_BLOCK_ENTITY, EnderShulkerRenderer::new);
	}
}
