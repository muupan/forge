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
package forge.game.card;

import forge.ImageKeys;
import forge.card.CardCharacteristicName;
import forge.card.CardRules;
import forge.card.CardSplitType;
import forge.card.ICardFace;
import forge.card.mana.ManaCost;
import forge.game.ability.AbilityFactory;
import forge.game.ability.AbilityUtils;
import forge.game.ability.ApiType;
import forge.game.ability.effects.CharmEffect;
import forge.game.cost.Cost;
import forge.game.player.Player;
import forge.game.replacement.ReplacementHandler;
import forge.game.spellability.*;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerHandler;
import forge.game.trigger.WrappedAbility;
import forge.item.IPaperCard;
import forge.item.PaperCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

/**
 * <p>
 * AbstractCardFactory class.
 * </p>
 * 
 * TODO The map field contains Card instances that have not gone through
 * getCard2, and thus lack abilities. However, when a new Card is requested via
 * getCard, it is this map's values that serve as the templates for the values
 * it returns. This class has another field, allCards, which is another copy of
 * the card database. These cards have abilities attached to them, and are owned
 * by the human player by default. <b>It would be better memory-wise if we had
 * only one or the other.</b> We may experiment in the future with using
 * allCard-type values for the map instead of the less complete ones that exist
 * there today.
 * 
 * @author Forge
 * @version $Id$
 */
public class CardFactory {
    /**
     * <p>
     * copyCard.
     * </p>
     * 
     * @param in
     *            a {@link forge.game.card.Card} object.
     * @return a {@link forge.game.card.Card} object.
     */
    public final static Card copyCard(final Card in, boolean assignNewId) {
        final CardCharacteristicName curState = in.getCurState();
        boolean alternate = false;
        if (in.isInAlternateState()) {
            alternate = true;
            in.setState(CardCharacteristicName.Original);
        }
        Card out = null;
        if (!in.isToken() || in.isCopiedToken()) {
            out = assignNewId ? getCard(in.getPaperCard(), in.getOwner()) 
                              : getCard(in.getPaperCard(), in.getOwner(), in.getUniqueNumber());
        } else { // token
            out = assignNewId ? new Card(in.getGame().nextCardId()) : new Card(in.getUniqueNumber());
            out = CardFactory.copyStats(in, in.getController());

            out.setName(in.getName());
            out.setImageKey(in.getImageKey());
            out.setManaCost(in.getManaCost());
            out.setColor(in.getColor());
            out.setType(in.getType());
            out.setBaseAttack(in.getBaseAttack());
            out.setBaseDefense(in.getBaseDefense());
            out.setToken(true);

            CardFactoryUtil.addAbilityFactoryAbilities(out);
            for (String s : out.getStaticAbilityStrings()) {
                out.addStaticAbility(s);
            }
        }
                
        CardFactory.copyCharacteristics(in, out);
        if (in.hasAlternateState()) {
            for (final CardCharacteristicName state : in.getStates()) {
                in.setState(state);
                if (state == CardCharacteristicName.Cloner) {
                    out.addAlternateState(state);
                }
                out.setState(state);
                CardFactory.copyCharacteristics(in, out);
            }
        }
        if (alternate) {
            in.setState(curState);
        }
        out.setState(curState);

        // I'm not sure if we really should be copying enchant/equip stuff over.
        out.setEquipping(in.getEquipping());
        out.setEquippedBy(in.getEquippedBy());
        out.setFortifying(in.getFortifying());
        out.setFortifiedBy(in.getFortifiedBy());
        out.setEnchantedBy(in.getEnchantedBy());
        out.setEnchanting(in.getEnchanting());
        out.setClones(in.getClones());
        out.setZone(in.getZone());
        for (final Object o : in.getRemembered()) {
            out.addRemembered(o);
        }
        for (final Card o : in.getImprinted()) {
            out.addImprinted(o);
        }
        out.setCommander(in.isCommander());
        /*
        if(out.isCommander())
        {
            out.addStaticAbility("Mode$ RaiseCost | Amount$ CommanderCostRaise | Type$ Spell | ValidCard$ Card.Self+wasCastFromCommand | EffectZone$ All | AffectedZone$ Stack");
            SpellAbility sa = AbilityFactory.getAbility(
                    "SP$ PermanentCreature | SorcerySpeed$ True | ActivationZone$ Command | SubAbility$ DBCommanderIncCast | Cost$ " + out.getManaCost().toString(),
                    out);
            
            out.addSpellAbility(sa);
        }
         */
        return out;

    }

