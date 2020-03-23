package space.bbkr.endershulkers.block;


import com.mojang.authlib.GameProfile;
import io.github.cottonmc.component.UniversalComponents;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.component.BlockComponentProvider;
import nerdhub.cardinal.components.api.component.Component;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.container.Container;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;
import space.bbkr.endershulkers.inventory.EnderShulkerInventory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EnderShulkerBlock extends BlockWithEntity implements BlockComponentProvider {
	public static final EnumProperty<Direction> FACING = FacingBlock.FACING;
	
	public EnderShulkerBlock(Settings settings) {
		super(settings);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockView view) {
		return new EnderShulkerBlockEntity();
	}

	public boolean canSuffocate(BlockState state, BlockView view, BlockPos pos) {
		return true;
	}

	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world.isClient) {
			return ActionResult.SUCCESS;
		} else if (player.isSpectator()) {
			return ActionResult.SUCCESS;
		} else {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EnderShulkerBlockEntity) {
				EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity) be;
				Direction direction = state.get(FACING);
				boolean canOpen;
				if (shulker.getAnimationStage() == ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
					Box box = VoxelShapes.fullCube().getBoundingBox().stretch(0.5F * (float) direction.getOffsetX(), 0.5F * (float) direction.getOffsetY(), 0.5F * (float) direction.getOffsetZ()).shrink(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
					canOpen = world.doesNotCollide(box.offset(pos.offset(direction)));
				} else {
					canOpen = true;
				}

				if (canOpen) {
					EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(shulker.getOwnerId(), shulker.getColor()).setCurrentBlockEntity(shulker);
					player.openContainer(shulker);
					player.incrementStat(Stats.OPEN_SHULKER_BOX);
				}
				return ActionResult.SUCCESS;

			} else {
				return ActionResult.PASS;
			}
		}
	}

	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getSide());
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	public void onBlockRemoved(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof EnderShulkerBlockEntity) {
				world.updateHorizontalAdjacent(pos, state.getBlock());
			}

			super.onBlockRemoved(state, world, pos, newState, moved);
		}
	}

	@Environment(EnvType.CLIENT)
	public void buildTooltip(ItemStack stack, @Nullable BlockView view, List<Text> tooltip, TooltipContext options) {
		super.buildTooltip(stack, view, tooltip, options);
		if (stack.getItem() instanceof DyeableItem) {
			tooltip.add(new TranslatableText("tooltip.endershulkers.channel", getChannelColor(((DyeableItem)stack.getItem()).getColor(stack))).formatted(Formatting.GRAY));
		}
		CompoundTag beTag = stack.getOrCreateSubTag("BlockEntityTag");
		if (beTag.containsUuid("Owner")) {
			CompoundTag newTag = new CompoundTag();
			newTag.putString("Id", beTag.getUuid("Owner").toString());
			GameProfile profile = NbtHelper.toGameProfile(newTag);
			tooltip.add(new TranslatableText("tooltip.endershulkers.owner", profile.getName()).formatted(Formatting.GRAY));
		}
		if (beTag.getBoolean("Locked")) tooltip.add(new TranslatableText("tooltip.endershulkers.locked").formatted(Formatting.GRAY));
	}

	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.DESTROY;
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, EntityContext context) {
		BlockEntity blockEntity = view.getBlockEntity(pos);
		return blockEntity instanceof EnderShulkerBlockEntity ? VoxelShapes.cuboid(((EnderShulkerBlockEntity)blockEntity).getBoundingBox(state)) : VoxelShapes.fullCube();
	}

	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EnderShulkerBlockEntity) {
			EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
			EnderShulkerInventory inv = shulker.getInventory();
			if (inv != null) return Container.calculateComparatorOutput(inv.asInventory());
		}
		return 0;
	}

	@Override
	public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EnderShulkerBlockEntity) {
			EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity) be;
			if (!world.isClient && player.isCreative()) {
				ItemStack stack = new ItemStack(EnderShulkers.ENDER_SHULKER_ITEM);
				CompoundTag tag = stack.getOrCreateSubTag("BlockEntityTag");
				tag.putInt("Channel", shulker.getColor());
				if (shulker.getOwnerId() != null)  tag.putUuid("Owner", shulker.getOwnerId());

				if (shulker.hasCustomName()) {
					stack.setCustomName(shulker.getCustomName());
				}

				ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				itemEntity.setToDefaultPickupDelay();
				world.spawnEntity(itemEntity);
			} else {
//				shulker.checkLootInteraction(player); TODO: necessary for us?
			}
		}
		super.onBreak(world, pos, state, player);
	}

	public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (stack.hasCustomName()) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EnderShulkerBlockEntity) {
				EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
				if (shulker.getOwnerId() == null) {
					shulker.setCustomName(stack.getName());
				}
			}
		}

	}

	@Environment(EnvType.CLIENT)
	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
		ItemStack stack = super.getPickStack(world, pos, state);
		EnderShulkerBlockEntity be = (EnderShulkerBlockEntity)world.getBlockEntity(pos);
		CompoundTag tag = new CompoundTag();
		tag.putInt("Channel", be.getColor());
		if (be.getOwnerId() != null) tag.putUuid("Owner", be.getOwnerId());
		tag.putBoolean("Locked", be.isLocked());
		if (!tag.isEmpty()) {
			stack.putSubTag("BlockEntityTag", tag);
		}

		if (be.hasCustomName()) stack.setCustomName(be.getName());

		return stack;
	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public <T extends Component> boolean hasComponent(BlockView world, BlockPos pos, ComponentType<T> type, @Nullable Direction dir) {
		if (type == UniversalComponents.INVENTORY_COMPONENT) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EnderShulkerBlockEntity) {
				EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
				return !shulker.isLocked();
			}
		}
		return false;
	}

	@Nullable
	@Override
	@SuppressWarnings("unchecked")
	public <T extends Component> T getComponent(BlockView world, BlockPos pos, ComponentType<T> type, @Nullable Direction dir) {
		if (type == UniversalComponents.INVENTORY_COMPONENT) {
			BlockEntity be = world.getBlockEntity(pos);
			if (be instanceof EnderShulkerBlockEntity) {
				EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity) be;
				if (shulker.isLocked()) return null;
				int channel = shulker.getColor();
				return (T) EnderShulkers.ENDER_SHULKER_COMPONENT.get(be.getWorld().getLevelProperties()).getInventory(shulker.getOwnerId(), channel);
			}
		}
		return null;
	}

	@Override
	public Set<ComponentType<?>> getComponentTypes(BlockView world, BlockPos pos, @Nullable Direction dir) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EnderShulkerBlockEntity) {
			EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
			if (!shulker.isLocked()) return Collections.singleton(UniversalComponents.INVENTORY_COMPONENT);
		}
		return Collections.emptySet();
	}

	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		for(int i = 0; i < 3; ++i) {
			int j = random.nextInt(2) * 2 - 1;
			int k = random.nextInt(2) * 2 - 1;
			double d = (double)pos.getX() + 0.5D + 0.25D * (double)j;
			double e = (float)pos.getY() + random.nextFloat();
			double f = (double)pos.getZ() + 0.5D + 0.25D * (double)k;
			double g = random.nextFloat() * (float)j;
			double h = ((double)random.nextFloat() - 0.5D) * 0.125D;
			double l = random.nextFloat() * (float)k;
			world.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
		}

	}

	public static String getChannelColor(int color) {
		String original = Integer.toHexString(color);
		StringBuilder hex = new StringBuilder(original);
		if (hex.length() < 6) {
			for (int i = 0; i < 6 - original.length(); i++) {
				hex.insert(0, "0");
			}
		}
		return hex.toString().toUpperCase();
	}
}
