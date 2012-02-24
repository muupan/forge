package forge.gui.home.draft;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;
import forge.Command;
import forge.game.GameType;
import forge.gui.GuiUtils;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.view.home.StartButton;
import forge.view.toolbox.DeckLister;
import forge.view.toolbox.FButton;
import forge.view.toolbox.FLabel;
import forge.view.toolbox.FOverlay;
import forge.view.toolbox.FPanel;
import forge.view.toolbox.FScrollPane;
import forge.view.toolbox.FSkin;

/** 
 * Singleton instance of "Draft" submenu in "Constructed" group.
 */
public enum VSubmenuSealed implements IVSubmenu {
    /** */
    SINGLETON_INSTANCE;

    @SuppressWarnings("serial")
    private final Command cmdExit = new Command() { @Override
        public void execute() { CSubmenuDraft.SINGLETON_INSTANCE.update(); } };

    /** */
    private final JPanel pnl            = new JPanel();
    private final StartButton btnStart  = new StartButton();
    private final DeckLister lstDecks   = new DeckLister(GameType.Draft, cmdExit);
    private final JLabel btnBuildDeck   = new FLabel.Builder()
        .fontScaleAuto(false).fontSize(16)
        .opaque(true).hoverable(true).text("Build a Sealed Deck").build();
    private final JLabel btnDirections = new FLabel.Builder()
        .fontScaleAuto(false).fontSize(16)
        .text("Click For Directions").fontAlign(SwingConstants.CENTER).build();

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        pnl.removeAll();
        pnl.setOpaque(false);
        pnl.setLayout(new MigLayout("insets 0, gap 0, hidemode 2, wrap"));

        btnStart.setEnabled(false);

        // Title
        final JLabel lblTitle = new JLabel("Select a deck for yourself, or build a new one: ");
        lblTitle.setFont(FSkin.getBoldFont(14));
        lblTitle.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);

        // Add components
        pnl.add(lblTitle, "w 100%!, gap 0 0 2% 2%");
        pnl.add(new FScrollPane(lstDecks), "w 90%!, h 35%!, gap 5% 0 2% 2%");
        pnl.add(btnBuildDeck, "w 50%!, h 5%!, gap 25% 0 0 0");
        pnl.add(btnStart, "ax center, gaptop 5%");
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroup() {
        return EMenuGroup.LIMITED;
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getPanel()
     */
    @Override
    public JPanel getPanel() {
        return pnl;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnDirections() {
        return this.btnDirections;
    }

    /** @return {@link javax.swing.JLabel} */
    public JLabel getBtnBuildDeck() {
        return this.btnBuildDeck;
    }

    /** @return {@link javax.swing.JButton} */
    public JButton getBtnStart() {
        return this.btnStart;
    }

    /** @return {@link forge.view.toolbox.DeckLister} */
    public DeckLister getLstDecks() {
        return lstDecks;
    }

    /** */
    public void showDirections() {
        final FOverlay overlay = GuiUtils.genericOverlay();
        final int w = overlay.getWidth();

        final String instructions = "SEALED DECK MODE INSTRUCTIONS"
                + "\r\n\r\n"
                + "In Sealed Deck tournaments, each player receives six booster packs"
                + "from which to build their deck."
                + "\r\n\r\n"
                + "Depending on which sets are to be used in a sealed deck event, "
                + "the distribution of packs can vary greatly."
                + "\r\n\r\n"
                + "Credit: Wikipedia";

        // Init directions text pane
        final JTextPane tpnDirections = new JTextPane();
        tpnDirections.setOpaque(false);
        tpnDirections.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        tpnDirections.setFont(FSkin.getFont(15));
        tpnDirections.setAlignmentX(SwingConstants.CENTER);
        tpnDirections.setFocusable(false);
        tpnDirections.setEditable(false);
        tpnDirections.setBorder(null);
        tpnDirections.setText(instructions);

        final StyledDocument doc = tpnDirections.getStyledDocument();
        final SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        final JButton btnCloseBig = new FButton("OK");
        btnCloseBig.setBounds(new Rectangle((w / 2 - 100), 510, 200, 30));
        btnCloseBig.addActionListener(new ActionListener() { @Override
            public void actionPerformed(final ActionEvent arg0) { overlay.hideOverlay(); } });

        final FPanel pnl = new FPanel();
        pnl.setCornerDiameter(0);
        pnl.setBackgroundTexture(FSkin.getIcon(FSkin.Backgrounds.BG_TEXTURE));
        pnl.setLayout(new MigLayout("insets 0, gap 0"));
        pnl.add(tpnDirections, "w 90%!, h 90%!, gap 5% 0 5% 0");
        pnl.setBounds(new Rectangle((w / 2 - 250), 80, 500, 400));

        overlay.setLayout(null);
        overlay.add(btnCloseBig);
        overlay.add(pnl);
        overlay.showOverlay();
    }
}
