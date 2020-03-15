package space.bbkr.endershulkers.item;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

public class EnderShulkerLockItem extends Item {
	public EnderShulkerLockItem(Settings settings) {
		super(settings);
	}

	@Override
	//TODO: setting to forbid inventory interaction, way to remove lock?
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (world.isClient) return ActionResult.SUCCESS;
		BlockPos pos = context.getBlockPos();
		PlayerEntity player = context.getPlayer();
		ItemStack stack = context.getStack();
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EnderShulkerBlockEntity && player.isSneaking()) {
			EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
			if (shulker.getOwnerId() == null) {
				shulker.setOwnerId(player.getUuid());
				if (!player.isCreative()) stack.decrement(1);
			}
		}
		return super.useOnBlock(context);
	}
}
