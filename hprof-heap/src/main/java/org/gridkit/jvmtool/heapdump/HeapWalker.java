/**
 * Copyright 2014 Alexey Ragozin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gridkit.jvmtool.heapdump;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.gridkit.jvmtool.heapdump.PathStep.Move;
import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.ObjectFieldValue;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

public class HeapWalker {

    private static final Set<String> BOX_TYPES = new HashSet<String>();

    static {
        BOX_TYPES.add(Boolean.class.getName());
        BOX_TYPES.add(Byte.class.getName());
        BOX_TYPES.add(Short.class.getName());
        BOX_TYPES.add(Character.class.getName());
        BOX_TYPES.add(Integer.class.getName());
        BOX_TYPES.add(Long.class.getName());
        BOX_TYPES.add(Float.class.getName());
        BOX_TYPES.add(Double.class.getName());
    }

    private static final Map<String, InstanceConverter> CONVERTERS = new HashMap<String, InstanceConverter>();

    static {
        CONVERTERS.put(String.class.getName(), new InstanceConverter() {
            @Override
            public Object convert(Instance instance) {
                return stringValue(instance);
            }
        });
        InstanceConverter primitiveConverter = new InstanceConverter() {
            @Override
            public Object convert(Instance instance) {
                return primitiveValue(instance);
            }
        };
        InstanceConverter primitiveArrayConverter = new InstanceConverter() {
            @Override
            public Object convert(Instance instance) {
                return primitiveArrayValue(instance);
            }
        };
        for (String ptype : BOX_TYPES) {
            CONVERTERS.put(ptype, primitiveConverter);
        }
        CONVERTERS.put("boolean[]", primitiveArrayConverter);
        CONVERTERS.put("byte[]", primitiveArrayConverter);
        CONVERTERS.put("char[]", primitiveArrayConverter);
        CONVERTERS.put("short[]", primitiveArrayConverter);
        CONVERTERS.put("int[]", primitiveArrayConverter);
        CONVERTERS.put("long[]", primitiveArrayConverter);
        CONVERTERS.put("float[]", primitiveArrayConverter);
        CONVERTERS.put("double[]", primitiveArrayConverter);
    }

    private static PrimitiveParser BOOL_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Boolean.valueOf(x);
        }
    };

    private static PrimitiveParser BYTE_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Byte.valueOf(x);
        }
    };

    private static PrimitiveParser SHORT_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Short.valueOf(x);
        }
    };

    private static PrimitiveParser CHAR_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return x.charAt(0);
        }
    };

    private static PrimitiveParser INT_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Integer.valueOf(x);
        }
    };

    private static PrimitiveParser LONG_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Long.valueOf(x);
        }
    };

    private static PrimitiveParser FLOAT_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Float.valueOf(x);
        }
    };

    private static PrimitiveParser DOUBLE_PARSER = new PrimitiveParser() {
        @Override
        public Object toValue(String x) {
            return Double.valueOf(x);
        }
    };

    /**
     * Converts instances of few well know Java classes from dump to normal java
     * objects. <br/>
     * Following are supported classes
     * <li>{@link String}</li>
     * <li>{@link Boolean}</li>
     * <li>{@link Byte}</li>
     * <li>{@link Short}</li>
     * <li>{@link Character}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link Float}</li>
     * <li>{@link Double}</li>
     * <li>primitive arrays</li>
     * <li>object arrays</li>
     */
    /*
     * TBD <li>{@link Arrays#asList(Object...)}</li> <li>{@link ArrayList}
     * (subclasses would be reduced to {@link ArrayList})</li> <li>{@link HashMap}
     * (subclasses including {@link LinkedHashMap} would be reduced to {@link
     * ArrayList})</li> <li>{@link HashSet} (subclasses including {@link
     * LinkedHashSet} would be reduced to {@link ArrayList})</li>
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Instance obj) {
        if (obj == null) {
            return null;
        }
        JavaClass t = obj.getJavaClass();
        InstanceConverter c = CONVERTERS.get(obj.getJavaClass().getName());
        while (c == null && t.getSuperClass() != null) {
            t = t.getSuperClass();
            c = CONVERTERS.get(obj.getJavaClass().getName());
        }
        if (c == null) {
            // return instance as is
            return (T) obj;
        } else {
            return (T) c.convert(obj);
        }
    }

    /**
     * Converts value referenced for path from dump to normal java objects. Few well
     * known Java classes as long as primitive types are supported. <br/>
     * Following are supported classes
     * <li>{@link String}</li>
     * <li>{@link Boolean}</li>
     * <li>{@link Byte}</li>
     * <li>{@link Short}</li>
     * <li>{@link Character}</li>
     * <li>{@link Integer}</li>
     * <li>{@link Long}</li>
     * <li>{@link Float}</li>
     * <li>{@link Double}</li>
     * <li>primitive arrays</li>
     * <li>object arrays</li>
     */
    /*
     * TBD <li>{@link Arrays#asList(Object...)}</li> <li>{@link ArrayList}
     * (subclasses would be reduced to {@link ArrayList})</li> <li>{@link HashMap}
     * (subclasses including {@link LinkedHashMap} would be reduced to {@link
     * ArrayList})</li> <li>{@link HashSet} (subclasses including {@link
     * LinkedHashSet} would be reduced to {@link ArrayList})</li>
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Instance obj, String pathSpec) {
        PathStep[] steps = HeapPathParser.parsePath(pathSpec, true);
        InstanceFunction func = null;
        if (isTailingFunction(steps)) {
            func = ((FunctionStep)steps[steps.length - 1]).func;
            steps = Arrays.copyOf(steps, steps.length - 1);
        } else {
            func = ValueOfFunc.INSTANCE;
        }
        if (steps.length > 0 && steps[steps.length - 1] instanceof FieldStep) {
            PathStep[] shortPath = Arrays.copyOf(steps, steps.length - 1);
            FieldStep lastStep = (FieldStep) steps[steps.length - 1];
            String fieldName = lastStep.getFieldName();
            for (Instance i : collect(obj, shortPath)) {
                for (FieldValue fv : i.getFieldValues()) {
                    if ((fieldName == null && (!fv.getField().isStatic()))
                            || (fv.getField().getName().equals(fieldName))) {
                        if (fv instanceof ObjectFieldValue) {
                            return (T) func.apply(((ObjectFieldValue) fv).getInstance());
                        } else {
                            // have to use this as private package API is used behind scene
                            return (T) func.applyToField(i, fv);
                        }
                    }
                }
            }
            return null;
        } else if (steps.length > 0 && steps[steps.length - 1] instanceof ArrayIndexStep) {
            PathStep[] shortPath = Arrays.copyOf(steps, steps.length - 1);
            ArrayIndexStep lastStep = (ArrayIndexStep) steps[steps.length - 1];
            for (Instance i : collect(obj, shortPath)) {
                if (i instanceof PrimitiveArrayInstance) {
                    return (T) func.applyToArray((PrimitiveArrayInstance) i, lastStep.getIndex());
                } else {
                    for (Instance x : collect(i, new PathStep[] { lastStep })) {
                        return (T) func.apply(x);
                    }
                }
            }
            return null;

        } else {
            for (Instance i : collect(obj, steps)) {
                return (T) func.apply(i);
            }
            return null;
        }
    }

    private static boolean isTailingFunction(PathStep[] steps) {
        return steps.length > 0 && (steps[steps.length - 1] instanceof FunctionStep);
    }

    public static String stringValue(Instance obj) {
        if (obj == null)
            return null;

        if (!"java.lang.String".equals(obj.getJavaClass().getName()))
            throw new IllegalArgumentException(
                    "Is not a string: " + obj.getInstanceId() + " (" + obj.getJavaClass().getName() + ")");

        Boolean COMPACT_STRINGS = (Boolean) obj.getJavaClass().getValueOfStaticField("COMPACT_STRINGS");
        if (COMPACT_STRINGS == null)
            return stringValue_java8(obj); // We're pre Java 9

        Object valueInstance = obj.getValueOfField("value");
        PrimitiveArrayInstance chars = (PrimitiveArrayInstance) valueInstance;

        byte UTF16 = 1;
        byte coder = COMPACT_STRINGS ? (Byte) obj.getValueOfField("coder") : UTF16;
        int len = chars.getLength() >> coder;
        char[] text = new char[len];

        @SuppressWarnings({ "unchecked", "rawtypes" }) // generic recast
        List<String> values = (List) chars.getValues();
        if (coder == UTF16)
            for (int i = 0; i < text.length; i++) {
                text[i] = (char) ((Integer.parseInt(values.get(i * 2 + 1)) << 8)
                        | (Integer.parseInt(values.get(i * 2)) & 0xFF));
            }
        else
            for (int i = 0; i < text.length; i++)
                text[i] = (char) Integer.parseInt(values.get(i));

        return new String(text);
    }

    static String stringValue_java8(Instance obj) {
        if (obj == null) {
            return null;
        }
        if (!"java.lang.String".equals(obj.getJavaClass().getName())) {
            throw new IllegalArgumentException(
                    "Is not a string: " + obj.getInstanceId() + " (" + obj.getJavaClass().getName() + ")");
        }
        char[] text = null;
        int offset = 0;
        int len = -1;
        for (FieldValue f : obj.getFieldValues()) {
            Field ff = f.getField();
            if ("value".equals(ff.getName())) {
                PrimitiveArrayInstance chars = (PrimitiveArrayInstance) ((ObjectFieldValue) f).getInstance();
                text = new char[chars.getLength()];
                for (int i = 0; i != text.length; ++i) {
                    text[i] = ((String) chars.getValues().get(i)).charAt(0);
                }
            }
            // fields below were removed in Java 7
            else if ("offset".equals(ff.getName())) {
                offset = Integer.valueOf(f.getValue());
            } else if ("count".equals(ff.getName())) {
                len = Integer.valueOf(f.getValue());
            }
        }

        if (len < 0) {
            len = text.length;
        }

        return new String(text, offset, len);
    }

    @SuppressWarnings("unchecked")
    public static <T> T primitiveValue(Instance obj) {
        if (obj == null) {
            return null;
        }
        String className = obj.getJavaClass().getName();
        if (BOX_TYPES.contains(className)) {
            return (T) obj.getValueOfField("value");
        } else {
            throw new IllegalArgumentException(
                    "Is not a primitive wrapper: " + obj.getInstanceId() + " (" + obj.getJavaClass().getName() + ")");
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T primitiveArrayValue(Instance obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof PrimitiveArrayInstance) {
            PrimitiveArrayInstance pa = (PrimitiveArrayInstance) obj;
            String type = pa.getJavaClass().getName();
            PrimitiveParser parser;
            Object array;
            int len = pa.getLength();
            if ("boolean[]".equals(type)) {
                array = new boolean[len];
                parser = BOOL_PARSER;
            } else if ("byte[]".equals(type)) {
                array = new byte[len];
                parser = BYTE_PARSER;
            } else if ("char[]".equals(type)) {
                array = new char[len];
                parser = CHAR_PARSER;
            } else if ("short[]".equals(type)) {
                array = new short[len];
                parser = SHORT_PARSER;
            } else if ("int[]".equals(type)) {
                array = new int[len];
                parser = INT_PARSER;
            } else if ("long[]".equals(type)) {
                array = new long[len];
                parser = LONG_PARSER;
            } else if ("float[]".equals(type)) {
                array = new float[len];
                parser = FLOAT_PARSER;
            } else if ("double[]".equals(type)) {
                array = new double[len];
                parser = DOUBLE_PARSER;
            } else {
                throw new IllegalArgumentException(
                        "Is not a primitive array: " + obj.getInstanceId() + " (" + obj.getJavaClass().getName() + ")");
            }

            List<Object> values = pa.getValues();
            for (int i = 0; i != values.size(); ++i) {
                Object val = values.get(i);
                if (val instanceof String) {
                    val = parser.toValue((String) val);
                }
                Array.set(array, i, val);
            }

            return (T) array;
        } else {
            throw new IllegalArgumentException(
                    "Is not a primitive array: " + obj.getInstanceId() + " (" + obj.getJavaClass().getName() + ")");
        }
    }

    public static Iterable<Instance> walk(final Heap heap, final String path) {
        return new Iterable<Instance>() {

            @Override
            public Iterator<Instance> iterator() {
                HeapIterator hi = new HeapIterator(heap, path, true);
                return hi;
            }
        };
    }

    public static Iterable<Instance> walk(Instance root, String path) {
        return collect(root, HeapPathParser.parsePath(path, true));
    }

    public static Instance walkFirst(Instance root, String path) {
        Iterator<Instance> it = walk(root, path).iterator();
        if (it.hasNext()) {
            return it.next();
        } else {
            return null;
        }
    }

    public static String explainPath(Instance root, String path) {
        PathStep[] chain = HeapPathParser.parsePath(path, true);
        StringBuilder sb = new StringBuilder();
        Instance o = root;
        for (int i = 0; i != chain.length; ++i) {
            if (chain[i] instanceof TypeFilterStep) {
                continue;
            }

            try {
                Move m = chain[i].track(o).next();
                // JavaClass hostType = hostType(o.getJavaClass(), m.pathSpec);
                // hostType = hostType == null ? o.getJavaClass() : hostType;

                // sb.append("(" + shortName(hostType.getName()) + ")");
                sb.append("(" + shortName(o.getJavaClass().getName()) + ")");
                sb.append(m.pathSpec);
                o = m.instance;
            } catch (NoSuchElementException e) {
                sb.append("{failed: " + chain[i] + "}");
                break;
            }
        }
        return sb.toString();
    }

    public static void validateHeapPath(String path) {
        HeapPathParser.parsePath(path, true);
    }

    @SuppressWarnings("unused")
    private static JavaClass hostType(JavaClass type, String pathSpec) {
        if (pathSpec.startsWith(".")) {
            pathSpec = pathSpec.substring(1);
        }
        for (Field f : type.getFields()) {
            if (!f.isStatic() && f.getName().equals(pathSpec)) {
                return f.getDeclaringClass();
            }
        }
        if (type.getSuperClass() != null) {
            return hostType(type.getSuperClass(), pathSpec);
        }
        return null;
    }

    public static Set<JavaClass> filterTypes(String filter, Iterable<JavaClass> types) {
        PathStep[] steps = HeapPathParser.parsePath("(" + filter + ")", true);
        if (steps.length != 1 || !(steps[0] instanceof TypeFilterStep)) {
            throw new IllegalArgumentException("Bad type filter: " + filter);
        }
        TypeFilterStep f = (TypeFilterStep) steps[0];
        Set<JavaClass> result = new LinkedHashSet<JavaClass>();
        for (JavaClass jc : types) {
            if (f.evaluate(jc)) {
                result.add(jc);
            }
        }

        return result;
    }

    private static final String shortName(String name) {
        int c = name.lastIndexOf('.');
        if (c >= 0) {
            return "**." + name.substring(c + 1);
        } else {
            return name;
        }
    }

    static Set<Instance> collect(Instance instance, PathStep[] steps) {

        if (isTailingFunction(steps)) {
            throw new IllegalArgumentException("Instance function is not allowed. Path: " + steps.toString());
        }

        Set<Instance> active = new HashSet<Instance>();
        Set<Instance> next = new HashSet<Instance>();
        active.add(instance);

        for(PathStep step: steps) {
            for(Instance i: active) {
                Iterator<Instance> it = step.walk(i);
                while(it.hasNext()) {
                    Instance sub = it.next();
                    if (sub != null) {
                        next.add(sub);
                    }
                }
            }
            // swap buffers
            active.clear();
            Set<Instance> s = active;
            active = next;
            next = s;
            if (active.isEmpty()) {
                return active;
            }
        }

        return active;
    }

    static Set<Move> track(Instance instance, PathStep[] steps) {

        Set<Move> active = new HashSet<Move>();
        Set<Move> next = new HashSet<Move>();
        active.add(new Move("", instance));

        for(PathStep step: steps) {
            for(Move i: active) {
                Iterator<Move> it = step.track(i.instance);
                while(it.hasNext()) {
                    Move sub = it.next();
                    if (sub.instance != null) {
                        next.add(new Move(i.pathSpec + sub.pathSpec, sub.instance));
                    }
                }
            }
            // swap buffers
            active.clear();
            Set<Move> s = active;
            active = next;
            next = s;
            if (active.isEmpty()) {
                return active;
            }
        }

        return active;
    }

    private interface InstanceConverter {

        public Object convert(Instance instance);
    }

    private interface PrimitiveParser {

        public Object toValue(String x);
    }

    private static class HeapIterator implements Iterator<Instance> {

        private final RefSet visited;
        private final Set<JavaClass> classFilter;
        private final PathStep[] steps;
        private final Iterator<Instance> walker;
        private Iterator<Instance> pathWalker;
        private Instance next;

        public HeapIterator(Heap heap, String path, boolean strict) {
            this.visited = strict ? new RefSet() : null;

            steps = HeapPathParser.parsePath(path, false);
            if (steps[0] instanceof TypeFilterStep) {
                TypeFilterStep filterStep = (TypeFilterStep) steps[0];
                classFilter = new HashSet<JavaClass>(filterStep.filter(heap.getAllClasses()));
            } else if (steps[0] instanceof ArrayIndexStep) {
                Set<JavaClass> arrays = new HashSet<JavaClass>();
                for (JavaClass jc : heap.getAllClasses()) {
                    if (jc.isArray()) {
                        arrays.add(jc);
                    }
                }
                classFilter = arrays;
            } else if (steps[0] instanceof FieldStep) {
                FieldStep fieldStep = (FieldStep) steps[0];
                if (fieldStep.getFieldName() != null) {
                    Set<JavaClass> types = new HashSet<JavaClass>();
                    for (JavaClass jc : heap.getAllClasses()) {
                        if (hasField(jc, fieldStep.getFieldName())) {
                            types.add(jc);
                        }
                    }
                    classFilter = types;
                } else {
                    // no idea
                    classFilter = null;
                }
            } else {
                classFilter = null;
            }
            walker = heap.getAllInstancesIterator();
        }

        @Override
        public boolean hasNext() {
            if (next == null) {
                while (true) {
                    Instance n;
                    if (pathWalker != null && pathWalker.hasNext()) {
                        n = pathWalker.next();
                        if (visited != null) {
                            if (visited.getAndSet(n.getInstanceId(), true)) {
                                continue;
                            }
                        }
                        next = n;
                        break;
                    }
                    if (walker.hasNext()) {
                        n = walker.next();
                        if (classFilter != null && !classFilter.contains(n.getJavaClass())) {
                            continue;
                        } else {
                            pathWalker = collect(n, steps).iterator();
                        }
                    } else {
                        break;
                    }

                }
            }
            return next != null;
        }

        @Override
        public Instance next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            Instance result = next;
            next = null;
            return result;
        }

        private boolean hasField(JavaClass jc, String fieldName) {
            for (Field f : jc.getFields()) {
                if (fieldName.equals(f.getName())) {
                    return true;
                }
            }
            if (jc.getSuperClass() != null) {
                return hasField(jc.getSuperClass(), fieldName);
            }
            return false;
        }
    }
}
