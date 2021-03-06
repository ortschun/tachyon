package tachyon;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tachyon.thrift.ClientWorkerInfo;
import tachyon.thrift.NetAddress;

/**
 * The structure to store a worker's information in master node.
 */
public class MasterWorkerInfo {
  public final InetSocketAddress ADDRESS;
  private final long CAPACITY_BYTES;
  private final long START_TIME_MS;

  private long mId;
  private long mUsedBytes;
  private long mLastUpdatedTimeMs;
  private Set<Long> mBlocks;
  private Set<Long> mToRemoveBlocks;

  public MasterWorkerInfo(long id, InetSocketAddress address, long capacityBytes) {
    mId = id;
    ADDRESS = address;
    CAPACITY_BYTES = capacityBytes;
    START_TIME_MS = System.currentTimeMillis();

    mUsedBytes = 0;
    mBlocks = new HashSet<Long>();
    mToRemoveBlocks = new HashSet<Long>();
    mLastUpdatedTimeMs = System.currentTimeMillis();
  }

  public InetSocketAddress getAddress() {
    return ADDRESS;
  }

  public synchronized long getAvailableBytes() {
    return CAPACITY_BYTES - mUsedBytes;
  }

  public long getCapacityBytes() {
    return CAPACITY_BYTES;
  }

  public synchronized long getId() {
    return mId;
  }

  public synchronized long getLastUpdatedTimeMs() {
    return mLastUpdatedTimeMs;
  }

  /**
   * Get all blocks in the worker's memory.
   * @return ids of the blocks.
   */
  public synchronized Set<Long> getBlocks() {
    return new HashSet<Long>(mBlocks);
  }

  /**
   * Get all blocks in the worker's memory need to be removed.
   * @return ids of the blocks need to be removed.
   */
  public synchronized List<Long> getToRemovedBlocks() {
    return new ArrayList<Long>(mToRemoveBlocks);
  }

  public synchronized long getUsedBytes() {
    return mUsedBytes;
  }

  @Override
  public synchronized String toString() {
    StringBuilder sb = new StringBuilder("MasterWorkerInfo(");
    sb.append(" ID: ").append(mId);
    sb.append(", ADDRESS: ").append(ADDRESS);
    sb.append(", TOTAL_BYTES: ").append(CAPACITY_BYTES);
    sb.append(", mUsedBytes: ").append(mUsedBytes);
    sb.append(", mAvailableBytes: ").append(CAPACITY_BYTES - mUsedBytes);
    sb.append(", mLastUpdatedTimeMs: ").append(mLastUpdatedTimeMs);
    sb.append(", mBlocks: [ ");
    for (long blockId : mBlocks) {
      sb.append(blockId).append(", ");
    }
    sb.append("] )");
    return sb.toString();
  }

  public synchronized void updateBlock(boolean add, long blockId) {
    if (add) {
      mBlocks.add(blockId);
    } else {
      mBlocks.remove(blockId);
    }
  }

  public synchronized void updateBlocks(boolean add, Collection<Long> blockIds) {
    if (add) {
      mBlocks.addAll(blockIds);
    } else {
      mBlocks.removeAll(blockIds);
    }
  }

  public synchronized void updateToRemovedBlock(boolean add, long blockId) {
    if (add) {
      if (mBlocks.contains(blockId)) {
        mToRemoveBlocks.add(blockId);
      }
    } else {
      mToRemoveBlocks.remove(blockId);
    }
  }

  public synchronized void updateToRemovedBlocks(boolean add, Collection<Long> blockIds) {
    for (long blockId: blockIds) {
      updateToRemovedBlock(add, blockId);
    }
  }

  public synchronized void updateLastUpdatedTimeMs() {
    mLastUpdatedTimeMs = System.currentTimeMillis();
  }

  public synchronized void updateUsedBytes(long usedBytes) {
    mUsedBytes = usedBytes;
  }

  public synchronized ClientWorkerInfo generateClientWorkerInfo() {
    ClientWorkerInfo ret = new ClientWorkerInfo();
    ret.id = mId;
    ret.address = new NetAddress(ADDRESS.getHostName(), ADDRESS.getPort());
    ret.lastContactSec = (int) ((CommonUtils.getCurrentMs() - mLastUpdatedTimeMs) / 1000);
    ret.state = "In Service";
    ret.capacityBytes = CAPACITY_BYTES;
    ret.usedBytes = mUsedBytes;
    ret.starttimeMs = START_TIME_MS;
    return ret;
  }
}