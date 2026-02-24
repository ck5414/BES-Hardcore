package cn.tesseract.union;

import cn.tesseract.asm.HookClassTransformer;
import cn.tesseract.asm.Transformer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

public class AsmTransformer {
    public static HookClassTransformer transformer = new HookClassTransformer();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: <inputJar> <outputJar>");
            System.exit(2);
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        if (!Files.exists(input)) {
            System.err.println("Input jar not found: " + input);
            System.exit(3);
        }

        try (JarFile jarFile = new JarFile(input.toFile());
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(output.toFile().toPath()))) {
            for (String hookPath : new String[]{"hook.txt", "hook_core.txt"}) {
                JarEntry hook = jarFile.getJarEntry(hookPath);
                if (hook != null) try (InputStream hs = jarFile.getInputStream(hook);
                                       BufferedReader br = new BufferedReader(new InputStreamReader(hs, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            JarEntry entry = jarFile.getJarEntry(line.replace('.', '/') + ".class");
                            if (entry != null) {
                                InputStream is = jarFile.getInputStream(entry);
                                if (is == null) {
                                    transformer.logger.debug("Hooks container not found " + line);
                                    continue;
                                }
                                transformer.logger.debug("Parsing hooks container " + line);
                                transformer.registerHookContainer(Transformer.readAllBytes(is));
                            }
                        }
                    }
                }
            }

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream is = jarFile.getInputStream(entry);
                String name = entry.getName();
                JarEntry newEntry = new JarEntry(name);
                try {
                    jos.putNextEntry(newEntry);
                }catch (ZipException e) {
                    System.out.println("[WARNING]: " + e.getMessage());
                }
                if (!entry.isDirectory() && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    byte[] original = Transformer.readAllBytes(is);
                    byte[] transformed = transformer.transform(className, original);
                    jos.write(transformed);
                } else {
                    byte[] buf = Transformer.readAllBytes(is);
                    jos.write(buf);
                }
                jos.closeEntry();
                is.close();
            }
        }
        System.out.println("Wrote transformed jar to " + output);
    }
}
