package cn.tesseract.union;

import cn.tesseract.asm.Transformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

/**
 * Minimal TinyV1 remapper using ASM, replacing the net.fabricmc:tiny-remapper dependency.
 *
 * Two-pass design:
 *   Pass 1 – reads all .class entries to build the class hierarchy (superclass + interfaces).
 *            Then propagates method/field name mappings *upward* through that hierarchy so
 *            that interface and abstract-class declarations receive the same renamed symbol
 *            as their concrete implementations.
 *   Pass 2 – remaps every .class entry with ASM ClassRemapper using the enriched maps.
 */
public class TinyRemapper {

    /** intermediary class name -> named class name */
    private final Map<String, String> classMap = new HashMap<>();

    /** "owner\0fieldName\0fieldDescriptor" -> mapped name (intermediary namespace) */
    private final Map<String, String> fieldMap = new HashMap<>();

    /** "owner\0methodName\0methodDescriptor" -> mapped name (intermediary namespace) */
    private final Map<String, String> methodMap = new HashMap<>();

    // Hierarchy built from the game JAR (intermediary namespace)
    private final Map<String, String>       superMap     = new HashMap<>();
    private final Map<String, List<String>> interfaceMap = new HashMap<>();

    // -----------------------------------------------------------------------
    // Mappings loading
    // -----------------------------------------------------------------------

