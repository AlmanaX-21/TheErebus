package erebus.world.layer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import erebus.registries.world.ModBiomeLayerTypes;
import erebus.world.layer.area.Area;
import erebus.world.layer.area.LazyArea;
import erebus.world.layer.biome.BiomeLayerFactory;
import erebus.world.layer.biome.BiomeLayerStack;
import erebus.world.layer.biome.BiomeLayerType;
import erebus.world.layer.context.LazyAreaContext;
import erebus.world.layer.context.RandomContext;
import erebus.world.layer.trait.AreaTransformer1;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.function.LongFunction;

public record SurroundedSubBiomeLayer(ResourceKey<Biome> biome, ResourceKey<Biome> subBiome, int chance) implements AreaTransformer1 {
    @Override
    public ResourceKey<Biome> applyPixel(RandomContext randomContext, Area layer, int x, int z) {
        ResourceKey<Biome> center = layer.getBiome(x, z);
        if (center != biome) {
            return center;
        }

        for (int xo = -1; xo <= 1; xo++) {
            for (int zo = -1; zo <= 1; zo++) {
                if ((xo != 0 || zo != 0) && layer.getBiome(x + xo, z + zo) != biome) {
                    return center;
                }
            }
        }

        return randomContext.nextRandom(chance) == 0 ? subBiome : center;
    }

    @Override
    public int getParentX(int x) {
        return x;
    }

    @Override
    public int getParentZ(int z) {
        return z;
    }

    public static final class Factory implements BiomeLayerFactory {
        public static final MapCodec<Factory> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.LONG.fieldOf("salt").forGetter(Factory::salt),
                ResourceKey.codec(Registries.BIOME).fieldOf("biome").forGetter(Factory::biome),
                ResourceKey.codec(Registries.BIOME).fieldOf("sub_biome").forGetter(Factory::subBiome),
                Codec.INT.fieldOf("chance").forGetter(Factory::chance),
                BiomeLayerStack.HOLDER_CODEC.fieldOf("parent").forGetter(Factory::parent)
        ).apply(instance, Factory::new));
        private final long salt;
        private final ResourceKey<Biome> biome;
        private final ResourceKey<Biome> subBiome;
        private final int chance;
        private final Holder<BiomeLayerFactory> parent;
        private final SurroundedSubBiomeLayer instance;

        public Factory(long salt, ResourceKey<Biome> biome, ResourceKey<Biome> subBiome, int chance, Holder<BiomeLayerFactory> parent) {
            this.salt = salt;
            this.biome = biome;
            this.subBiome = subBiome;
            this.chance = chance;
            this.parent = parent;
            this.instance = new SurroundedSubBiomeLayer(biome, subBiome, chance);
        }

        @Override
        public LazyArea build(LongFunction<LazyAreaContext> contextFactory) {
            return instance.run(contextFactory.apply(salt), parent.value().build(contextFactory));
        }

        @Override
        public BiomeLayerType getType() {
            return ModBiomeLayerTypes.SURROUNDED_SUB_BIOMES.get();
        }

        public long salt() {
            return salt;
        }

        public ResourceKey<Biome> biome() {
            return biome;
        }

        public ResourceKey<Biome> subBiome() {
            return subBiome;
        }

        public int chance() {
            return chance;
        }

        public Holder<BiomeLayerFactory> parent() {
            return parent;
        }
    }
}
