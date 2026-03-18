const mysql = require('mysql2');

// Create a connection to the database
const connection = mysql.createConnection({
  host: 'tutor-app-db.cpoum26o8mmx.us-east-2.rds.amazonaws.com',   // AWS RDS Endpoint or local DB
  user: 'admin',                      // Database username
  password: 'macewanuniversity',                  // Database password
  database: 'tutor-app-db',                     // The database you're connecting to
  port: 3306                                  // MySQL port (default is 3306)
});

// Open the MySQL connection
connection.connect(err => {
  if (err) {
    console.error('Error connecting to the database:', err);
    return;
  }
  console.log('Successfully connected to the database.');
});

// Example of querying the database
connection.query('SELECT * FROM Users', (error, results) => {
  if (error) {
    console.error('Error executing query:', error);
    return;
  }
  console.log('Results:', results);
});

// Close the connection when you're done
connection.end();
