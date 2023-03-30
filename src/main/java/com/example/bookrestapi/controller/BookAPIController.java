package com.example.bookrestapi.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.example.bookrestapi.dao.BookDAO;
import com.example.bookrestapi.model.Book;
import com.example.bookrestapi.model.BookList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import javax.xml.bind.*;


@WebServlet(name = "bookapi", value = "/bookapi")
public class BookAPIController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private BookDAO bookDAO;
    private Gson gson;
    private JAXBContext jaxbContext;

    @Override
    public void init() throws ServletException {
        bookDAO = new BookDAO();
        gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            jaxbContext = JAXBContext.newInstance(BookList.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");

        if (contentType.equals("application/json") && accept.equals("application/json")) {
            BufferedReader reader = request.getReader();
            Book newBook = gson.fromJson(reader, Book.class);
            bookDAO.insertBook(newBook);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(newBook));
        } else if (contentType.equals("application/xml") && accept.equals("application/xml")) {
            Unmarshaller unmarshaller = null;
            try {
                unmarshaller = jaxbContext.createUnmarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            Book newBook = null;
            try {
                newBook = (Book) unmarshaller.unmarshal(request.getInputStream());
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            bookDAO.insertBook(newBook);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/xml");
            Marshaller marshaller = null;
            try {
                marshaller = jaxbContext.createMarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            try {
                marshaller.marshal(newBook, response.getOutputStream());
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        } else if (contentType.equals("text/plain") && accept.equals("text/plain")) {
            BufferedReader reader = request.getReader();
            String bookTitle = reader.readLine();
            String bookAuthor = reader.readLine();
            String bookDate = reader.readLine();
            String bookGenres = reader.readLine();
            String bookCharacters = reader.readLine();
            String bookSynopsis = reader.readLine();
            Book newBook = new Book(bookTitle, bookAuthor, bookDate, bookGenres, bookCharacters, bookSynopsis);
            bookDAO.insertBook(newBook);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("text/plain");
            response.getWriter().write(newBook.toString());
        } else {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported media type or format");
        }
    }



    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        String data = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);
        Book book = gson.fromJson(data, Book.class);
        System.out.println(data);
        bookDAO.updateBook(book);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String paramId = request.getParameter("id");
        bookDAO.deleteBook(Integer.parseInt(paramId));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String paramId = request.getParameter("id");
        String paramTitle = request.getParameter("title");
        String responseString;

        if (paramId == null && paramTitle == null) {
            ArrayList<Book> allBooks = bookDAO.getAllBooks();
            responseString = gson.toJson(allBooks);
        } else if (paramId != null) {
            int reqId = Integer.parseInt(paramId);
            Book reqBook = bookDAO.getBookByID(reqId);
            if (reqBook != null) {
                responseString = gson.toJson(reqBook);
            } else {
                responseString = "No book found with ID " + reqId;
            }
        } else {
            List<Book> booksByTitle = bookDAO.getBooksByTitle(paramTitle);
            if (booksByTitle.size() > 0) {
                responseString = gson.toJson(booksByTitle);
            } else {
                responseString = "No books found with title " + paramTitle;
            }
        }

        String format = request.getParameter("format");

        if ("xml".equalsIgnoreCase(format)) {
            response.setContentType("application/xml");
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = null;
            try {
                jaxbContext = JAXBContext.newInstance(BookList.class);
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            Marshaller marshaller = null;
            try {
                marshaller = jaxbContext.createMarshaller();
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
            try {
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            } catch (PropertyException e) {
                throw new RuntimeException(e);
            }
            if (paramId == null && paramTitle == null) {
                try {
                    marshaller.marshal(new ArrayList<Book>(bookDAO.getAllBooks()), writer);
                } catch (JAXBException e) {
                    throw new RuntimeException(e);
                }
            } else if (paramId != null) {
                Book book = bookDAO.getBookByID(Integer.parseInt(paramId));
                if (book != null) {
                    try {
                        marshaller.marshal(book, writer);
                    } catch (JAXBException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    writer.write("No book found with ID " + paramId);
                }
            } else {
                List<Book> booksByTitle = bookDAO.getBooksByTitle(paramTitle);
                if (booksByTitle.size() > 0) {
                    try {
                        marshaller.marshal(new BookList(booksByTitle), writer);
                    } catch (JAXBException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    writer.write("No books found with title " + paramTitle);
                }
            }
            response.getWriter().write(writer.toString());
        } else if ("text".equalsIgnoreCase(format)) {
            response.setContentType("text/plain");
            response.getWriter().write(responseString);
        } else {
            response.setContentType("application/json");
            PrintWriter out = response.getWriter();
            out.write(responseString);
        }
    }
    @Override
    public void destroy() {
        bookDAO.closeConnection();
    }
}