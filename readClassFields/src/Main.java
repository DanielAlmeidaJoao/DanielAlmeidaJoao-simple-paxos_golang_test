public class Main {
    public static void main(String[] args) throws IllegalAccessException {
        System.out.println("Hello world!");
        Message message = new Message(1, "Hello, Netty!", true);

        message.writeToByteBuf();
        message.id = 123;
        long start = System.nanoTime();
        message.auxField.get(message);
        long elapsed = System.nanoTime() - start;
        System.out.println("Read time: "+elapsed);

        start = System.nanoTime();
        message.auxField.get(message);
        elapsed = System.nanoTime() - start;

        System.out.println("Read time2: "+elapsed);
        System.out.println(message.auxField.get(message));

    }
}