# CraTer-Tool

## 1. Introduction
**CraTer-Tool** is a light-weight prototype of [CraTer](https://github.com/Gu-Youngfeng/CraTer/), it can quickly analysis a Java crash and predict whether the fault reside inside or outside of the stack trace. 

The key ideas of the prototype are: **1)** build a classification model based on the extracted features from historical crashes; **2)** use the built model to predict the fault position of new-submited crash.

## 2. Usage example

Please download the whole project since the `crater-tool.jar` requires the training data in the `files/` folder. 
For a quick start, we provide a usage example in the `example/` folder.

#### 2.1 INPUT:

1. The **path of the faulty project** is in the `example/test_proj/`. 

2. The **stack trace** of crash is in the `example/trace_iobe.txt` which record the stack trace of the IndexOutOfBoundsException crash,

```
Exception in thread "main" java.lang.IndexOutOfBoundsException: Index: 1, Size: 0
	at java.util.ArrayList.rangeCheck(ArrayList.java:659)
	at java.util.ArrayList.get(ArrayList.java:435)
	at IOBE.find_elem_by_idx(IOBE.java:27)
	at IOBE.trigger_null_pointer_exp(IOBE.java:12)
	at IOBE.main(IOBE.java:5)
```

Then we use the following command to predict the fault position of a crash.

```cmd
$ java -jar crater-tool.jar -projPath example/test_proj -projStackTrace example/trace_iobe.txt
```

#### 2.2 OUTPUT

After executing the above command in the terminal, we can get the following output, including the crash informaton,
extrcted features, and prediction results.

According to the prediction results, **crater-tool.jar** guess that the fault may reside inside of the stack trace.

```
>>>>> Crash Information: 
Crashed Java project: example/test_proj/
   Crash stack trace: example/trace_iobe.txt
      Exception Type: IndexOutOfBoundsException
         Trace lines: 5
   Number of Classes: 1
   Number of Methods: 3
  polymorphic or not: false
      Crash Position: IOBE, find_elem_by_idx, 27
       Crash Trigger: IOBE, main, 5
------------------------------------------------

>>>>> Extracted Features (all 89 features): 
  13.000,   5.000,   1.000,   3.000,   1.000,   4.000,  16.000,   4.000,   4.000,   2.000,
   2.000,   3.000,   0.000,   5.000,   1.000,   1.000,   1.000,   3.000,   2.000,   0.000,
   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,
   1.000,   1.000,   0.000,   0.000,   0.667,   0.000,   0.000,   0.000,   0.000,   0.000,
   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.333,   0.333,   0.000,   0.000,
   3.000,   0.000,   5.000,   1.000,   1.000,   1.000,   3.000,   1.000,   0.000,   0.000,
   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,   1.000,
   0.000,   0.000,   0.000,   0.333,   0.000,   0.000,   0.000,   0.000,   0.000,   0.000,
   0.000,   0.000,   0.000,   0.000,   0.000,   0.333,   0.000,   0.000,   0.000,
------------------------------------------------

>>>>> Prediction Results:
Classifier    : weka.classifiers.meta.FilteredClassifier
Possibility   : INSIDE - 39.97%, OUTSIDE - 60.03%.
Recommandation: The root-cause-line of the given crash may reside OUTSIDE of the stack trace. Try to check 
the code through the method invocations, 

	at IOBE.find_elem_by_idx(IOBE.java:27)
	at IOBE.trigger_null_pointer_exp(IOBE.java:12)
	at IOBE.main(IOBE.java:5)

------------------------------------------------
```

## 3. Parameter tuning
- If you wanna change the training set of `crater-tool.jar`, you can find and replace the `files/training_set.csv`.
- If you wanna change the classifier used in `crater-tool.jar`, you can find and modify the code in Line 77 of the 'cstar.yongfeng.launcher.Entry.java'
