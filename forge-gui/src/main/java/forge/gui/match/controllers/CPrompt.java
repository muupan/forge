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
package forge.gui.match.controllers;

import forge.UiCommand;
import forge.FThreads;
import forge.game.Game;
import forge.game.GameRules;
import forge.game.Match;
import forge.gui.InputProxy;
import forge.gui.framework.ICDoc;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.views.VPrompt;
import forge.gui.toolbox.FSkin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Controls the prompt panel in the match UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
public enum CPrompt implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private InputProxy inputControl = new InputProxy();
    private Component lastFocusedButton = null;
    private VPrompt view = VPrompt.SINGLETON_INSTANCE;

    private final ActionListener actCancel = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            inputControl.selectButtonCancel();
        }
    };
    private final ActionListener actOK = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent evt) {
            inputControl.selectButtonOK();
        }
    };

    private final FocusListener onFocus = new FocusAdapter() {
        @Override
        public void focusGained(FocusEvent e) {
            if (null != view.getParentCell() && view == view.getParentCell().getSelected()) {
                // only record focus changes when we're showing -- otherwise it is due to a tab visibility change
                lastFocusedButton = e.getComponent();
            }
        }
    };

    private void _initButton(JButton button, ActionListener onClick) {
        // remove to ensure listeners don't accumulate over many initializations
        button.removeActionListener(onClick);
        button.addActionListener(onClick);
        button.removeFocusListener(onFocus);
        button.addFocusListener(onFocus);
    }

    @Override
    public void initialize() {
        _initButton(view.getBtnCancel(), actCancel);
        _initButton(view.getBtnOK(), actOK);
    }

    /**
     * Gets the input control.
     * 
     * @return GuiInput
     */
    public InputProxy getInputControl() {
        return this.inputControl;
    }

    /** @param s0 &emsp; {@link java.lang.String} */
    public void setMessage(String s0) {
        view.getTarMessage().setText(FSkin.encodeSymbols(s0, false));
    }

    /** Flashes animation on input panel if play is currently waiting on input. */
    public void remind() {
        SDisplayUtil.remind(view);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public UiCommand getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */

    public void updateText(Game game) {
        FThreads.assertExecutedByEdt(true);
        final Match match = game.getMatch();
        final GameRules rules = game.getRules();
        final String text = String.format("T:%d G:%d/%d [%s]", game.getPhaseHandler().getTurn(), match.getPlayedGames().size() + 1, rules.getGamesPerMatch(), rules.getGameType());
        view.getLblGames().setText(text);
        view.getLblGames().setToolTipText(String.format("%s: Game #%d of %d, turn %d", rules.getGameType(), match.getPlayedGames().size() + 1, rules.getGamesPerMatch(), game.getPhaseHandler().getTurn()));
    }

    @Override
    public void update() {
        // set focus back to button that last had it
        if (null != lastFocusedButton) {
            lastFocusedButton.requestFocusInWindow();
        }
    }
}
