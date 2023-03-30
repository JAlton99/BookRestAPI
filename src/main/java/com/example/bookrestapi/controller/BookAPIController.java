package com.example.bookrestapi.controller;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.example.bookrestapi.dao.BookDAO;
import com.example.bookrestapi.model.Book;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;


@WebServlet(name = "bookapi", value = "/bookapi")
public class BookAPIController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private BookDAO bookDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        bookDAO = new BookDAO();
        gson = new GsonBuilder().setPrettyPrinting().create();

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        Book newBook = gson.fromJson(reader, Book.class);
        bookDAO.insertBook(newBook);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BufferedReader reader = request.getReader();
        Book book = gson.fromJson(reader, Book.class);
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
                jaxbContext = JAXBContext.newInstance(Book.class, ArrayList.class);
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
                        marshaller.marshal(new ArrayList<Book>(booksByTitle), writer);
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