import java.util.LinkedList;

public class Author {
    private LinkedList<Book> book = new LinkedList<>();
    private String name;
    private int id;

    LinkedList<Book> getBook() {
        return book;
    }

    String getName() {
        return name;
    }

    Author(String name, int id) {
        this.name = name;
        this.id = id;
    }
}
