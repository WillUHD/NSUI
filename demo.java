import NSUI.*;
import NSUI.NSWindow.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class demo {

    private static int trafficLightOffsetX = 0;

    static void main() {
        var window = NSWindow.builder("custom")
                .styleMask(
                        NSWindow.StyleMask.titled,
                        NSWindow.StyleMask.closable,
                        NSWindow.StyleMask.miniaturizable,
                        NSWindow.StyleMask.resizable,
                        NSWindow.StyleMask.fullSizeContentView
                )
                .titleProperties(props -> props.setTransparency(true))
                .background(bg -> bg.setMovable(true))
                .material(Materials.menu)
                .build();

        Display display = window.getDisplay();
        Shell mainShell = window.getShell();

        var layout = new GridLayout(2, false);
        layout.marginTop = 40;
        layout.marginWidth = 15;
        layout.marginHeight = 15;
        mainShell.setLayout(layout);

        createControls(mainShell, window);
        mainShell.open();

        while (!mainShell.isDisposed())
            if (!display.readAndDispatch()) display.sleep();

        display.dispose();
    }

    private static void createControls(Composite parent, NSWindow target) {
        new Label(parent, SWT.NONE).setText("Material:");
        var materialCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (var material : Materials.values()) {
            materialCombo.add(material.name());
        }
        materialCombo.setText(Materials.menu.name());
        materialCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        materialCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setMaterial(Materials.valueOf(materialCombo.getText()));
            }
        });

        new Label(parent, SWT.NONE).setText("Appearance:");
        var appearanceCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
        for (var appearance : Appearance.values()) {
            appearanceCombo.add(appearance.name());
        }
        appearanceCombo.select(0);
        appearanceCombo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        appearanceCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setAppearance(Appearance.valueOf(appearanceCombo.getText()));
            }
        });

        var titleVisibleCheck = new Button(parent, SWT.CHECK);
        titleVisibleCheck.setText("Title Visible");
        titleVisibleCheck.setSelection(true);
        titleVisibleCheck.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
        titleVisibleCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setTitleVisibility(titleVisibleCheck.getSelection());
            }
        });

        var titleTransparentCheck = new Button(parent, SWT.CHECK);
        titleTransparentCheck.setText("Titlebar Transparent");
        titleTransparentCheck.setSelection(true);
        titleTransparentCheck.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 2, 1));
        titleTransparentCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setTitlebarAppearsTransparent(titleTransparentCheck.getSelection());
            }
        });

        new Label(parent, SWT.NONE).setText("Titlebar Height:");
        var titleHeightSlider = new Slider(parent, SWT.HORIZONTAL);
        titleHeightSlider.setValues(28, 28, 121, 1, 1, 10);
        titleHeightSlider.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        titleHeightSlider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setTitlebarHeight(titleHeightSlider.getSelection());
            }
        });

        new Label(parent, SWT.NONE).setText("Blur Amount:");
        var blurSlider = new Slider(parent, SWT.HORIZONTAL);
        blurSlider.setValues(0, 0, 101, 1, 1, 10);
        blurSlider.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        blurSlider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                target.setBlurAmount(blurSlider.getSelection());
            }
        });

        new Label(parent, SWT.NONE).setText("Traffic Offset X:");
        var offsetXSlider = new Slider(parent, SWT.HORIZONTAL);
        offsetXSlider.setValues(0, -50, 51, 1, 1, 5);
        offsetXSlider.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
        offsetXSlider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                trafficLightOffsetX = offsetXSlider.getSelection();
                target.setTrafficLightsOffset(trafficLightOffsetX);
            }
        });
    }
}