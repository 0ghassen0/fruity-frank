package suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

import engine.AxisTest;
import engine.CircleTest;
import engine.GridTest;
import engine.ISUTest;


@Suite
@SuiteDisplayName("Engine Suite")
@SelectClasses({
    AxisTest.class,
    GridTest.class,
    ISUTest.class,
    CircleTest.class,
    
})
public class EngineSuite {
}