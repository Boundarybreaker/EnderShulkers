package space.bbkr.endershulkers.mixin;

import com.mojang.authlib.GameProfileRepository;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
	@Accessor
	GameProfileRepository getGameProfileRepo();
}
