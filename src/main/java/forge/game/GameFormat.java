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
package forge.game;

import java.util.Collections;
import java.util.List;

import net.slightlymagic.braids.util.lambda.Lambda1;
import forge.card.CardRules;
import forge.item.CardPrinted;
import forge.util.Predicate;

/**
 * TODO: Write javadoc for this type.
 * 
 */
public final class GameFormat {

    private final String name;
    // contains allowed sets, when empty allows all sets
    private final List<String> allowedSetCodes;
    private final List<String> bannedCardNames;

    private final Predicate<CardPrinted> filterRules;
    private final Predicate<CardPrinted> filterPrinted;

    /**
     * Instantiates a new game format.
     * 
     * @param fName
     *            the f name
     * @param sets
     *            the sets
     * @param bannedCards
     *            the banned cards
     */
    public GameFormat(final String fName, final List<String> sets, final List<String> bannedCards) {
        this.name = fName;
        this.allowedSetCodes = Collections.unmodifiableList(sets);
        this.bannedCardNames = Collections.unmodifiableList(bannedCards);
        this.filterRules = this.buildFilterRules();
        this.filterPrinted = this.buildFilterPritned();
    }

    private Predicate<CardPrinted> buildFilterPritned() {
        final Predicate<CardPrinted> banNames = CardPrinted.Predicates.namesExcept(this.bannedCardNames);
        final Predicate<CardPrinted> allowSets = (this.allowedSetCodes == null) || this.allowedSetCodes.isEmpty() ? CardPrinted.Predicates.Presets.IS_TRUE
                : CardPrinted.Predicates.printedInSets(this.allowedSetCodes, true);
        return Predicate.and(banNames, allowSets);
    }

    private Predicate<CardPrinted> buildFilterRules() {
        final Predicate<CardPrinted> banNames = CardPrinted.Predicates.namesExcept(this.bannedCardNames);
        final Predicate<CardPrinted> allowSets = (this.allowedSetCodes == null) || this.allowedSetCodes.isEmpty() ? CardPrinted.Predicates.Presets.IS_TRUE
                : Predicate.brigde(CardRules.Predicates.wasPrintedInSets(this.allowedSetCodes),
                        CardPrinted.FN_GET_RULES);
        return Predicate.and(banNames, allowSets);
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the filter rules.
     * 
     * @return the filter rules
     */
    public Predicate<CardPrinted> getFilterRules() {
        return this.filterRules;
    }

    /**
     * Gets the filter printed.
     * 
     * @return the filter printed
     */
    public Predicate<CardPrinted> getFilterPrinted() {
        return this.filterPrinted;
    }

    /**
     * Checks if is sets the legal.
     * 
     * @param setCode
     *            the set code
     * @return true, if is sets the legal
     */
    public boolean isSetLegal(final String setCode) {
        return this.allowedSetCodes.isEmpty() || this.allowedSetCodes.contains(setCode);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name + " (format)";
    }
    
    public static final Lambda1<String,GameFormat> FN_GET_NAME = new Lambda1<String, GameFormat>() {
        @Override
        public String apply(GameFormat arg1) {
            return arg1.getName();
        }
    }; 

}
