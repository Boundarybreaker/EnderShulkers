package space.bbkr.endershulkers.component;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.LevelSyncedComponent;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import space.bbkr.endershulkers.EnderShulkers;
import space.bbkr.endershulkers.inventory.EnderShulkerInventory;

public class EnderShulkerComponent implements LevelSyncedComponent {
	public static final EnderShulkerComponent INSTANCE = new EnderShulkerComponent();

	private Int2ObjectMap<EnderShulkerInventory> channels = new Int2ObjectOpenHashMap<>();

	private EnderShulkerComponent() { }

	@Override
	public ComponentType<?> getComponentType() {
		return EnderShulkers.ENDER_SHULKER_COMPONENT;
	}

	public EnderShulkerInventory getInventory(int color) {
		if (!channels.containsKey(color)) {
			channels.put(color, new EnderShulkerInventory());
		}
		return channels.get(color);
	}

	@Override
	public void fromTag(CompoundTag tag) {
		channels.clear();
		CompoundTag channelTag = tag.getCompound("Channels");
		for (String key : channelTag.getKeys()) {
			ListTag list = channelTag.getList(key, NbtType.COMPOUND);
			EnderShulkerInventory inv = new EnderShulkerInventory();
			inv.readTags(list);
			channels.put(Integer.parseInt(key), inv);
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		CompoundTag channelTag = new CompoundTag();
		for (int key : channels.keySet()) {
			channelTag.put(String.valueOf(key), channels.get(key).getTags());
		}
		tag.put("Channels", channelTag);
		return tag;
	}
}
