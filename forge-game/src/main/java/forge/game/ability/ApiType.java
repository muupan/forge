package forge.game.ability;


import forge.game.ability.effects.*;
import forge.util.ReflectionUtil;

import java.util.Map;
import java.util.TreeMap;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public enum ApiType {
    Abandon (AbandonEffect.class),
    AddOrRemoveCounter (CountersPutOrRemoveEffect.class),
    AddPhase (AddPhaseEffect.class),
    AddTurn (AddTurnEffect.class),
    Animate (AnimateEffect.class),
    AnimateAll (AnimateAllEffect.class),
    Attach (AttachEffect.class),
    Balance (BalanceEffect.class),
    BecomesBlocked (BecomesBlockedEffect.class),
    Bond (BondEffect.class),
    ChangeTargets (ChangeTargetsEffect.class),
    ChangeZone (ChangeZoneEffect.class),
    ChangeZoneAll (ChangeZoneAllEffect.class),

    Charm (CharmEffect.class),
    ChooseCard (ChooseCardEffect.class),
    ChooseColor (ChooseColorEffect.class),
    ChooseNumber (ChooseNumberEffect.class),
    ChoosePlayer (ChoosePlayerEffect.class),
    ChooseSource (ChooseSourceEffect.class),
    ChooseType (ChooseTypeEffect.class),
    Clash (ClashEffect.class),
    Cleanup (CleanUpEffect.class),
    Clone (CloneEffect.class),
    CopyPermanent (CopyPermanentEffect.class),
    CopySpellAbility (CopySpellAbilityEffect.class),
    ControlSpell (ControlSpellEffect.class),
    ControlPlayer (ControlPlayerEffect.class),
    Counter (CounterEffect.class),
    DamageAll (DamageAllEffect.class),
    DealDamage (DamageDealEffect.class),
    Debuff (DebuffEffect.class),
    DebuffAll (DebuffAllEffect.class),
    DeclareCombatants (DeclareCombatantsEffect.class),
    DelayedTrigger (DelayedTriggerEffect.class),
    Destroy (DestroyEffect.class),
    DestroyAll (DestroyAllEffect.class),
    Dig (DigEffect.class),
    DigUntil (DigUntilEffect.class),
    Discard (DiscardEffect.class),
    DrainMana (DrainManaEffect.class),
    Draw (DrawEffect.class),
    EachDamage (DamageEachEffect.class),
    Effect (EffectEffect.class),
    Encode (EncodeEffect.class),
    EndTurn (EndTurnEffect.class),
    ExchangeLife (LifeExchangeEffect.class),
    ExchangeControl (ControlExchangeEffect.class),
    ExchangePower (PowerExchangeEffect.class),
    ExchangeZone (ZoneExchangeEffect.class),
    Fight (FightEffect.class),
    FlipACoin (FlipCoinEffect.class),
    Fog (FogEffect.class),
    GainControl (ControlGainEffect.class),
    GainLife (LifeGainEffect.class),
    GainOwnership (OwnershipGainEffect.class),
    GenericChoice (ChooseGenericEffect.class),
    LoseLife (LifeLoseEffect.class),
    LosesGame (GameLossEffect.class),
    Mana (ManaEffect.class),
    ManaReflected (ManaReflectedEffect.class),
    Mill (MillEffect.class),
    MoveCounter (CountersMoveEffect.class),
    MultiplePiles (MultiplePilesEffect.class),
    MustAttack (MustAttackEffect.class),
    MustBlock (MustBlockEffect.class),
    NameCard (ChooseCardNameEffect.class),
    PeekAndReveal (PeekAndRevealEffect.class),
    PermanentCreature (PermanentCreatureEffect.class),
    PermanentNoncreature (PermanentNoncreatureEffect.class),
    Phases (PhasesEffect.class),
    Planeswalk (PlaneswalkEffect.class),
    Play (PlayEffect.class),
    PlayLandVariant (PlayLandVariantEffect.class),
    Poison (PoisonEffect.class),
    PreventDamage (DamagePreventEffect.class),
    PreventDamageAll (DamagePreventAllEffect.class),
    Proliferate (CountersProliferateEffect.class),
    Protection (ProtectEffect.class),
    ProtectionAll (ProtectAllEffect.class),
    Pump (PumpEffect.class),
    PumpAll (PumpAllEffect.class),
    PutCounter (CountersPutEffect.class),
    PutCounterAll (CountersPutAllEffect.class),
    RearrangeTopOfLibrary (RearrangeTopOfLibraryEffect.class),
    Regenerate (RegenerateEffect.class),
    RegenerateAll (RegenerateAllEffect.class),
    RemoveCounter (CountersRemoveEffect.class),
    RemoveCounterAll (CountersRemoveAllEffect.class),
    RemoveFromCombat (RemoveFromCombatEffect.class),
    ReorderZone (ReorderZoneEffect.class),
    Repeat (RepeatEffect.class),
    RepeatEach (RepeatEachEffect.class),
    RestartGame (RestartGameEffect.class),
    Reveal (RevealEffect.class),
    RevealHand (RevealHandEffect.class),
    RollPlanarDice (RollPlanarDiceEffect.class),
    RunSVarAbility (RunSVarAbilityEffect.class),
    Sacrifice (SacrificeEffect.class),
    SacrificeAll (SacrificeAllEffect.class),
    Scry (ScryEffect.class),
    SetInMotion (SetInMotionEffect.class),
    SetLife (LifeSetEffect.class),
    SetState (SetStateEffect.class),
    Shuffle (ShuffleEffect.class),
    SkipTurn (SkipTurnEffect.class),
    StoreSVar (StoreSVarEffect.class),
    Tap (TapEffect.class),
    TapAll (TapAllEffect.class),
    TapOrUntap (TapOrUntapEffect.class),
    TapOrUntapAll (TapOrUntapAllEffect.class),
    Token (TokenEffect.class, false),
    TwoPiles (TwoPilesEffect.class),
    Unattach (UnattachEffect.class),
    UnattachAll (UnattachAllEffect.class),
    Untap (UntapEffect.class),
    UntapAll (UntapAllEffect.class),
    WinsGame (GameWinEffect.class),


    InternalEtbReplacement (ETBReplacementEffect.class),
    InternalLegendaryRule (CharmEffect.class),
    InternalHaunt (CharmEffect.class),
    InternalIgnoreEffect (CharmEffect.class);


    private final SpellAbilityEffect instanceEffect;
    private final Class<? extends SpellAbilityEffect> clsEffect;

    private static final Map<String, ApiType> allValues = new TreeMap<String, ApiType>(String.CASE_INSENSITIVE_ORDER);

    ApiType(Class<? extends SpellAbilityEffect> clsEf) { this(clsEf, true); }
    ApiType(Class<? extends SpellAbilityEffect> clsEf, final boolean isStateLess) {
        clsEffect = clsEf;
        instanceEffect = isStateLess ? ReflectionUtil.makeDefaultInstanceOf(clsEf) : null; 
    }

    public static ApiType smartValueOf(String value) {
        if (allValues.isEmpty())
            for(ApiType c : ApiType.values())
                allValues.put(c.toString(), c);

        ApiType v = allValues.get(value);
        if ( v == null )
            throw new RuntimeException("Element " + value + " not found in ApiType enum");
        return v;
    }

    public SpellAbilityEffect getSpellEffect() {
        return instanceEffect != null ? instanceEffect : ReflectionUtil.makeDefaultInstanceOf(clsEffect);
    }
}