    /**
     * <p>
     * copySpellontoStack.
     * </p>
     * 
     * @param source
     *            a {@link forge.game.card.Card} object.
     * @param original
     *            a {@link forge.game.card.Card} object.
     * @param sa
     *            a {@link forge.game.spellability.SpellAbility} object.
     * @param bCopyDetails
     *            a boolean.
     */
    public final static SpellAbility copySpellAbilityAndSrcCard(final Card source, final Card original, final SpellAbility sa, final boolean bCopyDetails) {
        //Player originalController = original.getController();
        Player controller = sa.getActivatingPlayer();
        final Card c = copyCard(original, true);

        // change the color of the copy (eg: Fork)
        final SpellAbility sourceSA = source.getFirstSpellAbility();
        if (null != sourceSA && sourceSA.hasParam("CopyIsColor")) {
            String tmp = "";
            final String newColor = sourceSA.getParam("CopyIsColor");
            if (newColor.equals("ChosenColor")) {
                tmp = CardUtil.getShortColorsString(source.getChosenColor());
            } else {
                tmp = CardUtil.getShortColorsString(new ArrayList<String>(Arrays.asList(newColor.split(","))));
            }
            final String finalColors = tmp;

            c.addColor(finalColors, !sourceSA.hasParam("OverwriteColors"), true);
        }
        
        c.clearControllers();
        c.setOwner(controller);
        c.setCopiedSpell(true);

        final SpellAbility copySA;
        if(sa instanceof AbilityActivated)
        {
            copySA = ((AbilityActivated)sa).getCopy();
            copySA.setHostCard(original);
        }
        else if (sa.isTrigger()) {
            copySA = getCopiedTriggeredAbility(sa);
        }
        else
        {
            copySA = sa.copy();
            copySA.setHostCard(c);
            SpellAbility parentSA = copySA;
            SpellAbility subSA = copySA.getSubAbility();
            while (subSA != null) {
                AbilitySub copySubSA = ((AbilitySub) subSA).getCopy();
                parentSA.setSubAbility(copySubSA);
                copySubSA.setParent(parentSA);
                copySubSA.setHostCard(c);
                copySubSA.setCopied(true);
                parentSA = copySubSA;
                subSA = copySubSA.getSubAbility();
            }
        }
        copySA.setCopied(true);
        //remove all costs
        if (!copySA.isTrigger()) {
            copySA.setPayCosts(new Cost("", sa.isAbility()));
        }
        if (sa.getTargetRestrictions() != null) {
            TargetRestrictions target = new TargetRestrictions(sa.getTargetRestrictions());
            copySA.setTargetRestrictions(target);
        }
        copySA.setActivatingPlayer(controller);

        if (bCopyDetails) {
            c.addXManaCostPaid(original.getXManaCostPaid());
            c.addXManaCostPaidByColor(original.getXManaCostPaidByColor());
            c.setKickerMagnitude(original.getKickerMagnitude());

            for (OptionalCost cost : original.getOptionalCostsPaid()) {
                c.addOptionalCostPaid(cost);
            }
            copySA.setPaidHash(sa.getPaidHash());
        }
        return copySA;
    }

    /**
     * <p>
     * getCard.
     * </p>
     * 
     * @param cardName
     *            a {@link java.lang.String} object.
     * @param owner
     *            a {@link forge.game.player.Player} object.
     * @return a {@link forge.game.card.Card} instance, owned by owner; or the special
     *         blankCard
     */
    
