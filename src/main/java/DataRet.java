import com.sun.tools.doclint.Env;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;


class DataRet {
    private String query;

    String fetchReadBooks(int userId) throws IOException {
        query = userId + ".xml?key=" + URLEncoder.encode(Environment.GOODREADS_KEY, Constants.CHARACTER_ENCODING) +
                "&shelf=" + URLEncoder.encode("read", Constants.CHARACTER_ENCODING) + "&v=2" + "&per_page=200";
        URLConnection connection = new URL(Constants.GET_BOOKS_ON_A_SHELF_URL + query).openConnection();
        connection.setRequestProperty("Accept-Charset",Constants.CHARACTER_ENCODING);
        InputStream response = connection.getInputStream();
        Scanner scanner = new Scanner(response);
        return scanner.useDelimiter("\\A").next();
    }

    String fetchBookData(int book_id) throws IOException {
        query = book_id + ".xml?key=" + URLEncoder.encode(Environment.GOODREADS_KEY, Constants.CHARACTER_ENCODING);
        URLConnection connection = new URL(Constants.GET_BOOKS_GIVEN_ID + query).openConnection();
        connection.setRequestProperty("Accept-Charset",Constants.CHARACTER_ENCODING);
        try {
            InputStream response = connection.getInputStream();
            Scanner scanner = new Scanner(response);
            return scanner.useDelimiter("\\A").next();

        } catch (Exception ignored) { }

        return null;
    }


    LinkedList<String> fetchBookGenres(Book book) throws IOException, ParserConfigurationException, SAXException {
        URLConnection connection;
        Documenter documentParser = new Documenter();

        query = (book.getId()) + ".xml?key=" + URLEncoder.encode(Environment.GOODREADS_KEY, Constants.CHARACTER_ENCODING);
        connection = new URL(Constants.GET_BOOKS_GIVEN_ID + query).openConnection();
        connection.setRequestProperty("Accept-Charset",Constants.CHARACTER_ENCODING);

        InputStream response = connection.getInputStream();
        String responseBody = new Scanner(response).useDelimiter("\\A").next();
        return documentParser.parseBookGenres(responseBody,book);
    }

    LinkedList<Book> readBooksXML(String fileName) throws IOException, ParserConfigurationException, SAXException {
        LinkedList<Book> user_books = new LinkedList<>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        String title,genre;
        Document doc = documentBuilder.parse(fileName);
        doc.getDocumentElement().normalize();
        NodeList books = doc.getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {
            LinkedList<String> genres = new LinkedList<>();
            try {
                title = ((Element) books.item(i)).getAttribute("title");
                NodeList idd = ((Element) books.item(i)).getElementsByTagName("id");
                int id = Integer.parseInt(idd.item(0).getTextContent());
                NodeList authorr = ((Element) books.item(i)).getElementsByTagName("author");
                String author = authorr.item(0).getTextContent();
                NodeList imagee_url = ((Element) books.item(i)).getElementsByTagName("image_url");
                URL image_url = new URL(imagee_url.item(0).getTextContent());
                NodeList book_url = ((Element) books.item(i)).getElementsByTagName("book_url");
                URL book_urll = new URL(book_url.item(0).getTextContent());
                NodeList edition_id = ((Element) books.item(i)).getElementsByTagName("edition_id");
                int editionId = Integer.parseInt(edition_id.item(0).getTextContent());
                NodeList genress = ((Element) books.item(i)).getElementsByTagName("genres");
                genre = genress.item(0).getTextContent();
                genres.addAll(Arrays.asList(genre.split(",")));
                Book book = new Book();
                book.setGenres(genres);
                book.setId(id);
                book.setImage_URL(image_url);
                book.setTitle(title);
                book.setAuthor(author);
                book.setBook_link(book_urll);
                book.setEditionId(editionId);
                user_books.add(book);
            }catch(NullPointerException ignored) {
            }
        }
        return user_books;
    }

    LinkedList<Book> readBooksAllXML(String fileName) throws IOException, ParserConfigurationException, SAXException {
        LinkedList<Book> user_books = new LinkedList<>();
        HashSet<String> book_names = new HashSet<>();
        HashSet<Integer> book_ratings = new HashSet<>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        String title,genre;
        Document doc = documentBuilder.parse(fileName);
        doc.getDocumentElement().normalize();
        NodeList books = doc.getElementsByTagName("book");
        for (int i = 0; i < books.getLength(); i++) {
            title = ((Element)books.item(i)).getAttribute("title");
            NodeList idd = ((Element)books.item(i)).getElementsByTagName("id");
            int id = Integer.parseInt(idd.item(0).getTextContent());
            NodeList rating_count = ((Element)books.item(i)).getElementsByTagName("rating_count");
            int ratings = Integer.parseInt(rating_count.item(0).getTextContent());
            NodeList authorr = ((Element)books.item(i)).getElementsByTagName("author");
            String author = authorr.item(0).getTextContent();
            NodeList imagee_url = ((Element)books.item(i)).getElementsByTagName("image_url");
            URL image_url = new URL(imagee_url.item(0).getTextContent());
            try {
                NodeList genress = ((Element) books.item(i)).getElementsByTagName("genres");
                genre = genress.item(0).getTextContent();
            } catch(NullPointerException e) {
                continue;
            }
            LinkedList<String> genres = new LinkedList<>(Arrays.asList(genre.split(",")));
            Book book = new Book();
            book.setGenres(genres);
            book.setId(id);
            book.setImage_URL(image_url);
            book.setTitle(title);
            book.setAuthor(author);
            book.setRating_count(ratings);
            if ((book_names.add(book.getTitle())) && (book_ratings.add(book.getRating_count())))
                user_books.add(book);
        }
        return user_books;
    }
}
