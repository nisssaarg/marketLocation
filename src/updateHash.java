import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.logging.Logger;

public class updateHash {

    private static final String SELECT_FROM_HASH = "SELECT * FROM hash";
    private static final Logger logger = Logger.getLogger(updateHash.class.getName());

    public static HashMap<String, HashObject> getAllHashes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        HashMap<String,HashObject> map = new HashMap<>();

        try{
            conn = DatabasePool.getConnection();
            pstmt = conn.prepareStatement(SELECT_FROM_HASH);

            rs = pstmt.executeQuery();

            while(rs.next()){
                int hash_id = rs.getInt(1);
                String hash = rs.getString(2);
                String path = rs.getString(3);

                map.put(hash, new HashObject(path, hash_id));
                logger.info("Hash updated for: " + hash_id);
            }
        }catch(SQLException e){
            logger.info("FAILED SQL QUERY");
        }finally{
            closeResources(rs,pstmt,conn);
        }


        logger.info("Number of records updated :" + map.size());
        return map;
    }
    
    private static void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                logger.severe("Failed to close result set" + e.getMessage());
            }
        }
        
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                logger.severe("Failed to close Prepared Statemnt " + e.getMessage());
            }
        }
        if (conn != null) {
            logger.info("Connection released");
            DatabasePool.releaseConnection(conn);
        }
    }

    public static void main(String[] args){
        HashMap<String,HashObject> map = getAllHashes();
        System.out.println(map.toString());
    }
}
