package orespawn.mixin.client;

import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import orespawn.client.original.OriginalPack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

/** Gives the client's resource pack list OreSpawn's textures and sounds from the original 1.7.10 file ({@link OriginalPack}). */
@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
	@Shadow
	@Final
	@Mutable
	private Set<RepositorySource> sources;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void orespawn$addOriginalPack(RepositorySource[] given, CallbackInfo ci) {
		if (this.sources.stream().noneMatch(s -> s instanceof ClientPackSource)) return;
		this.sources = new LinkedHashSet<>(this.sources);
		this.sources.add(OriginalPack.source());
	}
}
