package net.darkhax.bingo;

import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.WorldWorkerManager.IWorker;

/*
 * Based on ChunkGenWorker from MinecraftForge
 * All BlockPos used in here are actually ChunkPos
 */
public class PregenWorker implements IWorker {

	private static final int TOTAL = 25;

	private final BlockPos start;
	private final Queue<BlockPos> posQueue;
	private final ServerWorld dim;
	private final Runnable callback;

	public PregenWorker(BlockPos startingPosition, ServerWorld dim, Runnable callback) {
		this(new ChunkPos(startingPosition), dim, callback);
	}

	public PregenWorker(ChunkPos startingPosition, ServerWorld dim, Runnable callback) {
		this.start = new BlockPos(startingPosition.x, 0, startingPosition.z);
		this.posQueue = buildQueue();
		this.dim = dim;
		this.callback = callback;
	}

	// Taken from MinecraftForge:
	// https://github.com/MinecraftForge/MinecraftForge/blob/29f175743b473cabd4f180485f1884e82d1b24cb/src/main/java/net/minecraftforge/server/command/ChunkGenWorker.java#L59-L83
	private Queue<BlockPos> buildQueue() {
		Queue<BlockPos> ret = new ArrayDeque<>();
		ret.add(start);

		// This *should* spiral outwards, starting on right side, down, left, up, right,
		// but hey we'll see!
		int radius = 1;
		while(ret.size() < TOTAL) {
			for(int q = -radius + 1; q <= radius && ret.size() < TOTAL; q++)
				ret.add(start.offset(radius, 0, q));

			for(int q = radius - 1; q >= -radius && ret.size() < TOTAL; q--)
				ret.add(start.offset(q, 0, radius));

			for(int q = radius - 1; q >= -radius && ret.size() < TOTAL; q--)
				ret.add(start.offset(-radius, 0, q));

			for(int q = -radius + 1; q <= radius && ret.size() < TOTAL; q++)
				ret.add(start.offset(q, 0, -radius));

			radius++;
		}
		return ret;
	}

	@Override
	public boolean hasWork() {
		return posQueue.size() > 0;
	}

	@Override
	public boolean doWork() {
		BlockPos next = posQueue.poll();

		if(next != null) {
			int x = next.getX();
			int z = next.getZ();

			if(!dim.hasChunk(x, z)) { // Chunk is unloaded
				IChunk chunk = dim.getChunk(x, z, ChunkStatus.EMPTY, true);
				if(!chunk.getStatus().isOrAfter(ChunkStatus.FULL)) {
					chunk = dim.getChunk(x, z, ChunkStatus.FULL);
				}
			}
		}

		if(posQueue.size() == 0) {
			// completed
			this.callback.run();
			return false;
		}
		return true;
	}

}
