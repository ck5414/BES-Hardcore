package cn.tesseract.bes.hook;

import cn.tesseract.asm.Hook;
import cn.tesseract.bes.Main;
import net.minecraft.ChatMessageComponent;
import net.minecraft.DamageSource;
import net.minecraft.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class HardcoreHook {

    private static final long RESET_DELAY_MS = 5000L;

    @Hook
    public static void onDeath(ServerPlayer player, DamageSource cause) {
        if (!Main.config.hardcore || !Main.config.worldReset) return;

        MinecraftServer server = MinecraftServer.getServer();
        server.getConfigurationManager().sendChatMsg(
                ChatMessageComponent.createFromText(
                        "§c[Hardcore] " + player.username + " died — the world will reset in "
                        + (RESET_DELAY_MS / 1000) + " seconds!"));

        Thread resetThread = new Thread(() -> {
            try {
                Thread.sleep(RESET_DELAY_MS);
            } catch (InterruptedException ignored) {
            }
            server.getConfigurationManager().sendChatMsg(
                    ChatMessageComponent.createFromText("§4[Hardcore] Resetting world now..."));
            try {
                deleteWorldFolder(new File(Main.mcDataDir, "world"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.exit(0);
        }, "World-Reset");
        // Not a daemon — must finish deleting the world before the JVM exits
        resetThread.start();
    }

    private static void deleteWorldFolder(File worldDir) throws IOException {
        if (!worldDir.exists()) return;
        Files.walkFileTree(worldDir.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
