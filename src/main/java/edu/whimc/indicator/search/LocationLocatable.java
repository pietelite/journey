package edu.whimc.indicator.search;

import edu.whimc.indicator.api.search.Locatable;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Objects;

public class LocationLocatable implements Locatable<LocationLocatable, World> {

  @Getter
  private final int blockX;
  @Getter
  private final int blockY;
  @Getter
  private final int blockZ;
  @Getter @NonNull
  private final World world;

  public LocationLocatable(Location location) {
    this.blockX = location.getBlockX();
    this.blockY = location.getBlockY();
    this.blockZ = location.getBlockZ();
    this.world = location.getWorld();
  }

  public LocationLocatable(int blockX, int blockY, int blockZ, World world) {
    this.blockX = blockX;
    this.blockY = blockY;
    this.blockZ = blockZ;
    this.world = world;
  }

  @Override
  public double distanceTo(LocationLocatable other) {
    return vectorSizeSquared(this.getBlockX() - other.getBlockX(),
        this.getBlockY() - other.getBlockY(),
        this.getBlockZ() - other.getBlockZ());
  }

  public Block getBlockAtOffset(int x, int y, int z) {
    return this.world.getBlockAt(blockX + x, blockY + y, blockZ + z);
  }

  public LocationLocatable createLocatableAtOffset(int x, int y, int z) {
    return new LocationLocatable(this.blockX + x, this.blockY + y, this.blockZ + z, this.world);
  }

  private double vectorSizeSquared(int distX, int distY, int distZ) {
    return distX * distX + distY * distY + distZ * distZ;
  }

  @Override
  public World getDomain() {
    return this.getWorld();
  }

  @Override
  public String print() {
    return String.format("(%d, %d, %d) in %s", blockX, blockY, blockZ, world.getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    LocationLocatable that = (LocationLocatable) o;
    return blockX == that.blockX && blockY == that.blockY && blockZ == that.blockZ && world.equals(that.world);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockX, blockY, blockZ, world);
  }
}