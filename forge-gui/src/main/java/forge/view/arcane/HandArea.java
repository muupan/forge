/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.view.arcane;

import forge.gui.match.CMatchUI;
import forge.gui.match.controllers.CPrompt;
import forge.gui.toolbox.FScrollPane;

import java.awt.event.MouseEvent;


/**
 * <p>
 * HandArea class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class HandArea extends CardArea {
    /** Constant <code>serialVersionUID=7488132628637407745L</code>. */
    private static final long serialVersionUID = 7488132628637407745L;

    /**
     * <p>
     * Constructor for HandArea.
     * </p>
     * TODO Make compatible with WindowBuilder
     * 
     * @param scrollPane
     */
    public HandArea(final FScrollPane scrollPane) {
        super(scrollPane);

        this.setDragEnabled(true);
        this.setVertical(true);
    }

    /** {@inheritDoc} */
    @Override
    public final void mouseOver(final CardPanel panel, final MouseEvent evt) {
        CMatchUI.SINGLETON_INSTANCE.setCard(panel.getCard(), evt.isShiftDown());
        super.mouseOver(panel, evt);
    }

    /** {@inheritDoc} */
    @Override
    public final void mouseLeftClicked(final CardPanel panel, final MouseEvent evt) {
        CPrompt.SINGLETON_INSTANCE.getInputControl().selectCard(panel.getCard(), evt);
        super.mouseLeftClicked(panel, evt);
    }

    /** {@inheritDoc} */
    @Override
    public final void mouseRightClicked(final CardPanel panel, final MouseEvent evt) {
        CPrompt.SINGLETON_INSTANCE.getInputControl().selectCard(panel.getCard(), evt);
        super.mouseRightClicked(panel, evt);
    }
}
