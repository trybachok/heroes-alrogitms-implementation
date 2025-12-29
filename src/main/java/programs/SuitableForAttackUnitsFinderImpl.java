package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.SuitableForAttackUnitsFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SuitableForAttackUnitsFinderImpl implements SuitableForAttackUnitsFinder {

    @Override
    public List<Unit> getSuitableUnits(List<List<Unit>> unitsByRow, boolean isLeftArmyTarget) {
        if (unitsByRow == null || unitsByRow.isEmpty()) {
            return Collections.emptyList();
        }

        List<Unit> result = new ArrayList<>(unitsByRow.size());

        for (List<Unit> row : unitsByRow) {
            if (row == null || row.isEmpty()) continue;

            Unit best = null;

            for (Unit u : row) {
                if (u == null || !u.isAlive()) continue;

                if (best == null) {
                    best = u;
                    continue;
                }

                int y = u.getyCoordinate();
                int bestY = best.getyCoordinate();

                // Если атакуем левую армию (компьютер) -> выбираем минимальный y (не закрыт слева)
                // Если атакуем правую армию (игрок) -> выбираем максимальный y (не закрыт справа)
                if (isLeftArmyTarget) {
                    if (y < bestY) best = u;
                } else {
                    if (y > bestY) best = u;
                }
            }

            if (best != null) {
                result.add(best);
            }
        }

        return result;
    }
}
