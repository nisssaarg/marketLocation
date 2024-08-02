import java.util.Map;

// todo - use final pretty much everywhere in thsi class
public final class Metadata {
    private static final String KEYWORD52 = "keyword5";
    private static final String KEYWORD32 = "keyword3";
    private static final String KEYWORD42 = "keyword4";
    private static final String KEYWORD22 = "keyword2";
    private static final String KEYWORD12 = "keyword1";
    private static final String SUBJECT2 = "subject";
    private static final String SEASON2 = "season";
    private static final String LOCATION2 = "location";
    private static final String PHOTO_PATH2 = "photo_path";
    private int metadataId;
    private String photo_path;      
    private String location;   
    private String season;     
    private String subject;    
    private String keyword1;   
    private String keyword2;   
    private String keyword3;   
    private String keyword4;   
    private String keyword5;

    // Constructor that accepts a Map
    public Metadata(Map<String, Object> data) {
        //this.metadataId = (int) data.get("metadataId");
        this.photo_path = (String) data.get(PHOTO_PATH2);
        this.location = (String) data.get(LOCATION2);
        this.season = (String) data.get(SEASON2);
        this.subject = (String) data.get(SUBJECT2);
        this.keyword1 = (String) data.get(KEYWORD12);
        this.keyword2 = (String) data.get(KEYWORD22);
        this.keyword3 = (String) data.get(KEYWORD32);
        this.keyword4 = (String) data.get(KEYWORD42);
        this.keyword5 = (String) data.get(KEYWORD52);
    }

    // Getters and Setters
    public int getMetadataId() {
        return metadataId;
    }

    public void setMetadataId(int metadataId) {
        this.metadataId = metadataId;
    }

    public String getPhoto_path() {
        return photo_path;
    }

    public void setPhotoId(String photo_path) {
        this.photo_path = photo_path;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getKeyword1() {
        return keyword1;
    }

    public void setKeyword1(String keyword1) {
        this.keyword1 = keyword1;
    }

    public String getKeyword2() {
        return keyword2;
    }

    public void setKeyword2(String keyword2) {
        this.keyword2 = keyword2;
    }

    public String getKeyword3() {
        return keyword3;
    }

    public void setKeyword3(String keyword3) {
        this.keyword3 = keyword3;
    }

    public String getKeyword4() {
        return keyword4;
    }

    public void setKeyword4(String keyword4) {
        this.keyword4 = keyword4;
    }

    public String getKeyword5() {
        return keyword5;
    }

    public void setKeyword5(String keyword5) {
        this.keyword5 = keyword5;
    }
}
