package NSUI;

import NSUI.NSWindow.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class NSWindow {

    private final Shell shell;
    private final CocoaShell cocoaShell;
    private final Display display;

    public record CGPoint(double x, double y) {}

    public enum Materials { windowBackground(12), underWindowBackground(21), underPageBackground(18), contentBackground(19), titleBar(3), headerView(10), sideBar(7), sheet(11), popover(6), menu(5), toolTip(17), selection(4), hudWindow(13), fullScreenUI(15), appearanceBased(0), light(1), dark(2), mediumLight(8), ultraDark(9);
        private final long value; Materials(long v) { this.value = v; } public long getValue() { return value; } }
    public enum ResizingMask { notSizable(0), minXMargin(1), viewWidthSizable(1 << 1), viewMaxXMargin(1 << 2), viewMinYMargin(1 << 3), viewHeightSizable(1 << 4), viewMaxYMargin(1 << 5);
        private final long value; ResizingMask(long v) { this.value = v; } public long getValue() { return value; } }
    public enum StyleMask { borderless(0), titled(1), closable(2), miniaturizable(1 << 2), resizable(1 << 3), utilityWindow(1 << 4), docModalWindow(1 << 6), nonActivatingPanel(1 << 7), texturedBackground(1 << 8), hudWindow(1 << 13), fullSizeContentView(1 << 15);
        private final long value; StyleMask(long v) { this.value = v; } public long getValue() { return value; } }
    public enum TrafficLights { close(0), miniaturize(1), zoom(2);
        private final long value; TrafficLights(long v) { this.value = v; } public long getValue() { return value; } }
    public enum WindowLevel { Normal(0), Floating(3), ModalPanel(8), MainMenu(20), Status(24);
        private final long value; WindowLevel(long v) { this.value = v; } public long getValue() { return value; } }
    public enum TabbingMode { Automatic(0), Preferred(1), Disallowed(2);
        private final long value; TabbingMode(long v) { this.value = v; } public long getValue() { return value; } }
    public enum Appearance {
        Aqua("NSAppearanceNameAqua"), Graphite("NSAppearanceNameGraphite"),
        VibrantLight("NSAppearanceNameVibrantLight"), VibrantDark("NSAppearanceNameVibrantDark");
        private final String name; Appearance(String n) { this.name = n; } public String getName() { return name; }
    }

    private NSWindow(Builder builder) {
        this.display = new Display();
        this.shell = new Shell(
                display,
                SWT.Settings |
                SWT.NO_BACKGROUND);

        this.shell.setBackground(null);

        this.shell.setBackgroundMode(SWT.INHERIT_NONE);
        this.shell.setText(builder.config.getTitle());
        this.shell.setSize(600, 400);
        this.cocoaShell = new CocoaShell(shell);
        applyConfiguration(builder.config);
    }

    public static Builder builder(String title) {
        return new Builder(title);
    }

    public void activate() {
        shell.open();
        while (!shell.isDisposed())
            if (!display.readAndDispatch()) display.sleep();
        display.dispose();
    }

    private void applyConfiguration(WindowConfiguration config) {
        cocoaShell.setStyleMask(config.getStyleMasks().toArray(new StyleMask[0]));
        cocoaShell.setTitle(config.getTitle());
        cocoaShell.setTitleVisibility(!config.isTitleHidden());
        cocoaShell.setTitlebarAppearsTransparent(config.isTitleTransparent());
        cocoaShell.setMovableByWindowBackground(config.isMovableByBackground());
        if (config.getMaterial() != null) cocoaShell.setMaterial(config.getMaterial());
        if (config.getBlurAmount() >= 0) cocoaShell.setBlurAmount(config.getBlurAmount());
        if (config.getTitlebarHeight() >= 0) cocoaShell.setTitlebarHeight(config.getTitlebarHeight());
        for (var light : config.getHiddenTrafficLights()) {
            var button = cocoaShell.getStandardWindowButton(light);
            NSUICore.sendMessage(button, "setHidden:", true);
        }
        for (var light : config.getDisabledTrafficLights()) {
            var button = cocoaShell.getStandardWindowButton(light);
            NSUICore.sendMessage(button, "setEnabled:", false);
        }
        if (config.getTrafficLightsOffset() != null) {
            cocoaShell.setTrafficLightsOffset(new NSUICore.CGPointRecord(config.getTrafficLightsOffset().x(), 0));
        }
        if (config.getAppearance() != null) {
            cocoaShell.setAppearance(config.getAppearance());
        }
        cocoaShell.apply();
    }

    public Shell getShell() { return this.shell; }
    public Display getDisplay() { return this.display; }
    public void setMaterial(Materials material) { cocoaShell.setMaterial(material); }
    public void setTitleVisibility(boolean isVisible) { cocoaShell.setTitleVisibility(isVisible); }
    public void setTitlebarAppearsTransparent(boolean isTransparent) { cocoaShell.setTitlebarAppearsTransparent(isTransparent); }
    public void setTitlebarHeight(double height) { cocoaShell.setTitlebarHeight(height); }
    public void setBlurAmount(double blur) { cocoaShell.setBlurAmount(blur); }
    public void setTrafficLightsOffset(double x) {
        cocoaShell.setTrafficLightsOffset(new NSUICore.CGPointRecord(x, 0));
    }
    public void setAppearance(Appearance appearance) { cocoaShell.setAppearance(appearance); }

    public static final class Builder {
        private final WindowConfiguration config;
        private Builder(String title) { this.config = new WindowConfiguration(); this.config.setTitle(title); }
        public Builder styleMask(StyleMask... masks) { config.setStyleMasks(Set.of(masks)); return this; }
        public Builder material(Materials material) { config.setMaterial(material); return this; }
        public Builder appearance(Appearance appearance) { config.setAppearance(appearance); return this; }
        public Builder titleProperties(Consumer<TitlePropertiesCustomizer> consumer) { consumer.accept(new TitlePropertiesCustomizer(this)); return this; }
        public Builder background(Consumer<BackgroundCustomizer> consumer) { consumer.accept(new BackgroundCustomizer(this)); return this; }
        public Builder trafficLights(Consumer<TrafficLightsCustomizer> consumer) { consumer.accept(new TrafficLightsCustomizer(this)); return this; }
        public NSWindow build() { return new NSWindow(this); }
        private void updateTitle(String title) { config.setTitle(title); }
        private void updateTitleHidden(boolean hidden) { config.setTitleHidden(hidden); }
        private void updateTitleTransparent(boolean transparent) { config.setTitleTransparent(transparent); }
        private void updateMovableByBackground(boolean movable) { config.setMovableByBackground(movable); }
        private void updateHiddenLights(Set<TrafficLights> lights) { config.setHiddenTrafficLights(lights); }
        private void updateDisabledLights(Set<TrafficLights> lights) { config.setDisabledTrafficLights(lights); }
        private void updateBlurAmount(double blur) { config.setBlurAmount(blur); }
        private void updateTitlebarHeight(double height) { config.setTitlebarHeight(height); }
        private void updateTrafficLightsOffset(CGPoint offset) { config.setTrafficLightsOffset(offset); }
    }
    public static class TitlePropertiesCustomizer {
        private final Builder builder;
        private TitlePropertiesCustomizer(Builder builder) { this.builder = builder; }
        public TitlePropertiesCustomizer setTitle(String title) { builder.updateTitle(title); return this; }
        public TitlePropertiesCustomizer setHidden(boolean hidden) { builder.updateTitleHidden(hidden); return this; }
        public TitlePropertiesCustomizer setTransparency(boolean transparent) { builder.updateTitleTransparent(transparent); return this; }
        public TitlePropertiesCustomizer setHeight(double height) { builder.updateTitlebarHeight(height); return this; }
    }
    public static class BackgroundCustomizer {
        private final Builder builder;
        private BackgroundCustomizer(Builder builder) { this.builder = builder; }
        public BackgroundCustomizer setMovable(boolean movable) { builder.updateMovableByBackground(movable); return this; }
        public BackgroundCustomizer setBlurAmount(double blur) { builder.updateBlurAmount(blur); return this; }
    }
    public static class TrafficLightsCustomizer {
        private final Builder builder;
        private TrafficLightsCustomizer(Builder builder) { this.builder = builder; }
        public TrafficLightsCustomizer setHidden(TrafficLights... lights) { var newSet = Stream.concat(builder.config.getHiddenTrafficLights().stream(), Stream.of(lights)).collect(Collectors.toSet()); builder.updateHiddenLights(newSet); return this; }
        public TrafficLightsCustomizer setDisabled(TrafficLights... lights) { var newSet = Stream.concat(builder.config.getDisabledTrafficLights().stream(), Stream.of(lights)).collect(Collectors.toSet()); builder.updateDisabledLights(newSet); return this; }
        public TrafficLightsCustomizer setOffset(double x) { builder.updateTrafficLightsOffset(new CGPoint(x, 0)); return this; }
    }
}