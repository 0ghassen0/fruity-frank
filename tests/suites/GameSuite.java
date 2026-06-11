package suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import game.EntityTest;
import game.GameTest;


@Suite
@SuiteDisplayName("Game Suite")
@SelectClasses({
    GameTest.class,
    EntityTest.class
})
public class GameSuite {
}