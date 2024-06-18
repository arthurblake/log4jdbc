/**
 * Copyright 2007-2024 Arthur Blake
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.log4jdbc;

/**
 * A provider for a SpyLogDelegator.  This allows a single switch point to abstract
 * away which logging system to use for spying on JDBC calls.
 *
 * The SLF4J logging facade is used, which is a very good general purpose facade for plugging into
 * numerous java logging systems, simply and easily.
 *
 * A program may optionally call overrideSpyLogDelegator to set a custom delegator for another
 * type of logging system, and/or to override other logging functionality.
 *
 * @author Arthur Blake
 */
public class SpyLogFactory
{
  private static final String defaultDelegator = "net.sf.log4jdbc.Slf4jSpyLogDelegator";
  private static final String delegatorClassName;

  /**
   * The delegator to the logging system of choice.
   */
  private static SpyLogDelegator delegator;

  static
  {
    final String delegatorClassNameCandidate =
      Log4JdbcProps.props.getProperty("log4jdbc.spylogdelegator");
    if (delegatorClassNameCandidate != null &&
      delegatorClassNameCandidate.length() > 0)
    {
      delegatorClassName = delegatorClassNameCandidate;
    }
    else
    {
      delegatorClassName = defaultDelegator;
    }
    try
    {
      @SuppressWarnings("unchecked")
      Class<SpyLogDelegator> t = (Class<SpyLogDelegator>) Class.forName(delegatorClassName);
      delegator = (SpyLogDelegator) t.getDeclaredConstructor().newInstance();
      delegator.debug("Startup using custom delegator class: " + delegatorClassName);
    }
    catch (Exception e)
    {
      if (delegator == null)
      {
        delegator = new Slf4jSpyLogDelegator();
      }
      delegator.debug(
        "Failed instantiating delegator class: " + delegatorClassName +
        " ; " + e.getMessage());
      delegator.debug("Fall back to " + defaultDelegator);
    }
  }

  /**
   * Do not allow instantiation.  Access is through static method.
   */
  private SpyLogFactory()
  {
  }

  /**
   * Get the default SpyLogDelegator for logging to the logger.
   *
   * @return the default SpyLogDelegator for logging to the logger.
   */
  public static SpyLogDelegator getSpyLogDelegator()
  {
    return delegator;
  }

  /**
   * Override the SpyLogDelegator used by the log4jdbc to provide for custom logging
   * scenarios.
   * @param overrideDelegator SpyLogDelegator to use for log4jdbc.
   */
  public static void overrideSpyLogDelegator(SpyLogDelegator overrideDelegator)
  {
    SpyLogFactory.delegator = overrideDelegator;
  }
}
