package dtspace;

public class Test {

    public static void main(String[] args) {

        Tuple t = new Tuple(Integer.valueOf(1));
        
        try {
            GlobalSpace gspace = new GlobalSpace();
            gspace.setNbNodes(Integer.parseInt(args[1]));

            if(args[0].equals("write")) {
                gspace.write(t);
            }
            else if(args[0].equals("ifExists")) {

                Tuple ret = gspace.readIfExists(t);
                System.out.println("READ IF " + ret.toString());
                ret = gspace.takeIfExists(t);
                System.out.println("TAKE IF " + ret.toString());
            }
            else if(args[0].equals("blocking")) {
                
                Tuple ret = gspace.read(t);
                System.out.println("READ " + ret.toString());
                ret = gspace.take(t);
                System.out.println("TAKE " + ret.toString());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
}
