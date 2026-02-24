package cn.tesseract.bes.hook;

import cn.tesseract.asm.Hook;
import cn.tesseract.asm.ReturnCondition;
import cn.tesseract.bes.Main;
import net.minecraft.*;
import net.minecraft.server.MinecraftServer;

public class FixHook {
    @Hook(returnCondition = ReturnCondition.ALWAYS)
    public static Minecraft getMinecraft(Minecraft c) {
        return Main.dummyMc;
    }

    public static Minecraft nullMinecraft() {
        return null;
    }

    @Hook
    public static void a(agq c, Achievement achievement, EntityPlayer entityPlayer) {
        if (!c.b.a(achievement) || !entityPlayer.username.equals(c.b.K.get(achievement).a))
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(ChatMessageComponent.createFromTranslationWithSubstitutions("%s 刚刚获得了 %s 成就！", entityPlayer.username, "§a[" + achievement + "]§r"));
    }
}
