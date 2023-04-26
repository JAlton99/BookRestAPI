import { useState } from 'react';
import { Link } from 'react-router-dom';
import axios from 'axios';
import './Navbar.css';

function Navbar() {
    const [query, setQuery] = useState('');
    const [searchResults, setSearchResults] = useState([]);

    const handleChange = (value) => {
        setQuery(value);

        if (value !== "") {
            axios.get(`http://localhost:8080/BookRestApi/bookapi`, {
                params: {
                    title: value
                }
            })
                .then((response) => {
                    setSearchResults(response.data);
                })
                .catch((error) => {
                    console.error(error);
                });
        } else {
            setSearchResults([]);
        }
    };

    const handleLinkClick = () => {
        setQuery('');
        setSearchResults([]);
    };

    return (
        <nav>
            <ul>
                <Link onClick={handleLinkClick} to="/">
                    <h1>Bookends</h1>
                </Link>
                <li className="search-container">
                    <label htmlFor="search">Search:</label>
                    <input
                        type="text"
                        id="search"
                        value={query}
                        onChange={(event) => handleChange(event.target.value)}
                        placeholder="Search Books by Title"
                    />
                    {query.length > 0 && (
                        <ul className="search-results">
                            {searchResults.filter(book => book).slice(0, 5).map((book) => (
                                <li key={book.id}>
                                    <Link onClick={handleLinkClick} to={`/InfoBook/${book.id}`}>{book.title}</Link>
                                </li>
                            ))}
                        </ul>
                    )}
                </li>
                <li>
                    <Link onClick={handleLinkClick} to="/AddBook">Add Book</Link>
                </li>
            </ul>
        </nav>
    );
}

export default Navbar;
