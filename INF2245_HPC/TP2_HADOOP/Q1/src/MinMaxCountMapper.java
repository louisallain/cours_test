import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class MinMaxCountMapper extends Mapper<Object, Text, Text, MinMaxCountTuple> {

	private Text outUserId = new Text();
	private MinMaxCountTuple outTuple = new MinMaxCountTuple();
	
	private final static SimpleDateFormat frmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	
	public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
		
		Map<String, String> parsed = XmlUtils.transformXmlToMap(value.toString());
		
		String strDate = parsed.get("CreationDate");
		String userId = parsed.get("UserId");
		
		if(strDate != null && userId != null) {
			Date creationDate = null;
			try {
				creationDate = frmt.parse(strDate);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outTuple.setMin(creationDate);
			outTuple.setMax(creationDate);
			
			outTuple.setCount(1);
			
			outUserId.set(userId);
			
			context.write(outUserId, outTuple);
		}
	}
}
