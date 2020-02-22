import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;


class DataRetriever {
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

    LinkedList<Book> readBooksFromXML(String fileName) throws IOException, ParserConfigurationException, SAXException {

        LinkedList<Book> user_books = new LinkedList<>();
        String title,genre;

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(fileName);
        doc.getDocumentElement().normalize();

        NodeList books = doc.getElementsByTagName("book");

        for (int i = 0; i < books.getLength(); i++) {
            try {
                title = ((Element) books.item(i)).getAttribute("title");

                NodeList bookID = ((Element) books.item(i)).getElementsByTagName("id");
                int id = Integer.parseInt(bookID.item(0).getTextContent());

                NodeList author = ((Element) books.item(i)).getElementsByTagName("author");
                String authorName = author.item(0).getTextContent();

                NodeList imageURL = ((Element) books.item(i)).getElementsByTagName("image_url");
                URL image_url = new URL(imageURL.item(0).getTextContent());

                NodeList book_URL = ((Element) books.item(i)).getElementsByTagName("book_url");
                URL bookURL = new URL(book_URL.item(0).getTextContent());

                NodeList edition_ID = ((Element) books.item(i)).getElementsByTagName("edition_id");
                int editionID = Integer.parseInt(edition_ID.item(0).getTextContent());

                NodeList bookGenres = ((Element) books.item(i)).getElementsByTagName("genres");
                genre = bookGenres.item(0).getTextContent();
                LinkedList<String> genres = new LinkedList<>(Arrays.asList(genre.split(",")));

                Book book = new Book();
                book.setGenres(genres);
                book.setId(id);
                book.setImage_URL(image_url);
                book.setTitle(title);
                book.setAuthor(authorName);
                book.setBook_link(bookURL);
                book.setEditionId(editionID);
                user_books.add(book);

            } catch(NullPointerException ignored) {}
        }
        return user_books;
    }
}
