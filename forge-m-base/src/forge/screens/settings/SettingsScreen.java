package forge.screens.settings;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge;
import forge.Forge.Graphics;
import forge.ai.AiProfileUtil;
import forge.assets.FSkin;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinColor.Colors;
import forge.game.GameLogEntryType;
import forge.model.FModel;
import forge.screens.FScreen;
import forge.toolbox.FList;
import forge.utils.ForgePreferences.FPref;
import forge.utils.Utils;

public class SettingsScreen extends FScreen {
    private static final float INSETS_FACTOR = 0.025f;
    private static final FSkinFont DESC_FONT = FSkinFont.get(11);
    private static final FSkinColor DESC_COLOR = FSkinColor.get(Colors.CLR_TEXT).alphaColor(0.5f);

    private final FList<Setting> lstSettings = add(new FList<Setting>());

    public SettingsScreen() {
        super(true, "Settings", false);
        lstSettings.setListItemRenderer(new SettingRenderer());

        lstSettings.addGroup("General Settings");
        lstSettings.addGroup("Gameplay Options");
        lstSettings.addGroup("Random Deck Generation");
        lstSettings.addGroup("Advanced Settings");
        lstSettings.addGroup("Graphic Options");
        lstSettings.addGroup("Sound Options");

        //General Settings
        lstSettings.addItem(new CustomSelectSetting(FPref.UI_SKIN, "Theme",
                "Sets the theme that determines how display components are skinned.",
                FSkin.getAllSkins()) {
            @Override
            public void valueChanged(String newValue) {
                FSkin.changeSkin(newValue);
            }
        }, 0);

        //Gameplay Options
        lstSettings.addItem(new CustomSelectSetting(FPref.UI_CURRENT_AI_PROFILE,
                "AI Personality",
                "Choose your AI opponent.",
                AiProfileUtil.getProfilesArray()),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.UI_ANTE,
                "Play for Ante",
                "Determines whether or not the game is played for ante."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.UI_UPLOAD_DRAFT,
                "Upload Draft Picks",
                "Sends draft picks to Forge servers for analysis, to improve draft AI."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.UI_ENABLE_AI_CHEATS,
                "Allow AI Cheating",
                "Allow the AI to cheat to gain advantage (for personalities that have cheat shuffling options set)."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.UI_MANABURN,
                "Mana Burn",
                "Play with mana burn (from pre-Magic 2010 rules)."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.ENFORCE_DECK_LEGALITY,
                "Deck Conformance",
                "Enforces deck legality relevant to each environment (minimum deck sizes, max card count etc)."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.UI_CLONE_MODE_SOURCE,
                "Clones Use Original Card Art",
                "When enabled clones will use their original art instead of the cloned card's art."),
                1);
        lstSettings.addItem(new BooleanSetting(FPref.MATCHPREF_PROMPT_FREE_BLOCKS,
                "Free Block Handling",
                "When enabled, if you would have to pay 0 to block, pay automatically without prompt."),
                1);

        //Random Deck Generation
        lstSettings.addItem(new BooleanSetting(FPref.DECKGEN_NOSMALL,
                "Remove Small Creatures",
                "Disables 1/1 and 0/X creatures in generated decks."),
                2);
        lstSettings.addItem(new BooleanSetting(FPref.DECKGEN_SINGLETONS,
                "Singleton Mode",
                "Disables non-land duplicates in generated decks."),
                2);
        lstSettings.addItem(new BooleanSetting(FPref.DECKGEN_ARTIFACTS,
                "Remove Artifacts",
                "Disables artifact cards in generated decks."),
                2);

        //Advanced Settings
        lstSettings.addItem(new BooleanSetting(FPref.DEV_MODE_ENABLED,
                "Developer Mode",
                "Enables menu with functions for testing during development."),
                3);
        lstSettings.addItem(new CustomSelectSetting(FPref.DEV_LOG_ENTRY_TYPE,
                "Game Log Verbosity",
                "Changes how much information is displayed in the game log. Sorted by least to most verbose.",
                GameLogEntryType.class),
                3);

        //Graphic Options
        lstSettings.addItem(new BooleanSetting(FPref.UI_OVERLAY_FOIL_EFFECT,
                "Display Foil Overlay",
                "Displays foil cards with the visual foil overlay effect."),
                4);
        lstSettings.addItem(new BooleanSetting(FPref.UI_RANDOM_FOIL,
                "Random Foil",
                "Adds foil effect to random cards."),
                4);
        lstSettings.addItem(new BooleanSetting(FPref.UI_RANDOM_ART_IN_POOLS,
                "Randomize Card Art",
                "Generates cards with random art in generated limited mode card pools."),
                4);
        lstSettings.addItem(new BooleanSetting(FPref.UI_HIDE_REMINDER_TEXT,
                "Hide Reminder Text",
                "Hide reminder text in Card Detail pane."),
                4);

