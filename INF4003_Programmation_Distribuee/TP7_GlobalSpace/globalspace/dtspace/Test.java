package dtspace;

public class Test {

    public static void main(String[] args) {

        try {
            GlobalSpace gspace = new GlobalSpace();
            gspace.setNbNodes(Integer.parseInt(args[0]));
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
