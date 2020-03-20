package space.bbkr.endershulkers.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

public class EnderShulkerBinderItem extends Item {
	public EnderShulkerBinderItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (world.isClient) return ActionResult.SUCCESS;
		PlayerEntity player = context.getPlayer();
		BlockEntity be = world.getBlockEntity(context.getBlockPos());
		if (be instanceof EnderShulkerBlockEntity && player.isSneaking()) {
			EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
			if (shulker.getOwnerId() == null) {
				shulker.setOwnerId(player.getUuid());
				if (!player.isCreative()) context.getStack().decrement(1);
			}
		}
		return super.useOnBlock(context);
	}
}
