<div align="center">

# NSUI

Make beautiful, native windows like this in Java.

<img width="1108" height="662" alt="image" src="https://github.com/user-attachments/assets/dc1a350c-9550-42ef-9b39-e2cff0e39e7c" />

<div align="left">

# 

Use a modern, and fluent API. 

```java

void main() {

    // setup window
    NSWindow nsWindow = NSWindow.builder("some title")
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
            )
            .trafficLights(lights -> lights
                    .setHidden(TrafficLights.zoom)
                    .setHidden(TrafficLights.close)
            )
            .build();
    
    // some point later
    nsWindow.activate();
}

```

# 

- NSUI uses SWT and `shell.view.id` as a backend, and leverages the FFM API (Panama) for JIT native window customization. 

- It's the only UI builder of its class to have such levels of native, AppKit-level customization over a macOS window in Java (bonus: the API also doesn't suck).

- Cold-start times on interpreter using Panama is approximately 3X faster than a previous version using JNA (0.7s vs 2.2s), potentially faster using native-image

- Note: `-XstartOnFirstThread` is a required VM argument due to SWT's limitations.

# 

> **WillUHD**

