package com.ejs.moobloommod.renderer;

import java.util.Map;

import com.ejs.moobloommod.entities.MoobloomEntity;
import com.google.common.collect.Maps;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class MoobEntityRenderer extends MobEntityRenderer<MoobloomEntity, CowEntityModel<MoobloomEntity>> {
	private static final Map<MoobloomEntity.Type, Identifier> TEXTURES = Util.make(Maps.newHashMap(), (hashMap) -> {
		hashMap.put(MoobloomEntity.Type.YELLOW, new Identifier("moobloommod", "textures/entity/cow/yellow_moobloom.png"));
		hashMap.put(MoobloomEntity.Type.BLUE, new Identifier("moobloommod", "textures/entity/cow/blue_moobloom.png"));
		hashMap.put(MoobloomEntity.Type.PINK, new Identifier("moobloommod", "textures/entity/cow/pink_moobloom.png"));
	});

	public MoobEntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher, new CowEntityModel<>(), 0.5F);
		this.addFeature(new MoobloomFlowerRender(this));
	}

	public Identifier getTexture(MoobloomEntity entity) {
		return TEXTURES.get(entity.getMoobType());
	}
}
