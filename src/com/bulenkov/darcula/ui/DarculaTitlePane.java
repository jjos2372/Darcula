/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bulenkov.darcula.ui;

import sun.awt.SunToolkit;

import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * @author Konstantin Bulenkov
 */
public class DarculaTitlePane extends JComponent {
  private static final int IMAGE_HEIGHT = 16;
  private static final int IMAGE_WIDTH = 16;

  private PropertyChangeListener myPropertyChangeListener;
  private JMenuBar myMenuBar;
  private Action myCloseAction;
  private Action myIconifyAction;
  private Action myRestoreAction;
  private Action myMaximizeAction;
  private JButton myToggleButton;
  private JButton myIconifyButton;
  private JButton myCloseButton;
  private Icon myMaximizeIcon;
  private Icon myMinimizeIcon;
  private Image mySystemIcon;
  private WindowListener myWindowListener;
  private Window myWindow;
  private final JRootPane myRootPane;
  private int myState;
  private final DarculaRootPaneUI rootPaneUI;

  private final Color myInactiveBackground = UIManager.getColor("inactiveCaption");
  private final Color myInactiveForeground = UIManager.getColor("inactiveCaptionText");
  private final Color myInactiveShadow = UIManager.getColor("inactiveCaptionBorder");
  private Color myActiveBackground = null;
  private Color myActiveForeground = null;
  private Color myActiveShadow = null;

  public DarculaTitlePane(JRootPane root, DarculaRootPaneUI ui) {
    this.myRootPane = root;
    rootPaneUI = ui;

    myState = -1;

    installSubcomponents();
    determineColors();
    installDefaults();

    setLayout(createLayout());
  }

  private void uninstall() {
    uninstallListeners();
    myWindow = null;
    removeAll();
  }

  private void installListeners() {
    if (myWindow != null) {
      myWindowListener = createWindowListener();
      myWindow.addWindowListener(myWindowListener);
      myPropertyChangeListener = createWindowPropertyChangeListener();
      myWindow.addPropertyChangeListener(myPropertyChangeListener);
    }
  }

  private void uninstallListeners() {
    if (myWindow != null) {
      myWindow.removeWindowListener(myWindowListener);
      myWindow.removePropertyChangeListener(myPropertyChangeListener);
    }
  }

  private WindowListener createWindowListener() {
    return new WindowHandler();
  }

  private PropertyChangeListener createWindowPropertyChangeListener() {
    return new PropertyChangeHandler();
  }

  public JRootPane getRootPane() {
    return myRootPane;
  }

  private int getWindowDecorationStyle() {
    return getRootPane().getWindowDecorationStyle();
  }

  public void addNotify() {
    super.addNotify();

    uninstallListeners();

    myWindow = SwingUtilities.getWindowAncestor(this);
    if (myWindow != null) {
      if (myWindow instanceof Frame) {
        setState(((Frame) myWindow).getExtendedState());
      } else {
        setState(0);
      }
      setActive(myWindow.isActive());
      installListeners();
      updateSystemIcon();
    }
  }

  public void removeNotify() {
    super.removeNotify();

    uninstallListeners();
    myWindow = null;
  }

  private void installSubcomponents() {
    int decorationStyle = getWindowDecorationStyle();
    if (decorationStyle == JRootPane.FRAME) {
      createActions();
      myMenuBar = createMenuBar();
      add(myMenuBar);
      createButtons();
      add(myIconifyButton);
      add(myToggleButton);
      add(myCloseButton);
    } else if (decorationStyle == JRootPane.PLAIN_DIALOG ||
      decorationStyle == JRootPane.INFORMATION_DIALOG ||
      decorationStyle == JRootPane.ERROR_DIALOG ||
      decorationStyle == JRootPane.COLOR_CHOOSER_DIALOG ||
      decorationStyle == JRootPane.FILE_CHOOSER_DIALOG ||
      decorationStyle == JRootPane.QUESTION_DIALOG ||
      decorationStyle == JRootPane.WARNING_DIALOG) {
      createActions();
      createButtons();
      add(myCloseButton);
    }
  }

