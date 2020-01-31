package carpet.patches;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.client.network.packet.EntityPositionS2CPacket;
import net.minecraft.client.network.packet.EntitySetHeadYawS2CPacket;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.network.NetworkSide;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionType;
import carpet.utils.Messenger;

@SuppressWarnings("EntityConstructor")
public class EntityPlayerMPFake extends ServerPlayerEntity
{
    public Runnable fixStartingPosition = () -> {};

    public static EntityPlayerMPFake createFake(String username, MinecraftServer server, double d0, double d1, double d2, double yaw, double pitch, DimensionType dimension, GameMode gamemode)
    {
        //prolly half of that crap is not necessary, but it works
        ServerWorld worldIn = server.getWorld(dimension);
        ServerPlayerInteractionManager interactionManagerIn = new ServerPlayerInteractionManager(worldIn);
        GameProfile gameprofile = server.getUserCache().findByName(username);
        if (gameprofile == null)
        {
            return null;
        }
        if (gameprofile.getProperties().containsKey("textures"))
        {
            gameprofile = SkullBlockEntity.loadProperties(gameprofile);
        }
        EntityPlayerMPFake instance = new EntityPlayerMPFake(server, worldIn, gameprofile, interactionManagerIn);
        instance.fixStartingPosition = () -> instance.refreshPositionAndAngles(d0, d1, d2, (float) yaw, (float) pitch);
        server.getPlayerManager().onPlayerConnect(new NetworkManagerFake(NetworkSide.SERVERBOUND), instance);
        if (instance.dimension != dimension) //player was logged in in a different dimension
        {
            ServerWorld old_world = server.getWorld(instance.dimension);
            instance.dimension = dimension;
            old_world.removePlayer(instance);
            instance.removed = false;
            //worldIn.spawnEntity(instance);
            instance.setWorld(worldIn);
            server.getPlayerManager().sendWorldInfo(instance, worldIn);
            instance.networkHandler.requestTeleport(d0, d1, d2, (float) yaw, (float) pitch);
            instance.interactionManager.setWorld(worldIn);
            worldIn.onPlayerChangeDimension(instance);
        }
        instance.setHealth(20.0F);
        instance.removed = false;
        instance.networkHandler.requestTeleport(d0, d1, d2, (float) yaw, (float) pitch);
        instance.stepHeight = 0.6F;
        interactionManagerIn.setGameMode(gamemode);
        server.getPlayerManager().sendToDimension(new EntitySetHeadYawS2CPacket(instance, (byte) (instance.headYaw * 256 / 360)), instance.dimension);
        server.getPlayerManager().sendToDimension(new EntityPositionS2CPacket(instance), instance.dimension);
        instance.getServerWorld().getChunkManager().updateCameraPosition(instance);
        instance.dataTracker.set(PLAYER_MODEL_PARTS, (byte) 0x7f); // show all model layers (incl. capes)
        return instance;
    }

    private EntityPlayerMPFake(MinecraftServer server, ServerWorld worldIn, GameProfile profile, ServerPlayerInteractionManager interactionManagerIn)
    {
        super(server, worldIn, profile, interactionManagerIn);
    }


    @Override
    public void kill()
    {
        this.server.send(new ServerTask(this.server.getTicks(), () -> {
            this.networkHandler.onDisconnected(Messenger.s("Killed"));
        }));
    }

    @Override
    public void tick()
    {
        if (this.getServer().getTicks() % 10 == 0)
        {
            this.networkHandler.syncWithPlayerPosition();
            this.getServerWorld().getChunkManager().updateCameraPosition(this);
        }
        super.tick();
        this.playerTick();
    }

    @Override
    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        setHealth(20);
        this.hungerManager = new HungerManager();
        kill();
    }
}
