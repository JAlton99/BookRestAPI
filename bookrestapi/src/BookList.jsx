import React, { useEffect, useState } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";
import "./BookList.css";

function BookList() {
    const navigate = useNavigate();
    const [books, setBooks] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [currentPage, setCurrentPage] = useState(1);
    const booksPerPage = 50;
    const [format, setFormat] = useState("JSON");

    useEffect(() => {
        axios
            .get("http://localhost:8080/BookRestApi/bookapi", {
                headers: { Accept: "application/json" },
            })
            .then((response) => {
                setBooks(response.data);
            })
            .catch((error) => {
                console.error(error);
            });
    }, []);

    const indexOfLastBook = currentPage * booksPerPage;
    const indexOfFirstBook = indexOfLastBook - booksPerPage;
    const filteredBooks = books.filter((book) =>
        book.title.toLowerCase().includes(searchQuery.toLowerCase())
    );
    const currentBooks = filteredBooks.slice(indexOfFirstBook, indexOfLastBook);
    const handleFormatChange = (event) => {
        setFormat(event.target.value);
    };

    const convertToXML = (books) => {
        const xml = books.map((book) => `
            <book>
                <title>${book.title}</title>
                <author>${book.author}</author>
                <date>${book.date}</date>
            </book>
        `).join('');

        return `<books>${xml}</books>`;
    };

    const convertToText = (books) => {
        return books.map((book) => `${book.title} by ${book.author} (${book.date})`).join('\n');
    };

    const handleSearchChange = (event) => {
        setSearchQuery(event.target.value);
    };

    const handleDelete = (id) => {
        console.log(`Deleting book with ID ${id}`);
        axios
            .delete(`http://localhost:8080/BookRestApi/bookapi?id=${id}`)
            .then((response) => {
                setBooks(books.filter((book) => book.id !== id));
            })
            .catch((error) => {
                console.error(error);
            });
    };

    const handleEdit = (id) => {
        navigate(`/edit/${id}`);
    };

    const prevPage = () => {
        if (currentPage > 1) {
            setCurrentPage(currentPage - 1);
            scrollToTop();
        }
    };

    const nextPage = () => {
        if (currentPage < Math.ceil(books.length / booksPerPage)) {
            setCurrentPage(currentPage + 1);
            scrollToTop();
        }
    };

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: "smooth" });
    };

    return (
        <div className="book-list">
            <h1>Book List</h1>
            <label htmlFor="search">Search:</label>
            <input
                type="text"
                id="search"
                value={searchQuery}
                onChange={handleSearchChange}
                placeholder="Filter Books by Title"
            />
            <label htmlFor="format">Display format:</label>
            <select id="format" value={format} onChange={handleFormatChange}>
                <option value="JSON">JSON</option>
                <option value="XML">XML</option>
                <option value="Text">Text</option>
            </select>

            {/* Render the book list based on the selected format */}
            {format === "JSON" && (
                <ul>
                    {currentBooks.map((book) => (
                        <li key={book.id} style={{ justifyContent: "space-between" }}>
                            <div>
                                <h2>
                                    <Link to={`/InfoBook/${book.id}`}>{book.title}</Link>
                                </h2>
                                <p>{book.author}</p>
                                <p>{book.date}</p>
                            </div>
                            <div className="button-container">
                                <Link
                                    className="button"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        handleEdit(book.id);
                                    }}
                                >
                                    Edit
                                </Link>
                                <Link
                                    className="button"
                                    onClick={(e) => {
                                        e.preventDefault();
                                        handleDelete(book.id);
                                    }}
                                >
                                    Delete
                                </Link>
                            </div>
                        </li>
                    ))}
                </ul>
            )}
            {format === "XML" && (
                <pre>{convertToXML(currentBooks)}</pre>
            )}
            {format === "Text" && (
                <pre>{convertToText(currentBooks)}</pre>
            )}

            <button onClick={prevPage} disabled={currentPage === 1}>
                Previous
            </button>
            <span>
                {" "}
                Page {currentPage} of {Math.ceil(books.length / booksPerPage)}{" "}
            </span>
            <button
                onClick={nextPage}
                disabled={currentPage === Math.ceil(books.length / booksPerPage)}
            >
                Next
            </button>
        </div>
    );
}

export default BookList;