  private void determineColors() {
    switch (getWindowDecorationStyle()) {
      case JRootPane.FRAME:
        myActiveBackground = UIManager.getColor("activeCaption");
        myActiveForeground = UIManager.getColor("activeCaptionText");
        myActiveShadow = UIManager.getColor("activeCaptionBorder");
        break;
      case JRootPane.ERROR_DIALOG:
        myActiveBackground = UIManager.getColor("OptionPane.errorDialog.titlePane.background");
        myActiveForeground = UIManager.getColor("OptionPane.errorDialog.titlePane.foreground");
        myActiveShadow = UIManager.getColor("OptionPane.errorDialog.titlePane.shadow");
        break;
      case JRootPane.QUESTION_DIALOG:
      case JRootPane.COLOR_CHOOSER_DIALOG:
      case JRootPane.FILE_CHOOSER_DIALOG:
        myActiveBackground = UIManager.getColor("OptionPane.questionDialog.titlePane.background");
        myActiveForeground = UIManager.getColor("OptionPane.questionDialog.titlePane.foreground");
        myActiveShadow = UIManager.getColor("OptionPane.questionDialog.titlePane.shadow");
        break;
      case JRootPane.WARNING_DIALOG:
        myActiveBackground = UIManager.getColor("OptionPane.warningDialog.titlePane.background");
        myActiveForeground = UIManager.getColor("OptionPane.warningDialog.titlePane.foreground");
        myActiveShadow = UIManager.getColor("OptionPane.warningDialog.titlePane.shadow");
        break;
      case JRootPane.PLAIN_DIALOG:
      case JRootPane.INFORMATION_DIALOG:
      default:
        myActiveBackground = UIManager.getColor("activeCaption");
        myActiveForeground = UIManager.getColor("activeCaptionText");
        myActiveShadow = UIManager.getColor("activeCaptionBorder");
        break;
    }
  }

  private void installDefaults() {
    setFont(UIManager.getFont("InternalFrame.titleFont", getLocale()));
  }

  protected JMenuBar createMenuBar() {
    myMenuBar = new SystemMenuBar();
    myMenuBar.setFocusable(false);
    myMenuBar.setBorderPainted(true);
    myMenuBar.add(createMenu());
    return myMenuBar;
  }

  private void close() {
    Window window = getWindow();

    if (window != null) {
      window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
  }

  private void iconify() {
    Frame frame = getFrame();
    if (frame != null) {
      frame.setExtendedState(myState | Frame.ICONIFIED);
    }
  }

  private void maximize() {
    Frame frame = getFrame();
    if (frame != null) {
      frame.setExtendedState(myState | Frame.MAXIMIZED_BOTH);
    }
  }

  private void restore() {
    Frame frame = getFrame();

    if (frame == null) {
      return;
    }

    if ((myState & Frame.ICONIFIED) != 0) {
      frame.setExtendedState(myState & ~Frame.ICONIFIED);
    } else {
      frame.setExtendedState(myState & ~Frame.MAXIMIZED_BOTH);
    }
  }

  private void createActions() {
    myCloseAction = new CloseAction();
    if (getWindowDecorationStyle() == JRootPane.FRAME) {
      myIconifyAction = new IconifyAction();
      myRestoreAction = new RestoreAction();
      myMaximizeAction = new MaximizeAction();
    }
  }

  private JMenu createMenu() {
    JMenu menu = new JMenu("");
    if (getWindowDecorationStyle() == JRootPane.FRAME) {
      addMenuItems(menu);
    }
    return menu;
  }

  private void addMenuItems(JMenu menu) {
    menu.add(myRestoreAction);
    menu.add(myIconifyAction);
    if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
      menu.add(myMaximizeAction);
    }

    menu.add(new JSeparator());

    menu.add(myCloseAction);
  }

  private static JButton createButton(String accessibleName, Icon icon, Action action) {
    JButton button = new JButton();
    button.setFocusPainted(false);
    button.setFocusable(false);
    button.setOpaque(true);
    button.putClientProperty("paintActive", Boolean.TRUE);
    button.putClientProperty(AccessibleContext.ACCESSIBLE_NAME_PROPERTY, accessibleName);
    button.setBorder(new EmptyBorder(0, 0, 0, 0));
    button.setText(null);
    button.setAction(action);
    button.setIcon(icon);
    return button;
  }

