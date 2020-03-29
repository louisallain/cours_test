import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class MinMaxCountDriver {
	
	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();  

        Job job = Job.getInstance(conf, "MinMaxCount");
        job.setJarByClass(MinMaxCountDriver.class);
    
        //Setup des classes
        job.setMapperClass(MinMaxCountMapper.class);
        /*job.setCombinerClass(MinMaxCountReducer.class);*/
        job.setReducerClass(MinMaxCountReducer.class);

        //Format d'entrée (ici on lit ligne par ligne un fichier)
        job.setInputFormatClass(TextInputFormat.class); // optionnel car par défaut
        //Type de clé en sortie de map
        job.setMapOutputKeyClass(Text.class);
        //Type de la valeur en sortie de map
        job.setMapOutputValueClass(MinMaxCountTuple.class);

        //Format de sortie
        job.setOutputFormatClass(TextOutputFormat.class); // optionnel car par défaut
        //Type de la clé en sortie du combiner et du reducer
        job.setOutputKeyClass(Text.class);
        //Type de la valeur en sortie du combiner et du reducer
        job.setOutputValueClass(MinMaxCountTuple.class);

        //Chemin du fichier à lire
        FileInputFormat.setInputPaths(job, new Path("/data/stackoverflow/Comments.xml"));

        FileSystem hdfs = FileSystem.get(new URI("hdfs://hnn:9000"), conf);

        //Chemin de sortie
        Path output_path= new Path("output-minmaxcount");
        FileOutputFormat.setOutputPath(job, output_path);
        //supprime le répertoire de sortie
        hdfs.delete(output_path, true); 

        job.waitForCompletion(true);
	}
}
