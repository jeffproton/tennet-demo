import { useEffect, useState } from 'react';

export default function App() {
  const [message, setMessage] = useState('Loading...');
  const [error, setError] = useState(null);

  useEffect(() => {
    fetch('/api/greeting')
      .then((res) => {
        if (!res.ok) throw new Error('Failed to fetch greeting');
        return res.json();
      })
      .then((data) => setMessage(data.message))
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div style={{ fontFamily: 'sans-serif', textAlign: 'center', marginTop: '20vh' }}>
      {error ? (
        <p style={{ color: 'red' }}>{error}</p>
      ) : (
        <h1>{message}</h1>
      )}
    </div>
  );
}
