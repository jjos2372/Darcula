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

import com.bulenkov.iconloader.util.SystemInfo;
import com.bulenkov.iconloader.util.GraphicsConfig;
import com.bulenkov.iconloader.util.GraphicsUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import java.awt.*;

/**
 * @author Konstantin Bulenkov
 */
public class DarculaButtonUI extends BasicButtonUI {
  @SuppressWarnings("MethodOverridesStaticMethodOfSuperclass")
  public static ComponentUI createUI(JComponent c) {
    return new DarculaButtonUI();
  }

  public static boolean isSquare(Component c) {
    return c instanceof JButton && "square".equals(((JButton)c).getClientProperty("JButton.buttonType"));
  }

  @Override
  public void paint(Graphics g, JComponent c) {
    final AbstractButton button = (AbstractButton) c;
    final ButtonModel model = button.getModel();
    final Border border = c.getBorder();
    final GraphicsConfig config = GraphicsUtil.setupAAPainting(g);
    final boolean square = isSquare(c);
    if (c.isEnabled() && border != null && button.isContentAreaFilled() && !(c instanceof JToggleButton)) {
      final Insets ins = border.getBorderInsets(c);
      final int yOff = (ins.top + ins.bottom) / 4;
      if (!square) {
        if (c instanceof JButton && ((JButton)c).isDefaultButton() || model.isSelected()) {
          ((Graphics2D)g).setPaint(new GradientPaint(0, 0, getSelectedButtonColor1(), 0, c.getHeight(), getSelectedButtonColor2()));
        }
        else {
          ((Graphics2D)g).setPaint(new GradientPaint(0, 0, getButtonColor1(), 0, c.getHeight(), getButtonColor2()));
        }
      }
      g.fillRoundRect(square ? 2 : 4, yOff, c.getWidth() - 2 * 4, c.getHeight() - 2 * yOff, square ? 3 : 5, square ? 3 : 5);
    }
    config.restore();
    super.paint(g, c);
  }

  protected void paintText(Graphics g, JComponent c, Rectangle textRect, String text) {
    final AbstractButton button = (AbstractButton)c;
    final ButtonModel model = button.getModel();
    Color fg = button.getForeground();
    if (fg instanceof UIResource && button instanceof JButton && ((JButton)button).isDefaultButton()) {
      final Color selectedFg = UIManager.getColor("Button.darcula.selectedButtonForeground");
      if (selectedFg != null) {
        fg = selectedFg;
      }
    }
    g.setColor(fg);

    FontMetrics metrics = c.getFontMetrics(g.getFont());
    int mnemonicIndex = button.getDisplayedMnemonicIndex();
    if (model.isEnabled()) {

      BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex,
                                                textRect.x + getTextShiftOffset(),
                                                textRect.y + metrics.getAscent() + getTextShiftOffset());
    }
    else {
      g.setColor(UIManager.getColor("Button.darcula.disabledText.shadow"));
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, -1,
                                                textRect.x + getTextShiftOffset()+1,
                                                textRect.y + metrics.getAscent() + getTextShiftOffset()+1);
      g.setColor(UIManager.getColor("Button.disabledText"));
      BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, -1,
                                                textRect.x + getTextShiftOffset(),
                                                textRect.y + metrics.getAscent() + getTextShiftOffset());


    }
  }

  @Override
  public void update(Graphics g, JComponent c) {
    super.update(g, c);
    if (c instanceof JButton && ((JButton)c).isDefaultButton() && !SystemInfo.isMac) {
      if (!c.getFont().isBold()) {
       c.setFont(c.getFont().deriveFont(Font.BOLD));
      }
    }
  }

  protected Color getButtonColor1() {
    return UIManager.getColor("Button.darcula.color1");
  }

  protected Color getButtonColor2() {
    return UIManager.getColor("Button.darcula.color2");
  }

  protected Color getSelectedButtonColor1() {
    return UIManager.getColor("Button.darcula.selection.color1");
  }

  protected Color getSelectedButtonColor2() {
    return UIManager.getColor("Button.darcula.selection.color2");
  }
}
