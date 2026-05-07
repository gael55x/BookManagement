package bookmanagement;

//  Subclass of Book — adds a helper used when showing book info (OOP: inheritance).
public class LibraryBook extends Book {

    public LibraryBook() {
        super();
    }

    public LibraryBook(int bookId, String title, String author, String category, String status) {
        super(bookId, title, author, category, status);
    }

    public String getBookDetails() {
        return getBookId() + " | " + getTitle() + " by " + getAuthor()
                + " [" + getCategory() + "] — " + getStatus();
    }
}
