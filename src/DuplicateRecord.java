public class DuplicateRecord {
    private String hashId;
    private String hash;
    private String photoPath;

    public DuplicateRecord(String hashId, String hash, String photoPath) {
        this.hashId = hashId;
        this.hash = hash;
        this.photoPath = photoPath;
    }

    public String getHashId() {
        return hashId;
    }

    public String getHash() {
        return hash;
    }

    public String getPhotoPath() {
        return photoPath;
    }
}
