package space.bbkr.endershulkers.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import space.bbkr.endershulkers.DyeableBlockEntity;

import java.util.Collections;

@Mixin(DyeItem.class)
public abstract class MixinDyeItem extends Item {
	public MixinDyeItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld().isClient) return ActionResult.SUCCESS;
		PlayerEntity player = context.getPlayer();
		BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
		if (be instanceof DyeableBlockEntity && player.isSneaking()) {
			((DyeableBlockEntity) be).blendAndSetColor(Collections.singletonList((DyeItem)(Object)this));
			if (!player.isCreative()) {
				context.getStack().decrement(1);
			}
			return ActionResult.SUCCESS;
		}
		return super.useOnBlock(context);
	}
}
