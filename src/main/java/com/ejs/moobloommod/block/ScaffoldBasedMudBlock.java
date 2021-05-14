package com.ejs.moobloommod.block;

import java.util.Iterator;
import java.util.Random;

import com.ejs.moobloommod.MobloomMod;
import com.ejs.moobloommod.registry.ModItems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ScaffoldBasedMudBlock extends Block {
	private static final VoxelShape NORMAL_OUTLINE_SHAPE;
	private static final VoxelShape BOTTOM_OUTLINE_SHAPE;
	private static final VoxelShape COLLISION_SHAPE = Block.createCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final VoxelShape OUTLINE_SHAPE = VoxelShapes.fullCube().offset(0.0D, -1.0D, 0.0D);
	public static final IntProperty DISTANCE;
	public static final BooleanProperty BOTTOM;

	public ScaffoldBasedMudBlock(AbstractBlock.Settings settings) {
		super(settings);
		this.setDefaultState((BlockState) ((BlockState) ((BlockState) ((BlockState) this.stateManager.getDefaultState())
				.with(DISTANCE, 7))).with(BOTTOM, false));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(DISTANCE, BOTTOM);
	}

	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (!context.isHolding(state.getBlock().asItem())) {
			return (Boolean) state.get(BOTTOM) ? BOTTOM_OUTLINE_SHAPE : NORMAL_OUTLINE_SHAPE;
		} else {
			return VoxelShapes.fullCube();
		}
	}

	public VoxelShape getRaycastShape(BlockState state, BlockView world, BlockPos pos) {
		return VoxelShapes.fullCube();
	}

	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!world.isClient) {
			world.getBlockTickScheduler().schedule(pos, this, 1);
		}

	}

	public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState,
			WorldAccess world, BlockPos pos, BlockPos posFrom) {
		if (direction == Direction.UP && !state.canPlaceAt(world, pos)) {
			world.getBlockTickScheduler().schedule(pos, this, 1);
		}

		return super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
	}

	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
		int i = calculateDistance(world, pos);
		BlockState blockState = (BlockState) ((BlockState) state.with(DISTANCE, i)).with(BOTTOM,
				this.shouldBeBottom(world, pos, i));
		if ((Integer) blockState.get(DISTANCE) == 7) {
			if ((Integer) state.get(DISTANCE) == 7) {
				world.spawnEntity(new FallingBlockEntity(world, (double) pos.getX() + 0.5D, (double) pos.getY(),
						(double) pos.getZ() + 0.5D, (BlockState) blockState));
			} else {
				world.breakBlock(pos, true);
			}
		} else if (state != blockState) {
			world.setBlockState(pos, blockState, 3);
		}
	}

	@Environment(EnvType.CLIENT)
	public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
		int rand = random.nextInt(40);
		if (rand == 1) {
			if (!isWaterNearby(world, pos) && !world.hasRain(pos.up())) {
				this.setToDirt(pos);
			}
		}
	}

	public void setToDirt(BlockPos pos) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeBlockPos(pos);
		buf.writeIdentifier(new Identifier("minecraft", "dirt"));
		ClientPlayNetworking.send(MobloomMod.SET_BLOCK_PACKET, buf);
	}
	

	@SuppressWarnings("rawtypes")
	private static boolean isWaterNearby(World world, BlockPos pos) {
		Iterator var2 = BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4)).iterator();

		BlockPos blockPos;
		do {
			if (!var2.hasNext()) {
				return false;
			}

			blockPos = (BlockPos) var2.next();
		} while (!world.getFluidState(blockPos).isIn(FluidTags.WATER));

		return true;
	}

	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return calculateDistance(world, pos) < 7;
	}

	public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		if (context.isAbove(VoxelShapes.fullCube(), pos, true) && !context.isDescending()) {
			return NORMAL_OUTLINE_SHAPE;
		} else {
			return (Integer) state.get(DISTANCE) != 0 && (Boolean) state.get(BOTTOM)
					&& context.isAbove(OUTLINE_SHAPE, pos, true) ? COLLISION_SHAPE : VoxelShapes.empty();
		}
	}

	private boolean shouldBeBottom(BlockView world, BlockPos pos, int distance) {
		return distance > 0 && !world.getBlockState(pos.down()).isOf(this);
	}

	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (entity instanceof LivingEntity) {
			LivingEntity livingEntity = (LivingEntity) entity;
			livingEntity.addStatusEffect(new StatusEffectInstance(ModItems.CAMO, 40));
		}
	}

	@SuppressWarnings("rawtypes")
	public static int calculateDistance(BlockView world, BlockPos pos) {
		BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.DOWN);
		BlockState blockState = world.getBlockState(mutable);
		int i = 7;
		if (blockState.isOf(ModItems.MUD)) {
			i = (Integer) blockState.get(DISTANCE);
		} else if (blockState.isSideSolidFullSquare(world, mutable, Direction.UP)) {
			return 0;
		}

		Iterator var5 = Direction.Type.HORIZONTAL.iterator();

		while (var5.hasNext()) {
			Direction direction = (Direction) var5.next();
			BlockState blockState2 = world.getBlockState(mutable.set(pos, direction));
			if (blockState2.isOf(ModItems.MUD)) {
				i = Math.min(i, (Integer) blockState2.get(DISTANCE) + 1);
				if (i == 1) {
					break;
				}
			}
		}

		return i;
	}

	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
		return false;
	}

	static {
		DISTANCE = Properties.DISTANCE_0_7;
		BOTTOM = Properties.BOTTOM;
		NORMAL_OUTLINE_SHAPE = VoxelShapes.fullCube();
		BOTTOM_OUTLINE_SHAPE = VoxelShapes.fullCube();
	}
}
