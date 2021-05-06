package com.ejs.moobloommod.effect;

import com.ejs.moobloommod.renderer.MoobloomFlowerRender;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.entity.player.PlayerEntity;

public class CamoStatusEffect extends StatusEffect {
	 public CamoStatusEffect() {
		    super(
		      StatusEffectType.BENEFICIAL, 
		      0x98D982);
		  }
	 @Override
	 public boolean canApplyUpdateEffect(int duration, int amplifier) {
		 return true;
	 }
		 
		  @Override
		  public void applyUpdateEffect(LivingEntity entity, int amplifier) {
		    if (entity instanceof PlayerEntity) {
		      ((PlayerEntity) entity).setInvisible(true);
		    }
		  }
}