        //Sound Options
        lstSettings.addItem(new BooleanSetting(FPref.UI_ENABLE_SOUNDS,
                "Enable Sounds",
                "Enable sound effects during the game."),
                5);
        lstSettings.addItem(new BooleanSetting(FPref.UI_ALT_SOUND_SYSTEM,
                "Use Alternate Sound System",
                "Use the alternate sound system (only use if you have issues with sound not playing or disappearing)."),
                5);
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        lstSettings.setBounds(0, startY, width, height - startY);
    }

    private abstract class Setting {
        protected String label;
        protected String description;
        protected FPref pref;

        public Setting(FPref pref0, String label0, String description0) {
            label = label0;
            description = description0;
            pref = pref0;
        }

        public abstract void select();
        public abstract void drawPrefValue(Graphics g, FSkinFont font, FSkinColor color, float x, float y, float width, float height);
    }

    private class BooleanSetting extends Setting {
        public BooleanSetting(FPref pref0, String label0, String description0) {
            super(pref0, label0, description0);
        }

        @Override
        public void select() {
            FModel.getPreferences().setPref(pref, !FModel.getPreferences().getPrefBoolean(pref));
            FModel.getPreferences().save();
        }

        @Override
        public void drawPrefValue(Graphics g, FSkinFont font, FSkinColor color, float x, float y, float w, float h) {
            x += w - h;
            w = h;
            g.drawRect(1, DESC_COLOR, x, y, w, h);
            if (FModel.getPreferences().getPrefBoolean(pref)) {
                //draw check mark
                x += 3;
                y++;
                w -= 6;
                h -= 3;
                g.drawLine(2, color, x, y + h / 2, x + w / 2, y + h);
                g.drawLine(2, color, x + w / 2, y + h, x + w, y);
            }
        }
    }

    private class CustomSelectSetting extends Setting {
        private final List<String> options = new ArrayList<String>();

        public CustomSelectSetting(FPref pref0, String label0, String description0, String[] options0) {
            super(pref0, label0 + ":", description0);

            for (String option : options0) {
                options.add(option);
            }
        }
        public CustomSelectSetting(FPref pref0, String label0, String description0, Iterable<String> options0) {
            super(pref0, label0 + ":", description0);

            for (String option : options0) {
                options.add(option);
            }
        }
        public <E extends Enum<E>> CustomSelectSetting(FPref pref0, String label0, String description0, Class<E> enumData) {
            super(pref0, label0 + ":", description0);

            for (E option : enumData.getEnumConstants()) {
                options.add(option.toString());
            }
        }

        public void valueChanged(String newValue) {
            FModel.getPreferences().setPref(pref, newValue);
            FModel.getPreferences().save();
        }

        @Override
        public void select() {
            Forge.openScreen(new CustomSelectScreen());
        }

        private class CustomSelectScreen extends FScreen {
            private final FList<String> lstOptions;
            private final String currentValue = FModel.getPreferences().getPref(pref);

            private CustomSelectScreen() {
                super(true, "Select " + label.substring(0, label.length() - 1), false);
                lstOptions = add(new FList<String>(options));
                lstOptions.setListItemRenderer(new FList.DefaultListItemRenderer<String>() {
                    @Override
                    public boolean tap(String value, float x, float y, int count) {
                        if (!value.equals(currentValue)) {
                            valueChanged(value);
                        }
                        Forge.back();
                        return true;
                    }

                    @Override
                    public void drawValue(Graphics g, String value, FSkinFont font, FSkinColor foreColor, float width, float height) {
                        float x = width * INSETS_FACTOR;
                        float y = 0;
                        width -= 2 * x;

                        g.drawText(value, font, foreColor, x, y, width, height, false, HAlignment.LEFT, true);

                        float radius = height / 5;
                        x += width - radius;
                        y = height / 2;
                        g.drawCircle(1, DESC_COLOR, x, y, radius);
                        if (value.equals(currentValue)) {
                            g.fillCircle(foreColor, x, y, radius / 2);
                        }
                    }
                });
            }

            @Override
            protected void doLayout(float startY, float width, float height) {
                lstOptions.setBounds(0, startY, width, height - startY);
            }
        }

        @Override
        public void drawPrefValue(Graphics g, FSkinFont font, FSkinColor color, float x, float y, float w, float h) {
            g.drawText(FModel.getPreferences().getPref(pref), font, color, x, y, w, h, false, HAlignment.RIGHT, false);
        }
    }

    private class SettingRenderer extends FList.ListItemRenderer<Setting> {
        @Override
        public float getItemHeight() {
            return Utils.AVG_FINGER_HEIGHT + 12;
        }

        @Override
        public boolean tap(Setting value, float x, float y, int count) {
            value.select();
            return true;
        }

        @Override
        public void drawValue(Graphics g, Setting value, FSkinFont font, FSkinColor color, float width, float height) {
            float x = width * INSETS_FACTOR;
            float y = x;
            float w = width - 2 * x;
            float h = font.getFont().getMultiLineBounds(value.label).height + 5;

            g.drawText(value.label, font, color, x, y, w, h, false, HAlignment.LEFT, false);
            value.drawPrefValue(g, font, color, x, y, w, h);
            h += 5;
            g.drawText(value.description, DESC_FONT, DESC_COLOR, x, y + h, w, height - h - y, true, HAlignment.LEFT, false);            
        }
    }
}
