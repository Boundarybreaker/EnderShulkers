package space.bbkr.endershulkers.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import space.bbkr.endershulkers.DyeableBlockEntity;

import javax.annotation.Nullable;

@Mixin(BucketItem.class)
public abstract class MixinBucketItem extends Item {

	@Shadow protected abstract ItemStack getEmptiedStack(ItemStack stack, PlayerEntity player);

	@Shadow @Final private Fluid fluid;

	@Shadow protected abstract void playEmptyingSound(@Nullable PlayerEntity player, IWorld world, BlockPos pos);

	public MixinBucketItem(Settings settings) {
		super(settings);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		if (context.getWorld().isClient) return ActionResult.SUCCESS;
		if (this.fluid == Fluids.WATER) {
			PlayerEntity player = context.getPlayer();
			BlockEntity be = context.getWorld().getBlockEntity(context.getBlockPos());
			if (be instanceof DyeableBlockEntity && player.isSneaking()) {
				((DyeableBlockEntity) be).removeColor();
				playEmptyingSound(null, context.getWorld(), context.getBlockPos());
				player.setStackInHand(context.getHand(), getEmptiedStack(context.getStack(), player));
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnBlock(context);
	}
}
