import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './InfoBook.css';


function InfoBook() {
    const [book, setBook] = useState({});
    const { id } = useParams();

    useEffect(() => {
        axios.get(`http://localhost:8080/BookRestApi/bookapi?id=${id}`)
            .then(response => {
                setBook(response.data);
            })
            .catch(error => {
                console.error(error);
            });
    }, [id]);

    return (
        <div className="info-book">
            <h1>{book.title}</h1>
            <p>Author: {book.author}</p>
            <p>Date: {book.date}</p>
            <p>Genres: {book.genres}</p>
            <p>Characters: {book.characters}</p>
            <p>Synopsis: {book.synopsis}</p>
        </div>
    );
}

export default InfoBook;
