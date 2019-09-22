# CraTer-Tool

## Introduction
**CraTer-Tool** is a light-weight prototype of [CraTer](https://github.com/Gu-Youngfeng/CraTer/), it can quickly analysis a Java crash and predict whether the fault reside inside or outside of the stack trace. 

The key ideas of the prototype are: **1)** build a classification model based on the extracted features from historical crashes; **2)** use the built model to predict the fault position of new-submited crash.

## Deployment
1. Download the project and import it into Eclipse IDE (or other Java IDE).
2. the compilation of faulty project (i.e., jar format) as well as its dependencies (i.e., jar format) are required to be added to the classpath of the **CraTer-Tool** project.
3. Package the `crater-tool.jar` from **CraTer-Tool** project and put it into the root directory of **CraTer-Tool** project. This step can be implemented by the support of Eclipse IDE.

## Usage example

Suppose that we have a faulty project `E:/codec/`, and its crash trace is saved in the file `E:/codec-1.txt`.

```
BUG-1
--- org.apache.commons.codec.binary.Base32InputStreamTest::testBase32InputStreamByteByByte
java.lang.ArrayIndexOutOfBoundsException: 1126916991
	at org.apache.commons.codec.binary.Base32.encode(Base32.java:515)
	at org.apache.commons.codec.binary.BaseNCodecInputStream.read(BaseNCodecInputStream.java:160)
	at org.apache.commons.codec.binary.BaseNCodecInputStream.read(BaseNCodecInputStream.java:97)
```

Then we use the following command to predict the fault position of a crash.

```cmd
$ java -jar crater-tool.jar -projPath E:/codec/ -projStackTrace E:/codec-1.txt
```

As we can see, **crater-tool.jar** has two inputs and its output is the prediction results as follows.
According to the prediction results, **crater-tool.jar** predicts the fault resides inside of the stack trace.

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

## Parameter tuning
- If you wanna change the training set of `crater-tool.jar`, you can find and replace the `files/training_set.csv`.
- If you wanna change the classifier used in `crater-tool.jar`, you can find and modify the code in Line 77 of the 'cstar.yongfeng.launcher.Entry.java'
