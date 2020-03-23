package space.bbkr.endershulkers.mixin;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.item.DyeableItem;
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

import java.util.UUID;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

	@Shadow public abstract Item getItem();

	@Shadow public abstract CompoundTag getOrCreateTag();

	@Shadow public abstract CompoundTag getOrCreateSubTag(String key);

	@Shadow public abstract boolean hasCustomName();

	@Shadow public abstract Text getName();

	@Inject(method = "setCustomName", at = @At("HEAD"))
	private void markNameDirty(Text name, CallbackInfoReturnable<ItemStack> info) {
		if (getItem() == EnderShulkers.ENDER_SHULKER_ITEM) {
			getOrCreateTag().putBoolean("DirtyName", true);
		}
	}

	//could theoretically go on the item class itself but I'm dealing with it in this mixin already so oh well
	@Inject(method = "inventoryTick", at = @At("HEAD"))
	private void renameEnderShulker(World world, Entity entity, int slot, boolean selected, CallbackInfo info) {
		if (world.isClient) return;
		if (getItem() == EnderShulkers.ENDER_SHULKER_ITEM) {
			CompoundTag tag = getOrCreateTag();
			if (tag.contains("DirtyName") && hasCustomName()) {
				tag.remove("DirtyName");
				CompoundTag beTag = getOrCreateSubTag("BlockEntityTag");
				if (beTag.containsUuid("Owner")) {
					int channel = ((DyeableItem)getItem()).getColor((ItemStack)(Object)this);
					UUID id = beTag.getUuid("Owner");
					EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(id, channel).setCustomName(getName());
				}
			}
		}
	}
}
