package Classes;

public class CustomerIDGenerator {

    private static int id = 0;

    public static int getID() {
        // TODO: lese id fra User_ID
        id++;
        return id;
    }

}
