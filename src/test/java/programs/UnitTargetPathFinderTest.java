package programs;

import com.battle.heroes.army.Unit;
import com.battle.heroes.army.programs.Edge;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class UnitTargetPathFinderTest extends TestCase {

    private UnitTargetPathFinderImpl pathFinder;

    protected void setUp() {
        pathFinder = new UnitTargetPathFinderImpl();
    }

    public void testGetTargetPathDirect() {
        Unit attacker = new Unit("A", "T1", 100, 10, 10, "P", null, null, 0, 0);
        Unit target = new Unit("B", "T1", 100, 10, 10, "P", null, null, 2, 2);
        
        List<Edge> path = pathFinder.getTargetPath(attacker, target, new ArrayList<>());
        
        assertFalse(path.isEmpty());
        assertEquals(0, path.get(0).getX());
        assertEquals(0, path.get(0).getY());
        assertEquals(2, path.get(path.size() - 1).getX());
        assertEquals(2, path.get(path.size() - 1).getY());
        assertTrue(path.size() <= 4); // 0,0 -> 1,1 -> 2,2 (size 3) or similar
    }

    public void testGetTargetPathWithObstacle() {
        Unit attacker = new Unit("A", "T1", 100, 10, 10, "P", null, null, 0, 0);
        Unit target = new Unit("B", "T1", 100, 10, 10, "P", null, null, 2, 0);
        
        // Препятствие на (1, 0)
        Unit obstacle = new Unit("O", "T1", 100, 10, 10, "P", null, null, 1, 0);
        
        List<Unit> existingUnits = new ArrayList<>();
        existingUnits.add(attacker);
        existingUnits.add(target);
        existingUnits.add(obstacle);
        
        List<Edge> path = pathFinder.getTargetPath(attacker, target, existingUnits);
        
        assertFalse(path.isEmpty());
        for (Edge edge : path) {
            assertFalse("Path should not go through obstacle", edge.getX() == 1 && edge.getY() == 0);
        }
    }

    public void testGetTargetPathNoPath() {
        // В данной реализации поле 27x21, трудно полностью заблокировать без множества юнитов, 
        // но можно проверить передачу null или крайние случаи
        List<Edge> path = pathFinder.getTargetPath(null, null, new ArrayList<>());
        assertTrue(path.isEmpty());
    }
}
