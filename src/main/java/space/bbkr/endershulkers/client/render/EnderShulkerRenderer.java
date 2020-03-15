package space.bbkr.endershulkers.client.render;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.entity.model.ShulkerEntityModel;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Direction;
import space.bbkr.endershulkers.EnderShulkersClient;
import space.bbkr.endershulkers.block.EnderShulkerBlock;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

public class EnderShulkerRenderer extends BlockEntityRenderer<EnderShulkerBlockEntity> {
	private final ShulkerEntityModel<?> model;

	public EnderShulkerRenderer(BlockEntityRenderDispatcher dispatcher) {
		super(dispatcher);
		this.model = new ShulkerEntityModel<>();
	}

	public void render(EnderShulkerBlockEntity be, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, int j) {
		Direction direction = Direction.UP;
		if (be.hasWorld()) {
			BlockState blockState = be.getWorld().getBlockState(be.getPos());
			if (blockState.getBlock() instanceof EnderShulkerBlock) {
				direction = blockState.get(EnderShulkerBlock.FACING);
			}
		}

		int color = be.getColor();
		SpriteIdentifier spriteId = new SpriteIdentifier(TexturedRenderLayers.SHULKER_BOXES_ATLAS_TEXTURE, be.getOwnerId() == null? EnderShulkersClient.ENDER_SHULKER_ID : EnderShulkersClient.PRIVATE_ENDER_SHULKER_ID);

		matrixStack.push();
		matrixStack.translate(0.5D, 0.5D, 0.5D);
		float scale = 0.9995F;
		matrixStack.scale(scale, scale, scale);
		matrixStack.multiply(direction.getRotationQuaternion());
		matrixStack.scale(1.0F, -1.0F, -1.0F);
		matrixStack.translate(0.0D, -1.0D, 0.0D);
		VertexConsumer vertexConsumer = spriteId.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);
		float r = (color >> 16 & 255) / 255f;
		float g = (color >> 8 & 255) / 255f;
		float b = (color & 255) / 255f;
		this.model.getBottomShell().render(matrixStack, vertexConsumer, i, j, r, g, b, 1f);
		matrixStack.translate(0.0D, -be.getAnimationProgress(f) * 0.5F, 0.0D);
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(270.0F * be.getAnimationProgress(f)));
		this.model.getTopShell().render(matrixStack, vertexConsumer, i, j, r, g, b, 1f);
		matrixStack.pop();
	}
}
