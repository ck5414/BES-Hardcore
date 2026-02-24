package cn.tesseract.bes.server;

import java.util.ArrayList;
import java.util.Random;

public class ServerConfig {
    public String ip = "127.0.0.1";
    public int port = 25565;
    public long seed = new Random().nextLong();
    public String motd = "打破一切！服务器官方群：1085649633";
    public ArrayList<String> ops = new ArrayList<>();

    public void normalize() {
        if (port < 1 || port > 65535) {
            port = 25565;
        }
    }
}

