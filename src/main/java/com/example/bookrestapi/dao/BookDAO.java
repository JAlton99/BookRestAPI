package com.example.bookrestapi.dao;

import com.example.bookrestapi.model.Book;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

	private Book oneBook = null;
	private Connection conn = null;
	private Statement stmt = null;
	private String user = "altonjac";
	private String password = "munverbE5";
	private String url = "jdbc:mysql://mudfoot.doc.stu.mmu.ac.uk:6306/altonjac";

	public BookDAO() {
	}

	private void openConnection() {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			System.out.println(e);
		}

		try {
			conn = DriverManager.getConnection(url, user, password);
			stmt = conn.createStatement();
		} catch (SQLException se) {
			System.out.println(se);
		}
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Book getNextBook(ResultSet rs) {
		Book thisBook = null;
		try {
			thisBook = new Book(
					rs.getInt("id"),
					rs.getString("title"),
					rs.getString("author"),
					rs.getString("date"),
					rs.getString("genres"),
					rs.getString("characters"),
					rs.getString("synopsis"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return thisBook;
	}

	public List<Book> getAllBooks() {
		ArrayList<Book> allBooks = new ArrayList<>();
		openConnection();

		try {
			String selectSQL = "SELECT * FROM books";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {
				Book oneBook = getNextBook(rs);
				allBooks.add(oneBook);
			}

			preparedStatement.close();
			closeConnection();
		} catch (SQLException se) {
			System.out.println(se);
		}

		return allBooks;
	}



	public Book getBookByID(int id) {
		openConnection();
		Book book = null;

		try {
			String selectSQL = "SELECT * FROM books WHERE id = ?";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery(); // parameterized query to prevent SQL injection attacks

			if (resultSet.next()) {
				book = getNextBook(resultSet);
			} else {
				System.out.println("No book found with ID " + id);
			}

			resultSet.close();
			preparedStatement.close();
		} catch (SQLException se) {
			System.out.println("Error retrieving book with ID " + id + ": " + se.getMessage());
		}

		closeConnection();
		return book;
	}

	public List<Book> getBooksByTitle(String title) {
		openConnection();
		List<Book> books = new ArrayList<>();

		try {
			String selectSQL = "SELECT * FROM books WHERE title LIKE ?";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setString(1, "%" + title + "%");
			ResultSet resultSet = preparedStatement.executeQuery();

			while (resultSet.next()) {
				books.add(getNextBook(resultSet));
			}

			resultSet.close();
			preparedStatement.close();
		} catch (SQLException se) {
			System.out.println("Error retrieving books with title " + title + ": " + se.getMessage());
		}

		closeConnection();
		return books;
	}

	public boolean deleteBook(int id) {
		openConnection();
		boolean deleted = false;
		try {
			String selectSQL = "SELECT * FROM books WHERE id=?";
			PreparedStatement preparedStatement = conn.prepareStatement(selectSQL);
			preparedStatement.setInt(1, id);
			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				String deleteSQL = "DELETE FROM books WHERE id=?";
				preparedStatement = conn.prepareStatement(deleteSQL);
				preparedStatement.setInt(1, id);
				preparedStatement.executeUpdate();
				preparedStatement.close();
			} else {
				throw new IllegalArgumentException("Book not found with id: " + id);
			}
			resultSet.close();
			deleted = true;
		} catch (SQLException se) {
			System.out.println(se);
		}
		closeConnection();
		return deleted;
	}

	public void updateBook(Book book) {
		openConnection();

		try {
			String selectSQL = "SELECT * FROM books WHERE id=?";
			PreparedStatement selectStatement = conn.prepareStatement(selectSQL);
			selectStatement.setInt(1, book.getId());
			ResultSet rs = selectStatement.executeQuery();
			if (!rs.next()) {
				System.out.println("Error: Book with ID " + book.getId() + " does not exist.");
				return;
			}
			selectStatement.close();

			String updateSQL = "UPDATE books SET title=?, author=?, date=?, genres=?, characters=?, synopsis=? WHERE id=?";
			PreparedStatement preparedStatement = conn.prepareStatement(updateSQL);
			preparedStatement.setString(1, book.getTitle());
			preparedStatement.setString(2, book.getAuthor());
			preparedStatement.setString(3, book.getDate());
			preparedStatement.setString(4, book.getGenres());
			preparedStatement.setString(5, book.getCharacters());
			preparedStatement.setString(6, book.getSynopsis());
			preparedStatement.setInt(7, book.getId());
			preparedStatement.executeUpdate();
			preparedStatement.close();
		} catch (SQLException se) {
			System.out.println(se);
		}

		closeConnection();
	}

	public void insertBook(Book book) {
		openConnection();
		System.out.println(book.toString());
		try {
			String insertSQL = "INSERT INTO books (title, author, date, genres, characters, synopsis) VALUES (?, ?, ?, ?, ?, ?)";
			PreparedStatement preparedStatement = conn.prepareStatement(insertSQL);
			preparedStatement.setString(1, book.getTitle());
			preparedStatement.setString(2, book.getAuthor());
			preparedStatement.setString(3, book.getDate());
			preparedStatement.setString(4, book.getGenres());
			preparedStatement.setString(5, book.getCharacters());
			preparedStatement.setString(6, book.getSynopsis());

			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Rows affected: " + rowsAffected);

			preparedStatement.close();
		} catch (SQLException se) {
			System.out.println("Error inserting book: " + se.getMessage());
		}

		closeConnection();
	}
}