    public final static Card getCard(final IPaperCard cp, final Player owner) {
        return getCard(cp, owner, owner == null ? 0 : owner.getGame().nextCardId());
    }
    public final static Card getCard(final IPaperCard cp, final Player owner, final int cardId) {
        //System.out.println(cardName);
        CardRules cardRules = cp.getRules();
        final Card c = readCard(cardRules, cardId);
        c.setRules(cardRules);
        c.setOwner(owner);
        buildAbilities(c);

        c.setCurSetCode(cp.getEdition());
        c.setRarity(cp.getRarity());

        // Would like to move this away from in-game entities
        String originalPicture = ImageKeys.getImageKey(cp, false);
        //System.out.println(c.getName() + " -> " + originalPicture);
        c.setImageKey(originalPicture);
        c.setToken(cp.isToken());

        if (c.hasAlternateState()) {
            if (c.isFlipCard()) {
                c.setState(CardCharacteristicName.Flipped);
                c.setImageKey(ImageKeys.getImageKey(cp, true));
            }
            else if (c.isDoubleFaced() && cp instanceof PaperCard) {
                c.setState(CardCharacteristicName.Transformed);
                c.setImageKey(ImageKeys.getImageKey(cp, true));
            }
            else if (c.isSplitCard()) {
                c.setState(CardCharacteristicName.LeftSplit);
                c.setImageKey(originalPicture);
                c.setCurSetCode(cp.getEdition());
                c.setRarity(cp.getRarity());
                c.setState(CardCharacteristicName.RightSplit);
                c.setImageKey(originalPicture);
            }

            c.setCurSetCode(cp.getEdition());
            c.setRarity(cp.getRarity());
            c.setState(CardCharacteristicName.Original);
        }
        
        return c;
    }

    private static void buildAbilities(final Card card) {
        final String cardName = card.getName();

        // may have to change the spell

        // this is the "default" spell for permanents like creatures and artifacts 
        if (card.isPermanent() && !card.isAura() && !card.isLand()) {
            card.addSpellAbility(new SpellPermanent(card));
        }

        CardFactoryUtil.parseKeywords(card, cardName);

        for (final CardCharacteristicName state : card.getStates()) {
            if (card.isDoubleFaced() && state == CardCharacteristicName.FaceDown) {
                continue; // Ignore FaceDown for DFC since they have none.
            }
            card.setState(state);
            CardFactoryUtil.addAbilityFactoryAbilities(card);
            for (String stAb : card.getStaticAbilityStrings()) {
                card.addStaticAbility(stAb);
            }


            if ( state == CardCharacteristicName.LeftSplit || state == CardCharacteristicName.RightSplit )
            {
                CardCharacteristics original = card.getState(CardCharacteristicName.Original);
                original.getSpellAbility().addAll(card.getCharacteristics().getSpellAbility());
                original.getIntrinsicKeyword().addAll(card.getIntrinsicKeyword()); // Copy 'Fuse' to original side
                original.getSVars().putAll(card.getCharacteristics().getSVars()); // Unfortunately need to copy these to (Effect looks for sVars on execute)
            }
        }

        card.setState(CardCharacteristicName.Original);

        // ******************************************************************
        // ************** Link to different CardFactories *******************

        if (card.isPlaneswalker()) {
            buildPlaneswalkerAbilities(card);
        } else if (card.isType("Plane")) {
            buildPlaneAbilities(card);
        }

        CardFactoryUtil.setupKeywordedAbilities(card);
    } // getCard2

    private static void buildPlaneAbilities(Card card) {
        StringBuilder triggerSB = new StringBuilder();
        triggerSB.append("Mode$ PlanarDice | Result$ Planeswalk | TriggerZones$ Command | Execute$ RolledWalk | ");
        triggerSB.append("Secondary$ True | TriggerDescription$ Whenever you roll Planeswalk, put this card on the ");
        triggerSB.append("bottom of its owner's planar deck face down, then move the top card of your planar deck off ");
        triggerSB.append("that planar deck and turn it face up");

        StringBuilder saSB = new StringBuilder();
        saSB.append("AB$ RollPlanarDice | Cost$ X | References$ X | SorcerySpeed$ True | AnyPlayer$ True | ActivationZone$ Command | ");
        saSB.append("SpellDescription$ Roll the planar dice. X is equal to the amount of times the planar die has been rolled this turn.");        

        card.setSVar("RolledWalk", "DB$ Planeswalk | Cost$ 0");
        Trigger planesWalkTrigger = TriggerHandler.parseTrigger(triggerSB.toString(), card, true);
        card.addTrigger(planesWalkTrigger);

        card.setSVar("X", "Count$RolledThisTurn");
        SpellAbility planarRoll = AbilityFactory.getAbility(saSB.toString(), card);
        card.addSpellAbility(planarRoll);
    }

