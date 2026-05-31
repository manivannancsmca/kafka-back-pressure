import http from 'k6/http';

export default function () {

    http.post(
        'http://localhost:8080/orders',
        JSON.stringify({
            eventId: crypto.randomUUID(),
            orderId: crypto.randomUUID(),
            product: 'Laptop - ' + Math.random().toString(36).slice(5),
            quantity: 1
        }),
        {
            headers: {
                'Content-Type': 'application/json'
            }
        }
    );
}