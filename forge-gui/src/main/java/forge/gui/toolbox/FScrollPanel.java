/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.toolbox;

import javax.swing.*;
import java.awt.*;

/** 
 * An extension of FScrollPane that can be used as a panel
 *
 */
@SuppressWarnings("serial")
public class FScrollPanel extends FScrollPane {
    private JPanel innerPanel;

    public FScrollPanel() {
        this(null);
    }
    public FScrollPanel(final LayoutManager layout) {
        this(layout, false);
    }
    public FScrollPanel(final LayoutManager layout, boolean useArrowButtons0) {
        this(layout, useArrowButtons0, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    }
    public FScrollPanel(final LayoutManager layout, boolean useArrowButtons0, final int vertical0, final int horizontal0) {
        super(new JPanel(layout), false, useArrowButtons0, vertical0, horizontal0);
        innerPanel = (JPanel)getViewport().getView();
        innerPanel.setOpaque(false);
    }

    //relay certain methods to the inner panel if it has been initialized
    @Override
    public Component add(Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(comp);
        }
        return super.add(comp);
    }

    @Override
    public void add(PopupMenu popup) {
        if (innerPanel != null) {
            innerPanel.add(popup);
            return;
        }
        super.add(popup);
    }

    @Override
    public void add(Component comp, Object constraints) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints);
            return;
        }
        super.add(comp, constraints);
    }

    @Override
    public Component add(Component comp, int index) {
        if (innerPanel != null) {
            return innerPanel.add(comp, index);
        }
        return super.add(comp, index);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        if (innerPanel != null) {
            innerPanel.add(comp, constraints, index);
            return;
        }
        super.add(comp, constraints, index);
    }

    @Override
    public Component add(String name, Component comp) {
        if (innerPanel != null) {
            return innerPanel.add(name, comp);
        }
        return super.add(name, comp);
    }

    @Override
    public void remove(Component comp) {
        if (innerPanel != null) {
            innerPanel.remove(comp);
            return;
        }
        super.remove(comp);
    }

    @Override
    public void remove(int index) {
        if (innerPanel != null) {
            innerPanel.remove(index);
            return;
        }
        super.remove(index);
    }

    @Override
    public void remove(MenuComponent popup) {
        if (innerPanel != null) {
            innerPanel.remove(popup);
            return;
        }
        super.remove(popup);
    }

    @Override
    public void removeAll() {
        if (innerPanel != null) {
            innerPanel.removeAll();
            return;
        }
        super.removeAll();
    }
}
