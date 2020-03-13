package space.bbkr.endershulkers.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.BasicInventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.Direction;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;
import space.bbkr.endershulkers.component.EnderShulkerComponent;

import javax.annotation.Nullable;

public class EnderShulkerInventory extends BasicInventory implements SidedInventory {
	private EnderShulkerBlockEntity currentBlockEntity;

	public EnderShulkerInventory() {
		super(27);
	}

	public void setCurrentBlockEntity(EnderShulkerBlockEntity be) {
		this.currentBlockEntity = be;
	}

	public void readTags(ListTag listTag) {
		int j;
		for(j = 0; j < this.getInvSize(); ++j) {
			this.setInvStack(j, ItemStack.EMPTY);
		}

		for(j = 0; j < listTag.size(); ++j) {
			CompoundTag compoundTag = listTag.getCompound(j);
			int k = compoundTag.getByte("Slot") & 255;
			if (k >= 0 && k < this.getInvSize()) {
				this.setInvStack(k, ItemStack.fromTag(compoundTag));
			}
		}

	}

	public ListTag getTags() {
		ListTag listTag = new ListTag();

		for(int i = 0; i < this.getInvSize(); ++i) {
			ItemStack itemStack = this.getInvStack(i);
			if (!itemStack.isEmpty()) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putByte("Slot", (byte)i);
				itemStack.toTag(compoundTag);
				listTag.add(compoundTag);
			}
		}

		return listTag;
	}

	public boolean canPlayerUseInv(PlayerEntity player) {
		return this.currentBlockEntity != null && !this.currentBlockEntity.canPlayerUse(player) ? false : super.canPlayerUseInv(player);
	}

	public void onInvOpen(PlayerEntity player) {
		if (this.currentBlockEntity != null) {
			this.currentBlockEntity.onOpen();
		}

		super.onInvOpen(player);
	}

	public void onInvClose(PlayerEntity player) {
		if (this.currentBlockEntity != null) {
			this.currentBlockEntity.onClose();
		}

		super.onInvClose(player);
		this.currentBlockEntity = null;
	}

	@Override
	public void markDirty() {
		super.markDirty();
		EnderShulkerComponent.INSTANCE.sync();
	}

	@Override
	public int[] getInvAvailableSlots(Direction side) {
		int[] ret = new int[27];
		for (int i = 0; i < 27; i++) {
			ret[i] = i;
		}
		return ret;
	}

	@Override
	public boolean canInsertInvStack(int slot, ItemStack stack, @Nullable Direction dir) {
		return true;
	}

	@Override
	public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
		return true;
	}
}
