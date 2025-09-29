package NSUI;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

import module java.base;

public final class NSWindow {

    private final Shell shell;
    private final CocoaShell cocoaShell;
    private final Display display;

    public enum Materials {

        windowBackground(12),
        underWindowBackground(21),
        underPageBackground(18),
        contentBackground(19),
        titleBar(3),
        headerView(10),
        sideBar(7),
        sheet(11),
        popover(6),
        menu(5),
        toolTip(17),
        selection(4),
        hudWindow(13),
        fullScreenUI(15),
        appearanceBased(0),
        light(1), dark(2),
        mediumLight(8),
        ultraDark(9);

        private final long value; Materials(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public enum ResizingMask {

        notSizable(0),
        minXMargin(1),
        viewWidthSizable(1 << 1),
        viewMaxXMargin(1 << 2),
        viewMinYMargin(1 << 3),
        viewHeightSizable(1 << 4),
        viewMaxYMargin(1 << 5);

        private final long value; ResizingMask(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public enum StyleMask {

        borderless(0),
        titled(1),
        closable(2),
        miniaturizable(1 << 2),
        resizable(1 << 3),
        utilityWindow(1 << 4),
        docModalWindow(1 << 6),
        nonActivatingPanel(1 << 7),
        texturedBackground(1 << 8),
        hudWindow(1 << 13),
        fullSizeContentView(1 << 15);

        private final long value; StyleMask(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public enum TrafficLights {

        close(0),
        miniaturize(1),
        zoom(2);

        private final long value; TrafficLights(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public enum WindowLevel {

        Normal(0),
        Floating(3),
        ModalPanel(8),
        MainMenu(20),
        Status(24);

        private final long value; WindowLevel(long v) { this.value = v; }
        public long getValue() { return value; }
    }

    public enum TabbingMode {

        Automatic(0),
        Preferred(1),
        Disallowed(2);

        private final long value; TabbingMode(long v) { this.value = v; }
        public long getValue() { return value; }
    }


    private NSWindow(Builder builder) {
        this.display = new Display();
        this.shell = new Shell(display, SWT.SHELL_TRIM);
        this.shell.setText(builder.config.title());

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
        cocoaShell.setStyleMask(config.styleMasks().toArray(new StyleMask[0]));
        cocoaShell.setTitle(config.title());
        cocoaShell.setTitleVisibility(!config.titleHidden());
        cocoaShell.setTitlebarAppearsTransparent(config.titleTransparent());
        cocoaShell.setMovableByWindowBackground(config.movableByBackground());
        cocoaShell.setHasShadow(config.hasShadow());
        cocoaShell.setAlpha(config.alpha());
        cocoaShell.setLevel(config.level());
        cocoaShell.setTabbingMode(config.tabbingMode());

        if (config.material() != null) cocoaShell.setMaterial(config.material());
        cocoaShell.setAutoresizingMask(config.resizingMasks().toArray(new ResizingMask[0]));
        cocoaShell.retainBlurOnFocusLoss(config.retainBlurOnFocusLoss());

        for (var light : config.hiddenTrafficLights()) {
            var button = cocoaShell.getStandardWindowButton(light);
            NSBridge.sendMessage(button, "setHidden:", true);
        }

        for (var light : config.disabledTrafficLights()) {
            var button = cocoaShell.getStandardWindowButton(light);
            NSBridge.sendMessage(button, "setEnabled:", false);
        }

        if (config.removeBorderEtch()) cocoaShell.removeBorderEtch();
        cocoaShell.apply();
    }

    private record WindowConfiguration(
            String title,
            Set<StyleMask> styleMasks,
            Materials material,
            boolean titleHidden,
            boolean titleTransparent,
            boolean movableByBackground,
            boolean retainBlurOnFocusLoss,
            boolean removeBorderEtch,
            boolean hasShadow,
            double alpha,
            WindowLevel level,
            TabbingMode tabbingMode,
            Set<ResizingMask> resizingMasks,
            Set<TrafficLights> hiddenTrafficLights,
            Set<TrafficLights> disabledTrafficLights
    ) {}

    public static final class Builder {
        private WindowConfiguration config;

        private Builder(String title) {
            this.config = new WindowConfiguration(
                    title,
                    Set.of(StyleMask.titled, StyleMask.closable, StyleMask.miniaturizable, StyleMask.resizable),
                    Materials.windowBackground,
                    false, false, // title
                    false, false, false, // background
                    true, 1.0, WindowLevel.Normal, TabbingMode.Automatic, // window
                    Set.of(ResizingMask.viewWidthSizable, ResizingMask.viewHeightSizable), // sizing
                    Collections.emptySet(), Collections.emptySet() // traffic lights
            );
        }

        public Builder preset(NSPreset preset) {
            this.config = preset.getConfiguration();
            this.config = new WindowConfiguration(
                    this.config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
            return this;
        }

        public Builder styleMask(StyleMask... masks) {
            var newMasks = Set.of(masks);
            this.config = new WindowConfiguration(
                    config.title, newMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
            return this;
        }

        public Builder material(Materials material) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
            return this;
        }

        public Builder titleProperties(Consumer<TitlePropertiesCustomizer> consumer) {
            consumer.accept(new TitlePropertiesCustomizer(this));
            return this;
        }

        public Builder background(Consumer<BackgroundCustomizer> consumer) {
            consumer.accept(new BackgroundCustomizer(this));
            return this;
        }

        public Builder trafficLights(Consumer<TrafficLightsCustomizer> consumer) {
            consumer.accept(new TrafficLightsCustomizer(this));
            return this;
        }

        public Builder windowProperties(Consumer<WindowPropertiesCustomizer> consumer) {
            consumer.accept(new WindowPropertiesCustomizer(this));
            return this;
        }

        public NSWindow build() {
            return new NSWindow(this);
        }

        private void updateTitle(String title) {
            this.config = new WindowConfiguration(
                    title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }

        private void updateTitleHidden(boolean hidden) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, hidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }

        private void updateTitleTransparent(boolean transparent) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    transparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }

        private void updateMovableByBackground(boolean movable) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, movable, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }

        private void updateRetainBlur(boolean retain) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, retain,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }

        private void updateHiddenLights(Set<TrafficLights> lights) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, lights, config.disabledTrafficLights
            );
        }

        private void updateDisabledLights(Set<TrafficLights> lights) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, config.hasShadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, lights
            );
        }

        private void updateHasShadow(boolean shadow) {
            this.config = new WindowConfiguration(
                    config.title, config.styleMasks, config.material, config.titleHidden,
                    config.titleTransparent, config.movableByBackground, config.retainBlurOnFocusLoss,
                    config.removeBorderEtch, shadow, config.alpha, config.level,
                    config.tabbingMode, config.resizingMasks, config.hiddenTrafficLights, config.disabledTrafficLights
            );
        }
    }

    public static class TitlePropertiesCustomizer {
        private final Builder builder;
        private TitlePropertiesCustomizer(Builder builder) { this.builder = builder; }

        public TitlePropertiesCustomizer setTitle(String title) {
            builder.updateTitle(title);
            return this;
        }

        public TitlePropertiesCustomizer setHidden(boolean hidden) {
            builder.updateTitleHidden(hidden);
            return this;
        }

        public TitlePropertiesCustomizer setTransparency(boolean transparent) {
            builder.updateTitleTransparent(transparent);
            return this;
        }
    }

    public static class BackgroundCustomizer {
        private final Builder builder;
        private BackgroundCustomizer(Builder builder) { this.builder = builder; }

        public BackgroundCustomizer setMovable(boolean movable) {
            builder.updateMovableByBackground(movable);
            return this;
        }

        public BackgroundCustomizer setRetainBlurOnFocusLoss(boolean retain) {
            builder.updateRetainBlur(retain);
            return this;
        }
    }

    public static class TrafficLightsCustomizer {
        private final Builder builder;
        private TrafficLightsCustomizer(Builder builder) { this.builder = builder; }

        public TrafficLightsCustomizer setHidden(TrafficLights... lights) {
            var newSet = Stream.concat(
                    builder.config.hiddenTrafficLights().stream(),
                    Stream.of(lights)
            ).collect(Collectors.toSet());
            builder.updateHiddenLights(newSet);
            return this;
        }

        public TrafficLightsCustomizer setDisabled(TrafficLights... lights) {
            var newSet = Stream.concat(
                    builder.config.disabledTrafficLights().stream(),
                    Stream.of(lights)
            ).collect(Collectors.toSet());
            builder.updateDisabledLights(newSet);
            return this;
        }
    }

    public static class WindowPropertiesCustomizer {
        private final Builder builder;
        private WindowPropertiesCustomizer(Builder builder) { this.builder = builder; }

        public WindowPropertiesCustomizer setHasShadow(boolean hasShadow) {
            builder.updateHasShadow(hasShadow);
            return this;
        }
    }

    public enum NSPreset {
        TOOL(new WindowConfiguration(
                "Tool Window",
                Set.of(StyleMask.titled, StyleMask.closable, StyleMask.utilityWindow, StyleMask.fullSizeContentView),
                Materials.titleBar,
                false, true, // title
                true, true, true, // background
                false, 0.95, WindowLevel.Floating, TabbingMode.Disallowed, // window
                Set.of(ResizingMask.viewWidthSizable, ResizingMask.viewHeightSizable),
                Set.of(TrafficLights.miniaturize, TrafficLights.zoom), Collections.emptySet()
        )),

        HUD(new WindowConfiguration(
                "HUD",
                Set.of(StyleMask.titled, StyleMask.closable, StyleMask.hudWindow, StyleMask.utilityWindow),
                Materials.hudWindow,
                false, false, // title
                true, true, true, // background
                true, 0.85, WindowLevel.Floating, TabbingMode.Disallowed, // window
                Collections.emptySet(), // not resizable
                Set.of(TrafficLights.miniaturize, TrafficLights.zoom), Collections.emptySet()
        ));

        private final WindowConfiguration configuration;
        NSPreset(WindowConfiguration config) { this.configuration = config; }
        public WindowConfiguration getConfiguration() { return this.configuration; }
    }
}