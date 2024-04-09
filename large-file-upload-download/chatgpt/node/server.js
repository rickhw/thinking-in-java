const http = require('http');
const fs = require('fs');

const server = http.createServer((req, res) => {
    res.writeHead(200, { 'Content-Type': 'text/plain' });
    
    // 使用流进行Chunked Transfer Encoding
    const fileStream = fs.createReadStream('/Users/rickhwang/Repos/rickhwang/github/java-lab/large-file-upload/node/received.dat');

    // 每次读取固定大小的数据块并发送给客户端
    fileStream.on('data', (chunk) => {
        console.log(` received data`)
        res.write(chunk);
    });

    // 所有数据块都发送完毕后结束响应
    fileStream.on('end', () => {
        console.log(` finish received data.`)
        res.end();
    });
});

const port = 3001;
server.listen(port, () => {
    console.log(`Server running at http://localhost:${port}/`);
});
