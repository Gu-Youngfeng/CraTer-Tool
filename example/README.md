### Example of CraTer-Tool

The `test_proj` is an example of faulty project which contains two Java file (`src/NPE.java` and `src/IOBE.java`).
When you run these two Java file, you will get two Java crashes. 

The exception stack traces are saved in the `trace_npe.txt` and the `trace_iobe.txt` respectively.

For the above two crashes, to predict whether the fault reside in the stack trace, we can use the following command to locate the crashing-fault,

For the crash in `src/NPE.java`
```
java -jar crater-tool.jar -projPath example/test_proj/ -projStackTrace example/trace_npe.txt
```

For the crash in `src/IOBE.java`
```
java -jar crater-tool.jar -projPath example/test_proj/ -projStackTrace example/trace_iobe.txt
```
