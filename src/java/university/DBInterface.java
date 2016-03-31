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
import java.sql.SQLException;
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

    private static final String url = "jdbc:postgresql://localhost/sample_db";
    private final String user = "postgres";
    private final String password = "admin";
    

 public Connection connectDB() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL server successfully.");
        } catch (SQLException e) {
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

  public List GetBooks() {

      List<String> list = new ArrayList<String>();

      try {

//          Class.forName("com.mysql.jdbc.Driver").newInstance();
          Connection con = connectDB();

          Statement stmt = con.createStatement();

          ResultSet result = stmt.executeQuery("SELECT * FROM books");
          
          while(result.next())
          {
        	  System.out.println("getBooks(): ID: " + result.getString("id"));
             list.add(result.getString("id"));
          } 

          con.close();

      } catch (Exception ex) {
          Logger.getLogger(Controller.class.getName()).log( 
                           Level.SEVERE, null, ex);
      }
          return list;
  }
    
}
