package com.example.bookrestapi.controller;

import java.io.*;
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
    public void init() {
        bookDAO = new BookDAO();
        gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            jaxbContext = JAXBContext.newInstance(BookList.class);


        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");

        try {
            if ("application/json".equalsIgnoreCase(contentType) && "application/json".equalsIgnoreCase(accept)) {
                handleJsonPostRequest(request, response);
            } else if ("application/xml".equalsIgnoreCase(contentType) && "application/xml".equalsIgnoreCase(accept)) {
                handleXmlPostRequest(request, response);
            } else if ("text/plain".equalsIgnoreCase(contentType) && "text/plain".equalsIgnoreCase(accept)) {
                handleTextPostRequest(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported media type or format");
            }
        } catch (Exception e) {
            throw new ServletException("Error processing POST request", e);
        }
    }

    private void handleJsonPostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            Book newBook = gson.fromJson(reader, Book.class);
            bookDAO.insertBook(newBook);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(newBook));
        }
    }

    private void handleXmlPostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Book newBook = (Book) unmarshaller.unmarshal(request.getInputStream());
            bookDAO.insertBook(newBook);
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/xml");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(newBook, response.getOutputStream());
        } catch (JAXBException e) {
            // Log the error and send a response with an appropriate error message
            System.err.println("Error unmarshalling XML: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid XML format");
        } catch (IOException e) {
            System.err.println("Error handling XML request: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing XML request");
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error");
        }
    }

    private void handleTextPostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
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
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String contentType = request.getHeader("Content-Type");
        String accept = request.getHeader("Accept");

        try {
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID parameter is required");
                return;
            }
            int id = Integer.parseInt(idParam);

            if ("application/json".equalsIgnoreCase(contentType) && "application/json".equalsIgnoreCase(accept)) {
                handleJsonPutRequest(request, response, id);
            } else if ("application/xml".equalsIgnoreCase(contentType) && "application/xml".equalsIgnoreCase(accept)) {
                handleXmlPutRequest(request, response, id);
            } else if ("text/plain".equalsIgnoreCase(contentType) && "text/plain".equalsIgnoreCase(accept)) {
                handleTextPutRequest(request, response, id);
            } else {
                response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported media type or format");
            }
        } catch (Exception e) {
            throw new ServletException("Error processing PUT request", e);
        }
    }


    private void handleJsonPutRequest(HttpServletRequest request, HttpServletResponse response, int id) throws IOException {
        try (BufferedReader reader = request.getReader()) {
            String data = reader.lines().reduce("", (accumulator, actual) -> accumulator + actual);
            Book book = gson.fromJson(data, Book.class);
            book.setId(id); // Set the ID from the parameter
            bookDAO.updateBook(book);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("application/json");
            response.getWriter().write(gson.toJson(book));
        }
    }

    private void handleXmlPutRequest(HttpServletRequest request, HttpServletResponse response, int id) throws JAXBException, IOException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Book book = (Book) unmarshaller.unmarshal(request.getInputStream());
        book.setId(id); // Set the ID from the parameter
        bookDAO.updateBook(book);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/xml");
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(book, response.getOutputStream());
    }

    private void handleTextPutRequest(HttpServletRequest request, HttpServletResponse response, int id) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
            String bookTitle = reader.readLine();
            String bookAuthor = reader.readLine();
            String bookDate = reader.readLine();
            String bookGenres = reader.readLine();
            String bookCharacters = reader.readLine();
            String bookSynopsis = reader.readLine();
            Book book = new Book(bookTitle, bookAuthor, bookDate, bookGenres, bookCharacters, bookSynopsis);
            book.setId(id); // Set the ID from the parameter
            bookDAO.updateBook(book);
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            response.getWriter().write(book.toString());
        }
    }



    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        boolean deleted = bookDAO.deleteBook(id);
        if (deleted) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Book with id " + id + " deleted successfully");
            } catch (IOException e) {
                // Handle the error
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.setContentType("text/plain");
            try {
                response.getWriter().write("Book with id " + id + " not found");
            } catch (IOException e) {
                // Handle the error
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String format = request.getHeader("Accept");

        try {
            if ("application/xml".equalsIgnoreCase(format)) {
                handleXmlGetRequest(request, response);
            } else {
                handleJsonOrTextGetRequest(request, response);
            }
        } catch (Exception e) {
            throw new ServletException("Error processing GET request", e);
        }
    }

    private void handleXmlGetRequest(HttpServletRequest request, HttpServletResponse response) throws JAXBException, IOException {
        String paramId = request.getParameter("id");
        String paramTitle = request.getParameter("title");

        response.setContentType("application/xml");
        StringWriter writer = new StringWriter();
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        if (paramId != null) {
            int reqId = Integer.parseInt(paramId);
            Book reqBook = bookDAO.getBookByID(reqId);
            if (reqBook != null) {
                marshaller.marshal(reqBook, writer);
            } else {
                writer.write("No book found with ID " + reqId);
            }
        } else {
            List<Book> books = paramTitle != null ? bookDAO.getBooksByTitle(paramTitle) : bookDAO.getAllBooks();

            if (books.isEmpty()) {
                writer.write(paramTitle != null ? "No books found with title " + paramTitle : "No books found");
            } else {
                marshaller.marshal(new BookList(books), writer);
            }
        }

        response.getWriter().write(writer.toString());
    }

    private void handleJsonOrTextGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String paramId = request.getParameter("id");
        String paramTitle = request.getParameter("title");
        String format = request.getHeader("Accept");

        String responseString;
        if (paramId != null) {
            int reqId = Integer.parseInt(paramId);
            Book reqBook = bookDAO.getBookByID(reqId);
            responseString = reqBook != null ? gson.toJson(reqBook) : "No book found with ID " + reqId;
        } else {
            List<Book> books = paramTitle != null ? bookDAO.getBooksByTitle(paramTitle) : bookDAO.getAllBooks();
            responseString = gson.toJson(books);
        }

        if ("text/plain".equalsIgnoreCase(format)) {
            response.setContentType("text/plain");
        } else {
            response.setContentType("application/json");
        }

        PrintWriter out = response.getWriter();
        out.write(responseString);
    }


    @Override
    public void destroy() {
        bookDAO.closeConnection();
    }
}