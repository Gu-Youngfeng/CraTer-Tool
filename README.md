# CraTer-Tool

### Introduction
CraTer-Tool is a light-weight prototype of [CraTer](https://github.com/Gu-Youngfeng/CraTer/), it can quickly analysis a Java crash and predict whether the fault reside inside or outside of the stack trace. 

The key ideas of the prototype are: **1)** build a classification model based on the extracted features from historical crashes; **2)** use the built model to predict the fault position of new-submited crash.

### Deployment
JDK 8.0 is needed. The location of historical crashes (Line 51 and Line 60) is hard-implemented in the code, you have to change the directory to your own `files/empty.csv` and `files/total/training_set.csv`.

```java
Instances empty_ins = DataSource.read("C:/Users/yongfeng/Desktop/git/CraTer-tool/files/empty.arff");         /** Line 51 **/
empty_ins.setClassIndex(empty_ins.numAttributes() - 1);                                                      /** Line 52 **/
Instance currently_ins = new DenseInstance(feature_total.length);                                            /** Line 53 **/
 ...			
/** training set loading*/
Instances ins = DataSource.read("C:/Users/yongfeng/Desktop/git/CraTer-tool/files/total/training_set.arff");  /** Line 60 **/
ins.setClassIndex(ins.numAttributes() - 1);                                                                  /** Line 61 **/
```

Besides, remember to add the compilation jar and dependencies to the project before packaging the crater-tool.jar.

### Usage

Step-1: Package the `crater-tool.jar` from CraTer-Tool project.

This step can be implemented by the support of Eclipse IDE. 
Right click on project -> click "Export" -> click "Runnable JAR file" -> fill the "Launcher Configuration" and "Export destination" -> click "Finish".

Step-2: Use the following command to predict the fault position of a crash, where the `E:/codec` refers to the faulty code path and `E:/codec-1` denotes the stack trace path,  

```cmd
$ java -jar crater-tool.jar -projPath E:/codec/ -projStackTrace E:/codec-1.txt
```

As we can see, **crater-tool.jar** has two inputs and its output is the prediction results as follows,

```
>>>>> Crash Information:
Source code path : E:/codec/
Stack trace path : E:/codec-1.txt
Exception Type:     ArrayIndexOutOfBoundsException
Trace lines:        3
Number of Classes:  2
Number of Methods:  3
polymorphic or not: true
Crash Position:     org.apache.commons.codec.binary.Base32, encode, 515
Crash Trigger:      org.apache.commons.codec.binary.BaseNCodecInputStream, read, 97
------------------------------------------------

>>>>> Extracted Features:
14.0, 3.0, 2.0, 3.0, 0.0, 6.0, 6.0, 21.0, 4.0, 60.0, 82.0, 11.0, 14.0, 
3.0, 0.0, 0.0, 312.0, 95.0, 4.0, 5.0, 7.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 
...
0.09090909361839294, 0.1818181872367859, 0.1818181872367859, 0.0, 0.3636363744735718,
------------------------------------------------

>>>>> Crashing Fault Residence:
Intrace: 0.750 vs OutTrace: 0.250
The crashing fault may reside INSIDE of the stack trace. Try to check the following lines,

        at org.apache.commons.codec.binary.Base32.encode(Base32.java:515)
        at org.apache.commons.codec.binary.BaseNCodecInputStream.read(BaseNCodecInputStream.java:160)
        at org.apache.commons.codec.binary.BaseNCodecInputStream.read(BaseNCodecInputStream.java:97)

```
