package programs;

import com.battle.heroes.army.Unit;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SuitableForAttackUnitsFinderTest extends TestCase {

    private SuitableForAttackUnitsFinderImpl finder;

    protected void setUp() {
        finder = new SuitableForAttackUnitsFinderImpl();
    }

    public void testGetSuitableUnitsEmpty() {
        List<Unit> result = finder.getSuitableUnits(Collections.emptyList(), true);
        assertTrue(result.isEmpty());
    }

    public void testGetSuitableUnitsLeftArmyTarget() {
        // Левая армия -> минимальный Y
        Unit u1 = new Unit("u1", "type1", 100, 10, 10, "p1", null, null, 0, 1);
        Unit u2 = new Unit("u2", "type1", 100, 10, 10, "p1", null, null, 0, 0);
        Unit u3 = new Unit("u3", "type1", 100, 10, 10, "p1", null, null, 0, 2);
        
        List<Unit> row = Arrays.asList(u1, u2, u3);
        List<List<Unit>> unitsByRow = Collections.singletonList(row);
        
        List<Unit> result = finder.getSuitableUnits(unitsByRow, true);
        assertEquals(1, result.size());
        assertEquals(u2, result.get(0)); // u2 has y=0
    }

    public void testGetSuitableUnitsRightArmyTarget() {
        // Правая армия -> максимальный Y
        Unit u1 = new Unit("u1", "type1", 100, 10, 10, "p1", null, null, 0, 1);
        Unit u2 = new Unit("u2", "type1", 100, 10, 10, "p1", null, null, 0, 0);
        Unit u3 = new Unit("u3", "type1", 100, 10, 10, "p1", null, null, 0, 2);
        
        List<Unit> row = Arrays.asList(u1, u2, u3);
        List<List<Unit>> unitsByRow = Collections.singletonList(row);
        
        List<Unit> result = finder.getSuitableUnits(unitsByRow, false);
        assertEquals(1, result.size());
        assertEquals(u3, result.get(0)); // u3 has y=2
    }

    public void testGetSuitableUnitsMultipleRows() {
        Unit u1 = new Unit("u1", "type1", 100, 10, 10, "p1", null, null, 0, 1);
        Unit u2 = new Unit("u2", "type1", 100, 10, 10, "p1", null, null, 1, 5);
        
        List<List<Unit>> unitsByRow = Arrays.asList(
            Collections.singletonList(u1),
            Collections.singletonList(u2)
        );
        
        List<Unit> result = finder.getSuitableUnits(unitsByRow, true);
        assertEquals(2, result.size());
        assertTrue(result.contains(u1));
        assertTrue(result.contains(u2));
    }

    public void testGetSuitableUnitsWithDeadUnits() {
        Unit alive = new Unit("alive", "type1", 100, 10, 10, "p1", null, null, 0, 5);
        Unit dead = new Unit("dead", "type1", 100, 10, 10, "p1", null, null, 0, 1);
        dead.setAlive(false);
        
        List<Unit> row = Arrays.asList(alive, dead);
        List<List<Unit>> unitsByRow = Collections.singletonList(row);
        
        // Даже если dead имеет y=1 (меньше чем 5), он не должен быть выбран
        List<Unit> result = finder.getSuitableUnits(unitsByRow, true);
        assertEquals(1, result.size());
        assertEquals(alive, result.get(0));
    }
}
