import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class MinMaxCountReducer extends Reducer<Text, MinMaxCountTuple, Text, MinMaxCountTuple> {
	
	private MinMaxCountTuple result = new MinMaxCountTuple();
	
	public void reduce(Text key, Iterable<MinMaxCountTuple> values, Context context) throws IOException, InterruptedException {
		
		result.setMin(null);
		result.setMax(null);
		result.setCount(0);
		
		long sum = 0;
		
		for(MinMaxCountTuple val : values) {
			
			if(result.getMin() == null || val.getMin().compareTo(result.getMin()) < 0) {
				result.setMin(val.getMin());
			}
			
			if(result.getMax() == null || val.getMax().compareTo(result.getMax()) > 0) {
				result.setMax(val.getMax());
			}
			
			sum = sum + val.getCount();
		}
		
		result.setCount(sum);
		context.write(key, result);
	}
 }
