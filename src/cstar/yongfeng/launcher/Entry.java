package cstar.yongfeng.launcher;

import java.text.DecimalFormat;
import java.util.List;

import cstar.yongfeng.analysis.CrashNode;
import cstar.yongfeng.analysis.RepsUtilier;
import weka.classifiers.Classifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomForest;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
//import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.SMOTE;

/***
 * <p>CraTer is a light-weight prototype for Java crash localization task. 
 * Given the stack trace of crashed Java program as well as the source code,
 * CraTer can analyze and predict whether the root-cause-line of this crash reside in the stack trace or not.
 * </p>
 * 
 * <p>A usage example is given as follows,
 * <pre>java -jar -projPath x/xx -projStackTrace x/xx.txt</pre>
 * where the <b>"-projPath"</b> refers to the root path of the project and 
 * <b>"-projStackTrace"</b> refers to the path of stack trace file (in .txt format). 
 * </p>
 * @author yongfeng
 * @date 2021.1.16
 */
public class Entry {

	public static void main(String[] args) {
		int args_size = args.length;
		
		String[] valid_cmds = {"-projPath", "-projStackTrace"};
		
		String projPath = "";
		String tracePath = ""; 
		
		if(args_size == 4 && args[0].equals(valid_cmds[0]) 
				&& args[2].equals(valid_cmds[1])){
			// Run CraTer
			projPath = args[1];
			tracePath = args[3];
			new Entry().run(projPath, tracePath);
		}else{
			// Illegal input arguments
			printHelpInfo();
		}

	}
	
	/***
	 * Using the CraTer to analyze the given Java crash then predict
	 * the position of root-cause-line of the crash.
	 * @param projPath root path of the project
	 * @param tracePath path to stack trace file
	 */
	public void run(String projPath, String tracePath) {
				
		try {
			// extract features from stack trace and source code
			double[] feature_total = RepsUtilier.getFeatures(tracePath, projPath);			
			
			// currently instance
			Instance currently_ins = new DenseInstance(feature_total.length);
			for(int i=0; i<feature_total.length; i++) {
				currently_ins.setValue(i, feature_total[i]);
			}
			
//			Classifier fc = learnClassifierByTrainingSet("files/training_set.arff"); // learn a classifier
			Classifier fc = getClassifierByTrainedModel("files/crater.model");       // load the classifier

			double[] pre_dis = fc.distributionForInstance(currently_ins);
			DecimalFormat df = new DecimalFormat("0.00%");
			System.out.printf(">>>>> Prediction Results:\n");
			System.out.printf("Classifier    : %s\n", fc.getClass().getName());
			System.out.printf("Possibility   : INSIDE - %s, OUTSIDE - %s.\n", df.format(pre_dis[0]), df.format(pre_dis[1]));
							    
			List<CrashNode> lsCrashes = RepsUtilier.getSingleCrashWithoutBug(tracePath);
			List<String> stacktrace = lsCrashes.get(0).stackTraces;
			if(pre_dis[0] > pre_dis[1]){
				System.out.println("Recommandation: The root-cause-line of the given crash may reside INSIDE of the stack trace. Try to check \nthe following specific lines,\n");
				for(String line: stacktrace) System.out.println(line);
			}else{
				System.out.println("Recommandation: The root-cause-line of the given crash may reside OUTSIDE of the stack trace. Try to check \nthe code through the method invocations, \n");
				for(String line: stacktrace) System.out.println(line);
			}
			System.out.println("\n------------------------------------------------");
	
		}catch (Exception e) {
			printHelpInfo();
		}
	}
	
	/** To learn a classifier by the training set */
	public static Classifier learnClassifierByTrainingSet(String trainSetPath) throws Exception{
		// train
		Instances ins = DataSource.read(trainSetPath);
		ins.setClassIndex(ins.numAttributes() - 1);
		
		// imbalance processing
		SMOTE smote = new SMOTE();
		smote.setInputFormat(ins);
		
//		// classifiers setting
		RandomForest rf = new RandomForest();			
		FilteredClassifier fc = new FilteredClassifier();
		fc.setClassifier(rf);
		fc.setFilter(smote);
//		
//		// predict
		fc.buildClassifier(ins);
//		SerializationHelper.write("files/crater.model", fc);
		
		return fc;
	}
	
	/** To load a classifier from the modelPath */
	public static Classifier getClassifierByTrainedModel(String modelPath) throws Exception{
		return (Classifier) weka.core.SerializationHelper.read(modelPath);
	}
	
	/** Print the possible failed reasons. */
	public static void printHelpInfo(){
		System.err.println("Oops, CraTer is failed!\n-----------------");
		System.err.println("(1) Try to check the parameters that Crater supports:");
		System.out.println("\t -projPath       <path/to/project/>");
		System.out.println("\t -projStackTrace <path/to/crash.txt>");
		System.out.println("\t -cp             <path/to/proj.jar>");	
		System.err.println("(2) Try to check the format of crash.txt:");
		System.out.println("\t stack trace should be saved in a txt format.");
		System.err.println("(3) Try to check the class path of analysized project");
		System.out.println("\t Third-party libraries should be added followed by \'-cp\'");
	}

}
