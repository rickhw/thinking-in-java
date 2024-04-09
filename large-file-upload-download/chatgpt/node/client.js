const http = require('http');
const fs = require('fs');

const fileStream = fs.createReadStream('/Users/rickhwang/Repos/rickhwang/github/java-lab/large-file-upload/node/346811976_786838622803063_3390886969870428845_n.jpg');

const options = {
    hostname: 'localhost',
    port: 3001,
    path: '/',
    method: 'POST',
    headers: {
        'Content-Type': 'application/octet-stream', // 指定为二进制流
        'Transfer-Encoding': 'chunked' // 启用 Chunked Transfer Encoding
    }
};

const req = http.request(options, (res) => {
    console.log(`statusCode: ${res.statusCode}`);
    
    res.on('data', (chunk) => {
        console.log('sent data');
        console.log(chunk.toString());
    });

    res.on('end', () => {
        console.log('No more data');
    });
});

// 将文件流通过Chunked Transfer Encoding发送给服务器
fileStream.pipe(req);

req.on('error', (error) => {
    console.error(error);
});

req.end();
