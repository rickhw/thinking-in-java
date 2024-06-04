import http from 'k6/http';
import { check, sleep } from 'k6';

export default function () {
    const requests = [
        { url: 'http://localhost:8092/operate?value=-8'},
        { url: 'http://localhost:8092/operate?value=8'},
        // { url: 'http://localhost:8092/operate?value=-3'},
        // { url: 'http://localhost:8092/operate?value=3'},
    ];

    const responses = http.batch(requests.map(req => ({
        method: 'GET',
        url: req.url,
        params: req.params
    })));

    responses.forEach((res, index) => {
        check(res, {
            'status is 200': (r) => r.status === 200,
        });
    });

    sleep(1);
}
