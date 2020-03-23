package space.bbkr.endershulkers.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.item.BuiltinModelItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

@Mixin(BuiltinModelItemRenderer.class)
public class MixinBuiltinModelItemRenderer {
	private final EnderShulkerBlockEntity be = new EnderShulkerBlockEntity();

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private void injectEnderShulkerRenderer(ItemStack stack, MatrixStack matrix, VertexConsumerProvider vertexConsumerProvider, int light, int overlay, CallbackInfo info) {
		if (stack.getItem() == EnderShulkers.ENDER_SHULKER_ITEM) {
			be.fromTag(stack.getOrCreateSubTag("BlockEntityTag"));
			if (!stack.getOrCreateSubTag("BlockEntityTag").containsUuid("Owner")) be.setOwnerId(null);
			else be.setOwnerId(stack.getOrCreateSubTag("BlockEntityTag").getUuid("Owner"));
			int color = ((DyeableItem)stack.getItem()).getColor(stack);
			be.setColor(color);
			BlockEntityRenderDispatcher.INSTANCE.renderEntity(be, matrix, vertexConsumerProvider, light, overlay);
			info.cancel();
		}
	}
}
