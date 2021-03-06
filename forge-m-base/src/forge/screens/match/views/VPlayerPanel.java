package forge.screens.match.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.FSkinColor.Colors;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.screens.match.FControl;
import forge.screens.match.MatchScreen;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;

public class VPlayerPanel extends FContainer {
    private static final FSkinFont LIFE_FONT = FSkinFont.get(18);
    private static final FSkinFont INFO_FONT = FSkinFont.get(12);
    private static final FSkinColor INFO_FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor DISPLAY_AREA_BACK_COLOR = FSkinColor.get(Colors.CLR_INACTIVE).alphaColor(0.5f);

    private final Player player;
    private final VPhaseIndicator phaseIndicator;
    private final VField field;
    private final VAvatar avatar;
    private final LifeLabel lblLife;
    private final InfoTab tabManaPool;
    private final InfoTab tabFlashbackZone;
    private final Map<ZoneType, InfoTab> zoneTabs = new HashMap<ZoneType, InfoTab>();
    private final List<InfoTab> tabs = new ArrayList<InfoTab>();
    private InfoTab selectedTab;

    public VPlayerPanel(Player player0) {
        player = player0;
        phaseIndicator = add(new VPhaseIndicator());
        field = add(new VField(player));
        avatar = add(new VAvatar(player));
        lblLife = add(new LifeLabel());
        addZoneDisplay(ZoneType.Hand, FSkinImage.HAND);
        addZoneDisplay(ZoneType.Graveyard, FSkinImage.GRAVEYARD);
        addZoneDisplay(ZoneType.Library, FSkinImage.LIBRARY);

        VFlashbackZone flashbackZone = add(new VFlashbackZone(player0));
        tabFlashbackZone = add(new InfoTab(FSkinImage.FLASHBACK, flashbackZone));
        tabs.add(tabFlashbackZone);

        VManaPool manaPool = add(new VManaPool(player));
        tabManaPool = add(new InfoTab(FSkinImage.MANA_X, manaPool));
        tabs.add(tabManaPool);

        addZoneDisplay(ZoneType.Exile, FSkinImage.EXILE);
        addZoneDisplay(ZoneType.Command, FSkinImage.PLANESWALKER);
    }

    public Player getPlayer() {
        return player;
    }

    public void addZoneDisplay(ZoneType zoneType, FSkinImage tabIcon) {
        VZoneDisplay zoneDisplay = add(new VZoneDisplay(player, zoneType));
        InfoTab zoneTab = add(new InfoTab(tabIcon, zoneDisplay));
        zoneTabs.put(zoneType, zoneTab);
        tabs.add(zoneTab);
    }

    public InfoTab getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedZone(ZoneType zoneType) {
        setSelectedTab(zoneTabs.get(zoneType));
    }

    private void setSelectedTab(InfoTab selectedTab0) {
        if (selectedTab == selectedTab0) {
            return;
        }

        if (selectedTab != null) {
            selectedTab.displayArea.setVisible(false);
        }

        selectedTab = selectedTab0;

        if (selectedTab != null) {
            selectedTab.displayArea.setVisible(true);
        }

        if (FControl.getView() != null) { //must revalidate entire screen so panel heights updated
            FControl.getView().revalidate();
        }
    }

    public boolean isFlipped() {
        return field.isFlipped();
    }
    public void setFlipped(boolean flipped0) {
        field.setFlipped(flipped0);
    }

    public VField getField() {
        return field;
    }

    public VPhaseIndicator getPhaseIndicator() {
        return phaseIndicator;
    }

    public VAvatar getAvatar() {
        return avatar;
    }

    public void updateLife() {
        lblLife.update();
    }

    public void updateManaPool() {
        tabManaPool.update();
    }

    public void updateZone(ZoneType zoneType) {
        if (zoneType == ZoneType.Battlefield) {
            field.update();
        }
        else {
            zoneTabs.get(zoneType).update();
        }
    }

