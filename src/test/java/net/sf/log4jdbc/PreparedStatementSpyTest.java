package net.sf.log4jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.util.stream.Stream;

import static net.sf.log4jdbc.PreparedStatementSpyTest.Exec.withFreshlyLoaded;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("Prepared Statement Spy should")
public class PreparedStatementSpyTest {


    static final String showSqlParamsKey = "LOG4JDBC_SQL_SHOW_PARAMS";

    static class LoggingParamsIsDisabledCase implements Case {

        @Override
        public void exec() {
            String sql = "INSERT INTO People VALUES (?, ?);";
            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            PreparedStatementSpy pss = new PreparedStatementSpy(sql, cs, ps);
            assertEquals(sql, pss.dumpedSql());
        }
    }

    static class LoggingParamsIsEnabledCase implements Case {

        @Override
        public void exec() {
            System.setProperty(showSqlParamsKey, "true");

            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            PreparedStatementSpy pss = new PreparedStatementSpy("INSERT INTO People VALUES (?, ?);", cs, ps);
            pss.argTraceSet(1, "INT", "1");
            pss.argTraceSet(2, "VARCHAR(15)", "Marissa Coala");

            assertEquals("INSERT INTO People VALUES (1, Marissa Coala);", pss.dumpedSql());
        }
    }

    static class InvalidShowSqlParamsValueCase implements Case {

        @Override
        public void exec() {
            System.setProperty(showSqlParamsKey, "moo"); // neither 'true', nor 'false'

            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            Throwable t = assertThrows(
                    ExceptionInInitializerError.class, () -> new PreparedStatementSpy(mock(String.class), cs, ps));
            assertTrue(t.getCause() instanceof IllegalArgumentException);
            assertEquals(t.getCause().getMessage(),
                    "Value of " + showSqlParamsKey + " should be either 'true' or 'false'. Was 'moo'.");
        }
    }

    @Test
    @DisplayName("not log SQL params if LOG4JDBC_SQL_SHOW_PARAMS sys property is not specified")
    void loggingParamsIsDisabledByDefault() throws Throwable {
        System.clearProperty(showSqlParamsKey);
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsDisabledCase.class);
    }

    @Test
    @DisplayName("not log SQL params if LOG4JDBC_SQL_SHOW_PARAMS sys property set to 'false'")
    void loggingParamsIsDisabled() throws Throwable {
        System.setProperty(showSqlParamsKey, "false");
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsDisabledCase.class);
    }

    @Test
    @DisplayName("log SQL params if LOG4JDBC_SQL_SHOW_PARAMS sys property set to 'true'")
    void loggingParamsIsEnabled() throws Throwable {
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsEnabledCase.class);
    }

    @Test
    @DisplayName("fail on loading if LOG4JDBC_SQL_SHOW_PARAMS is set and neither 'true' nor 'false'")
    void invalidShowSqlParamsValue() throws Throwable {
        withFreshlyLoaded(PreparedStatementSpy.class).exec(InvalidShowSqlParamsValueCase.class);
    }

    interface Case {

        void exec();
    }

    static class Exec {

        static Exec withFreshlyLoaded(Class<?>... cs) {
            URL[] urls = Stream.concat(Stream.of(cs), Stream.of(Case.class))
                    .map(c -> c.getProtectionDomain().getCodeSource().getLocation()).toArray(URL[]::new);
            return new Exec(new ChildFirstClassLoader(urls, Exec.class.getClassLoader()));
        }

        private final ClassLoader cl;

        Exec(ClassLoader cl) {
            this.cl = cl;
        }

        void exec(Class<? extends Case> c) throws Throwable {
            Constructor<?> ctor = cl.loadClass(c.getName()).getDeclaredConstructors()[0];
            ctor.setAccessible(true);
            Object obj = ctor.newInstance();
            Method exec = obj.getClass().getDeclaredMethod("exec");
            exec.setAccessible(true);
            try {
                exec.invoke(obj);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }

    /**
     * A classloader that does not follow standard delegation model and tries to load the requested class first,
     * reverting to the parent only if it can't be done.
     */
    static class ChildFirstClassLoader extends URLClassLoader {

        public ChildFirstClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> cl = findLoadedClass(name);

            if (cl != null) {
                return cl;
            }

            try {
                cl = findClass(name);
                if (resolve) {
                    resolveClass(cl);
                }
                return cl;
            } catch (ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }
}
