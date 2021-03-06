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
package forge.quest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import forge.Singletons;
import forge.card.CardEdition;
import forge.card.UnOpenedProduct;
import forge.gui.CardListViewer;
import forge.gui.GuiChoose;
import forge.gui.toolbox.FOptionPane;
import forge.item.PaperCard;
import forge.item.SealedProduct;
import forge.quest.io.ReadPriceList;
import forge.util.storage.IStorage;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.*;

/** 
 * This is a helper class for unlocking new sets during a format-limited
 * quest.
 *
 */
public class QuestUtilUnlockSets {
    private static int UNLOCK_COST = 4000;

    /**
     * Consider unlocking a new expansion in limited quest format.
     * @param qData the QuestController for the current quest
     * @param freeUnlock this unlock is free (e.g., a challenge reward), NOT IMPLEMENTED YET
     * @param presetChoices List<CardEdition> a pregenerated list of options, NOT IMPLEMENTED YET
     * @return CardEdition, the unlocked edition if any.
     */
    public static ImmutablePair<CardEdition, Integer> chooseSetToUnlock(final QuestController qData, final boolean freeUnlock,
            List<CardEdition> presetChoices) {

        if (qData.getFormat() == null || !qData.getFormat().canUnlockSets()) {
            return null;
        }

        final ReadPriceList prices = new ReadPriceList();
        final Map<String, Integer> mapPrices = prices.getPriceList();
        final List<ImmutablePair<CardEdition, Integer>> setPrices = new ArrayList<ImmutablePair<CardEdition, Integer>>();

        for (CardEdition ed : getUnlockableEditions(qData)) {
            int price = UNLOCK_COST;
            if (mapPrices.containsKey(ed.getName() + " Booster Pack")) {
                price = Math.max(new Double(30 * Math.pow(Math.sqrt(mapPrices.get(ed.getName()
                        + " Booster Pack")), 1.70)).intValue(), UNLOCK_COST);
            }
            setPrices.add(ImmutablePair.of(ed, price));
        }

        final String setPrompt = "You have " + qData.getAssets().getCredits() + " credits. Unlock:";
        List<String> options = new ArrayList<String>();
        for (ImmutablePair<CardEdition, Integer> ee : setPrices) {
            options.add(String.format("%s [PRICE: %d credits]",  ee.left.getName(), ee.right));
        }

        int index = options.indexOf(GuiChoose.oneOrNone(setPrompt, options));
        if (index < 0 || index >= options.size()) {
            return null;
        }

        ImmutablePair<CardEdition, Integer> toBuy = setPrices.get(index);

        int price = toBuy.right;
        CardEdition choosenEdition = toBuy.left;

        if (qData.getAssets().getCredits() < price) {
            FOptionPane.showMessageDialog("Unfortunately, you cannot afford that set yet.\n"
                    + "To unlock " + choosenEdition.getName() + ", you need " + price + " credits.\n"
                    + "You have only " + qData.getAssets().getCredits() + " credits.",
                    "Failed to unlock " + choosenEdition.getName(),
                    null);
            return null;
        }

        if (!FOptionPane.showConfirmDialog(
                "Unlocking " + choosenEdition.getName() + " will cost you " + price + " credits.\n"
                + "You have " + qData.getAssets().getCredits() + " credits.\n\n"
                + "Are you sure you want to unlock " + choosenEdition.getName() + "?",
                "Confirm Unlocking " + choosenEdition.getName())) {
            return null;
        }
        return toBuy;
    }

    /**
     * Helper function for unlockSet().
     * 
     * @return unmodifiable list, assorted sets that are not currently in the format.
     */
    private static final List<CardEdition> emptyEditions = ImmutableList.<CardEdition>of();
    private static final EnumSet<CardEdition.Type> unlockableSetTypes =
        EnumSet.of(CardEdition.Type.CORE, CardEdition.Type.EXPANSION, CardEdition.Type.REPRINT, CardEdition.Type.STARTER);

