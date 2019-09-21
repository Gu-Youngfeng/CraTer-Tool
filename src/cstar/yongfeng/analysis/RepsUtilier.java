package cstar.yongfeng.analysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/***
 * <p>This class <b>RepUtilier</b> provides the functions of extracting crash information({@link#getSingleCrash}) 
 * and related features({@link#getFeatures}).</p>
 * @author yongfeng
 *
 */
public class RepsUtilier { 
	
	/**
	 * <p>To extract features from <b>Stack Trace</b> (xx_mutants.txt) and <b>Buggy Source Code</b> (xx_parent/). Note that,</p>
	 * <li>xx_mutants.txt can be either the list of crash or single crash.</li>
	 * <li>the original 89 features are from paper "Does the fault reside in stack trace?"</li>
	 * @param path path of stack trace
	 * @param proj path of source code
	 * @throws Exception
	 */
	public static double[] getFeatures(String path, String proj){
		List<CrashNode> lsCrash = new ArrayList<CrashNode>();
		try {
			lsCrash = getSingleCrash(path);
		} catch (Exception e) {			
			e.printStackTrace();
		}
		
		double[] feature_total = {};
		
		for(int i=0; i<lsCrash.size(); i++){ // for each crash node: start
	
			System.out.println(">>>>> Crash Information: ");
			System.out.println("Source code path : " + proj);
			System.out.println("Stack trace path : " + path);

			lsCrash.get(i).showBasicInfo();
			
			/** Features from Stack Trace **/
			double[] features_0 = lsCrash.get(i).showSTFeatures(); // features from stack trace: ST01~ST09
			SRCAnalyzer srcAzer = new SRCAnalyzer(proj);
			double[] features_1 = srcAzer.showSRCFeatures(); //features from  project: ST10~ST11
			
			/** Features from Source Code 
			 *  1. top-class
			 * **/
			String topClsName = lsCrash.get(i).getTopClassName(); // top class name
			String topMedName = lsCrash.get(i).getTopMethodName(); // top method name
			int topMedLine = lsCrash.get(i).getTopMethodLine(); // top method line
			
			CLSAnalyzer clsAzer1 = new CLSAnalyzer(proj, topClsName);
			double[] features_2 = clsAzer1.showCLSFeatures(); //features from top class: CT01~CT06

			MEDAnalyzer medAzer1;
			double[] features_3= {};
			try {
				medAzer1 = new MEDAnalyzer(proj, topClsName, topMedName, topMedLine);
				features_3 = medAzer1.showMEDFeatures();//features from top method: CT07~CT23, AT01~AT16
			} catch (Exception e) {
				System.out.println("[TOP METHOD NOT FOUND!]" + topClsName + "::" + topMedName + ": " + topMedLine);
				e.printStackTrace();
			}			
			
			/** Features from Source Code 
			 *  2. bottom-class
			 * **/
			String botClsName = lsCrash.get(i).getBottomClassName(); // bottom class name
			String botMedName = lsCrash.get(i).getBottomMethodName(); // bottom method name
			int botMedLine = lsCrash.get(i).getBottomMethodLine(); // bottom method line
			
			CLSAnalyzer clsAzer2 = new CLSAnalyzer(proj, topClsName);
			double[] features_4 = clsAzer2.showCLSFeatures(); //features from bottom class: CB01~CB06
			
			MEDAnalyzer medAzer2;
			double[] features_5= {};
			try {
				medAzer2 = new MEDAnalyzer(proj, botClsName, botMedName, botMedLine);
				features_5= medAzer2.showMEDFeatures();//features from bottom method: BT07~CB23, AB01~AB16
			} catch (Exception e) { 
				/** If the method is not found in medAzer2 (medAzer2=null)
				 *  there is possible that the method is inherited, and no related source in medAzer2
				 * **/
				String bot2ClsName = lsCrash.get(i).getBottom2ClassName(); // last 2 bottom class name
				String bot2MedName = lsCrash.get(i).getBottom2MethodName(); // bottom method name
				int bot2MedLine = lsCrash.get(i).getBottom2MethodLine(); // bottom class name
//				System.out.println("[BOTTOM METHOD NOT FOUND!]");
				try {
					medAzer2 = new MEDAnalyzer(proj, bot2ClsName, bot2MedName, bot2MedLine);
					features_5= medAzer2.showMEDFeatures();//features from bottom method: BT07~CB23, AB01~AB16
				} catch (Exception e1) {
					System.out.println("[BOTTOM-2 METHOD NOT FOUND!]" + bot2ClsName + "::" + bot2MedName + ": " + bot2MedLine);
					e.printStackTrace();
				}
				
			}
			System.out.println(">>>>> Extracted Features: ");
			feature_total = addMultipleArraies(features_0, features_1, features_2, features_3, features_4, features_5);
			for(double fea: feature_total) System.out.print(fea + ", "); // break line for next crash
			System.out.println();
		}//end
		
		return feature_total;
		
	}
	
