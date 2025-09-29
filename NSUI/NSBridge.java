package NSUI;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class NSBridge {
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup foundation;

    public static final StructLayout CGPoint = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("x"),
            ValueLayout.JAVA_DOUBLE.withName("y")
    ).withName("CGPoint");

    public static final StructLayout CGSize = MemoryLayout.structLayout(
            ValueLayout.JAVA_DOUBLE.withName("width"),
            ValueLayout.JAVA_DOUBLE.withName("height")
    ).withName("CGSize");

    public static final StructLayout CGRect = MemoryLayout.structLayout(
            CGPoint.withName("origin"),
            CGSize.withName("size")
    ).withName("CGRect");

    public record CGRectRecord(double x, double y, double width, double height) {}

    private static final MethodHandle objc_getClass;
    private static final MethodHandle sel_getUid;
    private static final MethodHandle objc_msgSend_retId;
    private static final MethodHandle objc_msgSend_retId_long;
    private static final MethodHandle objc_msgSend_initWithFrame;
    private static final MethodHandle objc_msgSend_set_long;
    private static final MethodHandle objc_msgSend_set_bool;
    private static final MethodHandle objc_msgSend_set_double;
    private static final MethodHandle objc_msgSend_set_ptr;
    private static final MethodHandle objc_msgSend_addSubview;
    private static final MethodHandle objc_msgSend_stringWithUTF8;
    private static final MethodHandle objc_msgSend_get_ptr_long;


    static {
        SymbolLookup foundationLookup = SymbolLookup.libraryLookup("/System/Library/Frameworks/Foundation.framework/Foundation", Arena.global());
        foundation = foundationLookup.or(Linker.nativeLinker().defaultLookup());

        var objc_getClass_addr = foundation.find("objc_getClass").get();
        var sel_getUid_addr = foundation.find("sel_getUid").get();
        var objc_msgSend_addr = foundation.find("objc_msgSend").get();

        objc_getClass = linker.downcallHandle(objc_getClass_addr, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        sel_getUid = linker.downcallHandle(sel_getUid_addr, FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

        objc_msgSend_retId = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        objc_msgSend_retId_long = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        objc_msgSend_initWithFrame = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, CGRect));
        objc_msgSend_set_long = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
        objc_msgSend_set_bool = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_BOOLEAN));
        objc_msgSend_set_double = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE));
        objc_msgSend_set_ptr = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        objc_msgSend_addSubview = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        objc_msgSend_stringWithUTF8 = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        objc_msgSend_get_ptr_long = linker.downcallHandle(objc_msgSend_addr,
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
    }

    private static final Map<String, MemorySegment> classCache = new HashMap<>();
    private static final Map<String, MemorySegment> selectorCache = new HashMap<>();

    public static MemorySegment getClass(String name) {
        return classCache.computeIfAbsent(name, n -> {
            try (var arena = Arena.ofConfined()) {
                var nativeString = arena.allocateFrom(n);
                return (MemorySegment) objc_getClass.invokeExact(nativeString);
            } catch (Throwable e) { throw new RuntimeException(e); }
        });
    }

    public static MemorySegment getSelector(String name) {
        return selectorCache.computeIfAbsent(name, n -> {
            try (var arena = Arena.ofConfined()) {
                var nativeString = arena.allocateFrom(n);
                return (MemorySegment) sel_getUid.invokeExact(nativeString);
            } catch (Throwable e) { throw new RuntimeException(e); }
        });
    }

    public static MemorySegment createNSString(String s) {
        try (var arena = Arena.ofConfined()) {
            var nativeString = arena.allocateFrom(s);
            return (MemorySegment) objc_msgSend_stringWithUTF8.invokeExact(getClass("NSString"), getSelector("stringWithUTF8String:"), nativeString);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static MemorySegment sendMessage(MemorySegment receiver, String selector) {
        try { return (MemorySegment) objc_msgSend_retId.invokeExact(receiver, getSelector(selector)); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static MemorySegment sendMessage(MemorySegment receiver, String selector, long arg) {
        try { return (MemorySegment) objc_msgSend_retId_long.invokeExact(receiver, getSelector(selector), arg); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static MemorySegment sendMessageRetPtr(MemorySegment receiver, String selector, long arg) {
        try { return (MemorySegment) objc_msgSend_get_ptr_long.invokeExact(receiver, getSelector(selector), arg); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void sendMessage(MemorySegment receiver, String selector, boolean arg) {
        try { objc_msgSend_set_bool.invokeExact(receiver, getSelector(selector), arg); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void sendMessage(MemorySegment receiver, String selector, double arg) {
        try { objc_msgSend_set_double.invokeExact(receiver, getSelector(selector), arg); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void sendMessage(MemorySegment receiver, String selector, MemorySegment arg) {
        try { objc_msgSend_set_ptr.invokeExact(receiver, getSelector(selector), arg); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static MemorySegment sendMessage(MemorySegment receiver, String selector, CGRectRecord frameRecord) {
        try (var arena = Arena.ofConfined()) {
            var nativeFrame = arena.allocate(CGRect);
            nativeFrame.set(ValueLayout.JAVA_DOUBLE, CGRect.byteOffset(MemoryLayout.PathElement.groupElement("origin"), MemoryLayout.PathElement.groupElement("x")), frameRecord.x());
            nativeFrame.set(ValueLayout.JAVA_DOUBLE, CGRect.byteOffset(MemoryLayout.PathElement.groupElement("origin"), MemoryLayout.PathElement.groupElement("y")), frameRecord.y());
            nativeFrame.set(ValueLayout.JAVA_DOUBLE, CGRect.byteOffset(MemoryLayout.PathElement.groupElement("size"), MemoryLayout.PathElement.groupElement("width")), frameRecord.width());
            nativeFrame.set(ValueLayout.JAVA_DOUBLE, CGRect.byteOffset(MemoryLayout.PathElement.groupElement("size"), MemoryLayout.PathElement.groupElement("height")), frameRecord.height());
            return (MemorySegment) objc_msgSend_initWithFrame.invokeExact(receiver, getSelector(selector), nativeFrame);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void sendMessage(MemorySegment receiver, String selector, MemorySegment arg1, long arg2, MemorySegment arg3) {
        try { objc_msgSend_addSubview.invokeExact(receiver, getSelector(selector), arg1, arg2, arg3); }
        catch (Throwable e) { throw new RuntimeException(e); }
    }
}