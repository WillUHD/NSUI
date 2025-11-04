package NSUI;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;

import static java.lang.foreign.FunctionDescriptor.*;
import static java.lang.foreign.ValueLayout.*;

public final class NSUICore {
    private static final Linker linker = Linker.nativeLinker();
    private static final SymbolLookup objc = SymbolLookup.libraryLookup(
            "/usr/lib/libobjc.A.dylib", Arena.global());
    private static final SymbolLookup coreGraphics = SymbolLookup.libraryLookup(
            "/System/Library/Frameworks/ApplicationServices.framework/ApplicationServices", Arena.global());

    public static final StructLayout CGPoint = MemoryLayout.structLayout(
            JAVA_DOUBLE.withName("x"), JAVA_DOUBLE.withName("y"));
    public static final StructLayout CGSize = MemoryLayout.structLayout(
            JAVA_DOUBLE.withName("width"), JAVA_DOUBLE.withName("height"));
    public static final StructLayout CGRect = MemoryLayout.structLayout(
            CGPoint.withName("origin"), CGSize.withName("size"));
    public record CGRectRecord(double x, double y, double width, double height) {}
    public record CGPointRecord(double x, double y) {}

    private static final Map<String, MemorySegment> selectorCache = new HashMap<>();
    private static final Map<String, MemorySegment> classCache = new HashMap<>();
    private static final MethodHandle objc_getClass,
                                      sel_getUid,
                                      class_getInstMethod,
                                      class_addMethod,
                                      method_exchangeImplementations,
                                      objc_getAssociatedObject,
                                      objc_setAssociatedObject;
    private static final MethodHandle msgSend_retPtr,
                                      msgSend_retPtr_long,
                                      msgSend_retLong,
                                      msgSend_void,
                                      msgSend_void_long,
                                      msgSend_void_bool,
                                      msgSend_void_ptr,
                                      msgSend_initWithFrame,
                                      msgSend_fpret,
                                      msgSend_retPtr_ptrArg,
                                      msgSend_void_double;
    private static final MethodHandle msgSend_retCGPoint,
                                      msgSend_retCGRect,
                                      msgSend_void_ptr_ptr;
    private static final MethodHandle msgSend_void_CGRect;
    private static final MethodHandle cgsMainConnectionID,
                                      cgsSetWindowBackgroundBlurRadius;
    public static final MemorySegment titlebarHeight = Arena.global().allocateFrom("cw_titlebarHeight");
    public static final MemorySegment redTrafficOffset = Arena.global().allocateFrom("cw_closeButtonOffset");
    private static final MemorySegment swizzledTitlebarHeight,
                                       swizzledRedTrafficOrigin,
                                       swizzledMinOffset;

