import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.crypto.Data;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


class Documenter {

   void parseUserReadShelf(String userBooks,LinkedList<Book> user_books,int user_id) throws IOException, SAXException, ParserConfigurationException {
       InputStream inputStream = new ByteArrayInputStream(userBooks.getBytes(StandardCharsets.UTF_8));
       DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
       DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
       Document doc = documentBuilder.parse(inputStream);
       doc.getDocumentElement().normalize();
       int book_count = getUserBookCount(user_id);

       NodeList book_name, book_id, book_image_URL, book_rating_count,edition_id,work;
       for (int i=0; i < book_count; i++) {
           NodeList books = doc.getElementsByTagName("book");
           book_name = ((Element) books.item(i)).getElementsByTagName("title");
           book_id = ((Element) books.item(i)).getElementsByTagName("id");
           book_image_URL = ((Element) books.item(i)).getElementsByTagName("image_url");
           book_rating_count = ((Element) books.item(i)).getElementsByTagName("ratings_count");
           if ((Integer.parseInt(book_rating_count.item(0).getTextContent())) < 5000) continue;
           work = ((Element)books.item(i)).getElementsByTagName("work");
           edition_id = ((Element) work.item(0)).getElementsByTagName("id");


           Book book;
           book = new Book(Integer.parseInt(book_id.item(0).getTextContent()), book_name.item(0).getTextContent()
                   , new URL(book_image_URL.item(0).getTextContent())
                   , Integer.parseInt(book_rating_count.item(0).getTextContent()));

           NodeList authors = ((Element) books.item(i)).getElementsByTagName("authors");
           NodeList authorr = ((Element) authors.item(0)).getElementsByTagName("author");
           NodeList author_name = ((Element) authorr.item(0)).getElementsByTagName("name");
           NodeList urll = ((Element)books.item(i)).getElementsByTagName("link");
           URL book_url = new URL(urll.item(0).getTextContent());
           String author = author_name.item(0).getTextContent();
           book.setAuthor(author);
           book.setEditionId(Integer.parseInt(edition_id.item(0).getTextContent()));
           book.setBook_link(book_url);
           user_books.add(book);
       }

   }

    int getUserBookCount(int user_id) throws IOException, ParserConfigurationException, SAXException {
       String query = "https://www.goodreads.com/shelf/list.xml?user_id=" + user_id + "&key=" + Environment.GOODREADS_KEY;
        URLConnection connection = new URL(query).openConnection();
        connection.setRequestProperty("Accept-Charset",Constants.CHARACTER_ENCODING);

        InputStream response = connection.getInputStream();
        Scanner scanner = new Scanner(response);
        String userShelves = scanner.useDelimiter("\\A").next();

        InputStream inputStream = new ByteArrayInputStream(userShelves.getBytes(StandardCharsets.UTF_8));
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();
        NodeList shelves = doc.getElementsByTagName("shelves");

        NodeList name;
        NodeList book_count;
        int i = 0;
        do {
            NodeList user_shelves = ((Element) shelves.item(i)).getElementsByTagName("user_shelf");
            name = ((Element) user_shelves.item(0)).getElementsByTagName("name");
            book_count = ((Element) user_shelves.item(0)).getElementsByTagName("book_count");
            i++;
        }while (!name.item(0).getTextContent().equals("read"));
        return Integer.parseInt(book_count.item(0).getTextContent());
    }