  private void createButtons() {
    myCloseButton = createButton("Close", UIManager.getIcon("InternalFrame.closeIcon"), myCloseAction);

    if (getWindowDecorationStyle() == JRootPane.FRAME) {
      myMaximizeIcon = UIManager.getIcon("InternalFrame.maximizeIcon");
      myMinimizeIcon = UIManager.getIcon("InternalFrame.minimizeIcon");

      myIconifyButton = createButton("Iconify", UIManager.getIcon("InternalFrame.iconifyIcon"), myIconifyAction);
      myToggleButton = createButton("Maximize", myMaximizeIcon, myRestoreAction);
    }
  }

  private LayoutManager createLayout() {
    return new TitlePaneLayout();
  }

  private void setActive(boolean active) {
    myCloseButton.putClientProperty("paintActive", active);

    if (getWindowDecorationStyle() == JRootPane.FRAME) {
      myIconifyButton.putClientProperty("paintActive", active);
      myToggleButton.putClientProperty("paintActive", active);
    }

    getRootPane().repaint();
  }

  private void setState(int state) {
    setState(state, false);
  }

  private void setState(int state, boolean updateRegardless) {
    Window wnd = getWindow();

    if (wnd != null && getWindowDecorationStyle() == JRootPane.FRAME) {
      if (myState == state && !updateRegardless) {
        return;
      }
      Frame frame = getFrame();

      if (frame != null) {
        JRootPane rootPane = getRootPane();

        if (((state & Frame.MAXIMIZED_BOTH) != 0) &&
          (rootPane.getBorder() == null || (rootPane.getBorder() instanceof UIResource)) &&
          frame.isShowing()) {
          rootPane.setBorder(null);
        } else if ((state & Frame.MAXIMIZED_BOTH) == 0) {
          // This is a croak, if state becomes bound, this can
          // be nuked.
          rootPaneUI.installBorder(rootPane);
        }
        if (frame.isResizable()) {
          if ((state & Frame.MAXIMIZED_BOTH) != 0) {
            updateToggleButton(myRestoreAction, myMinimizeIcon);
            myMaximizeAction.setEnabled(false);
            myRestoreAction.setEnabled(true);
          } else {
            updateToggleButton(myMaximizeAction, myMaximizeIcon);
            myMaximizeAction.setEnabled(true);
            myRestoreAction.setEnabled(false);
          }
          if (myToggleButton.getParent() == null || myIconifyButton.getParent() == null) {
            add(myToggleButton);
            add(myIconifyButton);
            revalidate();
            repaint();
          }
          myToggleButton.setText(null);
        } else {
          myMaximizeAction.setEnabled(false);
          myRestoreAction.setEnabled(false);
          if (myToggleButton.getParent() != null) {
            remove(myToggleButton);
            revalidate();
            repaint();
          }
        }
      } else {
        // Not contained in a Frame
        myMaximizeAction.setEnabled(false);
        myRestoreAction.setEnabled(false);
        myIconifyAction.setEnabled(false);
        remove(myToggleButton);
        remove(myIconifyButton);
        revalidate();
        repaint();
      }
      myCloseAction.setEnabled(true);
      myState = state;
    }
  }

  private void updateToggleButton(Action action, Icon icon) {
    myToggleButton.setAction(action);
    myToggleButton.setIcon(icon);
    myToggleButton.setText(null);
  }

  private Frame getFrame() {
    Window window = getWindow();

    if (window instanceof Frame) {
      return (Frame) window;
    }
    return null;
  }

  private Window getWindow() {
    return myWindow;
  }

  private String getTitle() {
    Window w = getWindow();

    if (w instanceof Frame) {
      return ((Frame) w).getTitle();
    } else if (w instanceof Dialog) {
      return ((Dialog) w).getTitle();
    }
    return null;
  }

