import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Book {
    private int id,rating_count;
    private String title;
    private String author;
    private LinkedList<String> genres;
    private int editionId;
    private URL book_link;

    public int getEditionId() {
        return editionId;
    }

    public void setEditionId(int best_book_id) {
        this.editionId = best_book_id;
    }

    public URL getBook_link() {
        return book_link;
    }

    public void setBook_link(URL book_link) {
        this.book_link = book_link;
    }

    public void setId(int id) {
        this.id = id;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenres(LinkedList<String> genres) {
        this.genres = genres;
    }

    public void setImage_URL(URL image_URL) {
        this.image_URL = image_URL;
    }

    private List<String> forbiddenGenres = Arrays.asList("currently-reading","to-read","owned","favorites","library","my-books",
            "books-i-own","owned-books","default","to-buy","fiction","non-fiction","to-get","e-book","literature","novels","series"," series"
    ," classics","book-club");

    int getRating_count() {
        return rating_count;
    }

    public Book() {
    }

    LinkedList<String> getGenres() {
        return genres;
    }

    List<String> getForbiddenGenres() {
        return forbiddenGenres;
    }

    int getId() {
        return id;
    }


    String getTitle() {
        return title;
    }

    String getAuthor() {
        return author;
    }

    void setAuthor(String author) {
        this.author = author;
    }


    private URL image_URL;

    Book(int id, String title, URL image_URL, int rating_count) {
        this.id = id;
        this.title = title;
        this.image_URL = image_URL;
        this.rating_count = rating_count;
    }

    URL getImage_URL() {
        return image_URL;
    }
}