    Book parseBook(String response) throws IOException, ParserConfigurationException {
        InputStream inputStream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = null;
        try {
            doc = documentBuilder.parse(inputStream);
        } catch (Exception ignored){}
        if (doc == null) {
            return null;
        }
        doc.getDocumentElement().normalize();
        NodeList books = doc.getElementsByTagName("book");
        NodeList book_name, book_id,book_image_URL,book_rating_count,book_url,edition_id,work;

        book_name = ((Element) books.item(0)).getElementsByTagName("title");
        book_id = ((Element) books.item(0)).getElementsByTagName("id");
        book_image_URL = ((Element) books.item(0)).getElementsByTagName("image_url");
        book_rating_count = ((Element) books.item(0)).getElementsByTagName("ratings_count");
        work = ((Element) books.item(0)).getElementsByTagName("work");
        edition_id = ((Element) work.item(0)).getElementsByTagName("id");

        Book book;
        book = new Book(Integer.parseInt(book_id.item(0).getTextContent()), book_name.item(0).getTextContent()
                , new URL(book_image_URL.item(0).getTextContent())
                , Integer.parseInt(book_rating_count.item(0).getTextContent()));

        NodeList authors = ((Element) books.item(0)).getElementsByTagName("authors");
        NodeList authorr = ((Element) authors.item(0)).getElementsByTagName("author");
        NodeList author_name = ((Element) authorr.item(0)).getElementsByTagName("name");
        String author = author_name.item(0).getTextContent();
        book.setAuthor(author);
        book_url = ((Element)books.item(0)).getElementsByTagName("url");
        book.setEditionId(Integer.parseInt(edition_id.item(0).getTextContent()));
        book.setBook_link(new URL(book_url.item(0).getTextContent()));

        return book;
    }

