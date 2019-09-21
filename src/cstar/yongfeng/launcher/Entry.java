package cstar.yongfeng.launcher;

import java.util.List;

import cstar.yongfeng.analysis.CrashNode;
import cstar.yongfeng.analysis.RepsUtilier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.SMOTE;

public class Entry {

	public static void main(String[] args) {
		int args_size = args.length;
		
		String[] valid_cmds = {"-projPath", "-projStackTrace", "-projClasspath"};
		
		String path = "";
		String proj = ""; 
		String[] dependencies = {};
		
		for(int i=0; i<args_size-1; i++) {
			if(args[i].equals(valid_cmds[0])) { 
				proj = args[i+1];
			}else if(args[i].equals(valid_cmds[1])) {
				path = args[i+1];
			}else if(args[i].equals(valid_cmds[2])) {
				dependencies = args[i+1].split(";");
			}
		}
		
		new Entry().run(path, proj);

	}
	
	public void run(String path, String proj) {
		double[] feature_total = RepsUtilier.getFeatures(path, proj);
		
		System.out.println("------------------------------------------------\n");
		System.out.println(">>>>> Crashing Fault Residence:");
//		String pwd = System.getProperty("user.dir").replace("\\", "/") + "/";
//		System.out.println(pwd);
		
		try {
			
			/** currently instance*/
			Instances empty_ins = DataSource.read("C:/Users/yongfeng/Desktop/git/CraTer-tool/files/empty.arff");
			empty_ins.setClassIndex(empty_ins.numAttributes() - 1);
			Instance currently_ins = new DenseInstance(feature_total.length);
			for(int i=0; i<feature_total.length; i++) {
				currently_ins.setValue(i, feature_total[i]);
			}
//			System.out.print(empty_ins.classAttribute());
			
			/** training set loading*/
			Instances ins = DataSource.read("C:/Users/yongfeng/Desktop/git/CraTer-tool/files/total/training_set.arff");
			ins.setClassIndex(ins.numAttributes() - 1);
			
			/** imbalance processing*/
			SMOTE smote = new SMOTE();
			smote.setInputFormat(ins);
			
			/** classifiers setting*/
			RandomForest rf = new RandomForest();			
			FilteredClassifier fc = new FilteredClassifier();
			fc.setClassifier(rf);
			fc.setFilter(smote);
			
			fc.buildClassifier(ins);
//			String pre_result = fc.classifyInstance(currently_ins)>0?"OutTrace":"InTrace";
			double[] pre_dis = fc.distributionForInstance(currently_ins);
			System.out.printf("Intrace: %-5.3f vs OutTrace: %-5.3f\n", pre_dis[0], pre_dis[1]);
			
			List<CrashNode> lsCrashes = RepsUtilier.getSingleCrash(path);
			List<String> stacktrace = lsCrashes.get(0).stackTraces;
			if(pre_dis[0] > pre_dis[1]){
				System.out.println("The crashing fault may reside INSIDE of the stack trace. Try to check the following lines,\n");
				for(String line: stacktrace) System.out.println(line);
			}else{
				System.out.println("The crashing fault may reside OUTSIDE of the stack trace. Try to check the code through the method invocations, \n");
				for(String line: stacktrace) System.out.println(line);
			}

			
		}catch (Exception e) {
			System.out.println("[ERROR]: CraTer failed!");
		}
	}

}
