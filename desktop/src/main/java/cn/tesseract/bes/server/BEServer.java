//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.tesseract.bes.server;

import cn.tesseract.bes.Main;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class BEServer extends MinecraftServer {
    public final agD theWorldSettings;
    public final Ov F;
    public final BESListenThread theServerListeningThread;
    public ThreadLanServerPing lanServerPing;
    public final Ov M;

    public BEServer(long seed, String ip, int port) {
        super(new File(System.getProperty("user.dir")));
        this.F = new Ow("Minecraft-Server", " [SERVER]", (new File(Main.mcDataDir, "log/output-server.log")).getAbsolutePath());
        this.M = new Ow("Suspicious-Log", null, (new File(Main.mcDataDir, "log/suspicious.log")).getAbsolutePath());
        Minecraft.c = F;
        Minecraft.d = F;
        this.setServerOwner("Tesseract");
        this.setFolderName("world");
        this.setWorldName("world");
        this.setDemo(false);
        this.canCreateBonusChest(false);
        this.setBuildLimit(256);
        this.setConfigurationManager(new BESPlayerList(this));
        this.theWorldSettings = new agD(seed, EnumGameType.SURVIVAL, true, false, WorldType.parseWorldType("largeBiomes"), false);
        try {
            this.theServerListeningThread = new BESListenThread(this, InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public final void loadAllWorlds(String var1, String var2, long var3, WorldType var5, String var6) {
        this.convertMapIfNeeded(var1);
        this.worldServers = new WorldServer[4];
        this.timeOfLastDimensionTick = new long[this.worldServers.length][100];
        ISaveHandler var7 = this.getActiveAnvilConverter().getSaveLoader(var1, true);

        for (int var8 = 0; var8 < this.worldServers.length; ++var8) {
            byte var4 = (byte) f(var8);
            if (var8 == 0) {
                if (this.isDemo()) {
                    this.worldServers[var8] = new ahA(this, var7, var2, var4, this.theProfiler, this.F);
                } else {
                    this.worldServers[var8] = new WorldServer(this, var7, var2, var4, this.theWorldSettings, this.theProfiler, this.F);
                }
            } else {
                this.worldServers[var8] = new agC(this, var7, var2, var4, this.theWorldSettings, this.worldServers[0], this.theProfiler, this.F);
            }

            this.worldServers[var8].addWorldAccess(new ags(this, this.worldServers[var8]));
            xi.a(this.worldServers[var8]);
            this.getConfigurationManager().setPlayerManager(this.worldServers);
        }

        this.initialWorldChunkLoad();
    }

    public final boolean startServer() {
        this.F.a("Starting integrated minecraft server version 1.6.4", new Object[0]);
        this.d(false);
        this.e(true);
        this.f(true);
        this.g(true);
        this.setAllowFlight(true);
        this.F.a("Generating keypair", new Object[0]);
        this.setKeyPair(CryptManager.createNewKeyPair());
        this.loadAllWorlds(this.getFolderName(), this.getWorldName(), this.theWorldSettings.a, this.theWorldSettings.f, this.theWorldSettings.h);

        Sc.a();
        return true;
    }

    public final void tick() {
        super.tick();
    }

    public final void ab() {
        if (!D) {
            this.F.a("Saving all players and worlds...", new Object[0]);
            this.getConfigurationManager().saveAllPlayerData();
            this.a(false);
            Xe.a(this);
        }

    }

    public final boolean canStructuresSpawn() {
        return true;
    }

    public final EnumGameType getGameType() {
        return this.theWorldSettings.b;
    }

    public final afZ f() {
        return afZ.a;
    }

    public final boolean isHardcore() {
        return this.theWorldSettings.e;
    }

    public final File getDataDirectory() {
        return Main.mcDataDir;
    }

    public final boolean isDedicatedServer() {
        return true;
    }

    public final void finalTick(CrashReport var1) {
        while (isServerRunning()) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException var3) {
                var3.printStackTrace();
            }
        }
    }

    public final CrashReport addServerInfoToCrashReport(CrashReport var1) {
        return super.addServerInfoToCrashReport(var1);
    }

    public final void addServerStatsToSnooper(PlayerUsageSnooper var1) {
        super.addServerStatsToSnooper(var1);
    }

    public final boolean isSnooperEnabled() {
        return true;
    }

    public final int a(EnumGameType var1, boolean var2, int var3) {
        return 0;
    }

    public String getMOTD() {
        return this.motd;
    }

    public final Ov getLogAgent() {
        return this.F;
    }

    public final Ov R() {
        return this.M;
    }

    public final void stopServer() {
        super.stopServer();
        if (this.lanServerPing != null) {
            this.lanServerPing.interrupt();
            this.lanServerPing = null;
        }

    }

    public final void initiateShutdown() {
        super.initiateShutdown();
        if (this.lanServerPing != null) {
            this.lanServerPing.interrupt();
            this.lanServerPing = null;
        }
    }

    public final boolean isCommandBlockEnabled() {
        return true;
    }

    public final boolean h() {
        return this.theWorldSettings.i;
    }

    public final boolean i() {
        return this.theWorldSettings.j;
    }

    public final NetworkListenThread getNetworkThread() {
        return this.theServerListeningThread;
    }

    public final boolean V() {
        return false;
    }
}
