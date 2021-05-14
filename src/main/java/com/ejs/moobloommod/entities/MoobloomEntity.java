package com.ejs.moobloommod.entities;

import java.util.Random;

import com.ejs.moobloommod.MobloomMod;
import com.ejs.moobloommod.registry.ModItems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Shearable;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class MoobloomEntity extends CowEntity implements Shearable {
	private static final TrackedData<String> TYPE;

	public boolean MoobCanSpawn = true;

	public MoobloomEntity(EntityType<? extends MoobloomEntity> entityType, World world) {
		super(entityType, world);
	}
	
	public float getPathfindingFavor(BlockPos pos, WorldView world) {
		      return world.getBlockState(pos.down()).isOf(Blocks.DIRT) ? 10.0F : world.getBrightness(pos) - 0.5F;
	}

	@Override
	public boolean canSpawn(WorldView view) {
		BlockPos blockunderentity = new BlockPos(this.getX(), this.getY() - 1, this.getZ());
		BlockPos posentity = new BlockPos(this.getX(), this.getY(), this.getZ());
		return view.intersectsEntities(this) && !world.containsFluid(this.getBoundingBox())
				&& this.world.getBlockState(posentity).getBlock().canMobSpawnInside() && this.world
						.getBlockState(blockunderentity).allowsSpawning(view, blockunderentity, MobloomMod.MOOBLOOM)
				&& MoobCanSpawn;
	}

	protected void initDataTracker() {
		super.initDataTracker();
		Random rn = new Random();
		int random = rn.nextInt(3) + 1;
		if (random == 1) {
			this.dataTracker.startTracking(TYPE, MoobloomEntity.Type.BLUE.name);
		} else if(random == 2){
			this.dataTracker.startTracking(TYPE, MoobloomEntity.Type.YELLOW.name);
		} else {
			this.dataTracker.startTracking(TYPE, MoobloomEntity.Type.PINK.name);
		}
	}

	public void tickMovement() {
		super.tickMovement();
		if (!this.world.isClient) {
			int i = MathHelper.floor(this.getX());
			int j = MathHelper.floor(this.getY());
			int k = MathHelper.floor(this.getZ());
			if (this.world.getBiome(new BlockPos(i, 0, k)).getTemperature(new BlockPos(i, j, k)) > 1.0F) {
				this.damage(DamageSource.ON_FIRE, 1.0F);
			}

			if (!this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) {
				return;
			}

			Random rn = new Random();
			int random = rn.nextInt(4) + 1;

			BlockState blockState = this.getMoobType().getMushroomState();
			if(this.getMoobType() == MoobloomEntity.Type.YELLOW) {
				blockState = Blocks.DANDELION.getDefaultState();
			}

			if (random == 1) {
				blockState = Blocks.OXEYE_DAISY.getDefaultState();
			} else if (random == 2) {
				blockState = Blocks.AZURE_BLUET.getDefaultState();
			}

			for (int l = 0; l < 4; ++l) {
				i = MathHelper.floor(this.getX() + (double) ((float) (l % 2 * 2 - 1) * 0.25F));
				j = MathHelper.floor(this.getY());
				k = MathHelper.floor(this.getZ() + (double) ((float) (l / 2 % 2 * 2 - 1) * 0.25F));
				BlockPos blockPos = new BlockPos(i, j, k);
				if (this.world.getBlockState(blockPos).isAir() && blockState.canPlaceAt(this.world, blockPos)) {
					this.world.setBlockState(blockPos, blockState);
				}
			}
		}

	}

	protected void initGoals() {
		this.goalSelector.add(0, new SwimGoal(this));
		this.goalSelector.add(1, new EscapeDangerGoal(this, 2.0D));
		this.goalSelector.add(2, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(3, new TemptGoal(this, 1.25D, Ingredient.fromTag(ItemTags.FLOWERS), false));
		this.goalSelector.add(4, new FollowParentGoal(this, 1.25D));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		this.goalSelector.add(7, new LookAroundGoal(this));

	}

	public static DefaultAttributeContainer.Builder createMoobAttributes() {
		return MobEntity.createMobAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1D);
	}

	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_COW_AMBIENT;
	}

	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.ENTITY_COW_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_COW_DEATH;
	}

	protected void playStepSound(BlockPos pos, BlockState state) {
		this.playSound(SoundEvents.ENTITY_COW_STEP, 0.15F, 1.0F);
	}

	protected float getSoundVolume() {
		return 0.4F;
	}

	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		ItemStack itemStack = player.getStackInHand(hand);
		if (itemStack.getItem() == Items.BUCKET && !this.isBaby()) {
			player.playSound(SoundEvents.ENTITY_COW_MILK, 1.0F, 1.0F);
			ItemStack itemStack2 = ItemUsage.method_30012(itemStack, player, Items.MILK_BUCKET.getDefaultStack());
			player.setStackInHand(hand, itemStack2);
			return ActionResult.success(this.world.isClient);
		} else {
			return super.interactMob(player, hand);
		}
	}

	@Override
	public void sheared(SoundCategory shearedSoundCategory) {
		this.world.playSoundFromEntity((PlayerEntity) null, this, SoundEvents.ENTITY_MOOSHROOM_SHEAR,
				shearedSoundCategory, 1.0F, 1.0F);
		if (!this.world.isClient()) {
			((ServerWorld) this.world).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getBodyY(0.5D),
					this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
			this.remove();
			CowEntity cowEntity = (CowEntity) EntityType.COW.create(this.world);
			cowEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.yaw, this.pitch);
			cowEntity.setHealth(this.getHealth());
			cowEntity.bodyYaw = this.bodyYaw;
			if (this.hasCustomName()) {
				cowEntity.setCustomName(this.getCustomName());
				cowEntity.setCustomNameVisible(this.isCustomNameVisible());
			}

			if (this.isPersistent()) {
				cowEntity.setPersistent();
			}

			cowEntity.setInvulnerable(this.isInvulnerable());
			this.world.spawnEntity(cowEntity);

			for (int i = 0; i < 5; ++i) {
				this.world.spawnEntity(new ItemEntity(this.world, this.getX(), this.getBodyY(1.0D), this.getZ(),
						new ItemStack(this.getMoobType().flower.getBlock())));
			}
		}
	}

	@Override
	public boolean isShearable() {
		return this.isAlive() && !this.isBaby();
	}

	private void setType(MoobloomEntity.Type type) {
		this.dataTracker.set(TYPE, type.name);
	}

	public MoobloomEntity.Type getMoobType() {
		return MoobloomEntity.Type.fromName((String) this.dataTracker.get(TYPE));
	}

	public MoobloomEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
		MoobloomEntity moobEntity = (MoobloomEntity) MobloomMod.MOOBLOOM.create(serverWorld);
		moobEntity.setType(this.chooseBabyType((MoobloomEntity) passiveEntity));
		return moobEntity;
	}

	private MoobloomEntity.Type chooseBabyType(MoobloomEntity passiveEntity) {
		MoobloomEntity.Type type = this.getMoobType();
		MoobloomEntity.Type type2 = passiveEntity.getMoobType();
		MoobloomEntity.Type type4;
		if (type == type2) {
			type4 = type;
		} else {
			type4 = this.random.nextBoolean() ? type : type2;
		}

		return type4;
	}

	static {
		TYPE = DataTracker.registerData(MoobloomEntity.class, TrackedDataHandlerRegistry.STRING);
	}

	public static enum Type {
		YELLOW("yellow", ModItems.BUTTERCUP.getDefaultState()), BLUE("blue", Blocks.CORNFLOWER.getDefaultState()),
		PINK("pink", Blocks.ALLIUM.getDefaultState());

		private final String name;
		private final BlockState flower;

		private Type(String name, BlockState flower) {
			this.name = name;
			this.flower = flower;
		}

		@Environment(EnvType.CLIENT)
		public BlockState getMushroomState() {
			return this.flower;
		}

		private static MoobloomEntity.Type fromName(String name) {
			if(MoobloomEntity.Type.YELLOW.name == name) {
				return YELLOW;
			} if(MoobloomEntity.Type.BLUE.name == name) {
				return BLUE;
			} if(MoobloomEntity.Type.PINK.name == name) {
				return PINK;
			}
			return YELLOW;
		}
	}
}
