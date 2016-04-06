package university;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Davide
 */
public class DBInterface {

    private static final String url = "jdbc:postgresql://localhost:5432/rome_osm";
    private final String user = "postgres";
    private final String password = "admin";
    

 public Connection connectDB() {
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
 
        return conn;
    }
 
 public void Insert(String nomeImmagine, String titolo, String autore, 
          String isbn, String prezzo, String descr) {
      try {
          String insert = "INSERT INTO books(img, titolo, autore, isbn, prezzo, descr)" +
                  "VALUES (?, ?, ?, ?, ?, ?)";

//          Class.forName("com.mysql.jdbc.Driver").newInstance();
          Connection con = connectDB();
 
          PreparedStatement ps = con.prepareStatement(insert);
          
          ps.setString(1, nomeImmagine + ".jpg");
          ps.executeUpdate();
          con.close();

      } catch (Exception ex) {
          Logger.getLogger(Controller.class.getName()).log(
                           Level.SEVERE, null, ex);
      }
  }

  public List GetPointList(String latitude, String longitude, String radius) {

      List<PointInfo> list = new ArrayList<PointInfo>();

      try {
          Connection con = connectDB();

          Statement stmt = con.createStatement();
          
          String devicePoint = "'POINT(" + longitude + " " + latitude + ")', 4326";

          ResultSet result = stmt.executeQuery(
                "SELECT name, ST_X(geom) AS longitude, ST_Y(geom) AS latitude,\n" +
                "ST_Distance(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography) AS distance,\n" +
                "tags\n" +
                "FROM rome_italy_osm_point\n" +
                "WHERE\n" +
                "(historic IS NOT NULL OR\n" +
                "tourism IS NOT NULL) AND\n" +
                "name IS NOT NULL AND\n" +
                "ST_DWithin(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography, " + radius + ")\n" +
                "ORDER BY distance ASC;");
          
          while(result.next())
          {
            System.out.println("GetPointList(): Name: " + result.getString("name"));
            PointInfo data = new PointInfo();
            
            data.setName(result.getString("name"));
            data.setLatitude(result.getDouble("latitude"));
            data.setLongitude(result.getDouble("longitude"));
            data.setDistance(result.getFloat("distance"));
            data.setWikiText(result.getString("tags"));
                  
            list.add(data);
          } 

          con.close();

      } catch (Exception ex) {
          Logger.getLogger(Controller.class.getName()).log( 
                           Level.SEVERE, null, ex);
      }
          return list;
  }
    
}
