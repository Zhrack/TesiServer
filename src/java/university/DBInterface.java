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

    private static final String url = "jdbc:postgresql://aazw7m4e04vdwx.cqm5ejeb3xmk.us-west-2.rds.amazonaws.com:5432/osm";
//    private static final String url = "jdbc:postgresql://localhost:5432/rome_osm";
    private final String user = "postgres";
    private final String password = "postgres";
//    private final String password = "admin";
    

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
          
          String query = 
                "SELECT name, distance, wiki_text AS wiki,\n" +
                "latitude,\n" +
                "longitude\n" +
                "FROM (SELECT name, ST_X(geom) AS longitude, ST_Y(geom) AS latitude,\n" +
                "ST_Distance(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography) AS distance,\n" +
                "tags,\n" +
                "osm_id\n" +
                "FROM rome_italy_osm_point\n" +
                "WHERE\n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "ST_DWithin(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography, " + radius + ")\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT name, ST_X(ST_Centroid(geom)) AS longitude, ST_Y(ST_Centroid(geom)) AS latitude,\n" +
                "ST_Distance(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography) AS distance,\n" +
                "tags,\n" +
                "osm_id\n" +
                "FROM rome_italy_osm_line\n" +
                "WHERE \n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "ST_DWithin(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography, " + radius + ")\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT\n" +
                "name, \n" +
                "ST_X(ST_StartPoint(ST_ExteriorRing(ST_GeometryN(geom, 1)))) AS longitude, \n" +
                "ST_Y(ST_StartPoint(ST_ExteriorRing(ST_GeometryN(geom, 1)))) AS latitude, \n" +
                "ST_Distance(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography) AS distance,\n" +
                "tags,\n" +
                "osm_id\n" +
                "FROM\n" +
                "rome_italy_osm_polygon\n" +
                "WHERE\n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "ST_DWithin(ST_Centroid(geom)::geography, ST_GeomFromText(" + devicePoint + ")::geography, " + radius + ")\n" +
                ") gis_data, wiki_data  wiki\n" +
                "WHERE (wiki.osm_id = gis_data.osm_id)\n" +
                "ORDER BY distance ASC;";

          ResultSet result = stmt.executeQuery(query);
          
          while(result.next())
          {
            System.out.println("GetPointList(): Name: " + result.getString("name"));
            PointInfo data = new PointInfo();
            
            data.setName(result.getString("name"));
            data.setLatitude(result.getDouble("latitude"));
            data.setLongitude(result.getDouble("longitude"));
            data.setDistance(result.getFloat("distance"));
            data.setWikiText(result.getString("wiki"));
                  
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
