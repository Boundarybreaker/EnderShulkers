package space.bbkr.endershulkers.inventory;

import io.github.cottonmc.component.compat.vanilla.SidedInventoryWrapper;
import io.github.cottonmc.component.item.InventoryComponent;
import io.github.cottonmc.component.item.impl.SimpleInventoryComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;
import space.bbkr.endershulkers.component.EnderShulkerComponent;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

public class EnderShulkerInventory extends SimpleInventoryComponent {
	private EnderShulkerBlockEntity currentBlockEntity;

	public EnderShulkerInventory() {
		super(27);
	}

	public void setCurrentBlockEntity(EnderShulkerBlockEntity be) {
		this.currentBlockEntity = be;
	}

	@Override
	public Inventory asInventory() {
		return new EnderShulkerWrapper();
	}

	@Nullable
	@Override
	public SidedInventory asLocalInventory(IWorld world, BlockPos pos) {
		return new EnderShulkerWrapper(world, pos);
	}

	@Override
	public void markDirty() {
		super.markDirty();
		EnderShulkerComponent.INSTANCE.sync();
	}
	
	private class EnderShulkerWrapper implements SidedInventoryWrapper {
		private EnderShulkerInventory inv = EnderShulkerInventory.this;
		private EnderShulkerBlockEntity be;
		
		private EnderShulkerWrapper() {
			this.be = inv.currentBlockEntity;
		}
		
		private EnderShulkerWrapper(IWorld world, BlockPos pos) {
			be = (EnderShulkerBlockEntity)world.getBlockEntity(pos);
		}

		@Nullable
		@Override
		public InventoryComponent getComponent(@Nullable Direction direction) {
			return inv;
		}

		@Override
		public boolean canPlayerUseInv(PlayerEntity player) {
			return (be == null || be.canPlayerUse(player));
		}

		@Override
		public void onInvOpen(PlayerEntity player) {
			if (be != null) {
				be.onOpen();
			}
		}

		@Override
		public void onInvClose(PlayerEntity player) {
			if (be != null) {
				be.onClose();
			}

			be = null;
		}

		@Override
		public int[] getInvAvailableSlots(Direction side) {
			if (be.isLocked()) return new int[0];
			return IntStream.range(0, getInvSize()).toArray();
		}

		@Override
		public boolean canInsertInvStack(int slot, ItemStack stack, @Nullable Direction dir) {
			return !be.isLocked();
		}

		@Override
		public boolean canExtractInvStack(int slot, ItemStack stack, Direction dir) {
			return !be.isLocked();
		}

		@Override
		public void markDirty() {
			inv.markDirty();
		}
	};
}