    private static void buildPlaneswalkerAbilities(Card card) {
        if (card.getBaseLoyalty() > 0) {
            final String loyalty = Integer.toString(card.getBaseLoyalty());
            card.addIntrinsicKeyword("etbCounter:LOYALTY:" + loyalty + ":no Condition:no desc");
        }

        //Planeswalker damage redirection
        String replacement = "Event$ DamageDone | ActiveZones$ Battlefield | IsCombat$ False | ValidSource$ Card.OppCtrl"
                + " | ValidTarget$ You | Optional$ True | OptionalDecider$ Opponent | ReplaceWith$ ChooseDmgPW | Secondary$ True"
                + " | AICheckSVar$ DamagePWAI | AISVarCompare$ GT4 | Description$ Redirect damage to a planeswalker you control" ;
        card.addReplacementEffect(ReplacementHandler.parseReplacement(replacement, card, true));
        card.setSVar("ChooseDmgPW", "AB$ ChooseCard | Cost$ 0 | Defined$ ReplacedSourceController | Choices$ Planeswalker.YouCtrl" +
        		" | ChoiceZone$ Battlefield | Mandatory$ True | SubAbility$ DamagePW | ChoiceTitle$ Choose a planeswalker to redirect damage");
        card.setSVar("DamagePW", "DB$ DealDamage | Defined$ ChosenCard | NumDmg$ DamagePWX | DamageSource$ ReplacedSource | References$ DamagePWX,DamagePWAI");
        card.setSVar("DamagePWX", "ReplaceCount$DamageAmount");
        card.setSVar("DamagePWAI", "ReplaceCount$DamageAmount/NMinus.DamagePWY");
        card.setSVar("DamagePWY", "Count$YourLifeTotal");
    }

    private static Card readCard(final CardRules rules, int cardId) {

        final Card card = new Card(cardId);

        // 1. The states we may have:
        CardSplitType st = rules.getSplitType();
        if ( st ==  CardSplitType.Split) {
            card.addAlternateState(CardCharacteristicName.LeftSplit);
            card.setState(CardCharacteristicName.LeftSplit);
        } 

        readCardFace(card, rules.getMainPart());

        if ( st != CardSplitType.None) {
            card.addAlternateState(st.getChangedStateName());
            card.setState(st.getChangedStateName());
            readCardFace(card, rules.getOtherPart());
        }
        
        if (card.isInAlternateState()) {
            card.setState(CardCharacteristicName.Original);
        }

        if ( st == CardSplitType.Split ) {
            card.setName(rules.getName());

            // Combined mana cost
            ManaCost combinedManaCost = ManaCost.combine(rules.getMainPart().getManaCost(), rules.getOtherPart().getManaCost());
            card.setManaCost(combinedManaCost);

            // Combined card color
            int combinedColor = rules.getMainPart().getColor().getColor() | rules.getOtherPart().getColor().getColor();
            CardColor combinedCardColor = new CardColor((byte)combinedColor);
            ArrayList<CardColor> combinedCardColorArr = new ArrayList<CardColor>();
            combinedCardColorArr.add(combinedCardColor);
            card.setColor(combinedCardColorArr);

            // Super and 'middle' types should use enums.
            List<String> coreTypes = rules.getType().getTypesBeforeDash();
            coreTypes.addAll(rules.getType().getSubTypes());
            card.setType(coreTypes);

            // Combined text based on Oracle text - might not be necessary, temporarily disabled.
            //String combinedText = String.format("%s: %s\n%s: %s", rules.getMainPart().getName(), rules.getMainPart().getOracleText(), rules.getOtherPart().getName(), rules.getOtherPart().getOracleText());
            //card.setText(combinedText);
        }

        return card;
    }

