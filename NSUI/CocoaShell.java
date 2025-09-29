package NSUI;

import org.eclipse.swt.widgets.Shell;
import java.lang.foreign.*;

public class CocoaShell {

    private final MemorySegment window;
    private final MemorySegment effect;

    public CocoaShell(Shell shell) {
        var shellView = MemorySegment.ofAddress(shell.view.id);
        this.window = NSBridge.sendMessage(shellView, "window");

        var bounds = shell.getClientArea();
        var frame = new NSBridge.CGRectRecord(0, 0, bounds.width, bounds.height);

        var effectViewClass = NSBridge.getClass("NSVisualEffectView");
        var effectView = NSBridge.sendMessage(effectViewClass, "alloc");
        this.effect = NSBridge.sendMessage(effectView, "initWithFrame:", frame);
    }

    public MemorySegment getStandardWindowButton(NSWindow.TrafficLights light) {
        return NSBridge.sendMessageRetPtr(window, "standardWindowButton:", light.getValue());
    }

    public CocoaShell setStyleMask(NSWindow.StyleMask... m) {
        var combinedMask = 0;
        for (var v : m) combinedMask |= v.getValue();
        NSBridge.sendMessage(window, "setStyleMask:", (long) combinedMask);
        return this;
    }

    public CocoaShell setTitlebarAppearsTransparent(boolean isTransparent) {
        NSBridge.sendMessage(window, "setTitlebarAppearsTransparent:", isTransparent);
        return this;
    }

    public CocoaShell setTitleVisibility(boolean isVisible) {
        NSBridge.sendMessage(window, "setTitleVisibility:", isVisible ? 0L : 1L);
        return this;
    }

    public CocoaShell setTitle(String title) {
        var nsTitle = NSBridge.createNSString(title);
        NSBridge.sendMessage(window, "setTitle:", nsTitle);
        return this;
    }

    public CocoaShell setMovableByWindowBackground(boolean isMovable) {
        NSBridge.sendMessage(window, "setMovableByWindowBackground:", isMovable);
        return this;
    }

    public CocoaShell setHasShadow(boolean hasShadow) {
        NSBridge.sendMessage(window, "setHasShadow:", hasShadow);
        return this;
    }

    public CocoaShell setAlpha(double alpha) {
        NSBridge.sendMessage(window, "setAlphaValue:", alpha);
        return this;
    }

    public CocoaShell removeBorderEtch() {
        var clearColor = NSBridge.sendMessage(NSBridge.getClass("NSColor"), "clearColor");
        NSBridge.sendMessage(window, "setBackgroundColor:", clearColor);
        return this;
    }

    public CocoaShell setMaterial(NSWindow.Materials m) {
        NSBridge.sendMessage(effect, "setMaterial:", m.getValue());
        return this;
    }

    public CocoaShell setLevel(NSWindow.WindowLevel l) {
        NSBridge.sendMessage(window, "setLevel:", l.getValue());
        return this;
    }

    public CocoaShell retainBlurOnFocusLoss(boolean retain) {
        NSBridge.sendMessage(effect, "setState:", retain ? 1L : 0L);
        return this;
    }

    public CocoaShell setTabbingMode(NSWindow.TabbingMode m) {
        NSBridge.sendMessage(window, "setTabbingMode:", m.getValue());
        return this;
    }

    public CocoaShell setAutoresizingMask(NSWindow.ResizingMask... m) {
        var combinedMask = 0;
        for (var v : m) combinedMask |= v.getValue();
        NSBridge.sendMessage(effect, "setAutoresizingMask:", combinedMask);
        return this;
    }

    public void apply() {
        var contentView = NSBridge.sendMessage(window, "contentView");
        NSBridge.sendMessage(contentView, "addSubview:positioned:relativeTo:",
                this.effect,
                -1L,
                MemorySegment.NULL
        );
    }
}