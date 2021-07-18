public class NPE {

    private String name;
    /** set printer's name */
    public void setName(String name) {
        this.name = name;
    }
    /** print the printer's information */
    public void print() {
        printString(name);
    }

    private void printString(String s) {
        System.out.println(s + " (" + s.length() + ")");
    }

    public static void main(String[] args) {
        NPE printer = new NPE();  // initializing NPE object without setting its name
        printer.print();
    }
}
