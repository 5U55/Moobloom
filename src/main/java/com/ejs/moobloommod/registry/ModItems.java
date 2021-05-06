package com.ejs.moobloommod.registry;

import com.ejs.moobloommod.MobloomMod;
import com.ejs.moobloommod.block.ScaffoldBasedMudBlock;
import com.ejs.moobloommod.effect.CamoStatusEffect;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.Material;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ModItems {
	public static final Item MOOBEGG = new SpawnEggItem(MobloomMod.MOOBLOOM, 0xFFE252, 0xFFFFFF, new Item.Settings().group(ItemGroup.MISC));
	public static final FlowerBlock BUTTERCUP = new FlowerBlock(StatusEffect.byRawId(13), 200, FabricBlockSettings.of(Material.PLANT).strength(0.0f).breakInstantly());
	public static final ScaffoldBasedMudBlock MUD = new ScaffoldBasedMudBlock(FabricBlockSettings.of(Material.SNOW_BLOCK).strength(2.0f).slipperiness(0.7f));
	public static final BlockItem MUDITEM = new BlockItem(MUD, new Item.Settings().group(ItemGroup.MATERIALS));
	public static final StatusEffect CAMO = new CamoStatusEffect();
	
	public static void registerItems() {
		Registry.register(Registry.ITEM, new Identifier(MobloomMod.MOD_ID, "moobloom_spawn_egg"), MOOBEGG);
		Registry.register(Registry.STATUS_EFFECT, new Identifier(MobloomMod.MOD_ID, "camoflauge"), CAMO);
		Registry.register(Registry.BLOCK, new Identifier(MobloomMod.MOD_ID, "buttercup"), BUTTERCUP);
		Registry.register(Registry.BLOCK, new Identifier(MobloomMod.MOD_ID, "mud"), MUD);
		Registry.register(Registry.ITEM, new Identifier(MobloomMod.MOD_ID, "mud"), MUDITEM);
	}
}
