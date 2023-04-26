import React, { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axios from "axios";
import './EditBook.css';

function EditBook() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [book, setBook] = useState({
        id: "",
        title: "",
        author: "",
        date: "",
        genres: "",
        characters: "",
        synopsis: "",
    });

    useEffect(() => {
        axios
            .get(`http://localhost:8080/BookRestApi/bookapi?id=${id}`)
            .then((response) => {
                setBook(response.data);
            })
            .catch((error) => {
                console.error(error);
            });
    }, [id]);

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setBook((prevBook) => ({
            ...prevBook,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        try {
            await axios.put(
                `http://localhost:8080/BookRestApi/bookapi?id=${book.id}`,
                book,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Accept: "application/json",
                    },
                }
            );
            navigate("/");
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div className="edit-book">
            <h1>Edit Book</h1>
            <form onSubmit={handleSubmit}>
                <label htmlFor="title">Title:</label>
                <input
                    type="text"
                    id="title"
                    name="title"
                    value={book.title}
                    onChange={handleInputChange}
                    required
                />

                <label htmlFor="author">Author:</label>
                <input
                    type="text"
                    id="author"
                    name="author"
                    value={book.author}
                    onChange={handleInputChange}
                    required
                />

                <label htmlFor="date">Date:</label>
                <input
                    type="text"
                    id="date"
                    name="date"
                    value={book.date}
                    onChange={handleInputChange}
                    required
                />

                <label htmlFor="genres">Genres:</label>
                <input
                    type="text"
                    id="genres"
                    name="genres"
                    value={book.genres}
                    onChange={handleInputChange}
                    required
                />

                <label htmlFor="characters">Characters:</label>
                <input
                    type="text"
                    id="characters"
                    name="characters"
                    value={book.characters}
                    onChange={handleInputChange}
                />

                <label htmlFor="synopsis">Synopsis:</label>
                <textarea
                    id="synopsis"
                    name="synopsis"
                    value={book.synopsis}
                    onChange={handleInputChange}
                    rows="5"
                    cols="50"
                    required
                ></textarea>

                <button type="submit">Save</button>
            </form>
        </div>
    );
}

export default EditBook;
