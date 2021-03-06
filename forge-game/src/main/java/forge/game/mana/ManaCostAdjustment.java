package forge.game.mana;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;

import forge.card.mana.ManaCostShard;
import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardFactoryUtil;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.player.Player;
import forge.game.spellability.AbilityActivated;
import forge.game.spellability.Spell;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.TargetRestrictions;
import forge.game.staticability.StaticAbility;
import forge.game.zone.ZoneType;

public class ManaCostAdjustment {

    public static final void adjust(ManaCostBeingPaid cost, final SpellAbility sa, boolean test) {
        final Game game = sa.getActivatingPlayer().getGame();
        // Beached
        final Card originalCard = sa.getHostCard();
        if (sa.isXCost() && !originalCard.isCopiedSpell()) {
            originalCard.setXManaCostPaid(0);
        }
    
        if (sa.isTrigger()) {
            return;
        }
    
        if (sa.isSpell()) {
            if (sa.isDelve()) {
                final Player pc = originalCard.getController();
                final List<Card> mutableGrave = new ArrayList<Card>(pc.getCardsIn(ZoneType.Graveyard));
                final List<Card> toExile = pc.getController().chooseCardsToDelve(cost.getColorlessManaAmount(), mutableGrave);
                for (final Card c : toExile) {
                    cost.decreaseColorlessMana(1);
                    if (!test) {
                        pc.getGame().getAction().exile(c);
                    }
                }
            }
            else if (sa.getHostCard().hasKeyword("Convoke")) {
                adjustCostByConvoke(cost, sa);
            }
        } // isSpell
    
        List<Card> cardsOnBattlefield = Lists.newArrayList(game.getCardsIn(ZoneType.Battlefield));
        cardsOnBattlefield.addAll(game.getCardsIn(ZoneType.Stack));
        cardsOnBattlefield.addAll(game.getCardsIn(ZoneType.Command));
        if (!cardsOnBattlefield.contains(originalCard)) {
            cardsOnBattlefield.add(originalCard);
        }
        final ArrayList<StaticAbility> raiseAbilities = new ArrayList<StaticAbility>();
        final ArrayList<StaticAbility> reduceAbilities = new ArrayList<StaticAbility>();
        final ArrayList<StaticAbility> setAbilities = new ArrayList<StaticAbility>();
    
        // Sort abilities to apply them in proper order
        for (Card c : cardsOnBattlefield) {
            final ArrayList<StaticAbility> staticAbilities = c.getStaticAbilities();
            for (final StaticAbility stAb : staticAbilities) {
                if (stAb.getMapParams().get("Mode").equals("RaiseCost")) {
                    raiseAbilities.add(stAb);
                }
                else if (stAb.getMapParams().get("Mode").equals("ReduceCost")) {
                    reduceAbilities.add(stAb);
                }
                else if (stAb.getMapParams().get("Mode").equals("SetCost")) {
                    setAbilities.add(stAb);
                }
            }
        }
        // Raise cost
        for (final StaticAbility stAb : raiseAbilities) {
            applyAbility(stAb, "RaiseCost", sa, cost);
        }
    
        // Reduce cost
        for (final StaticAbility stAb : reduceAbilities) {
            applyAbility(stAb, "ReduceCost", sa, cost);
        }
        if (sa.isSpell() && sa.isOffering()) { // cost reduction from offerings
            adjustCostByOffering(cost, sa);
        }
    
        // Set cost (only used by Trinisphere) is applied last
        for (final StaticAbility stAb : setAbilities) {
            applyAbility(stAb, "SetCost", sa, cost);
        }
    } // GetSpellCostChange


    /**
     * Apply ability.
     * 
     * @param mode
     *            the mode
     * @param sa
     *            the SpellAbility
     * @param originalCost
     *            the originalCost
     * @return the modified ManaCost
     */
    private static final void applyAbility(StaticAbility stAb, final String mode, final SpellAbility sa, final ManaCostBeingPaid originalCost) {

        // don't apply the ability if it hasn't got the right mode
        if (!stAb.getMapParams().get("Mode").equals(mode)) {
            return;
        }

        if (stAb.isSuppressed() || !stAb.checkConditions()) {
            return;
        }

        if (mode.equals("RaiseCost")) {
            applyRaiseCostAbility(stAb, sa, originalCost);
        }
        if (mode.equals("ReduceCost")) {
            applyReduceCostAbility(stAb, sa, originalCost);
        }
        if (mode.equals("SetCost")) { //Set cost is only used by Trinisphere
            applyRaiseCostAbility(stAb, sa, originalCost);
        }
    }    
    