	/**
	 * <p>To return the crash node list in path, each of which contains all stack traces in the corresponding crash. 
	 * <br>Here is the example of a single crash: </p>
	 * 
	 * <pre> --- org.apache.commons.lang3.math.NumberUtilsTest::testCreateNumber
     * java.lang.NumberFormatException: 2. is not a valid number.
	 *     at org.apache.commons.lang3.math.NumberUtils.createNumber(NumberUtils.java:546)
	 *     at org.apache.commons.lang3.math.NumberUtilsTest.testCreateNumber(NumberUtilsTest.java:213)
     * MUTATIONID: &lt;&lt; org.apache.commons.lang3.math.NumberUtils,createNumber,491 &gt;&gt;</pre>
	 * @param path path of project.txt
	 * @return list of crash node
	 * @throws Exception 
	 */
	public static List<CrashNode> getSingleCrash(String path) throws Exception{
		List<CrashNode> lsCrash = new ArrayList<CrashNode>();
		
		// split the xx_mutants.txt in "crashrep"
		File file = new File(path);
		if(!file.exists()){
			System.out.println("[ERROR]: No such file! " + path); 
			return null;
		}
		
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		
		String str = "";
		List<String> singleCrash = new ArrayList<String>(); // each single crash is a string list 
		while((str=br.readLine())!=null){
			
			if(str.contains("(Unknown Source)")){ // we cannot solve the UNKONWN SOURCE in stack trace
				continue;
			}
			
			if(str.startsWith("BUG")) {
				if(singleCrash.size()!=0) {
					CrashNode crash = new CrashNode(singleCrash);
					lsCrash.add(crash);
					singleCrash.clear();
				}
			}else {
				if(str.trim().equals("")){
					continue;
				}else{
					singleCrash.add(str);
				}
			}
			
		}
		if(singleCrash.size()!=0) {
			CrashNode crash = new CrashNode(singleCrash);
			lsCrash.add(crash);
			singleCrash.clear();
		}
		
		br.close();
		fr.close();
		
		return lsCrash;
	}
	
	/***
	 * <p>To get the simple class name from qualified class name. </p>
	 * <p>E.g., sklse.yongfeng.analysis.RepsUtilier &gt; RepsUtilier</p>
	 * @param qualifiedName qualified class name
	 * @return simple class name
	 */
	public static String getSimpleClassName(String qualifiedName){
		String simpleName = "";
		
		if(qualifiedName.contains(".")){
			int index = qualifiedName.lastIndexOf(".");
			simpleName = qualifiedName.substring(index+1);
		}else{
			simpleName = qualifiedName;
		}
		
//		System.out.println(simpleName);
		
		return simpleName;
	}
	
	public static double[] addMultipleArraies(double[] ... features) {
		
		int count = 0;
		int feature_size = 0;
		
		for(int i=0; i<features.length; i++) {
			if(features[i].length == 0) {
				System.out.printf("[ERROR]: Array %d has no element!\n", i);
				continue;
			}
			feature_size += features[i].length;
		}
		
		double[] feature_total = new double[feature_size];
		
		for(int i=0; i<features.length; i++) {
			for(int j=0; j<features[i].length; j++) {
				feature_total[count] = features[i][j];
				count++;
			}
		}
		
		return feature_total;
	}

}
