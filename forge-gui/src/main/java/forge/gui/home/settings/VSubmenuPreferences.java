package forge.gui.home.settings;

import forge.Singletons;
import forge.control.FControl.CloseAction;
import forge.control.KeyboardShortcuts;
import forge.control.KeyboardShortcuts.Shortcut;
import forge.game.GameLogEntryType;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.gui.home.EMenuGroup;
import forge.gui.home.IVSubmenu;
import forge.gui.home.VHomeUI;
import forge.gui.toolbox.*;
import forge.gui.toolbox.FSkin.SkinnedLabel;
import forge.gui.toolbox.FSkin.SkinnedTextField;
import forge.properties.ForgePreferences.FPref;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * Assembles Swing components of preferences submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 */
public enum VSubmenuPreferences implements IVSubmenu<CSubmenuPreferences> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Preferences");

    /** */
    private final JPanel pnlPrefs = new JPanel();
    private final FScrollPane scrContent = new FScrollPane(pnlPrefs, false,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    private final FLabel btnReset = new FLabel.Builder().opaque(true).hoverable(true).text("Reset to Default Settings").build();
    private final FLabel btnDeleteMatchUI = new FLabel.Builder().opaque(true).hoverable(true).text("Reset Match Layout").build();
    private final FLabel btnDeleteEditorUI = new FLabel.Builder().opaque(true).hoverable(true).text("Reset Editor Layout").build();
    private final FLabel btnDeleteWorkshopUI = new FLabel.Builder().opaque(true).hoverable(true).text("Reset Workshop Layout").build();
    private final FLabel btnUserProfileUI = new FLabel.Builder().opaque(true).hoverable(true).text("Open User Directory").build();
    private final FLabel btnContentDirectoryUI = new FLabel.Builder().opaque(true).hoverable(true).text("Open Content Directory").build();
    private final FLabel btnPlayerName = new FLabel.Builder().opaque(true).hoverable(true).text("").build();

    private final JCheckBox cbRemoveSmall = new OptionsCheckBox("Remove Small Creatures");
    private final JCheckBox cbSingletons = new OptionsCheckBox("Singleton Mode");
    private final JCheckBox cbRemoveArtifacts = new OptionsCheckBox("Remove Artifacts");
    private final JCheckBox cbAnte = new OptionsCheckBox("Play for Ante");
    private final JCheckBox cbUploadDraft = new OptionsCheckBox("Upload Draft Picks");
    private final JCheckBox cbEnableAICheats = new OptionsCheckBox("Allow AI Cheating");
    private final JCheckBox cbManaBurn = new OptionsCheckBox("Mana Burn");
    private final JCheckBox cbDevMode = new OptionsCheckBox("Developer Mode");
    private final JCheckBox cbEnforceDeckLegality = new OptionsCheckBox("Deck Conformance");
    private final JCheckBox cbCloneImgSource = new OptionsCheckBox("Clones Use Original Card Art");
    private final JCheckBox cbScaleLarger = new OptionsCheckBox("Scale Image Larger");
    private final JCheckBox cbDisplayFoil = new OptionsCheckBox("Display Foil Overlay");
    private final JCheckBox cbRandomFoil = new OptionsCheckBox("Random Foil");
    private final JCheckBox cbRandomArtInPools = new OptionsCheckBox("Randomize Card Art in Generated Card Pools");
    private final JCheckBox cbEnableSounds = new OptionsCheckBox("Enable Sounds");
    private final JCheckBox cbAltSoundSystem = new OptionsCheckBox("Use Alternate Sound System");
    private final JCheckBox cbUiForTouchScreen = new OptionsCheckBox("Enchance UI for Touchscreens");
    private final JCheckBox cbCompactMainMenu = new OptionsCheckBox("Use Compact Main Sidebar Menu");
    private final JCheckBox cbUseThemes = new OptionsCheckBox("Enable Themes");
    private final JCheckBox cbPromptFreeBlocks = new OptionsCheckBox("Free Block Handling");
    private final JCheckBox cbCompactPrompt = new OptionsCheckBox("Compact Prompt");
    private final JCheckBox cbStackCardView = new OptionsCheckBox("Card View for Stack");
    private final JCheckBox cbHideReminderText = new OptionsCheckBox("Hide Reminder Text");

    private final Map<FPref, KeyboardShortcutField> shortcutFields = new HashMap<FPref, KeyboardShortcutField>();

    // ComboBox items are added in CSubmenuPreferences since this is just the View.
    private final FComboBoxPanel<GameLogEntryType> cbpGameLogEntryType = new FComboBoxPanel<GameLogEntryType>("Game Log Verbosity:");
    private final FComboBoxPanel<CloseAction> cbpCloseAction = new FComboBoxPanel<CloseAction>("Close Action:");
    private final FComboBoxPanel<String> cbpAiProfiles = new FComboBoxPanel<String>("AI Personality:");

    /**
     * Constructor.
     */
    private VSubmenuPreferences() {

        pnlPrefs.setOpaque(false);
        pnlPrefs.setLayout(new MigLayout("insets 0, gap 0, wrap 2"));

        // Spacing between components is defined here.
        final String sectionConstraints = "w 80%!, h 42px!, gap 10% 0 10px 10px, span 2 1";
        final String regularConstraints = "w 80%!, h 22px!, gap 10% 0 0 10px, span 2 1";

        // Troubleshooting
        pnlPrefs.add(new SectionLabel("Troubleshooting"), sectionConstraints);

        // Reset buttons
        final String twoButtonConstraints1 = "w 38%!, h 30px!, gap 10% 0 0 10px";
        final String twoButtonConstraints2 = "w 38%!, h 30px!, gap 0 0 0 10px";
        pnlPrefs.add(btnReset, twoButtonConstraints1);
        pnlPrefs.add(btnDeleteMatchUI, twoButtonConstraints2);
        pnlPrefs.add(btnDeleteEditorUI, twoButtonConstraints1);
        pnlPrefs.add(btnDeleteWorkshopUI, twoButtonConstraints2);
        pnlPrefs.add(btnUserProfileUI, twoButtonConstraints1);
        pnlPrefs.add(btnContentDirectoryUI, twoButtonConstraints2);

        // General Configuration
        pnlPrefs.add(new SectionLabel("General Configuration"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(getPlayerNamePanel(), regularConstraints + ", h 26px!");
        pnlPrefs.add(new NoteLabel("Sets the name that you will be referred to by Forge during gameplay."), regularConstraints);

        pnlPrefs.add(cbCompactMainMenu, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enable for a space efficient sidebar that displays only one menu group at a time (RESTART REQUIRED)."), regularConstraints);


        // Gameplay Options
        pnlPrefs.add(new SectionLabel("Gameplay"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(cbpAiProfiles, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Choose your AI opponent."), regularConstraints);

        pnlPrefs.add(cbAnte, regularConstraints);
        pnlPrefs.add(new NoteLabel("Determines whether or not the game is played for ante."), regularConstraints);

        pnlPrefs.add(cbUploadDraft, regularConstraints);
        pnlPrefs.add(new NoteLabel("Sends draft picks to Forge servers for analysis, to improve draft AI."), regularConstraints);

        pnlPrefs.add(cbEnableAICheats, regularConstraints);
        pnlPrefs.add(new NoteLabel("Allow the AI to cheat to gain advantage (for personalities that have cheat shuffling options set)."), regularConstraints);

        pnlPrefs.add(cbManaBurn, regularConstraints);
        pnlPrefs.add(new NoteLabel("Play with mana burn (from pre-Magic 2010 rules)."), regularConstraints);

        pnlPrefs.add(cbEnforceDeckLegality, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enforces deck legality relevant to each environment (minimum deck sizes, max card count etc)."), regularConstraints);

        pnlPrefs.add(cbCloneImgSource, regularConstraints);
        pnlPrefs.add(new NoteLabel("When enabled clones will use their original art instead of the cloned card's art."), regularConstraints);

        pnlPrefs.add(cbPromptFreeBlocks, regularConstraints);
        pnlPrefs.add(new NoteLabel("When enabled, if you would have to pay 0 to block, pay automatically without prompt."), regularConstraints);

        // Deck building options
        pnlPrefs.add(new SectionLabel("Random Deck Generation"), sectionConstraints);

        pnlPrefs.add(cbRemoveSmall, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables 1/1 and 0/X creatures in generated decks."), regularConstraints);

        pnlPrefs.add(cbSingletons, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables non-land duplicates in generated decks."), regularConstraints);

        pnlPrefs.add(cbRemoveArtifacts, regularConstraints);
        pnlPrefs.add(new NoteLabel("Disables artifact cards in generated decks."), regularConstraints);

        // Advanced
        pnlPrefs.add(new SectionLabel("Advanced Settings"), sectionConstraints);

        pnlPrefs.add(cbDevMode, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enables menu with functions for testing during development."), regularConstraints);

        pnlPrefs.add(cbpGameLogEntryType, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Changes how much information is displayed in the game log. Sorted by least to most verbose."), regularConstraints);

        pnlPrefs.add(cbUseThemes, regularConstraints);
        pnlPrefs.add(new NoteLabel("Currently only applies to UIManager-related theme settings. (RESTART REQUIRED)"), regularConstraints);

        pnlPrefs.add(cbpCloseAction, "w 80%!, gap 10% 0 0 10px, span 2 1");
        pnlPrefs.add(new NoteLabel("Changes what happens when clicking the X button in the upper right."), regularConstraints);

        // Graphic Options
        pnlPrefs.add(new SectionLabel("Graphic Options"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(cbDisplayFoil, regularConstraints);
        pnlPrefs.add(new NoteLabel("Displays foil cards with the visual foil overlay effect."), regularConstraints);

        pnlPrefs.add(cbRandomFoil, regularConstraints);
        pnlPrefs.add(new NoteLabel("Adds foil effect to random cards."), regularConstraints);

        pnlPrefs.add(cbScaleLarger, regularConstraints);
        pnlPrefs.add(new NoteLabel("Allows card pictures to be expanded larger than their original size."), regularConstraints);

        pnlPrefs.add(cbRandomArtInPools, regularConstraints);
        pnlPrefs.add(new NoteLabel("Generates cards with random art in generated limited mode card pools."), regularConstraints);

        pnlPrefs.add(cbUiForTouchScreen, regularConstraints);
        pnlPrefs.add(new NoteLabel("Increases some UI elements to provide a better experience on touchscreen devices. (Needs restart)"), regularConstraints);

        pnlPrefs.add(cbCompactPrompt, regularConstraints);
        pnlPrefs.add(new NoteLabel("Hide header and use smaller font in Prompt pane to make it more compact."), regularConstraints);

        /*pnlPrefs.add(cbStackCardView, regularConstraints); TODO: Show this checkbox when setting can support being enabled
        pnlPrefs.add(new NoteLabel("Show cards and abilities on Stack in card view rather than list view."), regularConstraints);*/

        pnlPrefs.add(cbHideReminderText, regularConstraints);
        pnlPrefs.add(new NoteLabel("Hide reminder text in Card Detail pane."), regularConstraints);

        // Sound options
        pnlPrefs.add(new SectionLabel("Sound Options"), sectionConstraints + ", gaptop 2%");

        pnlPrefs.add(cbEnableSounds, regularConstraints);
        pnlPrefs.add(new NoteLabel("Enable sound effects during the game."), regularConstraints);

        pnlPrefs.add(cbAltSoundSystem, regularConstraints);
        pnlPrefs.add(new NoteLabel("Use the alternate sound system (only use if you have issues with sound not playing or disappearing)."), regularConstraints);


        // Keyboard shortcuts
        final JLabel lblShortcuts = new SectionLabel("Keyboard Shortcuts");
        pnlPrefs.add(lblShortcuts, sectionConstraints + ", gaptop 2%");

        final List<Shortcut> shortcuts = Singletons.getControl().getShortcuts();

        for (final Shortcut s : shortcuts) {
            pnlPrefs.add(new FLabel.Builder().text(s.getDescription())
                    .fontAlign(SwingConstants.RIGHT).build(), "w 50%!, h 22px!, gap 0 2% 0 1%");
            KeyboardShortcutField field = new KeyboardShortcutField(s);
            pnlPrefs.add(field, "w 25%!");
            shortcutFields.put(s.getPrefKey(), field);
        }
    }

    public void reloadShortcuts() {
        for (Map.Entry<FPref, KeyboardShortcutField> e : shortcutFields.entrySet()) {
            e.getValue().reload(e.getKey());
        }
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0"));
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrContent, "w 98%!, h 98%!, gap 1% 0 1% 0");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.SETTINGS;
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Preferences";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_PREFERENCES;
    }

    /** Consolidates checkbox styling in one place. */
    @SuppressWarnings("serial")
    private class OptionsCheckBox extends FCheckBox {
        public OptionsCheckBox(final String txt0) {
            super(txt0);
            this.setFont(FSkin.getBoldFont(12));
        }
    }

    /** Consolidates section title label styling in one place. */
    @SuppressWarnings("serial")
    private class SectionLabel extends SkinnedLabel {
        public SectionLabel(final String txt0) {
            super(txt0);
            this.setBorder(new FSkin.MatteSkinBorder(0, 0, 1, 0, FSkin.getColor(FSkin.Colors.CLR_BORDERS)));
            setHorizontalAlignment(SwingConstants.CENTER);
            this.setFont(FSkin.getBoldFont(16));
            this.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
    }

    /** Consolidates notation label styling in one place. */
    @SuppressWarnings("serial")
    private class NoteLabel extends SkinnedLabel {
        public NoteLabel(final String txt0) {
            super(txt0);
            this.setFont(FSkin.getItalicFont(12));
            this.setForeground(FSkin.getColor(FSkin.Colors.CLR_TEXT));
        }
    }

    /**
     * A FTextField plus a "codeString" property, that stores keycodes for the
     * shortcut. Also, an action listener that handles translation of keycodes
     * into characters and (dis)assembly of keycode stack.
     */
    @SuppressWarnings("serial")
    public class KeyboardShortcutField extends SkinnedTextField {
        private String codeString;

        /**
         * A JTextField plus a "codeString" property, that stores keycodes for
         * the shortcut. Also, an action listener that handles translation of
         * keycodes into characters and (dis)assembly of keycode stack.
         * 
         * @param shortcut0 &emsp; Shortcut object
         */
        public KeyboardShortcutField(final Shortcut shortcut0) {
            super();
            this.setEditable(false);
            this.setFont(FSkin.getFont(14));
            final FPref prefKey = shortcut0.getPrefKey();
            reload(prefKey);

            this.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(final KeyEvent evt) {
                    KeyboardShortcuts.addKeyCode(evt);
                }
            });

            this.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(final FocusEvent evt) {
                    KeyboardShortcutField.this.setBackground(FSkin.getColor(FSkin.Colors.CLR_ACTIVE));
                }

                @Override
                public void focusLost(final FocusEvent evt) {
                    Singletons.getModel().getPreferences().setPref(prefKey, getCodeString());
                    Singletons.getModel().getPreferences().save();
                    shortcut0.attach();
                    KeyboardShortcutField.this.setBackground(Color.white);
                }
            });
        }

        public void reload(FPref prefKey) {
            this.setCodeString(Singletons.getModel().getPreferences().getPref(prefKey));
        }

        /**
         * Gets the code string.
         * 
         * @return String
         */
        public final String getCodeString() {
            return this.codeString;
        }

        /**
         * Sets the code string.
         * 
         * @param str0
         *            &emsp; The new code string (space delimited)
         */
        public final void setCodeString(final String str0) {
            if ("null".equals(str0)) {
                return;
            }

            this.codeString = str0.trim();

            final List<String> codes = new ArrayList<String>(Arrays.asList(this.codeString.split(" ")));
            final List<String> displayText = new ArrayList<String>();

            for (final String s : codes) {
                if (!s.isEmpty()) {
                    displayText.add(KeyEvent.getKeyText(Integer.valueOf(s)));
                }
            }

            this.setText(StringUtils.join(displayText, ' '));
        }
    }

    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbCompactMainMenu() {
        return cbCompactMainMenu;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbRemoveSmall() {
        return cbRemoveSmall;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public final JCheckBox getCbSingletons() {
        return cbSingletons;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRemoveArtifacts() {
        return cbRemoveArtifacts;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbUploadDraft() {
        return cbUploadDraft;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbEnableAICheats() {
        return cbEnableAICheats;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbDisplayFoil() {
        return cbDisplayFoil;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRandomFoil() {
        return cbRandomFoil;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbAnte() {
        return cbAnte;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbManaBurn() {
        return cbManaBurn;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbScaleLarger() {
        return cbScaleLarger;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbRandomArtInPools() {
        return cbRandomArtInPools;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbDevMode() {
        return cbDevMode;
    }

    public FComboBoxPanel<String> getAiProfilesComboBoxPanel() {
        return cbpAiProfiles;
    }

    public FComboBoxPanel<GameLogEntryType> getGameLogVerbosityComboBoxPanel() {
        return cbpGameLogEntryType;
    }

    public FComboBoxPanel<CloseAction> getCloseActionComboBoxPanel() {
        return cbpCloseAction;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbEnforceDeckLegality() {
        return cbEnforceDeckLegality;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbCloneImgSource() {
        return cbCloneImgSource;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbPromptFreeBlocks() {
        return cbPromptFreeBlocks;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbEnableSounds() {
        return cbEnableSounds;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbAltSoundSystem() {
        return cbAltSoundSystem;
    }

    public final JCheckBox getCbUiForTouchScreen() {
        return cbUiForTouchScreen;
    }

    public final JCheckBox getCbCompactPrompt() {
        return cbCompactPrompt;
    }

    public final JCheckBox getCbStackCardView() {
        return cbStackCardView;
    }

    public final JCheckBox getCbHideReminderText() {
        return cbHideReminderText;
    }

    /** @return {@link forge.gui.toolbox.FLabel} */
    public FLabel getBtnReset() {
        return btnReset;
    }

    /** @return {@link javax.swing.JCheckBox} */
    public JCheckBox getCbUseThemes() {
        return cbUseThemes;
    }

    public FLabel getBtnPlayerName() {
        return btnPlayerName;
    }

    //========== Overridden from IVDoc

    public final FLabel getBtnDeleteMatchUI() {
        return btnDeleteMatchUI;
    }

    public final FLabel getBtnDeleteEditorUI() {
        return btnDeleteEditorUI;
    }

    public final FLabel getBtnDeleteWorkshopUI() {
        return btnDeleteWorkshopUI;
    }

    public final FLabel getBtnContentDirectoryUI() { return btnContentDirectoryUI; }

    public final FLabel getBtnUserProfileUI() { return btnUserProfileUI; }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_PREFERENCES;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CSubmenuPreferences getLayoutControl() {
        return CSubmenuPreferences.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return parentCell;
    }

    private JPanel getPlayerNamePanel() {
        JPanel p = new JPanel(new MigLayout("insets 0, gap 0!"));
        p.setOpaque(false);
        FLabel lbl = new FLabel.Builder().text("Player Name: ").fontSize(12).fontStyle(Font.BOLD).build();
        p.add(lbl, "aligny top, h 100%");
        p.add(btnPlayerName, "aligny top, h 100%, w 200px!");
        return p;
    }
}
