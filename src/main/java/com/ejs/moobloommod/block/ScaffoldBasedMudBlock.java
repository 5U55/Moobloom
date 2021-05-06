package com.ejs.moobloommod.block;

import java.util.Iterator;
import java.util.Random;

import com.ejs.moobloommod.registry.ModItems;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.StemBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
//test push
	public class ScaffoldBasedMudBlock extends Block implements Waterloggable {
	   private static final VoxelShape NORMAL_OUTLINE_SHAPE;
	   private static final VoxelShape BOTTOM_OUTLINE_SHAPE;
	   private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	   private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.fullCube().offset(0.0D, -1.0D, 0.0D);
	   public static final IntProperty DISTANCE;
	   public static final BooleanProperty WATERLOGGED;
	   public static final BooleanProperty BOTTOM;

	   public ScaffoldBasedMudBlock(AbstractBlock.Settings settings) {
	      super(settings);
	      this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(DISTANCE, 7)).with(WATERLOGGED, false)).with(BOTTOM, false));
	   }

	   protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
	      builder.add(DISTANCE, WATERLOGGED, BOTTOM);
	   }

	   public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
	      if (!context.isHolding(state.getBlock().asItem())) {
	         return (Boolean)state.get(BOTTOM) ? BOTTOM_OUTLINE_SHAPE : NORMAL_OUTLINE_SHAPE;
	      } else {
	         return VoxelShapes.fullCube();
	      }
	   }
	   
	   public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		      if (!world.isClient) {
		         if (entity instanceof LivingEntity) {
		            LivingEntity livingEntity = (LivingEntity)entity;
		               livingEntity.addStatusEffect(new StatusEffectInstance(ModItems.CAMO, 65));
		      }
		   }}

	   public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
	      return VoxelShapes.fullCube();
	   }

	   public boolean canReplace(BlockState state, ItemPlacementContext context) {
	      return context.getStack().getItem() == this.asItem();
	   } 

	   public BlockState getPlacementState(ItemPlacementContext ctx) {
	      BlockPos blockPos = ctx.getBlockPos();
	      World world = ctx.getWorld();
	      int i = calculateDistance(world, blockPos);
	      return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(WATERLOGGED, true)).with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(world, blockPos, i));
	   }

	   public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
	      if (!world.isClient) {
	         world.getBlockTickScheduler().schedule(pos, this, 1);
	      }

	   }

	   public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
	      if ((Boolean)state.get(WATERLOGGED)) {
	         world.getFluidTickScheduler().schedule(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
	      }

	      if (!world.isClient()) {
	         world.getBlockTickScheduler().schedule(pos, this, 1);
	      }
	      return state;
	   }

	   public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
	      int i = calculateDistance(world, pos);
	      BlockState blockState = (BlockState)((BlockState)state.with(DISTANCE, i)).with(BOTTOM, this.shouldBeBottom(world, pos, i));
	      if ((Integer)blockState.get(DISTANCE) == 7) {
	         if ((Integer)state.get(DISTANCE) == 7) {
	            world.spawnEntity(new FallingBlockEntity(world, (double)pos.getX() + 0.5D, (double)pos.getY(), (double)pos.getZ() + 0.5D, (BlockState)blockState.with(WATERLOGGED, false)));
	         } else {
	            world.breakBlock(pos, true);
	         }
	      } else if (state != blockState) {
	         world.setBlockState(pos, blockState, 3);
	      }
	      if (!state.canPlaceAt(world, pos)) {
		         setToDirt(state, world, pos);
		      }
	   }

	   public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
	      return calculateDistance(world, pos) < 7;
	   }

	   public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
	      if (context.isAbove(VoxelShapes.fullCube(), pos, true) && !context.isDescending()) {
	         return NORMAL_OUTLINE_SHAPE;
	      } else {
	         return (Integer)state.get(DISTANCE) != 0 && (Boolean)state.get(BOTTOM) && context.isAbove(OUTLINE_SHAPE, pos, true) ? COLLISION_SHAPE : VoxelShapes.empty();
	      }
	   }

	   public FluidState getFluidState(BlockState state) {
	      return (Boolean)state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	   }

	   private boolean shouldBeBottom(BlockView world, BlockPos pos, int distance) {
	      return distance > 0 && !world.getBlockState(pos.down()).isOf(this);
	   }

	   public static int calculateDistance(BlockView world, BlockPos pos) {
	      BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.DOWN);
	      BlockState blockState = world.getBlockState(mutable);
	      int i = 7;
	      if (blockState.isOf(ModItems.MUD)) {
	         i = (Integer)blockState.get(DISTANCE);
	      } else if (blockState.isSideSolidFullSquare(world, mutable, Direction.UP)) {
	         return 0;
	      }

	      Iterator<Direction> var5 = Direction.Type.HORIZONTAL.iterator();

	      while(var5.hasNext()) {
	         Direction direction = (Direction)var5.next();
	         BlockState blockState2 = world.getBlockState(mutable.set(pos, direction));
	         if (blockState2.isOf(ModItems.MUD)) {
	            i = Math.min(i, (Integer)blockState2.get(DISTANCE) + 1);
	            if (i == 1) {
	               break;
	            }
	         }
	      }

	      return i;
	   }

		   public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		      if (!isWaterNearby(world, pos) && !world.hasRain(pos.up())) {
		            setToDirt(state, world, pos);
		         }
		   }

		   public void onLandedUpon(World world, BlockPos pos, Entity entity, float distance) {
		      if (!world.isClient && world.random.nextFloat() < distance - 0.5F && entity instanceof LivingEntity && (entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512F) {
		         setToDirt(world.getBlockState(pos), world, pos);
		      }

		      super.onLandedUpon(world, pos, entity, distance);
		   }

		   public static void setToDirt(BlockState state, World world, BlockPos pos) {
		      world.setBlockState(pos, pushEntitiesUpBeforeBlockChange(state, Blocks.DIRT.getDefaultState(), world, pos));
		   }

		   private static boolean isWaterNearby(WorldView world, BlockPos pos) {
		      Iterator<BlockPos> var2 = BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4)).iterator();

		      BlockPos blockPos;
		      do {
		         if (!var2.hasNext()) {
		            return false;
		         }

		         blockPos = (BlockPos)var2.next();
		      } while(!world.getFluidState(blockPos).isIn(FluidTags.WATER));

		      return true;
		   }


	   static {
	      DISTANCE = Properties.DISTANCE_0_7;
	      WATERLOGGED = Properties.WATERLOGGED;
	      BOTTOM = Properties.BOTTOM;
	      VoxelShape voxelShape = Block.createCuboidShape(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	      VoxelShape voxelShape2 = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 16.0D, 2.0D);
	      VoxelShape voxelShape3 = Block.createCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 16.0D, 2.0D);
	      VoxelShape voxelShape4 = Block.createCuboidShape(0.0D, 0.0D, 14.0D, 2.0D, 16.0D, 16.0D);
	      VoxelShape voxelShape5 = Block.createCuboidShape(14.0D, 0.0D, 14.0D, 16.0D, 16.0D, 16.0D);
	      NORMAL_OUTLINE_SHAPE = VoxelShapes.union(voxelShape, voxelShape2, voxelShape3, voxelShape4, voxelShape5);
	      VoxelShape voxelShape6 = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 2.0D, 16.0D);
	      VoxelShape voxelShape7 = Block.createCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	      VoxelShape voxelShape8 = Block.createCuboidShape(0.0D, 0.0D, 14.0D, 16.0D, 2.0D, 16.0D);
	      VoxelShape voxelShape9 = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 2.0D);
	      BOTTOM_OUTLINE_SHAPE = VoxelShapes.union(COLLISION_SHAPE, NORMAL_OUTLINE_SHAPE, voxelShape7, voxelShape6, voxelShape9, voxelShape8);
	   }
	}
