const axios = require('axios');

for (let i = 0; i < 1000; i++) {
  axios.get('http://example.com')
    .then(response => console.log(response.data))
    .catch(error => console.error(error));
}