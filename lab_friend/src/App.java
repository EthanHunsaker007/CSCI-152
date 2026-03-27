public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}

class Friend {
    private boolean happy;
    private String name;

    public Friend(String name) {
        this.happy = false;
        this.name = name;
    }

    public void makeHappy() {
        this.happy = true;
    }

    public void makeSad() {
        this.happy = false;
    }
}