    static {
        try {
            var addr = objc.find("objc_msgSend").get();
            var fpret = objc.find("objc_msgSend_fpret").get();
            var stret = objc.find("objc_msgSend_stret").get();

            objc_getClass = linker.downcallHandle(objc.find("objc_getClass").get(),
                    of(ADDRESS, ADDRESS));
            sel_getUid = linker.downcallHandle(objc.find("sel_getUid").get(),
                    of(ADDRESS, ADDRESS));
            class_getInstMethod = linker.downcallHandle(objc.find("class_getInstanceMethod").get(),
                    of(ADDRESS, ADDRESS, ADDRESS));
            class_addMethod = linker.downcallHandle(objc.find("class_addMethod").get(),
                    of(ValueLayout.JAVA_BOOLEAN, ADDRESS, ADDRESS, ADDRESS, ADDRESS));
            method_exchangeImplementations = linker.downcallHandle(objc.find("method_exchangeImplementations").get(),
                    ofVoid(ADDRESS, ADDRESS));
            objc_getAssociatedObject = linker.downcallHandle(objc.find("objc_getAssociatedObject").get(),
                    of(ADDRESS, ADDRESS, ADDRESS));
            objc_setAssociatedObject = linker.downcallHandle(objc.find("objc_setAssociatedObject").get(),
                    ofVoid(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG));

            msgSend_retPtr = linker.downcallHandle(addr, of(ADDRESS, ADDRESS, ADDRESS));
            msgSend_retPtr_long = linker.downcallHandle(addr, of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG));
            msgSend_retLong = linker.downcallHandle(addr, of(JAVA_LONG, ADDRESS, ADDRESS));
            msgSend_void = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS));
            msgSend_void_long = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, JAVA_LONG));
            msgSend_void_bool = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, JAVA_BOOLEAN));
            msgSend_void_ptr = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, ADDRESS));
            msgSend_void_double = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, JAVA_DOUBLE));
            msgSend_initWithFrame = linker.downcallHandle(addr, of(ADDRESS, ADDRESS, ADDRESS, CGRect));
            msgSend_fpret = linker.downcallHandle(fpret, of(JAVA_DOUBLE, ADDRESS, ADDRESS));
            msgSend_retPtr_ptrArg = linker.downcallHandle(addr, of(ADDRESS, ADDRESS, ADDRESS, ADDRESS));
            msgSend_retCGPoint = linker.downcallHandle(addr, of(CGPoint, ADDRESS, ADDRESS));
            msgSend_retCGRect = linker.downcallHandle(stret, ofVoid(ADDRESS, ADDRESS, ADDRESS));
            msgSend_void_ptr_ptr = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, ADDRESS, ADDRESS));
            msgSend_void_CGRect = linker.downcallHandle(addr, ofVoid(ADDRESS, ADDRESS, CGRect));

            cgsMainConnectionID = linker.downcallHandle(
                    coreGraphics.find("CGSMainConnectionID").get(),
                    of(ValueLayout.JAVA_INT));
            cgsSetWindowBackgroundBlurRadius = linker.downcallHandle(
                    coreGraphics.find("CGSSetWindowBackgroundBlurRadius").get(),
                    ofVoid(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, JAVA_LONG));

            swizzledTitlebarHeight = getSelector("_cw_titlebarHeight");
            swizzledRedTrafficOrigin = getSelector("_cw_closeButtonOrigin");
            swizzledMinOffset = getSelector("_cw_minYTitlebarButtonsOffset");
            patchNSThemeFrame();
        } catch (Throwable e) {throw new RuntimeException("Initialization error", e);}
    }

    private static void swizzle(MemorySegment cls,
                                String orig,
                                String newJava,
                                MethodType newType,
                                String newTypeEncoding,
                                FunctionDescriptor upcallDescriptor) throws Throwable {
        var newSelectorName = "_cw_" + orig.substring(1);
        var newSelector = getSelector(newSelectorName);
        var b = (boolean) class_addMethod.invokeExact(
                cls,
                newSelector,
                linker.upcallStub(MethodHandles.lookup().findStatic(NSUICore.class, newJava, newType),
                                  upcallDescriptor,
                                  Arena.global()),
                Arena.global().allocateFrom(newTypeEncoding));
        var originalMethod = (MemorySegment) class_getInstMethod.invokeExact(cls, getSelector(orig));
        var newMethod = (MemorySegment) class_getInstMethod.invokeExact(cls, newSelector);
        method_exchangeImplementations.invokeExact(originalMethod, newMethod);
    }

    private static void patchNSThemeFrame() throws Throwable {
        MemorySegment themeFrameClass = getClass("NSThemeFrame");
        swizzle(themeFrameClass,
                "_titlebarHeight",
                "getCustomTitlebarHeight",
                MethodType.methodType(double.class, MemorySegment.class, MemorySegment.class),
                "d@:",
                of(JAVA_DOUBLE, ADDRESS, ADDRESS));
        swizzle(themeFrameClass,
                "_closeButtonOrigin",
                "getCustomCloseButtonOrigin",
                MethodType.methodType(MemorySegment.class, MemorySegment.class, MemorySegment.class),
                "{CGPoint=dd}@:",
                of(CGPoint, ADDRESS, ADDRESS));
        swizzle(themeFrameClass,
                "_minYTitlebarButtonsOffset",
                "getCustomMinYOffset",
                MethodType.methodType(double.class, MemorySegment.class, MemorySegment.class),
                "d@:",
                of(JAVA_DOUBLE, ADDRESS, ADDRESS));
    }

    public static double getCustomTitlebarHeight(MemorySegment self, MemorySegment _cmd) {
        try {
            var associatedObject = (MemorySegment) objc_getAssociatedObject.invokeExact(self, titlebarHeight);
            if (associatedObject != null && !associatedObject.equals(MemorySegment.NULL))
                return (double) msgSend_fpret.invokeExact(associatedObject, getSelector("doubleValue"));
            return (double) msgSend_fpret.invokeExact(self, swizzledTitlebarHeight);
        } catch (Throwable e) { return 28.0; }
    }

    public static MemorySegment getCustomCloseButtonOrigin(MemorySegment self, MemorySegment _cmd) {
        Arena arena = Arena.ofConfined();
        try {
            var originalOrigin = (MemorySegment) msgSend_retCGPoint.invoke(arena, self, swizzledRedTrafficOrigin);
            var associatedObject = (MemorySegment) objc_getAssociatedObject.invokeExact(self, redTrafficOffset);

            if (associatedObject != null && !associatedObject.equals(MemorySegment.NULL)) {
                double xOffset = (double) msgSend_fpret.invokeExact(associatedObject, getSelector("doubleValue"));
                double originalX = originalOrigin.get(JAVA_DOUBLE, 0);
                originalOrigin.set(JAVA_DOUBLE, 0, originalX + xOffset);
            }
            return originalOrigin;
        } catch (Throwable e) {
            var errorPoint = arena.allocate(CGPoint);
            errorPoint.set(JAVA_DOUBLE, 0, 7.0);
            errorPoint.set(JAVA_DOUBLE, 8, 7.0);
            return errorPoint;
        }
    }

    public static double getCustomMinYOffset(MemorySegment self, MemorySegment _cmd) {
        try {
            var heightObject = (MemorySegment) objc_getAssociatedObject.invokeExact(self, titlebarHeight);
            if (heightObject != null && !heightObject.equals(MemorySegment.NULL)) {
                var titlebarHeight = (double) msgSend_fpret.invokeExact(heightObject, getSelector("doubleValue"));
                if (titlebarHeight > 0) {
                    var window = sendMessage(self, "window");
                    long styleMask = sendMessageRetLong(window, "styleMask");
                    var nsWindowClass = getClass("NSWindow");
                    var button = sendMessageRetPtr(nsWindowClass,
                                                   "standardWindowButton:forStyleMask:",
                                                   0L,
                                                   styleMask);
                    try (Arena arena = Arena.ofConfined()) {
                        MemorySegment frame = arena.allocate(CGRect);
                        msgSend_retCGRect.invokeExact(frame, button, getSelector("frame"));
                        double buttonHeight = frame.get(JAVA_DOUBLE, 16);
                        return (buttonHeight - titlebarHeight) / 2.0;
                    }
                }
            }
            return (double) msgSend_fpret.invokeExact(self, swizzledMinOffset);
        } catch (Throwable e) {return 0;}
    }

    public static MemorySegment getClearColor() {
        try {
            MemorySegment nsColorClass = getClass("NSColor");
            return sendMessage(nsColorClass, "clearColor");
        } catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment getClass(String name) {
        return classCache.computeIfAbsent(name, n -> {
            try {
                return (MemorySegment) objc_getClass.invokeExact(Arena.global().allocateFrom(n));
            } catch (Throwable e) {throw new RuntimeException(e);}
        });
    }

    public static MemorySegment getSelector(String name) {
        return selectorCache.computeIfAbsent(name, n -> {
            try {
                return (MemorySegment) sel_getUid.invokeExact(Arena.global().allocateFrom(n));
            } catch (Throwable e) {throw new RuntimeException(e);}
        });
    }

    public static CGRectRecord getFrame(MemorySegment receiver) {
        try (var arena = Arena.ofConfined()) {
            MemorySegment frame = arena.allocate(CGRect);
            msgSend_retCGRect.invokeExact(frame, receiver, getSelector("frame"));
            return new CGRectRecord(
                    frame.get(JAVA_DOUBLE, 0),
                    frame.get(JAVA_DOUBLE, 8),
                    frame.get(JAVA_DOUBLE, 16),
                    frame.get(JAVA_DOUBLE, 24)
            );
        } catch (Throwable e) {throw new RuntimeException("Can't get frame", e);}
    }

    public static void sendMessageSetFrame(MemorySegment receiver, String selector, CGRectRecord frameRecord) {
        try (var arena = Arena.ofConfined()) {
            var nativeFrame = arena.allocate(CGRect);
            nativeFrame.set(JAVA_DOUBLE, 0, frameRecord.x());
            nativeFrame.set(JAVA_DOUBLE, 8, frameRecord.y());
            nativeFrame.set(JAVA_DOUBLE, 16, frameRecord.width());
            nativeFrame.set(JAVA_DOUBLE, 24, frameRecord.height());
            msgSend_void_CGRect.invokeExact(receiver, getSelector(selector), nativeFrame);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static MemorySegment createNSNumber(double value) {
        try {
            var nsNumberClass = getClass("NSNumber");
            var numberWithDouble = linker.downcallHandle(
                    objc.find("objc_msgSend").get(),
                    of(ADDRESS, ADDRESS, ADDRESS, JAVA_DOUBLE));
            return (MemorySegment) numberWithDouble.invokeExact(nsNumberClass,
                                                                getSelector("numberWithDouble:"),
                                                                value);
        } catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment createNSString(String s) {
        try {
            return (MemorySegment) msgSend_retPtr_ptrArg.invokeExact(
                    getClass("NSString"),
                    getSelector("stringWithUTF8String:"),
                    Arena.global().allocateFrom(s));
        } catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void setAssociatedObject(MemorySegment object, MemorySegment key, MemorySegment value) {
        try {objc_setAssociatedObject.invokeExact(object, key, value, 5L);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment sendMessage(MemorySegment receiver, String selector) {
        try {return (MemorySegment) msgSend_retPtr.invokeExact(receiver, getSelector(selector));}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendVoidMessage(MemorySegment receiver, String selector) {
        try {msgSend_void.invokeExact(receiver, getSelector(selector));}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver, String selector, long arg) {
        try {msgSend_void_long.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment sendMessageRetPtr(MemorySegment receiver, String selector, long arg) {
        try {return (MemorySegment) msgSend_retPtr_long.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment sendMessageRetPtr(MemorySegment receiver, String selector, MemorySegment arg) {
        try {return (MemorySegment) msgSend_retPtr_ptrArg.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment sendMessageRetPtr(MemorySegment receiver,
                                                  String selector,
                                                  long arg1,
                                                  long arg2) {
        try {var handle = linker.downcallHandle(
                objc.find("objc_msgSend").get(),
                of(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG, JAVA_LONG));
            return (MemorySegment) handle.invokeExact(receiver, getSelector(selector), arg1, arg2);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver, String selector, boolean arg) {
        try {msgSend_void_bool.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver, String selector, MemorySegment arg) {
        try {msgSend_void_ptr.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver,
                                   String selector,
                                   MemorySegment arg1,
                                   MemorySegment arg2) {
        try {msgSend_void_ptr_ptr.invokeExact(receiver, getSelector(selector), arg1, arg2);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static long sendMessageRetLong(MemorySegment receiver, String selector) {
        try {return (long) msgSend_retLong.invokeExact(receiver, getSelector(selector));}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver,
                                   String selector,
                                   MemorySegment arg1,
                                   long arg2,
                                   MemorySegment arg3) {
        try {MethodHandle handle = linker.downcallHandle(
                objc.find("objc_msgSend").get(),
                ofVoid(ADDRESS, ADDRESS, ADDRESS, JAVA_LONG, ADDRESS));
            handle.invokeExact(receiver, getSelector(selector), arg1, arg2, arg3);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static MemorySegment sendMessageInitWithFrame(MemorySegment receiver,
                                                         String selector,
                                                         CGRectRecord frameRecord) {
        try (var arena = Arena.ofConfined()) {
            var nativeFrame = arena.allocate(CGRect);
            nativeFrame.set(JAVA_DOUBLE, 0, frameRecord.x());
            nativeFrame.set(JAVA_DOUBLE, 8, frameRecord.y());
            nativeFrame.set(JAVA_DOUBLE, 16, frameRecord.width());
            nativeFrame.set(JAVA_DOUBLE, 24, frameRecord.height());
            return (MemorySegment) msgSend_initWithFrame.invokeExact(receiver, getSelector(selector), nativeFrame);
        } catch (Throwable e) {throw new RuntimeException(e); }
    }

    public static int getMainConnectionID() {
        try {return (int) cgsMainConnectionID.invokeExact();}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void setWindowBackgroundBlurRadius(int cid, int wid, long blur) {
        try {cgsSetWindowBackgroundBlurRadius.invokeExact(cid, wid, blur);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static void sendMessage(MemorySegment receiver, String selector, double arg) {
        try {msgSend_void_double.invokeExact(receiver, getSelector(selector), arg);}
        catch (Throwable e) {throw new RuntimeException(e);}
    }
}