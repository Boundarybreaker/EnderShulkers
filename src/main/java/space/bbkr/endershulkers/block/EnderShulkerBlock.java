package space.bbkr.endershulkers.block;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.container.Container;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.LiteralText;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.block.entity.EnderShulkerBlockEntity;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class EnderShulkerBlock extends BlockWithEntity implements InventoryProvider {
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
				Direction direction = state.get(FACING);
				EnderShulkerBlockEntity shulker = (EnderShulkerBlockEntity)be;
				boolean canOpen;
				if (shulker.getAnimationStage() == ShulkerBoxBlockEntity.AnimationStage.CLOSED) {
					Box box = VoxelShapes.fullCube().getBoundingBox().stretch(0.5F * (float)direction.getOffsetX(), 0.5F * (float)direction.getOffsetY(), 0.5F * (float)direction.getOffsetZ()).shrink(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ());
					canOpen = world.doesNotCollide(box.offset(pos.offset(direction)));
				} else {
					canOpen = true;
				}

				if (canOpen) {
					EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(shulker.getChannel()).setCurrentBlockEntity(shulker);
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
	//TODO: fix
	public void buildTooltip(ItemStack stack, @Nullable BlockView view, List<Text> tooltip, TooltipContext options) {
		super.buildTooltip(stack, view, tooltip, options);
		CompoundTag compoundTag = stack.getSubTag("BlockEntityTag");
		if (compoundTag != null) {
			if (compoundTag.contains("LootTable", 8)) {
				tooltip.add(new LiteralText("???????"));
			}

			if (compoundTag.contains("Items", 9)) {
				DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
				Inventories.fromTag(compoundTag, defaultedList);
				int i = 0;
				int j = 0;
				Iterator var9 = defaultedList.iterator();

				while(var9.hasNext()) {
					ItemStack itemStack = (ItemStack)var9.next();
					if (!itemStack.isEmpty()) {
						++j;
						if (i <= 4) {
							++i;
							Text text = itemStack.getName().deepCopy();
							text.append(" x").append(String.valueOf(itemStack.getCount()));
							tooltip.add(text);
						}
					}
				}

				if (j - i > 0) {
					tooltip.add((new TranslatableText("container.shulkerBox.more", j - i)).formatted(Formatting.ITALIC));
				}
			}
		}

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
		return Container.calculateComparatorOutput((Inventory)world.getBlockEntity(pos));
	}

	//TODO
//	@Environment(EnvType.CLIENT)
//	public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
//		ItemStack itemStack = super.getPickStack(world, pos, state);
//		EnderShulkerBlockEntity shulkerBoxBlockEntity = (EnderShulkerBlockEntity)world.getBlockEntity(pos);
//		CompoundTag compoundTag = shulkerBoxBlockEntity.serializeInventory(new CompoundTag());
//		if (!compoundTag.isEmpty()) {
//			itemStack.putSubTag("BlockEntityTag", compoundTag);
//		}
//
//		return itemStack;
//	}

	public BlockState rotate(BlockState state, BlockRotation rotation) {
		return state.with(FACING, rotation.rotate(state.get(FACING)));
	}

	public BlockState mirror(BlockState state, BlockMirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING)));
	}

	@Override
	public SidedInventory getInventory(BlockState state, IWorld world, BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if (be instanceof EnderShulkerBlockEntity) {
			int channel = ((EnderShulkerBlockEntity)be).getChannel();
			return EnderShulkers.ENDER_SHULKER_COMPONENT.get(world.getLevelProperties()).getInventory(channel);
		}
		return null;
	}
}
