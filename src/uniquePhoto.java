import java.util.HashMap;

public class uniquePhoto {

    private static final String DUPLICATE_FOUND = "Duplicate found";
    private static uniquePhoto instance; 
    private static HashMap<String, HashObject> map;

    
    private uniquePhoto() {
        map = updateHash.getAllHashes(); 
    }

    
    public static uniquePhoto getInstance() {
        if (instance == null) {
            instance = new uniquePhoto(); 
        }
        return instance;
    }

    public DuplicateRecord findPhoto(String hash) {
        if (map.containsKey(hash)) {
            System.out.println(DUPLICATE_FOUND);
            HashObject hashObject = map.get(hash);
            return new DuplicateRecord(String.valueOf(hashObject.hash_id), hash, hashObject.path);
        }
        return null; 
    }

    public void addToMap(DuplicateRecord duplicateRecord) {
        map.putIfAbsent(duplicateRecord.getHash(),new HashObject(duplicateRecord.getPhotoPath(), Integer.valueOf(duplicateRecord.getHashId())));
        HashObject object = map.get(duplicateRecord.getHash());
        System.out.println("Updated records : " +  object.path );
    }
}
