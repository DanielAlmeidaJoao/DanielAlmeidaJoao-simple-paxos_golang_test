import java.lang.reflect.Field;

public class Message {
    public int id;
    private String content;
    private boolean isImportant;
    public Field auxField;

    // Constructor
    public Message(int id, String content, boolean isImportant) {
        this.id = id;
        this.content = content;
        this.isImportant = isImportant;
    }

    public void writeToByteBuf() throws IllegalAccessException  {
        Class<?> clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Field aux = null;
        for (Field field : fields) {
            field.setAccessible(true); // Allows access to private fields
            Object value = field.get(this);

            System.out.println("FIELD IS: " + value+ " "+field.getName());
            if (aux == null){
                aux = field;
            }
        }
        this.isImportant = false;
        System.out.println("ll"+aux.get(this));
        auxField = aux;

    }

    // Getters and setters (if needed)
}
