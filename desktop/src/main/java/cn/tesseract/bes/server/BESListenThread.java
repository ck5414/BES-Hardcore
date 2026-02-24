package cn.tesseract.bes.server;

import net.minecraft.NetworkListenThread;
import net.minecraft.ServerListenThread;
import net.minecraft.server.MinecraftServer;

import java.net.InetAddress;

public class BESListenThread extends NetworkListenThread {
    private final ServerListenThread theServerListenThread;

    public BESListenThread(MinecraftServer minecraftServer, InetAddress inetAddress, int integer) {
        super(minecraftServer);
        this.theServerListenThread = new ServerListenThread(this, inetAddress, integer);
        System.out.println("Starting BEServer on " + inetAddress + ":" + integer);
        this.theServerListenThread.start();
    }

    @Override
    public void stopListening() {
        super.stopListening();
        this.theServerListenThread.func_71768_b();
        this.theServerListenThread.interrupt();
    }

    @Override
    public void networkTick() {
        this.theServerListenThread.a();
        super.networkTick();
    }

    @Override
    public MinecraftServer getServer() {
        return super.getServer();
    }
}