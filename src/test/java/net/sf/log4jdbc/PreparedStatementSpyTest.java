package net.sf.log4jdbc;

import com.github.valfirst.slf4jtest.TestLoggerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.Stream;

import static net.sf.log4jdbc.PreparedStatementSpyTest.Exec.withFreshlyLoaded;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("Prepared Statement Spy should")
public class PreparedStatementSpyTest {

    static final String showSqlParamsKey = "LOG4JDBC_SQL_SHOW_PARAMS";

    @BeforeEach
    void resetLoggers() {
        TestLoggerFactory.clearAll();
    }

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
            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            Throwable t = assertThrows(
                    ExceptionInInitializerError.class, () -> new PreparedStatementSpy(mock(String.class), cs, ps));
            assertTrue(t.getCause() instanceof IllegalArgumentException);
            assertEquals(t.getCause().getMessage(),
                    "Value of " + showSqlParamsKey + " should be either 'true' or 'false'. Was 'moo'.");
        }
    }

    static class LoggingExceptionsIsDisabledCase implements Case {

        @Override
        public void exec() {
            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            String sqlStatement = "SQL statement";
            String methodCall = "Method call";
            SQLException exception = new SQLException("Exception message");

            PreparedStatementSpy pss = new PreparedStatementSpy(sqlStatement, cs, ps);

            pss.reportException(methodCall, exception, sqlStatement, 100500);
            pss.reportException(methodCall, exception, sqlStatement);
            pss.reportException(methodCall, exception);

            // Check if no exceptions were logged
            assertTrue(TestLoggerFactory.getAllLoggingEvents().stream().noneMatch(e -> e.getThrowable().isPresent()));
        }
    }

    static class LoggingExceptionsIsEnabledCase implements Case {

        @Override
        public void exec() {
            ConnectionSpy cs = mock(ConnectionSpy.class);
            PreparedStatement ps = mock(PreparedStatement.class);

            String sqlStatement = "SQL statement";
            String methodCall = "Method call";
            SQLException exception = new SQLException("Exception message");

            PreparedStatementSpy pss = new PreparedStatementSpy(sqlStatement, cs, ps);

            pss.reportException(methodCall, exception, sqlStatement, 100500);
            pss.reportException(methodCall, exception, sqlStatement);
            pss.reportException(methodCall, exception);

            // Check if SQL exception was logged
            assertTrue(TestLoggerFactory.getAllLoggingEvents().stream().anyMatch(
                    e -> e.getThrowable().isPresent() && e.getThrowable().get() == exception
            ));
        }
    }

    @Test
    @DisplayName("not log SQL params and exceptions if " + showSqlParamsKey + " sys property is not specified")
    void loggingParamsIsDisabledByDefault() throws Throwable {
        System.clearProperty(showSqlParamsKey);
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsDisabledCase.class);
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingExceptionsIsDisabledCase.class);
    }

    @Test
    @DisplayName("not log SQL params and exceptions if " + showSqlParamsKey + " sys property set to 'false'")
    void loggingParamsIsDisabled() throws Throwable {
        System.setProperty(showSqlParamsKey, "false");
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsDisabledCase.class);
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingExceptionsIsDisabledCase.class);
    }

    @Test
    @DisplayName("log SQL params and exceptions if " + showSqlParamsKey + " sys property set to 'true'")
    void loggingParamsIsEnabled() throws Throwable {
        System.setProperty(showSqlParamsKey, "true");
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingParamsIsEnabledCase.class);
        withFreshlyLoaded(PreparedStatementSpy.class).exec(LoggingExceptionsIsEnabledCase.class);
    }

    @Test
    @DisplayName("fail on loading if " + showSqlParamsKey + " is set and neither 'true' nor 'false'")
    void invalidShowSqlParamsValue() throws Throwable {
        System.setProperty(showSqlParamsKey, "moo"); // neither 'true', nor 'false'
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
     * reverting to the parent only if class was not found.
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
