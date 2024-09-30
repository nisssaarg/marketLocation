public class UniquePhotoTest {

    public static void main(String[] args) throws InterruptedException {
        final String TEST_HASH = "testHash";
        final String TEST_PATH = "testPath";
        final String TEST_PATH2 = "testPath2";
        final String TEST_ID = "1";

        uniquePhoto instance = uniquePhoto.getInstance();

        Runnable uploadTask = () -> {
            DuplicateRecord record = new DuplicateRecord(TEST_ID, TEST_HASH, TEST_PATH);
            instance.addToMap(record);
            
        };

        Runnable uploadTask2 = () -> {
            DuplicateRecord record = new DuplicateRecord(TEST_ID, TEST_HASH, TEST_PATH2);
            instance.addToMap(record);
            
        };

        // Create and start two threads
        Thread thread1 = new Thread(uploadTask);
        Thread thread2 = new Thread(uploadTask2);
        
        thread1.start();
        thread2.start();

        // Wait for both threads to finish
        thread1.join();
        thread2.join();

        // Check the result
        DuplicateRecord result = instance.findPhoto(TEST_HASH);
        if (result != null) {
            System.out.println("Record found in the map:");
            System.out.println("HashId: " + result.getHashId());
            System.out.println("Hash: " + result.getHash());
            System.out.println("Path: " + result.getPhotoPath());
        } else {
            System.out.println("Record not found in the map.");
        }
        System.out.println();
    }
}