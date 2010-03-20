/**
 * Copyright 2007-2010 Arthur Blake
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
 * Static utility methods for use throughout the project.
 */
public class Utilities {
  /**
   * Right justify a field within a certain number of spaces.
   * @param fieldSize field size to right justify field within.
   * @param field contents to right justify within field.
   * @return the field, right justified within the requested size.
   */
  public static String rightJustify(int fieldSize, String field)
  {
    if (field==null)
    {
      field="";
    }
    StringBuffer output = new StringBuffer();
    for (int i=0, j = fieldSize-field.length(); i < j; i++)
    {
      output.append(' ');
    }
    output.append(field);
    return output.toString();
  }

}
