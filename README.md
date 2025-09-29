<div align="center">

# NSUI

Make beautiful, native windows like this.

<img width="1161" height="662" alt="image" src="https://github.com/user-attachments/assets/a16728da-1b36-430a-bc22-fb1fe6cedfd5" />

<div align="left">

# 

Use a modern, and fluent API. 

```java

NSWindow.builder("some title")
        .styleMask(
                StyleMask.titled,
                StyleMask.miniaturizable,
                StyleMask.resizable,
                StyleMask.fullSizeContentView
        )
        .material(Materials.sideBar)
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

```

# 

NSUI uses Panama's FFM API for fast Cocoa window customizing, using an SWT backend and using `shell.view.id` to edit the native window. It's the only UI builder of its class to have such levels of native, AppKit-level customization over a macOS window in Java (bonus: it also doesn't suck).

# 

> **WillUHD**

