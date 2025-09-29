<div align="center">

# NSUI

Make beautiful, native windows like this.

<img width="1108" height="662" alt="image" src="https://github.com/user-attachments/assets/dc1a350c-9550-42ef-9b39-e2cff0e39e7c" />

<div align="left">

# 

Use a modern, and fluent API. 

```java

void main() {
    NSWindow.builder("some title")
        .styleMask(
                StyleMask.titled,
                StyleMask.miniaturizable,
                StyleMask.resizable,
                StyleMask.fullSizeContentView,
                StyleMask.nonActivatingPanel
        )
        .material(Materials.light)
        .titleProperties(props -> props
                .setTransparency(true)
                .setTitle("changing a title after it's assigned ")
        )
        .background(bg -> bg
                .setMovable(true)
                .setRetainBlurOnFocusLoss(false)
        )
        .trafficLights(lights -> lights
                .setHidden(TrafficLights.zoom)
                .setHidden(TrafficLights.close)
        )
        .build()
        .activate();
}

```

# 

NSUI uses Panama's FFM API for fast Cocoa window customizing, using an SWT backend and using `shell.view.id` to edit the native window. It's the only UI builder of its class to have such levels of native, AppKit-level customization over a macOS window in Java (bonus: it also doesn't suck).

Going from JNA Platform to FFM, window building is nearly instant compared to 1.5-2s.

# 

> **WillUHD**

