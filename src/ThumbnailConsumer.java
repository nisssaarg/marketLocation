public class ThumbnailConsumer {
    Queue kafkaQueue = new Queue();
    public ThumbnailConsumer() {
    }

    public void consume()  {
        while (true) {
            String result = kafkaQueue.dequeue();
            System.out.println("Received message: " + result);  
        }
    }

    public static void main(String[] args){
        ThumbnailConsumer tbc = new ThumbnailConsumer();
        tbc.consume();
    }
}
