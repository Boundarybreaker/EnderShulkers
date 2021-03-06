package space.bbkr.endershulkers.block.entity;

import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.container.Container;
import net.minecraft.container.NameableContainerFactory;
import net.minecraft.container.ShulkerBoxContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Nameable;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import space.bbkr.endershulkers.DyeableBlockEntity;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.block.EnderShulkerBlock;
import space.bbkr.endershulkers.inventory.EnderShulkerInventory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class EnderShulkerBlockEntity extends BlockEntity implements Tickable, BlockEntityClientSerializable, NameableContainerFactory, DyeableBlockEntity, Nameable {
	public float animationProgress;
	public float prevAnimationProgress;
	public int viewerCount;
	private ShulkerBoxBlockEntity.AnimationStage animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
	private int channel = 0xFFFFFF;
	private UUID ownerId = null;
	private boolean locked;
	private Text customName;

	public EnderShulkerBlockEntity() {
		super(EnderShulkers.ENDER_SHULKER_BLOCK_ENTITY);
	}

	public void tick() {
		this.updateAnimation();
		if (this.animationStage == ShulkerBoxBlockEntity.AnimationStage.OPENING || this.animationStage == ShulkerBoxBlockEntity.AnimationStage.CLOSING) {
			this.pushEntities();
		}

	}

	protected void updateAnimation() {
		this.prevAnimationProgress = this.animationProgress;
		switch(this.animationStage) {
			case CLOSED:
				this.animationProgress = 0.0F;
				break;
			case OPENING:
				this.animationProgress += 0.1F;
				if (this.animationProgress >= 1.0F) {
					this.pushEntities();
					this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENED;
					this.animationProgress = 1.0F;
					this.updateNeighborStates();
				}
				break;
			case CLOSING:
				this.animationProgress -= 0.1F;
				if (this.animationProgress <= 0.0F) {
					this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSED;
					this.animationProgress = 0.0F;
					this.updateNeighborStates();
				}
				break;
			case OPENED:
				this.animationProgress = 1.0F;
		}

	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
		if (world != null && !world.isClient) sync();
	}

	public void removeOwnerId() {
		this.ownerId = null;
	}

	@Override
	public boolean hasColor() {
		return channel != 0xFFFFFF;
	}

	@Override
	public int getColor() {
		return channel;
	}

	@Override
	public void setColor(int color) {
		this.channel = color;
		if (world != null && !world.isClient) sync();
	}

	@Override
	public void removeColor() {
		this.channel = 0xFFFFFF;
		if (world != null && !world.isClient) sync();
	}

	@Override
	public boolean hasCustomName() {
		if (ownerId == null) {
			return customName != null;
		}
		return getInventoryAlways().hasCustomName();
	}

	@Override
	public Text getCustomName() {
		if (ownerId == null) {
			return customName;
		}
		return getInventoryAlways().getCustomName();
	}

	@Override
	public Text getName() {
		if (hasCustomName()) return getCustomName();
		return new TranslatableText("title.endershulkers.ender_shulker", EnderShulkerBlock.getChannelColor(channel));
	}

	public void setCustomName(Text name) {
		if (ownerId == null) {
			this.customName = name;
			if (world != null && !world.isClient) sync();
		} else {
			getInventoryAlways().setCustomName(name);
		}
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
		if (world != null && !world.isClient) sync();
	}

	@Nullable
	public EnderShulkerInventory getInventory() {
		if (isLocked()) return null;
		return EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(ownerId, channel);
	}

	private EnderShulkerInventory getInventoryAlways() {
		return EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(ownerId, channel);
	}

	public ShulkerBoxBlockEntity.AnimationStage getAnimationStage() {
		return this.animationStage;
	}

	public Box getBoundingBox(BlockState state) {
		return this.getBoundingBox(state.get(ShulkerBoxBlock.FACING));
	}

	public Box getBoundingBox(Direction openDirection) {
		float f = this.getAnimationProgress(1.0F);
		return VoxelShapes.fullCube().getBoundingBox().stretch(0.5F * f * (float)openDirection.getOffsetX(), 0.5F * f * (float)openDirection.getOffsetY(), 0.5F * f * (float)openDirection.getOffsetZ());
	}

	private Box getCollisionBox(Direction facing) {
		Direction direction = facing.getOpposite();
		return this.getBoundingBox(facing).shrink(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
	}

	private void pushEntities() {
		BlockState blockState = this.world.getBlockState(this.getPos());
		if (blockState.getBlock() instanceof EnderShulkerBlock) {
			Direction direction = blockState.get(EnderShulkerBlock.FACING);
			Box box = this.getCollisionBox(direction).offset(this.pos);
			List<Entity> list = this.world.getEntities(null, box);
			if (!list.isEmpty()) {
				for(int i = 0; i < list.size(); ++i) {
					Entity entity = list.get(i);
					if (entity.getPistonBehavior() != PistonBehavior.IGNORE) {
						double d = 0.0D;
						double e = 0.0D;
						double f = 0.0D;
						Box box2 = entity.getBoundingBox();
						switch(direction.getAxis()) {
							case X:
								if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
									d = box.x2 - box2.x1;
								} else {
									d = box2.x2 - box.x1;
								}

								d += 0.01D;
								break;
							case Y:
								if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
									e = box.y2 - box2.y1;
								} else {
									e = box2.y2 - box.y1;
								}

								e += 0.01D;
								break;
							case Z:
								if (direction.getDirection() == Direction.AxisDirection.POSITIVE) {
									f = box.z2 - box2.z1;
								} else {
									f = box2.z2 - box.z1;
								}

								f += 0.01D;
						}

						entity.move(MovementType.SHULKER_BOX, new Vec3d(d * (double)direction.getOffsetX(), e * (double)direction.getOffsetY(), f * (double)direction.getOffsetZ()));
					}
				}

			}
		}
	}

	@Override
	public boolean onBlockAction(int type, int data) {
		if (type == 1) {
			this.viewerCount = data;
			if (data == 0) {
				this.animationStage = ShulkerBoxBlockEntity.AnimationStage.CLOSING;
				this.updateNeighborStates();
			}

			if (data == 1) {
				this.animationStage = ShulkerBoxBlockEntity.AnimationStage.OPENING;
				this.updateNeighborStates();
			}

			return true;
		} else {
			return super.onBlockAction(type, data);
		}
	}

	private void updateNeighborStates() {
		this.getCachedState().updateNeighborStates(this.getWorld(), this.getPos(), 3);
		if (!world.isClient) sync();
	}

	public void markRemoved() {
		this.resetBlock();
		super.markRemoved();
	}

	public void onOpen() {
		++this.viewerCount;
		this.world.addBlockAction(this.pos, EnderShulkers.ENDER_SHULKER_BLOCK, 1, this.viewerCount);
		world.playSound(null, getPos(), SoundEvents.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1f, 1f);
	}

	public void onClose() {
		--this.viewerCount;
		this.world.addBlockAction(this.pos, EnderShulkers.ENDER_SHULKER_BLOCK, 1, this.viewerCount);
		world.playSound(null, getPos(), SoundEvents.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1f, 1f);
	}

	public boolean canPlayerUse(PlayerEntity player) {
		if (this.world.getBlockEntity(this.pos) != this) {
			return false;
		} else {
			if (locked && ownerId != null) {
				if (!player.getUuid().equals(ownerId)) {
					player.playSound(SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1f, 1f);
					player.addChatMessage(new TranslatableText("msg.endershulkers.locked"), true);
					return false;
				}
			}
			return player.squaredDistanceTo((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	public float getAnimationProgress(float f) {
		return MathHelper.lerp(f, this.prevAnimationProgress, this.animationProgress);
	}

	@Override
	public void fromTag(CompoundTag tag) {
		super.fromTag(tag);
		channel = tag.getInt("Channel");
		if (tag.containsUuid("Owner")) ownerId = tag.getUuid("Owner");
		locked = tag.getBoolean("Locked");
		if (tag.contains("CustomName", NbtType.STRING)) {
			this.customName = Text.Serializer.fromJson(tag.getString("CustomName"));
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		super.toTag(tag);
		tag.putInt("Channel", channel);
		if (ownerId != null) tag.putUuid("Owner", ownerId);
		tag.putBoolean("Locked", locked);
		if (hasCustomName()) {
			tag.putString("CustomName", Text.Serializer.toJson(customName));
		}
		return tag;
	}

	@Override
	public void fromClientTag(CompoundTag tag) {
		fromTag(tag);
		viewerCount = tag.getInt("ViewerCount");
	}

	@Override
	public CompoundTag toClientTag(CompoundTag tag) {
		toTag(tag);
		tag.putInt("ViewerCount", viewerCount);
		return tag;
	}

	@Override
	public Text getDisplayName() {
		return getName();
	}

	@Nullable
	@Override
	public Container createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new ShulkerBoxContainer(syncId, inv, EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(ownerId, getColor()).asInventory());
	}
}
