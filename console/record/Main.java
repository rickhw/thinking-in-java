// java 16+

public class Main {
    
    public static void main(String[] args) {
        User u = new User("rick","123");

        System.out.println(u.name());
        System.out.println(u.toString());
        System.out.println(u.hashCode());
    }
}


record User(String name, String no) {}

record User2(String name, String no) {
    // Main.java:18: error: field declaration must be static
    static boolean isOk = true;
}



