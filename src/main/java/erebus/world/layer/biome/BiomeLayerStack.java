package erebus.world.layer.biome;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import erebus.Erebus;
import erebus.datagen.ModRegistries;
import erebus.registries.world.ModBiomeLayerTypes;
import erebus.registries.world.ModBiomes;
import erebus.world.biome.util.TerrainBuilder;
import erebus.world.layer.RandomBiomeLayer;
import erebus.world.layer.SurroundedSubBiomeLayer;
import erebus.world.layer.ZoomLayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class BiomeLayerStack {
    public static final Codec<BiomeLayerFactory> DISPATCH_CODEC = ModBiomeLayerTypes.CODEC.dispatch("layer_type", BiomeLayerFactory::getType, BiomeLayerType::getCodec);
    public static final Codec<Holder<BiomeLayerFactory>> HOLDER_CODEC = RegistryFileCodec.create(ModRegistries.BIOME_STACK, DISPATCH_CODEC, true);

    public static final ResourceKey<BiomeLayerFactory> RANDOM_EREBUS_BIOMES = registerKey(ModRegistries.BIOME_STACK, "random_erebus_biomes");
    public static final ResourceKey<BiomeDensitySource> BIOME_GRID = registerKey(ModRegistries.BIOME_TERRAIN_DATA, "biome_grid");

    public static <T> ResourceKey<T> registerKey(ResourceKey<Registry<T>> registry, String name) {
        return ResourceKey.create(registry, Erebus.prefix(name));
    }

    public static void bootstrap(BootstrapContext<BiomeLayerFactory> context) {
        ImmutableList.Builder<ResourceKey<Biome>> weightedBiomes = ImmutableList.builder();
        addWeightedBiome(weightedBiomes, ModBiomes.UNDERGROUND_JUNGLE.getResourceKey(), 22);
        addWeightedBiome(weightedBiomes, ModBiomes.VOLCANIC_DESERT.getResourceKey(), 16);
        addWeightedBiome(weightedBiomes, ModBiomes.SUBTERRANEAN_SAVANNAH.getResourceKey(), 20);
        addWeightedBiome(weightedBiomes, ModBiomes.ELYSIAN_FIELDS.getResourceKey(), 20);
        addWeightedBiome(weightedBiomes, ModBiomes.ULTERIOR_OUTBACK.getResourceKey(), 15);
        addWeightedBiome(weightedBiomes, ModBiomes.FUNGAL_FOREST.getResourceKey(), 12);
        addWeightedBiome(weightedBiomes, ModBiomes.SUBMERGED_SWAMP.getResourceKey(), 20);
        addWeightedBiome(weightedBiomes, ModBiomes.PETRIFIED_FOREST.getResourceKey(), 15);

        BiomeLayerFactory biomes = new RandomBiomeLayer.Factory(100L, 1, weightedBiomes.build(), ImmutableList.of());
        biomes = new ZoomLayer.Factory(2000L, false, Holder.direct(biomes));
        biomes = new SurroundedSubBiomeLayer.Factory(
                101L,
                ModBiomes.ELYSIAN_FIELDS.getResourceKey(),
                ModBiomes.ELYSIAN_FOREST.getResourceKey(),
                10,
                Holder.direct(biomes)
        );
        biomes = new ZoomLayer.Factory(2100L, false, Holder.direct(biomes));
        biomes = new ZoomLayer.Factory(2101L, false, Holder.direct(biomes));
        biomes = new ZoomLayer.Factory(2102L, false, Holder.direct(biomes));
        biomes = new ZoomLayer.Factory(2103L, false, Holder.direct(biomes));
        biomes = new ZoomLayer.Factory(2104L, false, Holder.direct(biomes));

        context.register(RANDOM_EREBUS_BIOMES, biomes);
    }

    private static void addWeightedBiome(ImmutableList.Builder<ResourceKey<Biome>> builder, ResourceKey<Biome> biome, int weight) {
        for (int i = 0; i < weight; i++) {
            builder.add(biome);
        }
    }

    public static void bootstrapData(BootstrapContext<BiomeDensitySource> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);

        context.register(BIOME_GRID, new BiomeDensitySource(
                TerrainBuilder.getBiomeColumns(biomeRegistry),
                context.lookup(ModRegistries.BIOME_STACK).getOrThrow(BiomeLayerStack.RANDOM_EREBUS_BIOMES)
        ));
    }
}