  public void paintComponent(Graphics g) {
    if (getFrame() != null) {
      setState(getFrame().getExtendedState());
    }
    JRootPane rootPane = getRootPane();
    Window window = getWindow();
    boolean leftToRight = window == null ?
      rootPane.getComponentOrientation().isLeftToRight() :
      window.getComponentOrientation().isLeftToRight();
    boolean isSelected = window == null || window.isActive();
    int width = getWidth();
    int height = getHeight();

    Color background;
    Color foreground;
    Color darkShadow;

    if (isSelected) {
      background = myActiveBackground;
      foreground = myActiveForeground;
      darkShadow = myActiveShadow;
    } else {
      background = myInactiveBackground;
      foreground = myInactiveForeground;
      darkShadow = myInactiveShadow;
    }

    g.setColor(background);
    g.fillRect(0, 0, width, height);

    g.setColor(darkShadow);
    g.drawLine(0, height - 1, width, height - 1);
    g.drawLine(0, 0, 0, 0);
    g.drawLine(width - 1, 0, width - 1, 0);

    int xOffset = leftToRight ? 5 : width - 5;

    if (getWindowDecorationStyle() == JRootPane.FRAME) {
      xOffset += leftToRight ? IMAGE_WIDTH + 5 : -IMAGE_WIDTH - 5;
    }

    String theTitle = getTitle();
    if (theTitle != null) {
      FontMetrics fm = g.getFontMetrics(g.getFont());

      g.setColor(foreground);

      int yOffset = ((height - fm.getHeight()) / 2) + fm.getAscent();

      Rectangle rect = new Rectangle(0, 0, 0, 0);
      if (myIconifyButton != null && myIconifyButton.getParent() != null) {
        rect = myIconifyButton.getBounds();
      }
      int titleW;

      if (leftToRight) {
        if (rect.x == 0) {
          rect.x = window.getWidth() - window.getInsets().right - 2;
        }
        titleW = rect.x - xOffset - 4;
        // BasicGraphicsUtils.getClippedString
//        theTitle = SwingUtilities2.clipStringIfNecessary(rootPane, fm, theTitle, titleW);
      } else {
        titleW = xOffset - rect.x - rect.width - 4;
//        theTitle = SwingUtilities2.clipStringIfNecessary(rootPane, fm, theTitle, titleW);
        xOffset -= fm.stringWidth(theTitle);
      }
      int titleLength = fm.stringWidth(theTitle);
      g.drawString(theTitle, xOffset, yOffset);
    }
  }

  private class CloseAction extends AbstractAction {
    public CloseAction() {
      super(UIManager.getString("DarculaTitlePane.closeTitle", getLocale()));
    }

    public void actionPerformed(ActionEvent e) {
      close();
    }
  }

  private class IconifyAction extends AbstractAction {
    public IconifyAction() {
      super(UIManager.getString("DarculaTitlePane.iconifyTitle", getLocale()));
    }

    public void actionPerformed(ActionEvent e) {
      iconify();
    }
  }

  private class RestoreAction extends AbstractAction {
    public RestoreAction() {
      super(UIManager.getString("DarculaTitlePane.restoreTitle", getLocale()));
    }

    public void actionPerformed(ActionEvent e) {
      restore();
    }
  }

  private class MaximizeAction extends AbstractAction {
    public MaximizeAction() {
      super(UIManager.getString("DarculaTitlePane.maximizeTitle", getLocale()));
    }

    public void actionPerformed(ActionEvent e) {
      maximize();
    }
  }

  private class SystemMenuBar extends JMenuBar {
    public void paint(Graphics g) {
      if (isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      }

      if (mySystemIcon != null) {
        g.drawImage(mySystemIcon, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
      } else {
        Icon icon = UIManager.getIcon("InternalFrame.icon");

        if (icon != null) {
          icon.paintIcon(this, g, 0, 0);
        }
      }
    }

    public Dimension getMinimumSize() {
      return getPreferredSize();
    }

    public Dimension getPreferredSize() {
      Dimension size = super.getPreferredSize();

      return new Dimension(Math.max(IMAGE_WIDTH, size.width), Math.max(size.height, IMAGE_HEIGHT));
    }
  }

  private class TitlePaneLayout implements LayoutManager {
    public void addLayoutComponent(String name, Component c) {
    }

    public void removeLayoutComponent(Component c) {
    }