    private static void readCardFace(Card c, ICardFace face) {
        for(String a : face.getAbilities())                 c.addIntrinsicAbility(a);
        for(String k : face.getKeywords())                  c.addIntrinsicKeyword(k);
        for(String r : face.getReplacements())              c.addReplacementEffect(ReplacementHandler.parseReplacement(r, c, true));
        for(String s : face.getStaticAbilities())           c.addStaticAbilityString(s);
        for(String t : face.getTriggers())                  c.addTrigger(TriggerHandler.parseTrigger(t, c, true));
        for(Entry<String, String> v : face.getVariables())  c.setSVar(v.getKey(), v.getValue());

        c.setName(face.getName());
        c.setManaCost(face.getManaCost());
        c.setText(face.getNonAbilityText());
        if( face.getInitialLoyalty() > 0 ) c.setBaseLoyalty(face.getInitialLoyalty());

        // Super and 'middle' types should use enums.
        List<String> coreTypes = face.getType().getTypesBeforeDash();
        coreTypes.addAll(face.getType().getSubTypes());
        c.setType(coreTypes);

        // What a perverted color code we have!
        CardColor col1 = new CardColor(face.getColor().getColor());
        ArrayList<CardColor> ccc = new ArrayList<CardColor>();
        ccc.add(col1);
        c.setColor(ccc);

        if ( face.getIntPower() >= 0 ) {
            c.setBaseAttack(face.getIntPower());
            c.setBaseAttackString(face.getPower());
        }
        if ( face.getIntToughness() >= 0 ) {
            c.setBaseDefense(face.getIntToughness());
            c.setBaseDefenseString(face.getToughness());
        }
    }

    /**
     * <p>
     * Copies stats like power, toughness, etc.
     * </p>
     * 
     * @param sim
     *            a {@link java.lang.Object} object.
     * @param newOwner 
     * @return a {@link forge.game.card.Card} object.
     */
    public static Card copyStats(final Card sim, Player newOwner) {
        int id = newOwner == null ? 0 : newOwner.getGame().nextCardId();
        final Card c = new Card(id);
    
        c.setOwner(newOwner);
        c.setCurSetCode(sim.getCurSetCode());
    
        final CardCharacteristicName origState = sim.getCurState();
        for (final CardCharacteristicName state : sim.getStates()) {
            c.addAlternateState(state);
            c.setState(state);
            sim.setState(state);
            CardFactory.copyCharacteristics(sim, c);
        }
    
        sim.setState(origState);
        c.setState(origState);
        c.setRules(sim.getRules());
    
        return c;
    } // copyStats()

    /**
     * Copy characteristics.
     * 
     * @param from
     *            the from
     * @param to
     *            the to
     */
    private static void copyCharacteristics(final Card from, final Card to) {
        to.setBaseAttack(from.getBaseAttack());
        to.setBaseDefense(from.getBaseDefense());
        to.setBaseLoyalty(from.getBaseLoyalty());
        to.setBaseAttackString(from.getBaseAttackString());
        to.setBaseDefenseString(from.getBaseDefenseString());
        to.setIntrinsicKeyword(from.getIntrinsicKeyword());
        to.setName(from.getName());
        to.setType(from.getCharacteristics().getType());
        to.setText(from.getSpellText());
        to.setManaCost(from.getManaCost());
        to.setColor(from.getColor());
        to.setSVars(from.getSVars());
        to.setIntrinsicAbilities(from.getUnparsedAbilities());
    
        to.setImageKey(from.getImageKey());
        to.setTriggers(from.getTriggers(), true);
        to.setReplacementEffects(from.getReplacementEffects());
        to.setStaticAbilityStrings(from.getStaticAbilityStrings());
    
    }

    /**
     * Copy characteristics.
     * 
     * @param from
     *            the from
     * @param stateToCopy
     *            the state to copy
     * @param to
     *            the to
     */
    public static void copyState(final Card from, final CardCharacteristicName stateToCopy, final Card to) {
    
        // copy characteristics not associated with a state
        to.setBaseLoyalty(from.getBaseLoyalty());
        to.setBaseAttackString(from.getBaseAttackString());
        to.setBaseDefenseString(from.getBaseDefenseString());
        to.setText(from.getSpellText());
    
        // get CardCharacteristics for desired state
        CardCharacteristics characteristics = from.getState(stateToCopy);
        to.getCharacteristics().copyFrom(characteristics);
        // handle triggers and replacement effect through Card class interface
        to.setTriggers(characteristics.getTriggers(), true);
        to.setReplacementEffects(characteristics.getReplacementEffects());
    }