    private static void adjustCostByConvoke(ManaCostBeingPaid cost, final SpellAbility sa) {
    
        List<Card> untappedCreats = CardLists.filter(sa.getActivatingPlayer().getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.CREATURES);
        untappedCreats = CardLists.filter(untappedCreats, CardPredicates.Presets.UNTAPPED);
    
        Map<Card, ManaCostShard> convokedCards = sa.getActivatingPlayer().getController().chooseCardsForConvoke(sa, cost.toManaCost(), untappedCreats);
        
        // Convoked creats are tapped here with triggers suppressed,
        // Then again when payment is done(In InputPayManaCost.done()) with suppression cleared.
        // This is to make sure that triggers go off at the right time
        // AND that you can't use mana tapabilities of convoked creatures to pay the convoked cost.
        for (final Entry<Card, ManaCostShard> conv : convokedCards.entrySet()) {
            sa.addTappedForConvoke(conv.getKey());
            cost.decreaseShard(conv.getValue(), 1);
            conv.getKey().setTapped(true);
        }
    }

    private static void adjustCostByOffering(final ManaCostBeingPaid cost, final SpellAbility sa) {
        String offeringType = "";
        for (String kw : sa.getHostCard().getKeyword()) {
            if (kw.endsWith(" offering")) {
                offeringType = kw.split(" ")[0];
                break;
            }
        }
    
        Card toSac = null;
        List<Card> canOffer = CardLists.filter(sa.getActivatingPlayer().getCardsIn(ZoneType.Battlefield),
                CardPredicates.isType(offeringType));
    
        final List<Card> toSacList = sa.getHostCard().getController().getController().choosePermanentsToSacrifice(sa, 0, 1, canOffer,
                offeringType);
    
        if (!toSacList.isEmpty()) {
            toSac = toSacList.get(0);
        }
        else {
            return;
        }
    
        cost.subtractManaCost(toSac.getManaCost());
    
        sa.setSacrificedAsOffering(toSac);
        toSac.setUsedToPay(true); //stop it from interfering with mana input
    }
    
    /**
     * Applies applyRaiseCostAbility ability.
     * 
     * @param staticAbility
     *            a StaticAbility
     * @param sa
     *            the SpellAbility
     * @param originalCost
     *            a ManaCost
     */
    private  static void applyRaiseCostAbility(final StaticAbility staticAbility, final SpellAbility sa, final ManaCostBeingPaid manaCost) {
        final Map<String, String> params = staticAbility.getMapParams();
        final Card hostCard = staticAbility.getHostCard();
        final Player activator = sa.getActivatingPlayer();
        final Card card = sa.getHostCard();
        final String amount = params.get("Amount");


        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return;
        }

        if (params.containsKey("Activator") && ((activator == null)
                || !activator.isValid(params.get("Activator"), hostCard.getController(), hostCard))) {
            return;
        }

