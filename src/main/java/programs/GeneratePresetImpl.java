package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.GeneratePreset;

import java.util.*;

/**
 * Creates AI army preset under constraints:
 * - total cost <= maxPoints
 * - up to 11 units per type
 * Preference order:
 * 1) attack/cost
 * 2) health/cost
 * 3) cheaper units first (to use remaining points)
 */
public final class GeneratePresetImpl implements GeneratePreset {

    private static final int MAX_UNITS_PER_TYPE = 11;

    private final UnitFactory unitFactory;
    private final UnitEfficiencyRanking ranking;

    public GeneratePresetImpl() {
        this(new DefaultUnitFactory(), new DefaultUnitEfficiencyRanking());
    }

    // Useful for testing / extension (DIP)
    GeneratePresetImpl(UnitFactory unitFactory, UnitEfficiencyRanking ranking) {
        this.unitFactory = Objects.requireNonNull(unitFactory);
        this.ranking = Objects.requireNonNull(ranking);
    }

    @Override
    public Army generate(List<Unit> unitList, int maxPoints) {
        if (unitList == null || unitList.isEmpty() || maxPoints <= 0) {
            return armyOf(Collections.emptyList(), 0);
        }

        List<Unit> types = new ArrayList<>(unitList);
        types.sort(ranking.comparator());

        Budget budget = new Budget(maxPoints);
        TypeCounter counter = new TypeCounter(MAX_UNITS_PER_TYPE);

        List<Unit> chosen = new ArrayList<>();

        // First pass: take as many as possible from best-ranked types
        for (Unit base : types) {
            addAsManyAsPossible(base, types, budget, counter, chosen);
            if (budget.isExhausted()) break;
        }

        // Second pass: try to spend remaining points (cheap fill within ranking order)
        fillRemaining(types, budget, counter, chosen);

        return armyOf(chosen, budget.spent());
    }

    private void addAsManyAsPossible(Unit base,
                                     List<Unit> rankedTypes,
                                     Budget budget,
                                     TypeCounter counter,
                                     List<Unit> out) {
        int cost = base.getCost();
        if (cost <= 0) return;

        int available = counter.remaining(base.getUnitType());
        if (available <= 0) return;

        int canBuy = Math.min(available, budget.maxAffordable(cost));
        if (canBuy <= 0) return;

        int startIndex = counter.currentCount(base.getUnitType()) + 1;
        for (int i = 0; i < canBuy; i++) {
            int idx = startIndex + i;
            out.add(unitFactory.createFrom(base, idx));
            budget.spend(cost);
        }
        counter.add(base.getUnitType(), canBuy);
    }

    private void fillRemaining(List<Unit> rankedTypes,
                               Budget budget,
                               TypeCounter counter,
                               List<Unit> out) {
        if (budget.isExhausted()) return;

        boolean added;
        do {
            added = false;
            for (Unit base : rankedTypes) {
                int cost = base.getCost();
                if (cost <= 0) continue;
                if (!budget.canAfford(cost)) continue;
                if (!counter.canAdd(base.getUnitType())) continue;

                int idx = counter.currentCount(base.getUnitType()) + 1;
                out.add(unitFactory.createFrom(base, idx));

                counter.add(base.getUnitType(), 1);
                budget.spend(cost);

                added = true;
                if (budget.isExhausted()) break;
            }
        } while (added && !budget.isExhausted());
    }

    private Army armyOf(List<Unit> units, int points) {
        Army army = new Army(units);
        army.setPoints(points);
        return army;
    }

    // ---------- Abstractions (SOLID) ----------

    interface UnitFactory {
        Unit createFrom(Unit base, int index);
    }

    interface UnitEfficiencyRanking {
        Comparator<Unit> comparator();
    }

    // ---------- Default implementations ----------

    static final class DefaultUnitFactory implements UnitFactory {
        @Override
        public Unit createFrom(Unit base, int index) {
            String baseName = base.getName();
            if (baseName == null || baseName.isBlank()) {
                baseName = base.getUnitType();
            }
            String uniqueName = baseName + "_" + index;

            return new Unit(
                    uniqueName,
                    base.getUnitType(),
                    base.getHealth(),
                    base.getBaseAttack(),
                    base.getCost(),
                    base.getAttackType(),
                    base.getAttackBonuses(),
                    base.getDefenceBonuses(),
                    0,
                    0
            );
        }
    }

    static final class DefaultUnitEfficiencyRanking implements UnitEfficiencyRanking {
        @Override
        public Comparator<Unit> comparator() {
            return (a, b) -> {
                int costA = Math.max(1, a.getCost());
                int costB = Math.max(1, b.getCost());

                // attack/cost (no doubles)
                long aAttackScaled = (long) a.getBaseAttack() * costB;
                long bAttackScaled = (long) b.getBaseAttack() * costA;
                if (aAttackScaled != bAttackScaled) {
                    return Long.compare(bAttackScaled, aAttackScaled); // desc
                }

                // health/cost
                long aHpScaled = (long) a.getHealth() * costB;
                long bHpScaled = (long) b.getHealth() * costA;
                if (aHpScaled != bHpScaled) {
                    return Long.compare(bHpScaled, aHpScaled); // desc
                }

                // cheaper first to fill remaining points
                int c = Integer.compare(costA, costB);
                if (c != 0) return c;

                return a.getUnitType().compareTo(b.getUnitType());
            };
        }
    }

    // ---------- Small helpers (clean code) ----------

    static final class Budget {
        private final int limit;
        private int left;

        Budget(int limit) {
            this.limit = Math.max(0, limit);
            this.left = this.limit;
        }

        boolean canAfford(int cost) {
            return cost > 0 && cost <= left;
        }

        int maxAffordable(int cost) {
            return (cost <= 0) ? 0 : (left / cost);
        }

        void spend(int cost) {
            if (cost <= 0) return;
            left -= cost;
            if (left < 0) left = 0;
        }

        boolean isExhausted() {
            return left == 0;
        }

        int spent() {
            return limit - left;
        }
    }

    static final class TypeCounter {
        private final int perTypeLimit;
        private final Map<String, Integer> counts = new HashMap<>();

        TypeCounter(int perTypeLimit) {
            this.perTypeLimit = Math.max(0, perTypeLimit);
        }

        int currentCount(String typeKey) {
            return counts.getOrDefault(typeKey, 0);
        }

        int remaining(String typeKey) {
            return perTypeLimit - currentCount(typeKey);
        }

        boolean canAdd(String typeKey) {
            return currentCount(typeKey) < perTypeLimit;
        }

        void add(String typeKey, int delta) {
            if (delta <= 0) return;
            counts.put(typeKey, Math.min(perTypeLimit, currentCount(typeKey) + delta));
        }
    }
}
