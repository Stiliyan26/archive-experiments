import { Kafka } from 'kafkajs';

const kafkaClient = new Kafka({
    clientId: process.env.KAFKA_CLIENT_ID,
    brokers: process.env.KAFKA_BROCKERS.split(',') 
});

async function run() {
    const consumer = kafkaClient.consumer({ groupId: process.env.CONSUMER_GROUP_ID });

    console.log('Connecting consumer...');
    await consumer.connect();
    console.log('Consumer connected.');

    await consumer.subscribe({ 
        fromBeginning: true, 
        topic: process.env.KAFKA_TOPIC 
    });

    await consumer.run({
        eachMessage: async ({ topic, partition, message}) => {
            const value = message.value.toString();
            const parsed = JSON.parse(value);
            console.log(`${partition}: Partition: Received message: `, parsed);
        }
    });
}

run().catch(console.error);