    @Override
    protected void doLayout(float width, float height) {
        //layout for bottom panel by default
        float x = VAvatar.WIDTH;
        phaseIndicator.setBounds(x, height - VPhaseIndicator.HEIGHT, width - VAvatar.WIDTH, VPhaseIndicator.HEIGHT);

        float y = height - VAvatar.HEIGHT;
        float displayAreaHeight = y / 3;
        y -= displayAreaHeight;
        for (InfoTab tab : tabs) {
            tab.displayArea.setBounds(0, y, width, displayAreaHeight);
        }

        y = height - VAvatar.HEIGHT;
        avatar.setPosition(0, y);

        float lifeLabelWidth = LIFE_FONT.getFont().getBounds("99").width * 1.2f; //make just wide enough for 2-digit life totals
        float infoLabelHeight = VAvatar.HEIGHT - VPhaseIndicator.HEIGHT;
        lblLife.setBounds(x, y, lifeLabelWidth, infoLabelHeight);
        x += lifeLabelWidth;

        float infoTabWidth = (getWidth() - x) / tabs.size();
        for (InfoTab tab : tabs) {
            tab.setBounds(x, y, infoTabWidth, infoLabelHeight);
            x += infoTabWidth;
        }

        if (selectedTab != null) {
            y -= displayAreaHeight;
        }
        field.setBounds(0, 0, width, y);

        if (isFlipped()) { //flip all positions across x-axis if needed
            for (FDisplayObject child : getChildren()) {
                child.setTop(height - child.getBottom());
            }
        }
    }

    @Override
    public void drawBackground(Graphics g) {
        if (selectedTab != null) { //draw background and border for selected zone if needed 
            float w = getWidth();
            VDisplayArea selectedDisplayArea = selectedTab.displayArea;
            g.fillRect(DISPLAY_AREA_BACK_COLOR, 0, selectedDisplayArea.getTop(), w, selectedDisplayArea.getHeight());

            float y = isFlipped() ? selectedDisplayArea.getTop() + 1 : selectedDisplayArea.getBottom();
            //leave gap at selected zone tab
            g.drawLine(1, MatchScreen.BORDER_COLOR, 0, y, selectedTab.getLeft(), y);
            g.drawLine(1, MatchScreen.BORDER_COLOR, selectedTab.getRight(), y, w, y);
        }
    }

    private class LifeLabel extends FDisplayObject {
        private String life = "20";

        private LifeLabel() {
        }

        private void update() {
            life = String.valueOf(player.getLife());
        }

        @Override
        public void draw(Graphics g) {
            g.drawText(life, LIFE_FONT, INFO_FORE_COLOR, 0, 0, getWidth(), getHeight(), false, HAlignment.CENTER, true);
        }
    }

    public class InfoTab extends FDisplayObject {
        private String value = "0";
        private final FSkinImage icon;
        private final VDisplayArea displayArea;

        private InfoTab(FSkinImage icon0, VDisplayArea displayArea0) {
            icon = icon0;
            displayArea = displayArea0;
        }

        @Override
        public boolean tap(float x, float y, int count) {
            if (selectedTab == this) {
                setSelectedTab(null);
            }
            else {
                setSelectedTab(this);
            }
            return true;
        }

        public void update() {
            displayArea.update();
            value = String.valueOf(displayArea.getCount());
        }

        @Override
        public void draw(Graphics g) {
            float x, y, w, h;
            float paddingX = 2;
            float paddingY = 2;

            if (selectedTab == this) {
                y = 0;
                w = getWidth();
                h = getHeight();
                float yAcross;
                if (isFlipped()) {
                    y += paddingY;
                    yAcross = y;
                    y--;
                    h++;
                }
                else {
                    h -= paddingY;
                    yAcross = h;
                    y--;
                    h += 2;
                }
                g.fillRect(DISPLAY_AREA_BACK_COLOR, 0, isFlipped() ? paddingY : 0, w, getHeight() - paddingY);
                g.startClip(-1, y, w + 2, h); //use clip to ensure all corners connect
                g.drawLine(1, MatchScreen.BORDER_COLOR, 0, yAcross, w, yAcross);
                g.drawLine(1, MatchScreen.BORDER_COLOR, 0, y, 0, h);
                g.drawLine(1, MatchScreen.BORDER_COLOR, w, y, w, h);
                g.endClip();
            }

            h = Math.round(getHeight() * 0.7f / 20f) * 20f; //round to nearest 20 so images look ok
            w = h;
            x = paddingX;
            y = (getHeight() - h) / 2;
            g.drawImage(icon, x, y, w, h);

            x += w * 1.1f;
            g.drawText(value, INFO_FONT, INFO_FORE_COLOR, x, 0, getWidth() - x + 1, getHeight(), false, HAlignment.LEFT, true);
        }
    }
}
