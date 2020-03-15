package space.bbkr.endershulkers.item;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import space.bbkr.endershulkers.DyeableBlockEntity;

public class DyeableBlockItem extends BlockItem implements DyeableItem {
	public DyeableBlockItem(Block block, Settings settings) {
		super(block, settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld().isClient) return ActionResult.SUCCESS;
		BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
		if (be instanceof DyeableBlockEntity && context.getPlayer().isSneaking()) {
			setColor(context.getStack(), ((DyeableBlockEntity)be).getColor());
			return ActionResult.SUCCESS;
		}
		return super.useOnBlock(context);
	}

	@Override
	public boolean hasColor(ItemStack stack) {
		return getColor(stack) != 0xFFFFFF;
	}

	@Override
	public int getColor(ItemStack stack) {
		CompoundTag tag = stack.getOrCreateSubTag("BlockEntityTag");
		return tag.contains("Channel", NbtType.INT) ? tag.getInt("Channel") : 0xFFFFFF;
	}

	@Override
	public void removeColor(ItemStack stack) {
		CompoundTag compoundTag = stack.getSubTag("BlockEntityTag");
		if (compoundTag != null && compoundTag.contains("Channel")) {
			compoundTag.remove("Channel");
		}

	}

	@Override
	public void setColor(ItemStack stack, int color) {
		CompoundTag tag = stack.getOrCreateSubTag("BlockEntityTag");
		tag.putInt("Channel", color);
	}
}
