package space.bbkr.endershulkers.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.InventoryProvider;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.inventory.EnderShulkerInventory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EnderShulkerComponent implements LevelSyncedComponent {
	public static final EnderShulkerComponent INSTANCE = new EnderShulkerComponent();

	private Int2ObjectMap<EnderShulkerInventory> channels = new Int2ObjectOpenHashMap<>();

	private Map<UUID, Int2ObjectMap<EnderShulkerInventory>> privateNetworks = new HashMap<>();

	private EnderShulkerComponent() { }

	@Override
	public ComponentType<?> getComponentType() {
		return EnderShulkers.ENDER_SHULKER_COMPONENT;
	}

	public EnderShulkerInventory getInventory(@Nullable UUID id, int color) {
		if (id == null) {
			if (!channels.containsKey(color)) {
				channels.put(color, new EnderShulkerInventory());
			}
			return channels.get(color);
		}
		if (!privateNetworks.containsKey(id)) {
			privateNetworks.put(id, new Int2ObjectOpenHashMap<>());
		}
		Int2ObjectMap<EnderShulkerInventory> map = privateNetworks.get(id);
		if (!map.containsKey(color)) {
			map.put(color, new EnderShulkerInventory());
		}
		return map.get(color);
	}

	@Override
	public void fromTag(CompoundTag tag) {
		channels.clear();
		privateNetworks.clear();
		CompoundTag channelTag = tag.getCompound("Channels");
		for (String key : channelTag.getKeys()) {
			CompoundTag invTag = channelTag.getCompound(key);
			EnderShulkerInventory inv = new EnderShulkerInventory();
			inv.fromTag(invTag);
			channels.put(Integer.parseInt(key), inv);
		}
		CompoundTag privateTag = tag.getCompound("PrivateChannels");
		for (String key : privateTag.getKeys()) {
			Int2ObjectMap<EnderShulkerInventory> map = new Int2ObjectOpenHashMap<>();
			UUID id = UUID.fromString(key);
			CompoundTag innerTag = privateTag.getCompound(key);
			for (String channel : innerTag.getKeys()) {
				CompoundTag invTag = innerTag.getCompound(key);
				EnderShulkerInventory inv = new EnderShulkerInventory();
				inv.fromTag(invTag);
				map.put(Integer.parseInt(channel), inv);
			}
			privateNetworks.put(id, map);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		CompoundTag channelTag = new CompoundTag();
		for (int key : channels.keySet()) {
			channelTag.put(String.valueOf(key), channels.get(key).toTag(new CompoundTag()));
		}
		tag.put("Channels", channelTag);
		CompoundTag privateTag = new CompoundTag();
		for (UUID id : privateNetworks.keySet()) {
			Int2ObjectMap<EnderShulkerInventory> map = privateNetworks.get(id);
			CompoundTag innerTag = new CompoundTag();
			for (int key : map.keySet()) {
				innerTag.put(String.valueOf(key), map.get(key).toTag(new CompoundTag()));
			}
		}
		tag.put("PrivateChannels", privateTag);
		return tag;
	}
}
