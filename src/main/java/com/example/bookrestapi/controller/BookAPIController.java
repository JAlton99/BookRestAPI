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

        response.setContentType("text/json");
        PrintWriter out = response.getWriter();
        out.write(responseString);
    }

    @Override
    public void destroy() {
        bookDAO.closeConnection();
    }
}