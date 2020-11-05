
package carpet.utils;

import carpet.mixins.WeightedPickerEntryMixin;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.text.BaseText;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnReporter
{
    public static final HashMap<RegistryKey<World>, Integer> chunkCounts = new HashMap<>();
    public static double mobcap_exponent = 0.0D;


    public static String creatureTypeColor(SpawnGroup type)
    {
        switch (type)
        {
            case MONSTER:
                return "n";
            case CREATURE:
                return "e";
            case AMBIENT:
                return "f";
            case WATER_CREATURE:
                return "v";
        }
        return "w";
    }

    public static final int MAGIC_NUMBER = (int)Math.pow(17.0D, 2.0D);
    public static double currentMagicNumber()
    {
        return MAGIC_NUMBER / (Math.pow(2.0,(SpawnReporter.mobcap_exponent/4)));
    }

    public static List<BaseText> printMobcapsForDimension(ServerWorld world, boolean multiline)
    {
        RegistryKey<World> dim = world.getRegistryKey();
        String name = dim.getValue().getPath();
        List<BaseText> lst = new ArrayList<>();
        if (multiline)
            lst.add(Messenger.s(String.format("Mobcaps for %s:",name)));
        SpawnHelper.Info lastSpawner = world.getChunkManager().getSpawnInfo();
        Object2IntMap<SpawnGroup> dimCounts = lastSpawner.getGroupToCount();
        int chunkcount = chunkCounts.getOrDefault(dim, -1);
        if (dimCounts == null || chunkcount < 0)
        {
            lst.add(Messenger.c("g   --UNAVAILABLE--"));
            return lst;
        }

        List<String> shortCodes = new ArrayList<>();
        for (SpawnGroup enumcreaturetype : SpawnGroup.values())
        {
            int cur = dimCounts.getOrDefault(enumcreaturetype, -1);
            int max = (int)(chunkcount * ((double)enumcreaturetype.getCapacity() / currentMagicNumber())); // from ServerChunkManager.CHUNKS_ELIGIBLE_FOR_SPAWNING
            String color = Messenger.heatmap_color(cur, max);
            String mobColor = creatureTypeColor(enumcreaturetype);
            if (multiline)
            {
                lst.add(Messenger.c(String.format("w   %s: ", enumcreaturetype.getName()),
                        (cur < 0) ? "g -" : (color + " " + cur), "g  / ", mobColor + " " + max
                ));
            }
            else
            {
                shortCodes.add(color+" "+((cur<0)?"-":cur));
                shortCodes.add("g /");
                shortCodes.add(mobColor+" "+max);
                shortCodes.add("g ,");
            }
        }
        if (!multiline)
        {
            if (shortCodes.size()>0)
            {
                shortCodes.remove(shortCodes.size() - 1);
                lst.add(Messenger.c(shortCodes.toArray(new Object[0])));
            }
            else
            {
                lst.add(Messenger.c("g   --UNAVAILABLE--"));
            }

        }
        return lst;
    }


    public static String get_type_string(SpawnGroup type)
    {
        return String.format("%s", type);
    }

    public static List<BaseText> printEntitiesByType(SpawnGroup cat, World worldIn) //Class<?> entityType)
    {
        List<BaseText> lst = new ArrayList<>();
        lst.add( Messenger.s(String.format("Loaded entities for %s class:", get_type_string(cat))));
        for (Entity entity : ((ServerWorld)worldIn).getEntitiesByType(null, (e) -> e.getType().getSpawnGroup()==cat))
        {
            if (!(entity instanceof MobEntity) || !((MobEntity)entity).isPersistent())
            {
                EntityType type = entity.getType();
                BlockPos pos = entity.getBlockPos();
                lst.add( Messenger.c(
                        "w  - ",
                        Messenger.tp("wb",pos),
                        String.format("w : %s", type.getName().getString())
                        ));
            }
        }
        if (lst.size()==1)
        {
            lst.add(Messenger.s(" - Empty."));
        }
        return lst;
    }



    public static void killEntity(LivingEntity entity)
    {
        if (entity.hasVehicle())
        {
            entity.getVehicle().remove();
        }
        if (entity.hasPassengers())
        {
            for (Entity e: entity.getPassengerList())
            {
                e.remove();
            }
        }
        if (entity instanceof OcelotEntity)
        {
            for (Entity e: entity.getEntityWorld().getOtherEntities(entity, entity.getBoundingBox()))
            {
                e.remove();
            }
        }
        entity.remove();
    }

    // yeeted from SpawnHelper - temporary fix
    private static List<SpawnSettings.SpawnEntry> method_29950(ServerWorld serverWorld, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, SpawnGroup spawnGroup, BlockPos blockPos, /*@Nullable*/ Biome biome) {
        return spawnGroup == SpawnGroup.MONSTER && serverWorld.getBlockState(blockPos.down()).getBlock() == Blocks.NETHER_BRICKS && structureAccessor.getStructureAt(blockPos, false, StructureFeature.FORTRESS).hasChildren() ? StructureFeature.FORTRESS.getMonsterSpawns() : chunkGenerator.getEntitySpawnList(biome != null ? biome : serverWorld.getBiome(blockPos), structureAccessor, spawnGroup, blockPos);
    }

    public static List<BaseText> report(BlockPos pos, ServerWorld worldIn)
    {
        List<BaseText> rep = new ArrayList<>();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        Chunk chunk = worldIn.getChunk(pos);
        int lc = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z) + 1;
        String where = String.format((y >= lc) ? "%d blocks above it." : "%d blocks below it.", MathHelper.abs(y - lc));
        if (y == lc) where = "right at it.";
        rep.add(Messenger.s(String.format("Maximum spawn Y value for (%+d, %+d) is %d. You are " + where, x, z, lc)));
        rep.add(Messenger.s("Spawns:"));
        for (SpawnGroup enumcreaturetype : SpawnGroup.values())
        {
            String type_code = String.format("%s", enumcreaturetype).substring(0, 3);
            List<SpawnSettings.SpawnEntry> lst = method_29950(worldIn, worldIn.getStructureAccessor(), worldIn.getChunkManager().getChunkGenerator(), enumcreaturetype, pos, worldIn.getBiome(pos));//  ((ChunkGenerator)worldIn.getChunkManager().getChunkGenerator()).getEntitySpawnList(, worldIn.getStructureAccessor(), enumcreaturetype, pos);
            if (lst != null && !lst.isEmpty())
            {
                for (SpawnSettings.SpawnEntry spawnEntry : lst)
                {
                    if (SpawnRestriction.getLocation(spawnEntry.type) == null)
                        continue; // vanilla bug
                    boolean canspawn = SpawnHelper.canSpawn(SpawnRestriction.getLocation(spawnEntry.type), worldIn, pos, spawnEntry.type);
                    int will_spawn = -1;
                    boolean fits;
                    boolean fits1;

                    MobEntity mob;
                    try
                    {
                        mob = (MobEntity) spawnEntry.type.create(worldIn);
                    }
                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                        return rep;
                    }

                    boolean fits_true = false;
                    boolean fits_false = false;

                    if (canspawn)
                    {
                        will_spawn = 0;
                        for (int attempt = 0; attempt < 50; ++attempt)
                        {
                            float f = (float) x + 0.5F;
                            float f1 = (float) z + 0.5F;
                            mob.refreshPositionAndAngles((double) f, (double) y, (double) f1, worldIn.random.nextFloat() * 360.0F, 0.0F);
                            fits1 = worldIn.isSpaceEmpty(mob);
                            EntityType etype = mob.getType();

                            for (int i = 0; i < 20; ++i)
                            {
                                if (
                                        SpawnRestriction.canSpawn(etype, worldIn, SpawnReason.NATURAL, pos, worldIn.random) &&
                                                SpawnHelper.canSpawn(SpawnRestriction.getLocation(etype), worldIn, pos, etype) &&
                                                mob.canSpawn(worldIn, SpawnReason.NATURAL) // && mob.canSpawn(worldIn) // entity collisions
                                )
                                {
                                    will_spawn += 1;
                                }
                            }
                            mob.initialize(worldIn, worldIn.getLocalDifficulty(mob.getBlockPos()), SpawnReason.NATURAL, null, null);
                            // the code invokes onInitialSpawn after getCanSpawHere
                            fits = fits1 && worldIn.isSpaceEmpty(mob);
                            if (fits)
                            {
                                fits_true = true;
                            }
                            else
                            {
                                fits_false = true;
                            }

                            killEntity(mob);

                            try
                            {
                                mob = (MobEntity) spawnEntry.type.create(worldIn);
                            }
                            catch (Exception exception)
                            {
                                exception.printStackTrace();
                                return rep;
                            }
                        }
                    }

                    String creature_name = mob.getType().getName().getString();
                    String pack_size = String.format("%d", mob.getLimitPerChunk());//String.format("%d-%d", animal.minGroupCount, animal.maxGroupCount);
                    int weight = ((WeightedPickerEntryMixin) spawnEntry).getWeight();
                    if (canspawn)
                    {
                        String c = (fits_true && will_spawn > 0) ? "e" : "gi";
                        rep.add(Messenger.c(
                                String.format("%s %s: %s (%d:%d-%d/%d), can: ", c, type_code, creature_name, weight, spawnEntry.minGroupSize, spawnEntry.maxGroupSize, mob.getLimitPerChunk()),
                                "l YES",
                                c + " , fit: ",
                                ((fits_true && fits_false) ? "y YES and NO" : (fits_true ? "l YES" : "r NO")),
                                c + " , will: ",
                                ((will_spawn > 0) ? "l " : "r ") + Math.round((double) will_spawn) / 10 + "%"
                        ));
                    }
                    else
                    {
                        rep.add(Messenger.c(String.format("gi %s: %s (%d:%d-%d/%d), can: ", type_code, creature_name, weight, spawnEntry.minGroupSize, spawnEntry.maxGroupSize, mob.getLimitPerChunk()), "n NO"));
                    }
                    killEntity(mob);
                }
            }
        }
        return rep;
    }
}