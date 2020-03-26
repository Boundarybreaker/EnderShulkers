package space.bbkr.endershulkers.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
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

	@Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;canPlayerModifyAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/BlockPos;)Z"), cancellable = true, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private void injectBucketWashing(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> info, ItemStack stack, HitResult hitResult, BlockHitResult blockHit, BlockPos pos, Direction dir, BlockPos offsetPos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (this.fluid == Fluids.WATER && user.isSneaking() && be instanceof DyeableBlockEntity) {
			((DyeableBlockEntity)be).removeColor();
			playEmptyingSound(null, world, pos);
			user.setStackInHand(hand, getEmptiedStack(stack, user));
			info.setReturnValue(TypedActionResult.success(user.getStackInHand(hand)));
		}
	}
}
