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

NSUI uses Panama features for fast window building, using an SWT backend and using `shell.view.id` to customize the native window handlers. 


> **WillUHD**