    private static List<CardEdition> getUnlockableEditions(final QuestController qData) {
        if (qData.getFormat() == null || !qData.getFormat().canUnlockSets()) {
            return emptyEditions;
        }

        if (qData.getUnlocksTokens() < 1) { // Should never happen if we made it this far but better safe than sorry...
            throw new RuntimeException("BUG? Could not find unlockable sets even though we should.");
        }
        List<CardEdition> options = new ArrayList<CardEdition>();

        // Sort current sets by date
        List<CardEdition> allowedSets = Lists.newArrayList(Iterables.transform(qData.getFormat().getAllowedSetCodes(), Singletons.getMagicDb().getEditions().FN_EDITION_BY_CODE));
        Collections.sort(allowedSets);
        
        // Sort unlockable sets by date
        List<CardEdition> excludedSets = Lists.newArrayList(Iterables.transform(qData.getFormat().getLockedSets(), Singletons.getMagicDb().getEditions().FN_EDITION_BY_CODE));
        Collections.sort(excludedSets);
        
        // get a number of sets between an excluded and any included set
        List<ImmutablePair<CardEdition, Long>> excludedWithDistances = new ArrayList<ImmutablePair<CardEdition, Long>>();
        for (CardEdition ex : excludedSets) {
            if (!unlockableSetTypes.contains(ex.getType())) // don't add non-traditional sets
                continue;
            long distance = Long.MAX_VALUE;
            for (CardEdition in : allowedSets) {
                long d = (Math.abs(ex.getDate().getTime() - in.getDate().getTime()));
                if (d < distance) {
                    distance = d;
                }
            }
            excludedWithDistances.add(ImmutablePair.of(ex, distance));
        }

         // sort by distance, then by code desc
         Collections.sort(excludedWithDistances, new Comparator<ImmutablePair<CardEdition, Long>>() {
            @Override
            public int compare(ImmutablePair<CardEdition, Long> o1, ImmutablePair<CardEdition, Long> o2) {
                long delta = o2.right - o1.right;
                return delta < 0 ? -1 : delta == 0 ? 0 : 1;
            }
         });


         for (ImmutablePair<CardEdition, Long> set : excludedWithDistances) {
             options.add(set.left);
             // System.out.println("Padded with: " + fillers.get(i).getName());
         }
         Collections.reverse(options);

         return options.subList(0, Math.min(options.size(), Math.min(8, 2 + ((qData.getAchievements().getWin()) / 50))));
    }

    /**
     * 
     * Unlock a set and get some free cards, if a tournament pack or boosters are available.
     * @param qData the quest controller
     * @param unlockedSet the edition to unlock
     */
    public static void doUnlock(QuestController qData, final CardEdition unlockedSet) {

        IStorage<SealedProduct.Template> starters = Singletons.getMagicDb().getTournamentPacks();
        IStorage<SealedProduct.Template> boosters = Singletons.getMagicDb().getBoosters();
        qData.getFormat().unlockSet(unlockedSet.getCode());

        List<PaperCard> cardsWon = new ArrayList<PaperCard>();

        if (starters.contains(unlockedSet.getCode())) {
            UnOpenedProduct starter = new UnOpenedProduct(starters.get(unlockedSet.getCode()));
            cardsWon.addAll(starter.get());
        }
        else if (boosters.contains(unlockedSet.getCode())) {
            UnOpenedProduct booster = new UnOpenedProduct(boosters.get(unlockedSet.getCode()));
            cardsWon.addAll(booster.get());
            cardsWon.addAll(booster.get());
            cardsWon.addAll(booster.get());
        }

        qData.getCards().addAllCards(cardsWon);
        final CardListViewer cardView = new CardListViewer(unlockedSet.getName(), "You get the following bonus cards:", cardsWon);
        cardView.setVisible(true);
        cardView.dispose();
        qData.save();
    }
}
