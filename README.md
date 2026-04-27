# BES-Hardcore

**Break Everything Server – Hardcore Edition**

A server-side mod for Minecraft 1.6.4 MITE that adds configurable hardcore
difficulty and optional automatic world-reset on player death.

---

## Features

### Hardcore Mode
Enables Minecraft's built-in hardcore difficulty on the server world.
Players **cannot respawn** after they die. Controlled by the `"hardcore"`
flag in `config.json`.

### World Reset
When `"worldReset"` is enabled (requires `"hardcore": true`), the moment
any player dies the server:
1. Broadcasts a warning to all online players.
2. Waits **5 seconds**.
3. Deletes the entire `world/` folder.
4. Exits cleanly (`System.exit(0)`).

On the next launch the server regenerates a fresh world using the **same
seed** stored in `config.json`, so the map layout is always reproducible.
Pair this with a restart loop (systemd, a shell `while` loop, etc.) for
fully automatic resets.

---

## Configuration (`config.json`)

Generated automatically next to the JAR on first launch.

```json
{
  "ip": "0.0.0.0",
  "port": 25565,
  "seed": 123456789,
  "motd": "Break Everything!",
  "ops": [],
  "hardcore": false,
  "worldReset": false
}
```

| Field        | Type    | Default      | Description                                      |
|--------------|---------|--------------|--------------------------------------------------|
| `ip`         | string  | `127.0.0.1`  | Bind address                                     |
| `port`       | integer | `25565`      | Listen port (1–65535)                            |
| `seed`       | long    | random       | World seed used on creation **and** after reset  |
| `motd`       | string  | —            | Server list description                          |
| `ops`        | array   | `[]`         | List of operator usernames                       |
| `hardcore`   | boolean | `false`      | Enable hardcore difficulty                       |
| `worldReset` | boolean | `false`      | Reset world on player death (requires hardcore)  |

---

## Building

See **[HOW_TO_BUILD.txt](HOW_TO_BUILD.txt)** for full details.

**Quick start:**
```bash
chmod +x gradlew          # Linux/macOS only
./gradlew :desktop:modJar
```

Output: `desktop/run/BES.jar`

**Requirements:**
- Java 17+
- Internet access to Maven Central
- `core/libs/1.6.4-MITE.jar` and `core/libs/mappings.tiny` present

---

## Running

```bash
java -jar BES.jar
```

---

## Technical Notes

- The build no longer depends on `maven.fabricmc.net`. Remapping is handled
  by a custom `TinyRemapper` class (built with ASM) bundled in the project.
- Hooks are injected at build time via `AsmTransformer`; no runtime agent needed.
- The world-reset logic lives in `HardcoreHook.java` and is wired into
  `net.minecraft.ServerPlayer#onDeath` via the hook system.

