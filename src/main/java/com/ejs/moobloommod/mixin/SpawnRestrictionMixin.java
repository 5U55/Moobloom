package com.ejs.moobloommod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import com.ejs.moobloommod.entities.MoobloomEntity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.world.Heightmap;

@Mixin(SpawnRestriction.class)
public interface SpawnRestrictionMixin {
    @Invoker("register")
    static <T extends MoobloomEntity> void register(
            EntityType<T> type,
            SpawnRestriction.Location location,
            Heightmap.Type heightmapType,
            SpawnRestriction.SpawnPredicate<T> predicate
    ) throws IllegalAccessException {
        throw new IllegalAccessException();
    }
}