
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlUtils {

  static final Pattern pattern_keyvalue= Pattern.compile("([a-zA-Z_]+)=(\\\"[^(\\\")]*\\\")");
  
  /**
   * Parse an XML string and extract the (first) value of a given attribute
   * @param key attribute key (case insensitive)
   * @param xml the string
   * @return the value of the attribute key or null if not found
   */
  public static String getAttributeValue(String key, String xml) {
    
    Matcher match = pattern_keyvalue.matcher(xml);
    while (match.find()) {
      try {
        if (! match.group(1).equalsIgnoreCase(key))
          continue;
      } catch (Exception e) {
        continue;
      }
      try {
        String value = match.group(2);
        return value.substring(1, value.length()-1);
      } catch (Exception e) {
        return "";
      }
    }
    return null;
  }
    
  /**
   * Parse an XML sting and extract (attribute name,attribute value) tuples
   * @param xml a string
   * @return the map of attribute name -> value
   */
  public static Map<String, String> transformXmlToMap(String xml) {

    Map<String, String> map = new HashMap<>();
    try { // splitting on ‘’ tokenizes the data nicely for us
      String[] tokens= xml.substring(5,xml.length()-3).split("\"");
      for (int i = 0; i < tokens.length - 1; i += 2) {
        String key = tokens[i].trim();
        String val = tokens[i + 1];
        map.put(key.substring(0, key.length() - 1), val);
      }
      
    } catch (StringIndexOutOfBoundsException e) {
      System.err.println(xml);
    }
    return map;
  }
  
  /**
   * Suppress the < and > around a list of tags
   * @param s a XML containing tags
   * @return the list of tags without < >
   */
  public static String formatTags(String s){
    
    return s.replaceAll("&lt;","").replaceAll("&gt;",",");
  }
  
  private static HashMap<String, String> htmlEscape = new HashMap<>();
  static{
    htmlEscape.put("&lt;" , "<");
    htmlEscape.put("&gt;" , ">");
    htmlEscape.put("&amp;" , "&");
    htmlEscape.put("&quot;" , "\"");
    htmlEscape.put("&nbsp;" , " ");
    htmlEscape.put("&apos;" , "'");
    htmlEscape.put("&#xA;" , "\n");
  }

 /**
  * Unescape XML strings
  * @param source an HTML escaped XML string
  * @return the string without escaped characters
 */
 public static final String unescapeHTML(String source) {
   StringBuilder sb= new StringBuilder(source);
   int sb_start= 0, entity_start= 0, entity_end= 0; // index on sb
   boolean continueLoop;
   do {
      continueLoop = false;
      entity_start = sb.indexOf("&", sb_start); // search for "&" on sb from sb_start
      if (entity_start > -1) { // "&" found at index entity_start
        entity_end = sb.indexOf(";", entity_start+1); // index of entity_end
        if (entity_end > entity_start) { // full entity found
          String html_entity = sb.substring(entity_start, entity_end + 1); // entity string
          String html_value= htmlEscape.get(html_entity);
          if (html_value != null) { // an html string has been found
            sb.replace(entity_start,entity_end+1,html_value); // replace the html entity by its html value
            sb_start= entity_start+html_value.length(); // next escaped string research will start after the html value inserted
          }else {
            System.err.println(html_entity+" not found");
            sb.delete(entity_start,entity_end+1); // delete the html entity
            sb_start= entity_start; 
          }
          continueLoop= true; // continue to traverse the source string
        }
      }
   } while (continueLoop);
   return sb.toString();
 }

 /**
  * Extract the first occurrence of a Wikipedia URL inside a given string
  * @param text the string
  * @return the Wikipedia URL
  */
  public static String getWikipediaURL(String text) {

    int idx = text.indexOf("\"http://en.wikipedia.org");
    if (idx == -1) {
        return null;
    }
    int idx_end = text.indexOf('"', idx + 1);

    if (idx_end == -1) {
        return null;
    }

    int idx_hash = text.indexOf('#', idx + 1);

    if (idx_hash != -1 && idx_hash < idx_end) {
        return text.substring(idx + 1, idx_hash);
    } else {
        return text.substring(idx + 1, idx_end);
    }

}
  // test
  public static void main(String[] args) {

    String xml_post="<row Id=\"36\" PostTypeId=\"1\" AcceptedAnswerId=\"352\" CreationDate=\"2008-08-01T12:35:56.917\" Score=\"120\" ViewCount=\"48219\" Body=\"&lt;p&gt;How can I monitor an SQL Server database for changes to a table without using triggers or modifying the structure of the database in any way? My preferred programming environment is &lt;a href=&quot;http://en.wikipedia.org/wiki/.NET_Framework&quot; rel=&quot;noreferrer&quot;&gt;.NET&lt;/a&gt; and C#.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;I'd like to be able to support any &lt;a href=&quot;http://en.wikipedia.org/wiki/Microsoft_SQL_Server#Genesis&quot; rel=&quot;noreferrer&quot;&gt;SQL Server 2000&lt;/a&gt; SP4 or newer. My application is a bolt-on data visualization for another company's product. Our customer base is in the thousands, so I don't want to have to put in requirements that we modify the third-party vendor's table at every installation.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;By &lt;em&gt;&quot;changes to a table&quot;&lt;/em&gt; I mean changes to table data, not changes to table structure.&lt;/p&gt;&#xA;&#xA;&lt;p&gt;Ultimately, I would like the change to trigger an event in my application, instead of having to check for changes at an interval.&lt;/p&gt;&#xA;&#xA;&lt;hr&gt;&#xA;&#xA;&lt;p&gt;The best course of action given my requirements (no triggers or schema modification, SQL Server 2000 and 2005) seems to be to use the &lt;code&gt;BINARY_CHECKSUM&lt;/code&gt; function in &lt;a href=&quot;http://en.wikipedia.org/wiki/Transact-SQL&quot; rel=&quot;noreferrer&quot;&gt;T-SQL&lt;/a&gt;. The way I plan to implement is this:&lt;/p&gt;&#xA;&#xA;&lt;p&gt;Every X seconds run the following query:&lt;/p&gt;&#xA;&#xA;&lt;pre&gt;&lt;code&gt;SELECT CHECKSUM_AGG(BINARY_CHECKSUM(*))&#xA;FROM sample_table&#xA;WITH (NOLOCK);&#xA;&lt;/code&gt;&lt;/pre&gt;&#xA;&#xA;&lt;p&gt;And compare that against the stored value. If the value has changed, go through the table row by row using the query:&lt;/p&gt;&#xA;&#xA;&lt;pre&gt;&lt;code&gt;SELECT row_id, BINARY_CHECKSUM(*)&#xA;FROM sample_table&#xA;WITH (NOLOCK);&#xA;&lt;/code&gt;&lt;/pre&gt;&#xA;&#xA;&lt;p&gt;And compare the returned checksums against stored values.&lt;/p&gt;&#xA;\" OwnerUserId=\"32\" LastEditorUserId=\"2571493\" LastEditorDisplayName=\"Mark Harrison\" LastEditDate=\"2016-11-30T07:50:37.517\" LastActivityDate=\"2017-02-27T13:35:29.970\" Title=\"Check for changes to an SQL Server table?\" Tags=\"&lt;sql&gt;&lt;sql-server&gt;&lt;datatable&gt;&lt;rdbms&gt;\" AnswerCount=\"8\" CommentCount=\"1\" FavoriteCount=\"23\" />";

    System.out.println("id="+getAttributeValue("id", xml_post));
    Map<String, String> map= transformXmlToMap(xml_post);
    for (String att:map.keySet()){
      System.out.print(att+"=");
      switch(att){
      case "Tags":
        System.out.println(formatTags(map.get(att)));
        break;
      case "Body":
        System.out.println(getWikipediaURL(unescapeHTML(map.get(att))));
        break;
      default:
        System.out.println(map.get(att));
      }
    }
  }
}