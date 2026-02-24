package cn.tesseract.union;

import cn.tesseract.asm.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Preprocessor {
    public static String side = "unknown";
    public static HashMap<String, List<Consumer<ClassNode>>> transformers = new HashMap<>();

    public static void transform(String className, ClassNode node) {
        node.access = ~(~node.access | Modifier.FINAL | Modifier.PRIVATE | Modifier.PROTECTED) | Modifier.PUBLIC;
        for (MethodNode method : node.methods)
            method.access = ~(~method.access | Modifier.FINAL | Modifier.PRIVATE | Modifier.PROTECTED) | Modifier.PUBLIC;

        if (className.equals("net.minecraft.client.main.Main") || className.equals("net.minecraft.Minecraft") || className.equals("net.minecraft.ServerConfigurationManager"))
            for (FieldNode field : node.fields)
                field.access = ~(~field.access | Modifier.FINAL | Modifier.PRIVATE | Modifier.PROTECTED) | Modifier.PUBLIC;
        else for (FieldNode field : node.fields)
            field.access = ~(~field.access | Modifier.PRIVATE | Modifier.PROTECTED) | Modifier.PUBLIC;
    }

    static {
        registerNodeTransformer("net.minecraft.Minecraft", classNode -> {
            MethodNode constructor = new MethodNode(Modifier.PUBLIC, "<init>", "()V", null, null);
            constructor.visitCode();
            constructor.visitVarInsn(Opcodes.ALOAD, 0);
            constructor.visitMethodInsn(183, "java/lang/Object", "<init>", "()V", false);
            constructor.visitInsn(Opcodes.RETURN);
            constructor.visitEnd();
            classNode.methods.add(constructor);
        });
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: <inputJar> <outputJar> [side]");
            System.exit(2);
        }
        Path input = Paths.get(args[0]);
        Path output = Paths.get(args[1]);
        if (args.length > 2) {
            side = args[2];
        }
        if (!Files.exists(input)) {
            System.err.println("Input jar not found: " + input);
            System.exit(3);
        }

        try (JarFile jarFile = new JarFile(input.toFile());
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(output.toFile()))) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream is = jarFile.getInputStream(entry);
                String name = entry.getName();
                JarEntry newEntry = new JarEntry(name);
                jos.putNextEntry(newEntry);
                if (!entry.isDirectory() && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    byte[] classBytes = Transformer.readAllBytes(is);

                    List<Consumer<ClassNode>> transformers = Preprocessor.transformers.get(className);

                    ClassNode classNode = new ClassNode();
                    ClassReader classReader = new ClassReader(classBytes);

                    classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

                    transform(className, classNode);

                    if (transformers != null) {
                        Iterator<Consumer<ClassNode>> it = transformers.iterator();
                        while (it.hasNext()) {
                            it.next().accept(classNode);
                            it.remove();
                        }
                    }

                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    classNode.accept(classWriter);

                    jos.write(classWriter.toByteArray());
                } else {
                    byte[] buf = Transformer.readAllBytes(is);
                    jos.write(buf);
                }
                jos.closeEntry();
                is.close();
            }
        }
    }

    public static void registerNodeTransformer(String className, Consumer<ClassNode> transformer) {
        List<Consumer<ClassNode>> list = transformers.computeIfAbsent(className.replace('/', '.'), k -> new ArrayList<>());
        list.add(transformer);
    }
}
