/**
 * Copyright 2007-2023 Arthur Blake
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Central loading location for log4jdbc properties loaded
 * from either Sstem properties or log4jdbc.properties file
 * found in class path.
 *
 * @author Arthur Blake
 */
public class Log4JdbcProps
{
	// since we don't know exactly what logging system we use until after loading
	// propertie,s queue up log messages that nornally go out over that logger
	// until that logger is known and set up.
	// (chicken - egg problem)
	static final List<String> queue = new ArrayList<>();
	static final Properties props = new Properties(System.getProperties());

	static
	{
		queue.add("... log4jdbc initializing ...");
		final InputStream propStream =
			DriverSpy.class.getResourceAsStream("/log4jdbc.properties");
		if (propStream != null)
		{
			try
			{
				props.load(propStream);
			}
			catch (IOException e)
			{
				queue.add("ERROR!  io exception loading " +
					"log4jdbc.properties from classpath: " + e.getMessage());
			}
			finally
			{
				try
				{
					propStream.close();
				}
				catch (IOException e)
				{
					queue.add("ERROR!  io exception closing property file stream: " +
						e.getMessage());
				}
			}
			queue.add("  log4jdbc.properties loaded from classpath");
		}
		else
		{
			queue.add("  log4jdbc.properties not found on classpath");
		}
	}
}
