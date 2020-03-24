package wc;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> { 
  
  private Pattern pattern_regexp;
  private static final Pattern pattern_word = Pattern.compile("\\w+");
  private static final IntWritable ONE=new IntWritable(1);
  
  public static enum MapCounters {
    MATCHED_WORDS, // nombre de mots qui ont été trouvés
  };

  @Override
  public void setup(Context context) {
    
    pattern_regexp = Pattern.compile(context.getConfiguration().get("regex"));
  }

  @Override
  public void map(LongWritable key, Text value, Context context) 
        throws IOException, InterruptedException { 
       
    Matcher match = pattern_word.matcher(value.toString());
    while (match.find()) {
      String word = match.group().toLowerCase();
      if (pattern_regexp.matcher(word).matches()){
        context.write(new Text(word), ONE);
        context.getCounter(MapCounters.MATCHED_WORDS).increment(1);
      }   
    }
  }

  @Override
  public void cleanup(Context context) {
  }
  

}