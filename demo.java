import NSUI.NSWindow;
import NSUI.NSWindow.Materials;
import NSUI.NSWindow.StyleMask;
import NSUI.NSWindow.TrafficLights;

public class demo {
    static void main() {
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
    }
}