    int parseUserId() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.parse(GROAuth.auth());
        doc.getDocumentElement().normalize();
        Node node = doc.getElementsByTagName("user").item(0);
        Element element = (Element) node;
        return Integer.parseInt(element.getAttribute("id"));
    }

    LinkedList<String> parseBookGenres(String book_details, Book book) throws IOException, SAXException, ParserConfigurationException {
        InputStream inputStream = new ByteArrayInputStream(book_details.getBytes(StandardCharsets.UTF_8));
        LinkedList<String> book_genres = new LinkedList<>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = documentBuilder.parse(inputStream);
        doc.getDocumentElement().normalize();

        NodeList popular_shelves = doc.getElementsByTagName("popular_shelves");
        NodeList shelf = ((Element) popular_shelves.item(0)).getElementsByTagName("shelf");
        Element element;
        for (int i = 0; i < shelf.getLength(); i++) {
            element = (Element) shelf.item(i);
            if (((book.getRating_count()) / 500) < Integer.parseInt(element.getAttribute("count"))) {
                if (!book.getForbiddenGenres().contains(element.getAttribute("name")))
                    book_genres.add(element.getAttribute("name"));
            }
        }
        return book_genres;
    }

    public void writeUserBooksXml(LinkedList<Book> user_books,String file) throws TransformerException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document doc = documentBuilder.newDocument();

        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);
        Element id,author,genres,book,image_url,link,edition_id,rating_count;
        Attr bookattr;
        for (Book temp : user_books) {
            book = doc.createElement("book");
            rootElement.appendChild(book);
            bookattr = doc.createAttribute("title");

            bookattr.setValue(temp.getTitle());
            book.setAttributeNode(bookattr);
            id = doc.createElement("id");
            id.appendChild(doc.createTextNode(String.valueOf(temp.getId())));
            book.appendChild(id);
            link = doc.createElement("book_url");
            link.appendChild(doc.createTextNode(temp.getBook_link().toString()));
            book.appendChild(link);
            edition_id = doc.createElement("edition_id");
            edition_id.appendChild(doc.createTextNode(String.valueOf(temp.getEditionId())));
            book.appendChild(edition_id);
            rating_count = doc.createElement("rating_count");
            rating_count.appendChild(doc.createTextNode(String.valueOf(temp.getRating_count())));
            book.appendChild(rating_count);
            author = doc.createElement("author");
            author.appendChild(doc.createTextNode(temp.getAuthor()));
            book.appendChild(author);
            book.appendChild(id);
            genres = doc.createElement("genres");
            String bookGenres = null;
            for (String s : temp.getGenres()) {
                bookGenres += s +", ";
            }
            bookGenres = bookGenres.substring(4, bookGenres.length()-1);
            genres.appendChild(doc.createTextNode(bookGenres));
            book.appendChild(genres);
            image_url = doc.createElement("image_url");
            image_url.appendChild(doc.createTextNode(temp.getImage_URL().toString()));
            book.appendChild(image_url);
        }
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(file + ".xml" ));
        transformer.transform(source, result);

        // Output to console for testing
    }


    public LinkedList<Book> readAllBooks() throws ParserConfigurationException, IOException, SAXException {
        Set<Integer> bookSet = new HashSet<>();
        DataRet dataRet = new DataRet();
        LinkedList<Book> book_list = new LinkedList<>();
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        LinkedList<Book> all_books = new LinkedList<>();
        for (int k = 0; k < 2; k++) {
            book_list = dataRet.readBooksXML("all_books" + k + ".xml");
            /*for (Book temp : book_list) {
                bookSet.add(temp.getEditionId());
            }*/
            all_books.addAll(book_list);
        }
        return all_books;
        //return book_list;

        /*int start = 12000,stop = 12400;
        for (int i = 0; i < 15; i++) {
            for (int j = start; j < stop; j++) {
                Book book;
                try {
                    book = parseBook(dataRet.fetchBookData(j));
                    if (!(bookSet.add(book.getEditionId())) || (book.getRating_count() < 5000)) {
                        continue;
                    }
                book.setGenres(dataRet.fetchBookGenres(book));
                } catch (NullPointerException ignored) {continue;}
                //System.out.println(book.getId() + " " + book.getEditionId());
                book_list.add(book);
            }
            //writeAllBooks(book_list);
            //start = stop;
            //stop += 400;
        }*/
    }

    public void writeAllBooks(LinkedList<Book> book_list) throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();
        Element rootElement = doc.createElement("books");
        doc.appendChild(rootElement);
        try {
            for (Book book : book_list) {
                Element book_node = doc.createElement("book");
                rootElement.appendChild(book_node);
                Attr attr = doc.createAttribute("title");
                attr.setValue(book.getTitle());
                book_node.setAttributeNode(attr);
                Element id = doc.createElement("id");
                id.appendChild(doc.createTextNode(String.valueOf(book.getId())));
                book_node.appendChild(id);
                Element author = doc.createElement("author");
                author.appendChild(doc.createTextNode(book.getAuthor()));
                book_node.appendChild(author);
                Element image_url = doc.createElement("image_url");
                image_url.appendChild(doc.createTextNode(book.getImage_URL().toString()));
                book_node.appendChild(image_url);
                Element rating_count = doc.createElement("rating_count");
                rating_count.appendChild(doc.createTextNode(String.valueOf(book.getRating_count())));
                book_node.appendChild(rating_count);
                Element genres = doc.createElement("genres");
                try {
                    String genres_String = book.getGenres().get(0);
                    for (String temp : book.getGenres()) {
                        genres_String = genres_String.concat("," + temp);
                    }
                    genres.appendChild(doc.createTextNode(genres_String));
                    book_node.appendChild(genres);
                } catch (IndexOutOfBoundsException e) {
                    continue;
                }
                Element edition_id = doc.createElement("edition_id");
                edition_id.appendChild(doc.createTextNode(String.valueOf(book.getEditionId())));
                book_node.appendChild(edition_id);
                Element book_url = doc.createElement("book_url");
                book_url.appendChild(doc.createTextNode(book.getBook_link().toString()));
                book_node.appendChild(book_url);
            }
        } catch (NullPointerException ignored) {
        }

        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("all_books2.xml"));
            transformer.transform(source, result);
            // Output to console for testing
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    public Map<String,Integer> findFrequency(LinkedList<Book> user_books) {
       Map<String,Integer> frequencyMap= new TreeMap<>();
       for (Book temp : user_books) {
           for (String genre : temp.getGenres()){
               if (frequencyMap.containsKey(genre)){
                   int oldValue = frequencyMap.get(genre);
                   frequencyMap.replace(genre,++oldValue);
               } else {
                   frequencyMap.put(genre, 1);
               }
           }
       }
       return frequencyMap;
    }
}
