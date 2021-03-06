package forge.gui.deckchooser;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.Singletons;
import forge.card.CardDb;
import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.card.ColorSet;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.deck.generation.*;
import forge.gui.toolbox.FOptionPane;
import forge.gui.toolbox.itemmanager.DeckManager;
import forge.item.PaperCard;
import forge.properties.ForgePreferences.FPref;
import forge.quest.QuestController;
import forge.quest.QuestEvent;
import forge.quest.QuestEventChallenge;
import forge.quest.QuestEventDuel;
import forge.util.Aggregates;
import forge.util.Lang;
import forge.util.MyRandom;
import forge.util.storage.IStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 
 * Utility collection for various types of decks.
 * - Builders (builds or retrieves deck based on a selection)
 * - Randomizers (retrieves random deck of selected type)
 * - Color checker (see javadoc)
 * - Decklist display-er
 */
// TODO This class can be used for home menu constructed deck generation as well.
public class DeckgenUtil {
    /** */
    public enum DeckTypes {
        COLORS,
        THEMES,
        CUSTOM,
        QUESTEVENTS,
        PRECON
    }

    /**
     * @param selection {@link java.lang.String} array
     * @return {@link forge.deck.Deck}
     */
    public static Deck buildColorDeck(List<String> selection, boolean forAi) {
        final Deck deck;
        String deckName = null;

        DeckGeneratorBase gen = null;
        CardDb cardDb = Singletons.getMagicDb().getCommonCards();
        if (selection.size() == 1) {
            gen = new DeckGeneratorMonoColor(cardDb, selection.get(0));
        }
        else if (selection.size() == 2) {
            gen = new DeckGenerator2Color(cardDb, selection.get(0), selection.get(1));
        }
        else if (selection.size() == 3) {
            gen = new DeckGenerator3Color(cardDb, selection.get(0), selection.get(1), selection.get(2));
        }
        else {
            gen = new DeckGenerator5Color(cardDb);
            deckName = "5 colors";
        }
        gen.setSingleton(Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_SINGLETONS));
        gen.setUseArtifacts(!Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
        CardPool cards = gen == null ? null : gen.getDeck(60, forAi);

        if (null == deckName) {
            deckName = Lang.joinHomogenous(Arrays.asList(selection));
        }

        // After generating card lists, build deck.
        deck = new Deck("Random deck : " + deckName);
        deck.getMain().addAll(cards);

        return deck;
    }

    public static QuestEvent getQuestEvent(final String name) {
        QuestController qCtrl = Singletons.getModel().getQuest();
        for (QuestEventChallenge challenge : qCtrl.getChallenges()) {
            if (challenge.getTitle().equals(name)) {
                return challenge;
            }
        }

        QuestEventDuel duel = Iterables.find(qCtrl.getDuelsManager().getAllDuels(), new Predicate<QuestEventDuel>() {
            @Override public boolean apply(QuestEventDuel in) { return in.getName().equals(name); }
        });
        return duel;
    }

    /** @return {@link forge.deck.Deck} */
    public static Deck getRandomColorDeck(boolean forAi) {
        final int[] colorCount = new int[] {1, 2, 3, 5};
        final int count = colorCount[MyRandom.getRandom().nextInt(colorCount.length)];
        final List<String> selection = new ArrayList<String>();

        // A simulated selection of "random 1" will trigger the AI selection process.
        for (int i = 0; i < count; i++) { selection.add("Random"); }

        return DeckgenUtil.buildColorDeck(selection, forAi);
    }

    /** @return {@link forge.deck.Deck} */
    public static Deck getRandomCustomDeck() {
        final IStorage<Deck> allDecks = Singletons.getModel().getDecks().getConstructed();
        final int rand = (int) (Math.floor(Math.random() * allDecks.size()));
        final String name = allDecks.getItemNames().toArray(new String[0])[rand];
        return allDecks.get(name);
    }

    /** @return {@link forge.deck.Deck} */
    public static Deck getRandomQuestDeck() {
        final List<Deck> allQuestDecks = new ArrayList<Deck>();
        QuestController qCtrl = Singletons.getModel().getQuest();

        for (final QuestEvent e : qCtrl.getDuelsManager().getAllDuels()) {
            allQuestDecks.add(e.getEventDeck());
        }

        for (final QuestEvent e : qCtrl.getChallenges()) {
            allQuestDecks.add(e.getEventDeck());
        }

        final int rand = (int) (Math.floor(Math.random() * allQuestDecks.size()));
        return allQuestDecks.get(rand);
    }

    public static void randomSelectColors(final DeckManager deckManager) {
        final int size = deckManager.getItemCount();
        if (size == 0) { return; }

        int nColors = MyRandom.getRandom().nextInt(3) + 1;
        Integer[] indices = new Integer[nColors];
        for (int i = 0; i < nColors; i++) {
            int next = MyRandom.getRandom().nextInt(size);

            boolean isUnique = true;
            for (int j = 0; j < i; j++) {
                if (indices[j] == next) {
                    isUnique = false;
                    break;
                }
            }
            if (isUnique) {
                indices[i] = next;
            }
            else {
                i--; // try over with this number
            }
        }
        deckManager.setSelectedIndices(indices);
    }

