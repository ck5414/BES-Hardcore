package cn.tesseract.bes;

import cn.tesseract.bes.server.BEServer;
import cn.tesseract.bes.server.ServerConfig;
import net.minecraft.*;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Main {
    public static final ServerConfig config = ConfigIO.loadOrCreate();
    public static final Minecraft dummyMc = new Minecraft();
    public static final File mcDataDir = new File(System.getProperty("user.dir"));

    static {
        dummyMc.O = mcDataDir;
        dummyMc.W = true;
        dummyMc.Y = new DummyResourceManager();
        StatList.nopInit();
    }

    public static BEServer server;

    public static void main(String[] var0) {
        server = new BEServer(config.seed, config.ip, config.port);
        server.E.start();
        server.setMOTD(config.motd);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stopServer()));
    }

    public static class DummyResourceManager implements ReloadableResourceManager {
        public void reloadResources(List<ResourcePack> list) {
        }

        public void registerReloadListener(ResourceManagerReloadListener resourceManagerReloadListener) {
        }

        public Set<String> getResourceDomains() {
            return Collections.emptySet();
        }

        public Resource getResource(ResourceLocation resourceLocation) {
            return new DummyResource(resourceLocation);
        }

        public List<Resource> getAllResources(ResourceLocation resourceLocation) {
            return Collections.emptyList();
        }
    }

    public static class DummyResource implements Resource {
        final ResourceLocation location;

        public DummyResource(ResourceLocation location) {
            this.location = location;
        }

        public InputStream getInputStream() {
            return Main.class.getResourceAsStream("/assets/" + location.resourceDomain + "/" + location.resourcePath);
        }

        public boolean hasMetadata() {
            return false;
        }

        public k getMetadata(String s) {
            return null;
        }
    }
}