    public Dimension preferredLayoutSize(Container c) {
      int height = computeHeight();
      //noinspection SuspiciousNameCombination
      return new Dimension(height, height);
    }

    public Dimension minimumLayoutSize(Container c) {
      return preferredLayoutSize(c);
    }

    private int computeHeight() {
      FontMetrics fm = myRootPane.getFontMetrics(getFont());
      int fontHeight = fm.getHeight();
      fontHeight += 7;
      int iconHeight = 0;
      if (getWindowDecorationStyle() == JRootPane.FRAME) {
        iconHeight = IMAGE_HEIGHT;
      }

      return Math.max(fontHeight, iconHeight);
    }

    public void layoutContainer(Container c) {
      boolean leftToRight = (myWindow == null) ?
        getRootPane().getComponentOrientation().isLeftToRight() :
        myWindow.getComponentOrientation().isLeftToRight();

      int w = getWidth();
      int x;
      int y = 3;
      int spacing;
      int buttonHeight;
      int buttonWidth;

      if (myCloseButton != null && myCloseButton.getIcon() != null) {
        buttonHeight = myCloseButton.getIcon().getIconHeight();
        buttonWidth = myCloseButton.getIcon().getIconWidth();
      } else {
        buttonHeight = IMAGE_HEIGHT;
        buttonWidth = IMAGE_WIDTH;
      }

      x = leftToRight ? w : 0;

      spacing = 5;
      x = leftToRight ? spacing : w - buttonWidth - spacing;
      if (myMenuBar != null) {
        myMenuBar.setBounds(x, y, buttonWidth, buttonHeight);
      }

      x = leftToRight ? w : 0;
      spacing = 4;
      x += leftToRight ? -spacing - buttonWidth : spacing;
      if (myCloseButton != null) {
        myCloseButton.setBounds(x, y, buttonWidth, buttonHeight);
      }

      if (!leftToRight) x += buttonWidth;

      if (getWindowDecorationStyle() == JRootPane.FRAME) {
        if (Toolkit.getDefaultToolkit().isFrameStateSupported(Frame.MAXIMIZED_BOTH)) {
          if (myToggleButton.getParent() != null) {
            spacing = 10;
            x += leftToRight ? -spacing - buttonWidth : spacing;
            myToggleButton.setBounds(x, y, buttonWidth, buttonHeight);
            if (!leftToRight) {
              x += buttonWidth;
            }
          }
        }

        if (myIconifyButton != null && myIconifyButton.getParent() != null) {
          spacing = 2;
          x += leftToRight ? -spacing - buttonWidth : spacing;
          myIconifyButton.setBounds(x, y, buttonWidth, buttonHeight);
          if (!leftToRight) {
            x += buttonWidth;
          }
        }
      }
    }
  }

  private class PropertyChangeHandler implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent pce) {
      String name = pce.getPropertyName();

      if ("resizable".equals(name) || "state".equals(name)) {
        Frame frame = getFrame();

        if (frame != null) {
          setState(frame.getExtendedState(), true);
        }
        if ("resizable".equals(name)) {
          getRootPane().repaint();
        }
      } else if ("title".equals(name)) {
        repaint();
      } else if ("componentOrientation".equals(name)) {
        revalidate();
        repaint();
      } else if ("iconImage".equals(name)) {
        updateSystemIcon();
        revalidate();
        repaint();
      }
    }
  }

  private void updateSystemIcon() {
    Window window = getWindow();
    if (window == null) {
      mySystemIcon = null;
      return;
    }

    List<Image> icons = window.getIconImages();
    assert icons != null;

    if (icons.size() == 0) {
      mySystemIcon = null;
    } else if (icons.size() == 1) {
      mySystemIcon = icons.get(0);
    } else {
      mySystemIcon = SunToolkit.getScaledIconImage(icons, IMAGE_WIDTH, IMAGE_HEIGHT);
    }
  }

  private class WindowHandler extends WindowAdapter {
    public void windowActivated(WindowEvent ev) {
      setActive(true);
    }

    public void windowDeactivated(WindowEvent ev) {
      setActive(false);
    }
  }
}
