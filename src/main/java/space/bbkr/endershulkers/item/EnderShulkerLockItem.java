package space.bbkr.endershulkers.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

public class EnderShulkerLockItem extends Item {
	public EnderShulkerLockItem(Settings settings) {
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
			if ((shulker.getOwnerId() == null || player.getUuid().equals(shulker.getOwnerId())) && !shulker.isLocked()) {
				shulker.setLocked(true);
				world.playSound(null, context.getBlockPos(), SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1f, 1f);
				if (!player.isCreative()) context.getStack().decrement(1);
			}
		}
		return super.useOnBlock(context);
	}
}
