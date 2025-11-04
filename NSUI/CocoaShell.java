package NSUI;

import org.eclipse.swt.widgets.Shell;
import java.lang.foreign.MemorySegment;

public class CocoaShell {

    private final MemorySegment window;
    private final MemorySegment effect;
    private final MemorySegment shellView;

    CocoaShell(Shell shell) {
        NSUICore.getSelector("init");
        this.shellView = MemorySegment.ofAddress(shell.view.id);
        NSUICore.sendMessage(shellView, "setWantsLayer:", true);
        MemorySegment layer = NSUICore.sendMessage(shellView, "layer");
        MemorySegment clearNSColor = NSUICore.sendMessage(NSUICore.getClass("NSColor"), "clearColor");
        MemorySegment clearCGColor = NSUICore.sendMessage(clearNSColor, "CGColor");
        NSUICore.sendMessage(layer, "setBackgroundColor:", clearCGColor);
        NSUICore.sendMessage(shellView, "setOpaque:", false);
        this.window = NSUICore.sendMessage(shellView, "window");
        var bounds = shell.getClientArea();
        var frame = new NSUICore.CGRectRecord(0, 0, bounds.width, bounds.height);
        var effectViewClass = NSUICore.getClass("NSVisualEffectView");
        var effectView = NSUICore.sendMessage(effectViewClass, "alloc");
        this.effect = NSUICore.sendMessageInitWithFrame(effectView, "initWithFrame:", frame);
        long mask = 1L << 1 | 1L << 4;
        NSUICore.sendMessage(this.effect, "setAutoresizingMask:", mask);
        NSUICore.sendMessage(this.effect, "setBlendingMode:", 2L);
        NSUICore.sendMessage(this.effect, "setState:", 1L);
        NSUICore.sendMessage(this.effect, "setMaskImage:", MemorySegment.NULL);
    }

    private MemorySegment getThemeFrame() {
        MemorySegment contentView = NSUICore.sendMessage(window, "contentView");
        return NSUICore.sendMessage(contentView, "superview");
    }

    MemorySegment getStandardWindowButton(NSWindow.TrafficLights light) {
        return NSUICore.sendMessageRetPtr(window, "standardWindowButton:", light.getValue());
    }

    CocoaShell setStyleMask(NSWindow.StyleMask... m) {
        var combinedMask = 0;
        for (var v : m) combinedMask |= v.getValue();
        NSUICore.sendMessage(window, "setStyleMask:", (long) combinedMask);
        return this;
    }

    CocoaShell setTitlebarAppearsTransparent(boolean isTransparent) {
        NSUICore.sendMessage(window, "setTitlebarAppearsTransparent:", isTransparent);
        return this;
    }

    CocoaShell setTitleVisibility(boolean isVisible) {
        NSUICore.sendMessage(window, "setTitleVisibility:", isVisible ? 0L : 1L);
        return this;
    }

    CocoaShell setTitle(String title) {
        NSUICore.sendMessage(window, "setTitle:", NSUICore.createNSString(title));
        return this;
    }

    CocoaShell setMovableByWindowBackground(boolean isMovable) {
        NSUICore.sendMessage(window, "setMovableByWindowBackground:", isMovable);
        return this;
    }

    CocoaShell setMaterial(NSWindow.Materials m) {
        NSUICore.sendMessage(effect, "setMaterial:", m.getValue());
        return this;
    }

    CocoaShell setBlurAmount(double blur) {
        int cid = NSUICore.getMainConnectionID();
        int wid = (int) NSUICore.sendMessageRetLong(window, "windowNumber");
        NSUICore.setWindowBackgroundBlurRadius(cid, wid, (long) blur);
        return this;
    }

    CocoaShell setTitlebarHeight(double height) {
        MemorySegment themeFrame = getThemeFrame();
        MemorySegment number = NSUICore.createNSNumber(height);
        NSUICore.setAssociatedObject(themeFrame, NSUICore.titlebarHeight, number);
        NSUICore.sendVoidMessage(themeFrame, "_resetTitleBarButtons");
        return this;
    }

    CocoaShell setTrafficLightsOffset(NSUICore.CGPointRecord offset) {
        MemorySegment themeFrame = getThemeFrame();
        MemorySegment number = NSUICore.createNSNumber(offset.x());
        NSUICore.setAssociatedObject(themeFrame, NSUICore.redTrafficOffset, number);
        NSUICore.sendVoidMessage(themeFrame, "_resetTitleBarButtons");
        return this;
    }

    CocoaShell setAppearance(NSWindow.Appearance appearance) {
        MemorySegment nsAppearanceClass = NSUICore.getClass("NSAppearance");
        MemorySegment appearanceName = NSUICore.createNSString(appearance.getName());
        MemorySegment appearanceObject = NSUICore.sendMessageRetPtr(nsAppearanceClass, "appearanceNamed:", appearanceName);
        NSUICore.sendMessage(window, "setAppearance:", appearanceObject);

        MemorySegment themeFrame = getThemeFrame();
        NSUICore.sendMessage(themeFrame, "setNeedsDisplay:", true);
        return this;
    }

    void apply() {
        NSUICore.sendMessage(window, "setOpaque:", false);
        NSUICore.sendMessage(window, "setBackgroundColor:", NSUICore.getClearColor());
        NSUICore.sendMessage(window, "setContentView:", effect);
        NSUICore.sendMessage(effect, "addSubview:", shellView);
        var effectFrame = NSUICore.getFrame(effect);
        NSUICore.sendMessageSetFrame(shellView, "setFrame:", effectFrame);
        long mask = 1L << 1 | 1L << 4;
        NSUICore.sendMessage(shellView, "setAutoresizingMask:", mask);
    }
}