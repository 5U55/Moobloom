package com.ejs.moobloommod;

import com.ejs.moobloommod.entities.MoobloomEntity;
import com.ejs.moobloommod.registry.ModItems;
import com.ejs.moobloommod.renderer.MoobEntityRenderer;
import com.google.common.collect.ImmutableList;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.UniformIntDistribution;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.DiskFeatureConfig;
import net.minecraft.world.gen.feature.Feature;

public class MobloomMod implements ModInitializer {
	public static final String MOD_ID = "moobloommod";
	public static final Identifier SET_BLOCK_PACKET = new Identifier("moobloommod", "setblock");
	public static final EntityType<MoobloomEntity> MOOBLOOM = Registry.register(Registry.ENTITY_TYPE,
			new Identifier("moobloommod", "moobloom"),
			FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, MoobloomEntity::new)
					.dimensions(EntityDimensions.fixed(2.0f, 1.0f)).build());
	
	public static final ConfiguredFeature<?, ?> DISK_MUD = (ConfiguredFeature<?, ?>)Feature.DISK.configure(new DiskFeatureConfig(ModItems.MUD.getDefaultState(), UniformIntDistribution.of(2, 4), 2, ImmutableList.of(Blocks.DIRT.getDefaultState(), Blocks.GRASS_BLOCK.getDefaultState()))).decorate(ConfiguredFeatures.Decorators.SQUARE_TOP_SOLID_HEIGHTMAP).repeat(3);
	
	@Override
	public void onInitialize() {
		ModItems.registerItems();
		FabricDefaultAttributeRegistry.register(MOOBLOOM, MoobloomEntity.createMoobAttributes());
		EntityRendererRegistry.INSTANCE.register(MobloomMod.MOOBLOOM, (dispatcher, context) -> {
			return new MoobEntityRenderer(dispatcher);
		});
		
		ServerPlayNetworking.registerGlobalReceiver(SET_BLOCK_PACKET, (server, player, handler, buf, sender) ->{
			BlockPos pos = buf.readBlockPos();
			Block blockToSet = Registry.BLOCK.get(buf.readIdentifier());
			
			server.execute(() ->{
				player.getServerWorld().setBlockState(pos, blockToSet.getDefaultState());
			});
		});
		Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier("moobloommod", "mud_disk_feature"), DISK_MUD);
	}
}