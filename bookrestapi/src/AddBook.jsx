import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './AddBook.css';


function AddBook() {
    const [book, setBook] = useState({
        title: '',
        author: '',
        date: '',
        genres: '',
        characters:'',
        synopsis: ''
    });

    const navigate = useNavigate();

    const handleChange = event => {
        const { name, value } = event.target;
        setBook(prevState => ({ ...prevState, [name]: value }));
    };

    const handleAddBook = (contentType, accept) => {
        let data;

        if (contentType === 'text/plain') {
            data = `${book.title}\n${book.author}\n${book.date}\n${book.genres}\n${book.characters}\n${book.synopsis}`;
        } else {
            data = book;
        }

        axios
            .post('http://localhost:8080/BookRestApi/bookapi', data, {
                headers: {
                    'Content-Type': contentType,
                    Accept: accept
                }
            })
            .then(response => {
                console.log(response.data);
                navigate('/');
            })
            .catch(error => {
                console.error(error);
            });
    };
    const handleSubmitJson = event => {
        event.preventDefault();
        handleAddBook('application/json', 'application/json');
    };

    const handleSubmitXml = event => {
        event.preventDefault();
        handleAddBook('application/xml', 'application/xml');
    };

    const handleSubmitText = event => {
        event.preventDefault();
        handleAddBook('text/plain', 'text/plain');
    };

    return (
        <div className="add-book">
            <h1>Add Book</h1>
            <form>
                <div>
                    <label htmlFor="title">Title:</label>
                    <input
                        type="text"
                        id="title"
                        name="title"
                        value={book.title}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="author">Author:</label>
                    <input
                        type="text"
                        id="author"
                        name="author"
                        value={book.author}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="date">Date:</label>
                    <input
                        type="text"
                        id="date"
                        name="date"
                        value={book.date}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="genres">Genres:</label>
                    <input
                        type="text"
                        id="genres"
                        name="genres"
                        value={book.genres}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="characters">Characters:</label>
                    <input
                        type="text"
                        id="characters"
                        name="characters"
                        value={book.characters}
                        onChange={handleChange}
                        required
                    />
                </div>
                <div>
                    <label htmlFor="synopsis">Synopsis:</label>
                    <textarea
                        id="synopsis"
                        name="synopsis"
                        value={book.synopsis}
                        onChange={handleChange}
                        required
                    />
                </div>
                <button onClick={handleSubmitJson}>Add Book as JSON</button>
                <button onClick={handleSubmitXml}>Add Book as XML</button>
                <button onClick={handleSubmitText}>Add Book as Text</button>
            </form>
        </div>
    );
}
export default AddBook;