    public void loadMappings(Path mappingsPath) throws IOException {
        try (var reader = Files.newBufferedReader(mappingsPath, StandardCharsets.UTF_8)) {
            reader.readLine(); // "v1\tintermediary\tnamed"
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                String[] p = line.split("\t");
                switch (p[0]) {
                    case "CLASS":
                        if (p.length >= 3) classMap.put(p[1], p[2]);
                        break;
                    case "FIELD":
                        // FIELD  owner  descriptor  intermediaryName  namedName
                        if (p.length >= 5) fieldMap.put(p[1] + "\0" + p[3] + "\0" + p[2], p[4]);
                        break;
                    case "METHOD":
                        // METHOD  owner  descriptor  intermediaryName  namedName
                        if (p.length >= 5) methodMap.put(p[1] + "\0" + p[3] + "\0" + p[2], p[4]);
                        break;
                    default:
                        break;
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Hierarchy analysis (first pass)
    // -----------------------------------------------------------------------

    private void buildHierarchy(Map<String, byte[]> classes) {
        for (Map.Entry<String, byte[]> e : classes.entrySet()) {
            ClassReader cr = new ClassReader(e.getValue());
            String sup = cr.getSuperName();
            if (sup != null) superMap.put(e.getKey(), sup);
            String[] ifaces = cr.getInterfaces();
            if (ifaces != null && ifaces.length > 0)
                interfaceMap.put(e.getKey(), Arrays.asList(ifaces));
        }
    }

    /**
     * Propagate method/field mappings upward through the inheritance chain.
     * Example: if aaQ.a(String,Z)->getSaveLoader and aaQ implements Zw, then
     * Zw.a(String,Z) also gets mapped to getSaveLoader.
     */
    private void propagateMappings() {
        // Only propagate method mappings upward through the class hierarchy.
        // Method overriding requires that a renamed method keeps the same name in
        // both the parent declaration and every child override, so propagation is
        // correct and necessary for methods.
        //
        // Fields are NOT polymorphic: each class declares its own field slot and
        // there is no overriding.  Propagating field renames upward causes the
        // common ancestor to receive mappings from multiple unrelated subclass
        // fields that happen to share the same obfuscated name (e.g. EntityArrow.q,
        // EntityFireball.k, EntityFishHook.j all map to "ticksInAir"), which
        // results in the ancestor class having several distinct fields all renamed
        // to the same name and triggers a ClassFormatError: Duplicate field name.
        Map<String, String> methodExtras = new HashMap<>();
        for (Map.Entry<String, String> entry : methodMap.entrySet()) {
            String[] p = entry.getKey().split("\0", 3);
            propagateUp(p[0], p[1], p[2], entry.getValue(), methodMap, methodExtras);
        }
        methodMap.putAll(methodExtras);
    }

    private void propagateUp(String owner, String name, String desc, String mappedName,
                              Map<String, String> existing, Map<String, String> accumulator) {
        // Walk super-class chain
        String sup = superMap.get(owner);
        if (sup != null && !sup.equals("java/lang/Object")) {
            String key = sup + "\0" + name + "\0" + desc;
            if (!existing.containsKey(key) && !accumulator.containsKey(key)) {
                accumulator.put(key, mappedName);
                propagateUp(sup, name, desc, mappedName, existing, accumulator);
            }
        }
        // Walk interfaces
        for (String iface : interfaceMap.getOrDefault(owner, Collections.emptyList())) {
            String key = iface + "\0" + name + "\0" + desc;
            if (!existing.containsKey(key) && !accumulator.containsKey(key)) {
                accumulator.put(key, mappedName);
                propagateUp(iface, name, desc, mappedName, existing, accumulator);
            }
        }
    }

    // -----------------------------------------------------------------------
    // ASM Remapper
    // -----------------------------------------------------------------------

    public Remapper createAsmRemapper() {
        return new Remapper() {
            @Override
            public String map(String internalName) {
                return classMap.getOrDefault(internalName, internalName);
            }

            @Override
            public String mapFieldName(String owner, String name, String descriptor) {
                return fieldMap.getOrDefault(owner + "\0" + name + "\0" + descriptor, name);
            }

            @Override
            public String mapMethodName(String owner, String name, String descriptor) {
                return methodMap.getOrDefault(owner + "\0" + name + "\0" + descriptor, name);
            }
        };
    }

    // -----------------------------------------------------------------------
    // Entry point
    // -----------------------------------------------------------------------

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.err.println("Usage: <inputJar> <outputJar> <mappings> [fromNs] [toNs]");
            System.exit(2);
        }
        Path input      = Paths.get(args[0]);
        Path output     = Paths.get(args[1]);
        Path mappings   = Paths.get(args[2]);

        if (!Files.exists(input))    { System.err.println("Input jar not found: "   + input);    System.exit(3); }
        if (!Files.exists(mappings)) { System.err.println("Mappings not found: "    + mappings); System.exit(3); }

        TinyRemapper remapper = new TinyRemapper();
        remapper.loadMappings(mappings);

        // Pass 1 – read all bytes, build hierarchy
        Map<String, byte[]> classBytes = new LinkedHashMap<>();
        Map<String, byte[]> otherBytes = new LinkedHashMap<>();

        try (JarFile jar = new JarFile(input.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                try (InputStream is = jar.getInputStream(entry)) {
                    byte[] bytes = Transformer.readAllBytes(is);
                    if (!entry.isDirectory() && name.endsWith(".class")) {
                        ClassReader cr = new ClassReader(bytes);
                        classBytes.put(cr.getClassName(), bytes);
                    } else {
                        otherBytes.put(name, bytes);
                    }
                }
            }
        }

        remapper.buildHierarchy(classBytes);
        remapper.propagateMappings();          // <-- key step
        Remapper asmRemapper = remapper.createAsmRemapper();

        // Pass 2 – remap and write
        try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(output))) {
            for (Map.Entry<String, byte[]> e : classBytes.entrySet()) {
                String newName = asmRemapper.map(e.getKey());
                ClassWriter writer = new ClassWriter(0);
                new ClassReader(e.getValue()).accept(new ClassRemapper(writer, asmRemapper), ClassReader.EXPAND_FRAMES);
                try {
                    jos.putNextEntry(new JarEntry(newName + ".class"));
                    jos.write(writer.toByteArray());
                    jos.closeEntry();
                } catch (ZipException ze) {
                    System.out.println("[WARNING] Skipping duplicate class entry: " + e.getKey() + " -> " + newName + ".class");
                }
            }

            for (Map.Entry<String, byte[]> e : otherBytes.entrySet()) {
                try {
                    jos.putNextEntry(new JarEntry(e.getKey()));
                    jos.write(e.getValue());
                    jos.closeEntry();
                } catch (ZipException ze) {
                    System.out.println("[WARNING] Skipping duplicate resource entry: " + e.getKey());
                }
            }
        }

        System.out.println("Remapped jar written to " + output);
    }
}
