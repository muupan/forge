package forge.gui.match;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.tuple.Pair;

import net.miginfocom.swing.MigLayout;
import forge.Command;
import forge.GameLog;
import forge.GameLogEntry;
import forge.GameLogEntryType;
import forge.Singletons;
import forge.game.Game;
import forge.game.GameOutcome;
import forge.game.player.LobbyPlayer;
import forge.game.player.PlayerStatistics;
import forge.gui.toolbox.FButton;
import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FOverlay;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FTextArea;
import forge.net.FServer;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class ViewWinLose {
    private final FButton btnContinue, btnRestart, btnQuit;
    private final JPanel pnlCustom;

    private final JLabel lblTitle = new JLabel("WinLoseFrame > lblTitle needs updating.");
    private final JLabel lblStats = new JLabel("WinLoseFrame > lblStats needs updating.");
    private final JPanel pnlOutcomes = new JPanel(new MigLayout("wrap, align center"));
    
    @SuppressWarnings("serial")
    public ViewWinLose(final Game game) {
        final JPanel overlay = FOverlay.SINGLETON_INSTANCE.getPanel();

        final JPanel pnlLeft = new JPanel();
        final JPanel pnlRight = new JPanel();
        final JScrollPane scrCustom = new JScrollPane();
        pnlCustom = new JPanel();

        btnContinue = new FButton();
        btnRestart = new FButton();
        btnQuit = new FButton();

        // Control of the win/lose is handled differently for various game modes.
        ControlWinLose control = null;
        switch (game.getType()) {
            case Quest:
                control = new QuestWinLose(this, game);
                break;
            case Draft:
                if (!Singletons.getModel().getGauntletMini().isGauntletDraft()) {
                    break;
                }
            case Sealed:
                control = new LimitedWinLose(this, game);
                break;
            case Gauntlet:
                control = new GauntletWinLose(this, game);
                break;
            default: // will catch it after switch
                break;
        }
        if (null == control) {
            control = new ControlWinLose(this, game);
        }


        pnlLeft.setOpaque(false);
        pnlRight.setOpaque(false);
        pnlCustom.setOpaque(false);
        scrCustom.setOpaque(false);
        scrCustom.setBorder(null);
        scrCustom.getVerticalScrollBar().setUnitIncrement(16);
        scrCustom.getViewport().setOpaque(false);
        scrCustom.getViewport().add(pnlCustom);

        lblTitle.setForeground(Color.white);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        FSkin.get(lblTitle).setFont(FSkin.getBoldFont(30));

        lblStats.setForeground(Color.white);
        lblStats.setHorizontalAlignment(SwingConstants.CENTER);
        FSkin.get(lblStats).setFont(FSkin.getFont(26));

        btnContinue.setText("Continue");
        FSkin.get(btnContinue).setFont(FSkin.getFont(22));
        btnRestart.setText("Restart");
        FSkin.get(btnRestart).setFont(FSkin.getFont(22));
        btnQuit.setText("Quit");
        FSkin.get(btnQuit).setFont(FSkin.getFont(22));
        btnContinue.setEnabled(!game.getMatch().isMatchOver());

        // Assemble game log scroller.
        final FTextArea txtLog = new FTextArea();
        txtLog.setText(game.getGameLog().getLogText(null));
        FSkin.get(txtLog).setFont(FSkin.getFont(14));
        txtLog.setFocusable(true); // allow highlighting and copying of log
        
        FLabel btnCopyLog = new FLabel.ButtonBuilder().text("Copy to clipboard").build();
        btnCopyLog.setCommand(new Command() {
            @Override
            public void run() {
                StringSelection ss = new StringSelection(txtLog.getText());
                try {
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                } catch (IllegalStateException ex) {
                    // ignore; may be unavailable on some platforms
                }
            }
        });

        // Add all components accordingly.
        overlay.setLayout(new MigLayout("insets 0, w 100%!, h 100%!"));
        pnlLeft.setLayout(new MigLayout("insets 0, wrap, align center"));
        pnlRight.setLayout(new MigLayout("insets 0, wrap"));
        pnlCustom.setLayout(new MigLayout("insets 0, wrap, align center"));

        final boolean customIsPopulated = control.populateCustomPanel();
        if (customIsPopulated) {
            overlay.add(pnlLeft, "w 40%!, h 100%!");
            overlay.add(pnlRight, "w 60%!, h 100%!");
            pnlRight.add(scrCustom, "w 100%!, h 100%!");
        }
        else {
            overlay.add(pnlLeft, "w 100%!, h 100%!");
        }

        pnlOutcomes.setOpaque(false);
        pnlLeft.add(lblTitle, "h 60px!, center");
        pnlLeft.add(pnlOutcomes, "center");
        pnlLeft.add(lblStats, "h 60px!, center");

        // A container must be made to ensure proper centering.
        final JPanel pnlButtons = new JPanel(new MigLayout("insets 0, wrap, ax center"));
        pnlButtons.setOpaque(false);

        final String constraints = "w 300px!, h 50px!, gap 0 0 20px 0";
        pnlButtons.add(btnContinue, constraints);
        pnlButtons.add(btnRestart, constraints);
        pnlButtons.add(btnQuit, constraints);
        pnlLeft.add(pnlButtons, "w 100%!");

        final JPanel pnlLog = new JPanel(new MigLayout("insets 0, wrap, ax center"));
        final FScrollPane scrLog = new FScrollPane(txtLog);
        scrLog.setBorder(null);
        pnlLog.setOpaque(false);

        pnlLog.add(new FLabel.Builder().text("Game Log").fontAlign(SwingConstants.CENTER)
                .fontSize(18).fontStyle(Font.BOLD).build(),
                "w 300px!, h 28px!, gaptop 20px");

        pnlLog.add(scrLog, "w 300px!, h 100px!, gap 0 0 10 10");
        pnlLog.add(btnCopyLog, "center, w pref+16, h pref+8");
        pnlLeft.add(pnlLog, "w 100%!");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                scrLog.getViewport().setViewPosition(new Point(0, 0));
                // populateCustomPanel may have changed which buttons are enabled; focus on the 'best' one
                if (btnContinue.isEnabled()) {
                    btnContinue.requestFocusInWindow();
                } else {
                    btnQuit.requestFocusInWindow();
                }
            }
        });
        
        lblTitle.setText(composeTitle(game.getOutcome()));

        GameLog log = game.getGameLog();

        for (GameLogEntry o : log.getLogEntriesExact(GameLogEntryType.GAME_OUTCOME)) 
            pnlOutcomes.add(new FLabel.Builder().text(o.message).fontSize(14).build(), "h 20!");

        for (GameLogEntry o : log.getLogEntriesExact(GameLogEntryType.MATCH_RESULTS))
            lblStats.setText(o.message);
    }

    private String composeTitle(GameOutcome outcome) {
        LobbyPlayer guiPlayer = FServer.instance.getLobby().getGuiPlayer();
        int nHumansInGame = 0;
        for(Pair<LobbyPlayer, PlayerStatistics> pps : outcome) {
            if( pps.getKey() == guiPlayer )
                nHumansInGame++;
        }
        LobbyPlayer winner = outcome.getWinner();
        if ( winner == null )
            return "It's a draw!";

        return nHumansInGame == 1 ? "You " + (winner == guiPlayer ? "won!" : "lost!") : winner.getName() + " Won!";
    }

    /** @return {@link forge.gui.toolbox.FButton} */
    public FButton getBtnContinue() {
        return this.btnContinue;
    }

    /** @return {@link forge.gui.toolbox.FButton} */
    public FButton getBtnRestart() {
        return this.btnRestart;
    }

    /** @return {@link forge.gui.toolbox.FButton} */
    public FButton getBtnQuit() {
        return this.btnQuit;
    }

    /** @return {@link javax.swing.JPanel} */
    public JPanel getPnlCustom() {
        return this.pnlCustom;
    }
}
