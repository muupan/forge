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
package forge.card.cost;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import forge.Card;
import forge.CardUtil;
import forge.card.spellability.SpellAbility;

/**
 * The Class CostPartWithList.
 */
public abstract class CostPartWithList extends CostPart {

    /** The list. */
    private Set<Card> list = new HashSet<Card>(); 
    // set is here because executePayment() adds card to list, while ai's decide payment does the same thing.
    // set allows to avoid duplication

    /**
     * Gets the list.
     * 
     * @return the list
     */
    public final Set<Card> getList() {
        return this.list;
    }

    /**
     * Sets the list.
     * 
     * @param setList
     *            the new list
     */
    public final void setList(final List<Card> setList) {
        this.list.clear();
        list.addAll(setList);
    }

    /**
     * Reset list.
     */
    public final void resetList() {
        this.list.clear();
    }

    /**
     * Adds the to list.
     * 
     * @param c
     *            the c
     */
    public final void addToList(final Card c) {
        this.list.add(c);
    }

    /**
     * Adds the list to hash.
     * 
     * @param sa
     *            the sa
     * @param hash
     *            the hash
     */
    public final void reportPaidCardsTo(final SpellAbility sa) {
        final String paymentMethod = getHashForList();
        for (final Card card : this.list) {
            sa.addCostToHashList(CardUtil.getLKICopy(card), paymentMethod);
        }
    }

    /**
     * Instantiates a new cost part with list.
     */
    public CostPartWithList() {
    }
    
    /**
     * Instantiates a new cost part with list.
     * 
     * @param amount
     *            the amount
     * @param type
     *            the type
     * @param description
     *            the description
     */
    public CostPartWithList(final String amount, final String type, final String description) {
        super(amount, type, description);
        this.resetList();
    }
    
    public abstract void executePayment(SpellAbility ability, Card targetCard);

    /**
     * TODO: Write javadoc for this method.
     * @return
     */
    public abstract String getHashForList();
}
