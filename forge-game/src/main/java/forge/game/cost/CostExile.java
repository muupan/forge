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
package forge.game.cost;

import forge.game.Game;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.zone.ZoneType;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class CostExile.
 */
public class CostExile extends CostPartWithList {
    // Exile<Num/Type{/TypeDescription}>
    // ExileFromHand<Num/Type{/TypeDescription}>
    // ExileFromGrave<Num/Type{/TypeDescription}>
    // ExileFromTop<Num/Type{/TypeDescription}> (of library)
    // ExileSameGrave<Num/Type{/TypeDescription}>

        /** 
     * TODO: Write javadoc for this type.
     *
     */

    public final ZoneType from;
    public final boolean sameZone;

    /**
     * Gets the from.
     * 
     * @return the from
     */
    public final ZoneType getFrom() {
        return this.from;
    }

    /**
     * Instantiates a new cost exile.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     * @param from
     *            the from
     */
    public CostExile(final String amount, final String type, final String description, final ZoneType from) {
        this(amount, type, description, from, false);
    }

    public CostExile(final String amount, final String type, final String description, final ZoneType from, final boolean sameZone) {
        super(amount, type, description);
        this.from = from != null ? from : ZoneType.Battlefield;
        this.sameZone = sameZone;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        final Integer i = this.convertAmount();
        sb.append("Exile ");

        if (this.payCostFromSource()) {
            sb.append(this.getType());
            if (!this.from.equals(ZoneType.Battlefield)) {
                sb.append(" from your ").append(this.from);
            }
            return sb.toString();
        } else if (this.getType().equals("All")) {
            sb.append(" all cards from your ").append(this.from);
            return sb.toString();
        }

        if (this.from.equals(ZoneType.Battlefield)) {
            final String desc = this.getTypeDescription() == null ? this.getType() : this.getTypeDescription();

            sb.append(Cost.convertAmountTypeToWords(i, this.getAmount(), desc));
            if (!this.payCostFromSource()) {
                sb.append(" you control");
            }
            return sb.toString();
        }

        if (i != null) {
            sb.append(i);
        } else {
            sb.append(this.getAmount());
        }
        if (!this.getType().equals("Card")) {
            sb.append(" " + this.getType());
        }
        sb.append(" card");
        if ((i == null) || (i > 1)) {
            sb.append("s");
        }

        if (this.sameZone) {
            sb.append(" from the same ");
        } else {
            sb.append(" from your ");
        }

        sb.append(this.from);

        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability) {
        final Player activator = ability.getActivatingPlayer();
        final Card source = ability.getHostCard();
        final Game game = activator.getGame();

        String type = this.getType();
        if (type.equals("All")) {
            return true; // this will always work
        }
        else if (type.contains("FromTopGrave")) {
            type = type.replace("FromTopGrave", "");
        }

        List<Card> list;
        if (this.from.equals(ZoneType.Stack)) {
            list = new ArrayList<Card>();
            for (SpellAbilityStackInstance si : game.getStack()) {
                list.add(si.getSourceCard());
            }
        }
        else if (this.sameZone) {
            list = new ArrayList<Card>(game.getCardsIn(this.from));
        }
        else {
            list = new ArrayList<Card>(activator.getCardsIn(this.from));
        }

        if (this.payCostFromSource()) {
            return list.contains(source);
        }

        list = CardLists.getValidCards(list, type.split(";"), activator, source);

        final Integer amount = this.convertAmount();
        if ((amount != null) && (list.size() < amount)) {
            return false;
        }

        if (this.sameZone && amount != null) {
            boolean foundPayable = false;
            List<Player> players = game.getPlayers();
            for (Player p : players) {
                if (CardLists.filter(list, CardPredicates.isController(p)).size() >= amount) {
                    foundPayable = true;
                    break;
                }
            }
            if (!foundPayable) {
                return false;
            }
        }
        return true;
    }


    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#executePayment(forge.card.spellability.SpellAbility, forge.Card)
     */
    @Override
    protected void doPayment(SpellAbility ability, Card targetCard) {
        Game game = targetCard.getGame();
        game.getAction().exile(targetCard);
        
        if (this.from.equals(ZoneType.Stack)) {
            ArrayList<SpellAbility> spells = targetCard.getSpellAbilities();
            for (SpellAbility spell : spells) {
                if (targetCard.isInZone(ZoneType.Exile)) {
                    final SpellAbilityStackInstance si = game.getStack().getInstanceFromSpellAbility(spell);
                    game.getStack().remove(si);
                }
            }
        }
    }

    public static final String HashListKey = "Exiled";
    /* (non-Javadoc)
     * @see forge.card.cost.CostPartWithList#getHashForList()
     */
    @Override
    public String getHashForList() {
        // TODO Auto-generated method stub
        return HashListKey;
    }
    
    

    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
}