    public static void copySpellAbility(SpellAbility from, SpellAbility to) {
        to.setDescription(from.getDescription());
        to.setStackDescription(from.getDescription());
    
        if (from.getSubAbility() != null) {
            to.setSubAbility(from.getSubAbility().getCopy());
        }
        if (from.getRestrictions() != null) {
            to.setRestrictions(from.getRestrictions());
        }
        if (from.getConditions() != null) {
            to.setConditions(from.getConditions());
        }
    
        for (String sVar : from.getSVars()) {
            to.setSVar(sVar, from.getSVar(sVar));
        }
    }

    public static List<Card> makeToken(final String name, final String imageName, final Player controller,
            final String manaCost, final String[] types, final int baseAttack, final int baseDefense,
            final String[] intrinsicKeywords) {
        final List<Card> list = new ArrayList<Card>();
        final Card c = new Card(controller.getGame().nextCardId());
        c.setName(name);
        c.setImageKey(ImageKeys.getTokenKey(imageName));
    
        // TODO - most tokens mana cost is 0, this needs to be fixed
        // c.setManaCost(manaCost);
        c.addColor(manaCost);
        c.setToken(true);
    
        for (final String t : types) {
            c.addType(t);
        }
    
        c.setBaseAttack(baseAttack);
        c.setBaseDefense(baseDefense);
    
        final int multiplier = controller.getTokenDoublersMagnitude();
        for (int i = 0; i < multiplier; i++) {
            Card temp = copyStats(c, controller);
    
            for (final String kw : intrinsicKeywords) {
                temp.addIntrinsicKeyword(kw);
            }
            temp.setOwner(controller);
            temp.setToken(true);
            CardFactoryUtil.parseKeywords(temp, temp.getName());
            CardFactoryUtil.setupKeywordedAbilities(temp);
            list.add(temp);
        }
        return list;
    }
    /**
     * Copy triggered ability
     * 
     * return a wrapped ability
     */
    public static SpellAbility getCopiedTriggeredAbility(final SpellAbility sa) {
        if (!sa.isTrigger()) {
            return null;
        }
        // Find trigger
        Trigger t = null;
        if (sa.isWrapper()) {
            // copy trigger?
            t = ((WrappedAbility) sa).getTrigger();
        } else { // some keyword ability, e.g. Exalted, Annihilator
            return sa.copy();
        }
        // set up copied wrapped ability
        SpellAbility trig = t.getOverridingAbility();
        if (trig == null) {
            trig = AbilityFactory.getAbility(sa.getHostCard().getSVar(t.getMapParams().get("Execute")), sa.getHostCard());
        }
        trig.setHostCard(sa.getHostCard());
        trig.setTrigger(true);
        trig.setSourceTrigger(t.getId());
        t.setTriggeringObjects(trig);
        trig.setTriggerRemembered(t.getTriggerRemembered());
        if (t.getStoredTriggeredObjects() != null) {
            trig.setAllTriggeringObjects(t.getStoredTriggeredObjects());
        }

        trig.setActivatingPlayer(sa.getActivatingPlayer());
        if (t.getMapParams().containsKey("TriggerController")) {
            Player p = AbilityUtils.getDefinedPlayers(t.getHostCard(), t.getMapParams().get("TriggerController"), trig).get(0);
            trig.setActivatingPlayer(p);
        }

        if (t.getMapParams().containsKey("RememberController")) {
            sa.getHostCard().addRemembered(sa.getActivatingPlayer());
        }

        trig.setStackDescription(trig.toString());
        if (trig.getApi() == ApiType.Charm && !trig.isWrapper()) {
            CharmEffect.makeChoices(trig);
        }

        WrappedAbility wrapperAbility = new WrappedAbility(t, trig, ((WrappedAbility) sa).getDecider());
        wrapperAbility.setTrigger(true);
        wrapperAbility.setMandatory(((WrappedAbility) sa).isMandatory());
        wrapperAbility.setDescription(wrapperAbility.getStackDescription());
        t.setTriggeredSA(wrapperAbility);
        return wrapperAbility;
    }


} // end class AbstractCardFactory
