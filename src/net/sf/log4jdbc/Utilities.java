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
