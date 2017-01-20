package mah.ui.support.swing.window;

import mah.ui.layout.Layout;
import mah.ui.layout.LayoutType;
import mah.ui.support.swing.layout.DefaultLayout;
import mah.ui.support.swing.layout.SwingLayout;
import mah.ui.theme.ThemeManager;
import mah.ui.util.ScreenUtils;
import mah.ui.window.WindowProperties;
import mah.ui.window.WindowSupport;

import javax.swing.*;
import java.awt.*;

/**
 * Created by zgq on 17-1-8 11:25.
 */
public class WindowImpl extends WindowSupport {

    private final WindowProperties properties;
    private JFrame frame;
    private DefaultLayout defaultLayout;
    private SwingLayout currentLayout;

    public WindowImpl(WindowProperties properties) {
        this.properties = properties;
    }

    @Override
    public void init() {
        initWindow();
        initLayout();
    }


    private void initLayout() {
        if (properties.getLayoutType() == LayoutType.DEFAULT) {
            defaultLayout = new DefaultLayout();
            defaultLayout.init();
            defaultLayout.setDefaultMode();
            addLayout(defaultLayout);
            ThemeManager.getInstance().applyTheme(defaultLayout);
            currentLayout = defaultLayout;
        }
        frame.pack();
        centerOnScreen();
    }

    private void initWindow() {
        frame = new JFrame();
        frame.setUndecorated(true);
        frame.setAlwaysOnTop(true);
        frame.setResizable(false);
        frame.setType(java.awt.Window.Type.UTILITY);
    }

    @Override
    public void show() {
        frame.setVisible(true);
    }

    private void updateLayout(SwingLayout layout) {
        JPanel currentPanel = currentLayout.getPanel();
        JPanel panel = layout.getPanel();
        if (currentPanel.equals(panel)) {
            return;
        }
        removeLayout();
        addLayout(layout);
        layout.onSetCurrentLayout();
        currentLayout = layout;
    }

    private void addLayout(SwingLayout layout) {
        frame.getContentPane().add(layout.getPanel());
    }

    private void removeLayout() {
        if (currentLayout != null)
            frame.getContentPane().remove(currentLayout.getPanel());
    }

    @Override
    public boolean isShowing() {
        return frame.isShowing();
    }

    @Override
    public boolean isFocused() {
        return frame.isFocused();
    }

    @Override
    public void hide() {
        frame.setVisible(false);
    }

    @Override
    public void moveToRight() {
        Rectangle currentScreen = getCurrentScreen();
        int y = getY(currentScreen.getHeight());
        frame.setLocation((int) (currentScreen.getX()+ currentScreen.getWidth()- getWindowWidth()), y);
    }

    @Override
    public void centerOnScreen() {
        Rectangle currentScreen = getCurrentScreen();
        double width = currentScreen.getWidth();
        int x = (int) (currentScreen.getX() + ((width - getWindowWidth()) / 2));
        int y = getY(currentScreen.getHeight());
        frame.setLocation(x, y);
    }

    private int getY(double screenHeight) {
        return (int) ((screenHeight - getWindowWidth()) / 2) + 130;
    }

    private Rectangle getCurrentScreen() {
        PointerInfo pointerInfo = MouseInfo.getPointerInfo();
        Rectangle curRect = ScreenUtils.getScreenBoundsAt(pointerInfo.getLocation());
        return curRect;
    }

    private int getWindowWidth() {
        return frame.getWidth();
    }

    @Override
    public void moveToLeft() {
        Rectangle currentScreen = getCurrentScreen();
        int y = getY(currentScreen.getHeight());
        frame.setLocation((int) currentScreen.getX(), y);
    }

    @Override
    public void useDefaultLayoutAsCurrentLayout() {
        defaultLayout.use();
        updateLayout(defaultLayout);
        frame.pack();
    }

    @Override
    public void setCurrentLayout(Layout layout) {
        if (layout instanceof SwingLayout) {
            SwingLayout swingLayout = (SwingLayout) layout;
            updateLayout(swingLayout);
            currentLayout = swingLayout;
            frame.pack();
        }
    }

    @Override
    public Layout getCurrentLayout() {
        return currentLayout;
    }

    @Override
    public Layout getDefaultLayout() {
        return defaultLayout;
    }

    @Override
    public void moveToCusorScreen() {
        centerOnScreen();
    }


}
