import java.util.Map;

// todo - use final pretty much everywhere in thsi class
public final class Metadata {
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
        this.photo_path = (String) data.get("photo_path");
        this.location = (String) data.get("location");
        this.season = (String) data.get("season");
        this.subject = (String) data.get("subject");
        this.keyword1 = (String) data.get("keyword1");
        this.keyword2 = (String) data.get("keyword2");
        this.keyword3 = (String) data.get("keyword3");
        this.keyword4 = (String) data.get("keyword4");
        this.keyword5 = (String) data.get("keyword5");
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
