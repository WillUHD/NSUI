package NSUI;

import NSUI.NSWindow.*;
import java.util.Collections;
import java.util.Set;

public class WindowConfiguration {

    private String title;
    private Set<StyleMask> styleMasks;
    private Materials material;
    private boolean titleHidden;
    private boolean titleTransparent;
    private boolean movableByBackground;
    private boolean retainBlurOnFocusLoss;
    private boolean removeBorderEtch;
    private boolean hasShadow;
    private double alpha;
    private WindowLevel level;
    private TabbingMode tabbingMode;
    private Set<ResizingMask> resizingMasks;
    private Set<TrafficLights> hiddenTrafficLights;
    private Set<TrafficLights> disabledTrafficLights;
    private double blurAmount;
    private double titlebarHeight;
    private NSWindow.CGPoint trafficLightsOffset;
    private NSWindow.Appearance appearance;

    public WindowConfiguration() {
        this.title = "";
        this.styleMasks = Set.of(StyleMask.titled, StyleMask.closable, StyleMask.miniaturizable, StyleMask.resizable);
        this.material = Materials.windowBackground;
        this.titleHidden = false;
        this.titleTransparent = false;
        this.movableByBackground = false;
        this.retainBlurOnFocusLoss = false;
        this.removeBorderEtch = false;
        this.hasShadow = true;
        this.alpha = 1.0;
        this.level = WindowLevel.Normal;
        this.tabbingMode = TabbingMode.Automatic;
        this.resizingMasks = Set.of(ResizingMask.viewWidthSizable, ResizingMask.viewHeightSizable);
        this.hiddenTrafficLights = Collections.emptySet();
        this.disabledTrafficLights = Collections.emptySet();
        this.blurAmount = -1.0;
        this.titlebarHeight = -1.0;
        this.trafficLightsOffset = null;
        this.appearance = null;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Set<StyleMask> getStyleMasks() { return styleMasks; }
    public void setStyleMasks(Set<StyleMask> styleMasks) { this.styleMasks = styleMasks; }

    public Materials getMaterial() { return material; }
    public void setMaterial(Materials material) { this.material = material; }

    public boolean isTitleHidden() { return titleHidden; }
    public void setTitleHidden(boolean titleHidden) { this.titleHidden = titleHidden; }

    public boolean isTitleTransparent() { return titleTransparent; }
    public void setTitleTransparent(boolean titleTransparent) { this.titleTransparent = titleTransparent; }

    public boolean isMovableByBackground() { return movableByBackground; }
    public void setMovableByBackground(boolean movableByBackground) { this.movableByBackground = movableByBackground; }

    public boolean isRetainBlurOnFocusLoss() { return retainBlurOnFocusLoss; }
    public void setRetainBlurOnFocusLoss(boolean retainBlurOnFocusLoss) { this.retainBlurOnFocusLoss = retainBlurOnFocusLoss; }

    public boolean isRemoveBorderEtch() { return removeBorderEtch; }
    public void setRemoveBorderEtch(boolean removeBorderEtch) { this.removeBorderEtch = removeBorderEtch; }

    public boolean hasShadow() { return hasShadow; }
    public void setHasShadow(boolean hasShadow) { this.hasShadow = hasShadow; }

    public double getAlpha() { return alpha; }
    public void setAlpha(double alpha) { this.alpha = alpha; }

    public WindowLevel getLevel() { return level; }
    public void setLevel(WindowLevel level) { this.level = level; }

    public TabbingMode getTabbingMode() { return tabbingMode; }
    public void setTabbingMode(TabbingMode tabbingMode) { this.tabbingMode = tabbingMode; }

    public Set<ResizingMask> getResizingMasks() { return resizingMasks; }
    public void setResizingMasks(Set<ResizingMask> resizingMasks) { this.resizingMasks = resizingMasks; }

    public Set<TrafficLights> getHiddenTrafficLights() { return hiddenTrafficLights; }
    public void setHiddenTrafficLights(Set<TrafficLights> hiddenTrafficLights) { this.hiddenTrafficLights = hiddenTrafficLights; }

    public Set<TrafficLights> getDisabledTrafficLights() { return disabledTrafficLights; }
    public void setDisabledTrafficLights(Set<TrafficLights> disabledTrafficLights) { this.disabledTrafficLights = disabledTrafficLights; }

    public double getBlurAmount() { return blurAmount; }
    public void setBlurAmount(double blurAmount) { this.blurAmount = blurAmount; }

    public double getTitlebarHeight() { return titlebarHeight; }
    public void setTitlebarHeight(double titlebarHeight) { this.titlebarHeight = titlebarHeight; }

    public NSWindow.CGPoint getTrafficLightsOffset() { return trafficLightsOffset; }
    public void setTrafficLightsOffset(NSWindow.CGPoint trafficLightsOffset) { this.trafficLightsOffset = trafficLightsOffset; }

    public NSWindow.Appearance getAppearance() { return appearance; }
    public void setAppearance(NSWindow.Appearance appearance) { this.appearance = appearance; }
}