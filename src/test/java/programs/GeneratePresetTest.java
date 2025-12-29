package programs;

import com.battle.heroes.army.Army;
import com.battle.heroes.army.Unit;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

public class GeneratePresetTest extends TestCase {

    private GeneratePresetImpl generator;

    protected void setUp() {
        generator = new GeneratePresetImpl();
    }

    public void testGenerate() {
        Unit archer = new Unit("Archer", "ArcherType", 50, 10, 20, "Pierce", null, null, 0, 0);
        archer.setCost(10);
        
        Unit knight = new Unit("Knight", "KnightType", 100, 20, 50, "Melee", null, null, 0, 0);
        knight.setCost(30);

        List<Unit> unitList = Arrays.asList(archer, knight);
        int maxPoints = 100;

        Army army = generator.generate(unitList, maxPoints);

        assertNotNull(army);
        assertTrue(army.getPoints() <= maxPoints);
        
        int archerCount = 0;
        int knightCount = 0;
        for (Unit u : army.getUnits()) {
            if ("ArcherType".equals(u.getUnitType())) archerCount++;
            if ("KnightType".equals(u.getUnitType())) knightCount++;
        }

        assertTrue(archerCount <= 11);
        assertTrue(knightCount <= 11);
    }

    public void testGenerateRespectsPointsLimit() {
        Unit dragon = new Unit("Dragon", "DragonType", 500, 100, 1000, "Fire", null, null, 0, 0);
        dragon.setCost(1000); // More than maxPoints

        List<Unit> unitList = Arrays.asList(dragon);
        Army army = generator.generate(unitList, 500);

        assertTrue(army.getUnits().isEmpty());
        assertEquals(0, army.getPoints());
    }
}
