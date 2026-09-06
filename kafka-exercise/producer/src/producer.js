import { Kafka } from 'kafkajs';
import { v4 } from 'uuid';

const kafkaClient = new Kafka({
    clientId: process.env.KAFKA_CLIENT_ID,
    brokers: process.env.KAFKA_BROCKERS.split(',')
});

async function run() {
    const producer = kafkaClient.producer();

    console.log('Connecting producer...');
    await producer.connect();
    console.log('Producer connected.');

    let counter = 0;

    setInterval(async () => {
        let key = v4();

        const message = {
            timestamp: Date.now(),
            temperature: (Math.random() * 20).toFixed(2),
            id: counter
        };

        await producer.send({
            topic: process.env.KAFKA_TOPIC,
            messages: [{ key, value: JSON.stringify(message) }]
        });
    }, 2000);
}

run().catch(console.error);
