package suites;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;


@Suite
@SuiteDisplayName("All Suites")
@SelectClasses({
    EngineSuite.class,
    GameSuite.class
})
public class AllSuites {
}