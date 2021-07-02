package net.darkhax.bingo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.darkhax.bingo.api.BingoAPI;
import net.darkhax.bingo.commands.CommandBingo;
import net.darkhax.bingo.data.BingoDataReader;
import net.darkhax.bingo.data.BingoPersistantData;
import net.darkhax.bingo.network.PacketSyncGameState;
import net.darkhax.bingo.network.PacketSyncGoal;
import net.darkhax.bookshelf.network.NetworkHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("bingo")
public class ModdedBingo {

    public static final String MOD_ID = "bingo";
    public static final NetworkHelper NETWORK = new NetworkHelper(new ResourceLocation(MOD_ID, "main"), "1.0");
    public static final Logger LOG = LogManager.getLogger("Bingo");
    
    public ModdedBingo() {
    	 MinecraftForge.EVENT_BUS.register(this);
    	 FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    	 
    	 new BingoAPI(); //Class load Bingo API
    }
    
    private void setup(final FMLCommonSetupEvent event) {    	
    	NETWORK.registerEnqueuedMessage(PacketSyncGoal.class, PacketSyncGoal::encode, PacketSyncGoal::new, PacketSyncGoal::handle);
    	NETWORK.registerEnqueuedMessage(PacketSyncGameState.class, PacketSyncGameState::encode, PacketSyncGameState::new, PacketSyncGameState::handle);
    }
    
    @SubscribeEvent
    public void onResourceReload(AddReloadListenerEvent event) {
    	event.addListener(new BingoDataReader());
    }
    
    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
    	CommandBingo.initializeCommands(event.getDispatcher());
    }

    @SubscribeEvent
    public void serverStarted (FMLServerStartedEvent event) {

        // Read the bingo persistent data from nbt data if it exists, when the server is
        // started.
        final Path bingoFile = event.getServer().getWorldPath(FolderName.ROOT).resolve("bingo.data");
        
        if (Files.exists(bingoFile)) {
            try{
            	byte[] data = Files.readAllBytes(bingoFile);
            	if(data.length != 0) {
            		BingoPersistantData.read(new PacketBuffer(Unpooled.wrappedBuffer(data)));
            		return;
            	}
            }
            catch (final IOException e) {
                LOG.warn("Failed to read bingo data. This is not good.");
                LOG.catching(e);
            }
        }
        // reset the game state
        BingoAPI.GAME_STATE.read(null);
    }

    @SubscribeEvent
    public void serverStopping (FMLServerStoppingEvent event) {

        // Write the bingo data to the world when the server stops.
    	final Path bingoFile = event.getServer().getWorldPath(FolderName.ROOT).resolve("bingo.data");
    	
        try(OutputStream os = Files.newOutputStream(bingoFile)) {
        	ByteBuf byteBuf = Unpooled.buffer();
        	BingoPersistantData.write(new PacketBuffer(byteBuf));
        	os.write(byteBuf.array());
        }
        catch (final IOException e) {
            LOG.error("Failed to write bingo data. This is not good.");
            LOG.catching(e);
        }
    }
}