import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static String Initiliazer(String user) throws Exception {
        Main.createFile();
        LinkedList<Book> all_books;
        Map<String, Integer> frequency;
        Documenter documenter = new Documenter();
        DataRetriever dataRet = new DataRetriever() ;
        //int user_id = documenter.parseUserId();
        LinkedList<Book> user_books;
       //onlineMode(documenter, dataRet, 8783538,user_books );
        user_books = dataRet.readBooksFromXML(user + ".xml");
        String info;

        frequency = documenter.findFrequency(user_books);

        List<Map.Entry<String, Integer> > freq =
                new LinkedList<>(frequency.entrySet());
        freq.sort(Map.Entry.comparingByValue());
        Collections.reverse(freq);

        HashMap<String, Integer> sortedGenreMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> aa : freq) {
            sortedGenreMap.put(aa.getKey(), aa.getValue());
        }

        documenter.mergeAllBooks();
        all_books = documenter.readAllBooks();

        cleanBooks(all_books,user_books);

        Map <String, Double> cosineValues = new HashMap<>();

        for (Book book: all_books) {
            Map<String, Integer> candidate = new HashMap<>();
            for (String genre : book.getGenres()) {
                candidate.put(genre,1);
            }
            cosineValues.put(book.getTitle(), Cosine.cosineSimilarity(sortedGenreMap,candidate));
        }

        List<Map.Entry<String, Double> > cosineList =
                new LinkedList<>(cosineValues.entrySet());
        cosineList.sort(Map.Entry.comparingByValue());
        Collections.reverse(cosineList);

        HashMap<String, Double> temp1 = new LinkedHashMap<>();
        for (Map.Entry<String, Double> aa : cosineList) {
            temp1.put(aa.getKey(), aa.getValue());
        }

        HashMap<Book,Double> bookMap = new HashMap<>();
        for (String str : temp1.keySet()) {
            for (Book book : all_books) {
                if (book.getTitle().equals(str)) {
                    bookMap.put(book,temp1.get(book.getTitle()));
                }
            }
        }

        List<Map.Entry<Book, Double> > temp2 =
                new LinkedList<>(bookMap.entrySet());

        temp2.sort(Map.Entry.comparingByValue());
        Collections.reverse(temp2);
        HashMap<Book, Double> temp3 = new LinkedHashMap<>();
        for (Map.Entry<Book, Double> aa : temp2) {
            temp3.put(aa.getKey(), aa.getValue());
        }
        int count = 0;
        String book_url = "";
        String image_url = "";
        for (Book book : temp3.keySet()) {
            book_url = book_url.concat(book.getBook_link().toString() + "\n");
            image_url = image_url.concat(book.getImage_URL().toString() + "\n");
            if (count == 35) break;
            count++;
        }
        book_url = book_url.substring(0,book_url.length()-1);
        image_url = image_url.substring(0,image_url.length()-1);
        Path file1 = Paths.get("books.txt");
        Files.write(file1, Collections.singletonList(book_url), StandardCharsets.UTF_8);
        Path file2 = Paths.get("images\\images.txt");
        Files.write(file2, Collections.singletonList(image_url), StandardCharsets.UTF_8);

        info = "Frequency of tag: \n";
        count = 0;
        for (String ss : sortedGenreMap.keySet()) {
            if (count == 10) break;
            info = info.concat(ss + " " + sortedGenreMap.get(ss).toString() + "\n");
            count++;
        }
        return info;

    }

   public static void cleanBooks(LinkedList<Book> all_books, LinkedList<Book> user_books) {
        LinkedList<Integer> all_books_ids = new LinkedList<>();
       LinkedList<Integer> user_books_ids = new LinkedList<>();
        for (Book book : all_books) {
            all_books_ids.add(book.getEditionId());

        }
       for (Book book : user_books) {
           user_books_ids.add(book.getEditionId());
       }
       for (Integer temp : user_books_ids) {
           if ( all_books_ids.contains(temp)){
               all_books.removeIf(book -> book.getEditionId() == temp);
           }
       }
   }


    static void onlineMode(Documenter documenter,DataRetriever dataRet,int user_id, LinkedList<Book> user_books) throws IOException, ParserConfigurationException, SAXException {
        documenter.parseUserReadShelf(dataRet.fetchReadBooks(user_id),user_books,user_id);

        user_books.forEach(book -> {
            try {
                book.setGenres(dataRet.fetchBookGenres(book));
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        });
        try {
            documenter.writeUserBooksXml(user_books,String.valueOf(user_id));
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static void createFile() throws IOException {
        File file1 = new File("C:\\Users\\Cagdas\\Desktop\\Java\\Goodreads\\books.txt");
        File file2 = new File("C:\\Users\\Cagdas\\Desktop\\Java\\Goodreads\\images\\images.txt");
        file1.createNewFile();
        file2.createNewFile();
    }

    public static void deleteFile() {
        File file1 = new File("C:\\Users\\Cagdas\\Desktop\\Java\\Goodreads\\books.txt");
        File file2 = new File("C:\\Users\\Cagdas\\Desktop\\Java\\Goodreads\\images\\images.txt");
        file1.delete();
        file2.delete();
        for (int i= 0; i<36; i++) {
            File image = new File("C:\\Users\\Cagdas\\Desktop\\Java\\Goodreads\\images\\book"+i);
            image.delete();
        }
    }

}