        if (params.containsKey("Type")) {
            if (params.get("Type").equals("Spell")) {
                if (!sa.isSpell()) {
                    return;
                }
            } else if (params.get("Type").equals("Ability")) {
                if (!(sa instanceof AbilityActivated)) {
                    return;
                }
            } else if (params.get("Type").equals("NonManaAbility")) {
                if (!(sa instanceof AbilityActivated) || sa.isManaAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("Flashback")) {
                if (!sa.isFlashBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("MorphUp")) {
                if (!sa.isMorphUp()) {
                    return;
                }
            }
        }
        if (params.containsKey("AffectedZone")) {
            List<ZoneType> zones = ZoneType.listValueOf(params.get("AffectedZone"));
            boolean found = false;
            for(ZoneType zt : zones) {
                if(card.isInZone(zt))
                {
                    found = true;
                    break;
                }
            }
            if(!found) {
                return;
            }
        }
        if (params.containsKey("ValidTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (Card target : sa.getTargets().getTargetCards()) {
                if (target.isValid(params.get("ValidTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        if (params.containsKey("ValidSpellTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (SpellAbility target : sa.getTargets().getTargetSpells()) {
                Card targetCard = target.getHostCard();
                if (targetCard.isValid(params.get("ValidSpellTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        int value = 0;
        try {
            value = Integer.parseInt(amount);
        }
        catch(NumberFormatException nfe) {
            if ("Min3".equals(amount)) {
                int cmc = manaCost.getConvertedManaCost();
                if (cmc < 3) {
                    value = 3 - cmc;
                }
            }
            else {
                value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar(amount));
            }
        }
        /*
        if ("X".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X"));
        } else if ("Y".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("Y"));
        } else if ("Min3".equals(amount)) {
            int cmc = manaCost.getConvertedManaCost();
            if (cmc < 3) {
                value = 3 - cmc;
            }
        } else {
            value = AbilityUtils.calculateAmount(card, amount, sa);
            //value = Integer.valueOf(amount);
        }
        */

        if (!params.containsKey("Color")) {
            manaCost.increaseColorlessMana(value);
            if (manaCost.toString().equals("{0}") && params.containsKey("MinMana")) {
                manaCost.increaseColorlessMana(Integer.valueOf(params.get("MinMana")));
            }
        } else {
            if (params.get("Color").equals("W")) {
                manaCost.increaseShard(ManaCostShard.WHITE, value);
            } else if (params.get("Color").equals("B")) {
                manaCost.increaseShard(ManaCostShard.BLACK, value);
            } else if (params.get("Color").equals("U")) {
                manaCost.increaseShard(ManaCostShard.BLUE, value);
            } else if (params.get("Color").equals("R")) {
                manaCost.increaseShard(ManaCostShard.RED, value);
            } else if (params.get("Color").equals("G")) {
                manaCost.increaseShard(ManaCostShard.GREEN, value);
            }
        }
    }

    /**
     * Applies applyReduceCostAbility ability.
     * 
     * @param staticAbility
     *            a StaticAbility
     * @param sa
     *            the SpellAbility
     * @param originalCost
     *            a ManaCost
     */
    private static void applyReduceCostAbility(final StaticAbility staticAbility, final SpellAbility sa, final ManaCostBeingPaid manaCost) {
        //Can't reduce zero cost
        if (manaCost.toString().equals("{0}")) {
            return;
        }
        final Map<String, String> params = staticAbility.getMapParams();
        final Card hostCard = staticAbility.getHostCard();
        final Player activator = sa.getActivatingPlayer();
        final Card card = sa.getHostCard();
        final String amount = params.get("Amount");


        if (params.containsKey("ValidCard")
                && !card.isValid(params.get("ValidCard").split(","), hostCard.getController(), hostCard)) {
            return;
        }
        if (params.containsKey("Activator") && ((activator == null)
                || !activator.isValid(params.get("Activator"), hostCard.getController(), hostCard))) {
            return;
        }
        if (params.containsKey("Type")) {
            if (params.get("Type").equals("Spell")) {
                if (!sa.isSpell()) {
                    return;
                }
            } else if (params.get("Type").equals("Ability")) {
                if (!(sa instanceof AbilityActivated)) {
                    return;
                }
            } else if (params.get("Type").equals("Buyback")) {
                if (!sa.isBuyBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("Cycling")) {
                if (!sa.isCycling()) {
                    return;
                }
            } else if (params.get("Type").equals("Equip")) {
                if (!(sa instanceof AbilityActivated) || !sa.hasParam("Equip")) {
                    return;
                }
            } else if (params.get("Type").equals("Flashback")) {
                if (!sa.isFlashBackAbility()) {
                    return;
                }
            } else if (params.get("Type").equals("MorphDown")) {
                if (!sa.isSpell() || !((Spell) sa).isCastFaceDown()) {
                    return;
                }
            } else if (params.get("Type").equals("SelfMonstrosity")) {
                if (!(sa instanceof AbilityActivated) || !sa.hasParam("Monstrosity") || sa.isTemporary()) {
                    // Nemesis of Mortals
                    return;
                }
            }
        }
        if (params.containsKey("ValidTarget")) {
            TargetRestrictions tgt = sa.getTargetRestrictions();
            if (tgt == null) {
                return;
            }
            boolean targetValid = false;
            for (Card target : sa.getTargets().getTargetCards()) {
                if (target.isValid(params.get("ValidTarget").split(","), hostCard.getController(), hostCard)) {
                    targetValid = true;
                }
            }
            if (!targetValid) {
                return;
            }
        }
        if (params.containsKey("AffectedZone") && !card.isInZone(ZoneType.smartValueOf(params.get("AffectedZone")))) {
            return;
        }
        int value = 0;
        if ("X".equals(amount)) {
            value = CardFactoryUtil.xCount(hostCard, hostCard.getSVar("X"));
        } else if ("AffectedX".equals(amount)) {
            value = CardFactoryUtil.xCount(card, hostCard.getSVar("AffectedX"));
        } else {
            value = Integer.valueOf(amount);
        }

        if (!params.containsKey("Color")) {
            manaCost.decreaseColorlessMana(value);
            if (manaCost.toString().equals("0") && params.containsKey("MinMana")) {
                manaCost.increaseColorlessMana(Integer.valueOf(params.get("MinMana")));
            }
        } else {
            if (params.get("Color").equals("W")) {
                manaCost.decreaseShard(ManaCostShard.WHITE, value);
            } else if (params.get("Color").equals("B")) {
                manaCost.decreaseShard(ManaCostShard.BLACK, value);
            } else if (params.get("Color").equals("R")) {
                manaCost.decreaseShard(ManaCostShard.RED, value);
            } else if (params.get("Color").equals("G")) {
                manaCost.decreaseShard(ManaCostShard.GREEN, value);
            }
        }
    }    
    
}