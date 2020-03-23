package space.bbkr.endershulkers.mixin;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import space.bbkr.endershulkers.EnderShulkers;


@Mixin(ItemStack.class)
public abstract class MixinItemStack {

	@Shadow public abstract Item getItem();

	@Shadow public abstract CompoundTag getOrCreateTag();

	@Inject(method = "setCustomName", at = @At("HEAD"))
	private void markNameDirty(Text name, CallbackInfoReturnable<ItemStack> info) {
		if (getItem() == EnderShulkers.ENDER_SHULKER_ITEM) {
			getOrCreateTag().putBoolean("DirtyName", true);
		}
	}

	//could theoretically go on the item class itself but I'm dealing with it in this mixin already so oh well
	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void renameEnderShulker(World world, Entity entity, int slot, boolean selected, CallbackInfo info) {

	}
}
