package space.bbkr.endershulkers.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
			ListTag list = channelTag.getList(key, NbtType.COMPOUND);
			EnderShulkerInventory inv = new EnderShulkerInventory();
			inv.readTags(list);
			channels.put(Integer.parseInt(key), inv);
		}
		CompoundTag privateTag = tag.getCompound("PrivateChannels");
		for (String key : privateTag.getKeys()) {
			Int2ObjectMap<EnderShulkerInventory> map = new Int2ObjectOpenHashMap<>();
			UUID id = UUID.fromString(key);
			CompoundTag innerTag = privateTag.getCompound(key);
			for (String channel : innerTag.getKeys()) {
				ListTag list = innerTag.getList(channel, NbtType.COMPOUND);
				EnderShulkerInventory inv = new EnderShulkerInventory();
				inv.readTags(list);
				map.put(Integer.parseInt(channel), inv);
			}
			privateNetworks.put(id, map);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		CompoundTag channelTag = new CompoundTag();
		for (int key : channels.keySet()) {
			channelTag.put(String.valueOf(key), channels.get(key).getTags());
		}
		tag.put("Channels", channelTag);
		CompoundTag privateTag = new CompoundTag();
		for (UUID id : privateNetworks.keySet()) {
			Int2ObjectMap<EnderShulkerInventory> map = privateNetworks.get(id);
			CompoundTag innerTag = new CompoundTag();
			for (int key : map.keySet()) {
				innerTag.put(String.valueOf(key), map.get(key).getTags());
			}
		}
		tag.put("PrivateChannels", privateTag);
		return tag;
	}
}