    public static void randomSelect(final DeckManager deckManager) {
        final int size = deckManager.getItemCount();
        if (size == 0) { return; }

        deckManager.setSelectedIndex(MyRandom.getRandom().nextInt(size));
    }

    /** 
     * Checks lengths of selected values for color lists
     * to see if a deck generator exists. Alert and visual reminder if fail.
     * 
     * @param colors0 String[]
     * @return boolean
     */
    public static boolean colorCheck(final List<String> colors0) {
        boolean result = true;

        if (colors0.size() == 4) {
            FOptionPane.showMessageDialog(
                    "Sorry, four color generated decks aren't supported yet."
                    + "\n\rPlease use 2, 3, or 5 colors for this deck.",
                    "Generate deck: 4 colors", FOptionPane.ERROR_ICON);
            result = false;
        }
        else if (colors0.size() > 5) {
            FOptionPane.showMessageDialog(
                    "Generate deck: maximum five colors!",
                    "Generate deck: too many colors", FOptionPane.ERROR_ICON);
            result = false;
        }
        return result;
    }

    public static CardPool generateSchemeDeck() {
        CardPool schemes = new CardPool();
        List<PaperCard> allSchemes = new ArrayList<PaperCard>();
        for (PaperCard c : Singletons.getMagicDb().getVariantCards().getAllCards()) {
            if (c.getRules().getType().isScheme()) {
                allSchemes.add(c);
            }
        }

        int schemesToAdd = 20;
        int attemptsLeft = 100; // to avoid endless loop
        while (schemesToAdd > 0 && attemptsLeft > 0) {
            PaperCard cp = Aggregates.random(allSchemes);
            int appearances = schemes.count(cp) + 1;
            if (appearances < 2) {
                schemes.add(cp);
                schemesToAdd--;
            }
            else {
                attemptsLeft--;
            }
        }

        return schemes;
    }

    public static CardPool generatePlanarDeck() {
        CardPool res = new CardPool();
        List<PaperCard> allPlanars = new ArrayList<PaperCard>();
        for (PaperCard c : Singletons.getMagicDb().getVariantCards().getAllCards()) {
            if (c.getRules().getType().isPlane() || c.getRules().getType().isPhenomenon()) {
                allPlanars.add(c);
            }
        }

        int phenoms = 0;
        int targetsize = MyRandom.getRandom().nextInt(allPlanars.size()-10)+10;
        while (true) {
            PaperCard rndPlane = Aggregates.random(allPlanars);
            allPlanars.remove(rndPlane);

            if (rndPlane.getRules().getType().isPhenomenon() && phenoms < 2) {
                res.add(rndPlane);
                phenoms++;
            }
            else if (rndPlane.getRules().getType().isPlane()) {
                res.add(rndPlane);
            }

            if (allPlanars.isEmpty() || res.countAll() == targetsize) {
                break;
            }
        }

        return res;
    }

    /** Generate a 2-color Commander deck. */
    public static Deck generateCommanderDeck(boolean forAi) {
        final Deck deck;
        CardDb cardDb = Singletons.getMagicDb().getCommonCards();
        PaperCard commander;
        ColorSet colorID;

        // Get random multicolor Legendary creature
        Predicate<CardRules> canPlay = forAi ? DeckGeneratorBase.AI_CAN_PLAY : DeckGeneratorBase.HUMAN_CAN_PLAY;
        Iterable<PaperCard> legends = cardDb.getAllCards(Predicates.compose(Predicates.and
                (CardRulesPredicates.Presets.IS_CREATURE, CardRulesPredicates.Presets.IS_LEGENDARY), PaperCard.FN_GET_RULES));
        legends = Iterables.filter(legends, Predicates.compose(Predicates.and
        		(CardRulesPredicates.Presets.IS_MULTICOLOR, canPlay), PaperCard.FN_GET_RULES));
        
        do {
            commander = Aggregates.random(legends);
            colorID = commander.getRules().getColorIdentity();
        } while (colorID.countColors() != 2);

        List<String> comColors = new ArrayList<String>(2);
        if (colorID.hasWhite()) {	comColors.add("White"); }
        if (colorID.hasBlue()) {	comColors.add("Blue"); }
        if (colorID.hasBlack()) {	comColors.add("Black"); }
        if (colorID.hasRed()) {		comColors.add("Red"); }
        if (colorID.hasGreen()) {	comColors.add("Green"); }
        
        DeckGeneratorBase gen = null;
        gen = new DeckGenerator2Color(cardDb, comColors.get(0), comColors.get(1));
        gen.setSingleton(true);
        gen.setUseArtifacts(!Singletons.getModel().getPreferences().getPrefBoolean(FPref.DECKGEN_ARTIFACTS));
        CardPool cards = gen == null ? null : gen.getDeck(99, forAi);

        // After generating card lists, build deck.
        deck = new Deck("Generated Commander deck (" + commander.getName() + ")");
        deck.getMain().addAll(cards);
        deck.getOrCreate(DeckSection.Commander).add(commander);

        return deck;
    }
}
