/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package university;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Davide
 */
public class WikiHandler {
    private static final String TAG = WikiHandler.class.getSimpleName() + ": ";
    
    private ThreadPoolExecutor executor;
    
    private DBInterface dbInterface;
    
    public WikiHandler(DBInterface dbInterface)
    {
        this.dbInterface = dbInterface;
        
        int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(NUMBER_OF_CORES);
    }
    
    public void updateWiki()
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // clear table
        // query su tutte le table per tag not null prendendo osm_id e tags
        // scorro con executor e jsoup
        System.out.println(TAG + "updateWiki");
        
        
        // clear wiki_data table
        try
        {
            Connection conn = dbInterface.connectDB();
        
            if(conn == null)
            {
                System.out.println(TAG + "DB connection null");
                return;
            }
            String dropTable = "DROP TABLE public.wiki_data;";
            String createTable = "CREATE TABLE public.wiki_data\n" +
            "(\n" +
            "id SERIAL NOT NULL,\n" +
            "osm_id numeric NOT NULL,\n" +
            "wiki_text character varying(2000),\n" +
            "\n" +
            "CONSTRAINT wiki_data_pkey PRIMARY KEY(id)\n" +
            ")";
            PreparedStatement ps = conn.prepareStatement(dropTable);
          
            ps.executeUpdate();
            
            ps = conn.prepareStatement(createTable);
          
            ps.executeUpdate();
            conn.close();
            System.out.println(TAG + "reset table");
        }
        catch(Exception e)
        {
            System.out.println(TAG + "1");
            System.out.println(TAG + e.getMessage());
        }
        
        // query all tables for tags, get osm_id and tags columns of tag != NULL
        try
        {
            String query = "SELECT osm_id, tags\n" +
                "FROM rome_italy_osm_point\n" +
                "WHERE \n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "tags IS NOT NULL\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT osm_id, tags\n" +
                "FROM rome_italy_osm_line\n" +
                "WHERE \n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "tags IS NOT NULL\n" +
                "\n" +
                "UNION\n" +
                "\n" +
                "SELECT osm_id, tags\n" +
                "FROM rome_italy_osm_polygon\n" +
                "WHERE \n" +
                "historic IS NOT NULL AND\n" +
                "name IS NOT NULL AND\n" +
                "tags IS NOT NULL";
            Connection conn = dbInterface.connectDB();
        
            if(conn == null)
            {
                System.out.println(TAG + "DB connection null");
                return;
            }
            
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery(query);
            
            while(result.next())
              {
                System.out.println(TAG + "new row");
                PointInfo data = new PointInfo();

                String osm_id = result.getString("osm_id");
                String tag = result.getString("tags");

                String lang = null;
                String wikiURL = null;
                boolean wikiPresent = false;
                if(tag.contains("wikipedia"))
                {
                    String[] tokens = tag.split("\", \"");
                    for(String token : tokens)
                    {
                        if(token.contains("wikipedia"))
                        {
                            System.out.println(TAG + "token: " + token);
                            String[] wikiString = token.split("\"=>\"");
                            // found an empty wiki link
                            if(wikiString.length < 2)
                                break;

                            String[] multipleWikiLinks = wikiString[1].split(";");
                            if(multipleWikiLinks.length != 1)
                            {
                                // multiple links found, get only first one
                                wikiString[1] = multipleWikiLinks[0];
                            }
                            String[] temp = wikiString[1].split(":");

                            lang = temp[0];
                            if(!lang.equals("it") && !lang.equals("en"))
                            {
                                // only italian and english accepted
                                break;
                            }
                            if(temp[1].endsWith("\""))
                                temp[1] = temp[1].substring(0, temp[1].length() - 1);
                            wikiURL = temp[1].replaceAll(" ", "_");
                            System.out.println(TAG + lang);
                            System.out.println(TAG + wikiURL);
                            wikiPresent = true;
                            break;
                        }
                    } // inner loop
                }
                
                if(wikiPresent)
                {
                    // connect to jsoup
                    final String langStr = lang;
                    final String wikiURLStr = wikiURL;
                    final String osm_idStr = osm_id;
                    System.out.println(TAG + "Starting runnable " + wikiURLStr);
                    try {
                        String url = "http://" + langStr + ".wikipedia.org/wiki/" + wikiURLStr;
                                System.out.println(TAG + "Connection to wiki " + url);
                                Document doc = Jsoup.connect(url).get();

                                Elements paragraphs = doc.select("#mw-content-text > p");

                                Element firstParagraph = paragraphs.first();
                                String firstParagraphText = firstParagraph.text();
                                // sometimes the coordinates are taken as first paragraph, check for that
                                if(firstParagraphText.startsWith("Coord"))
                                {
                                    firstParagraph = paragraphs.get(1);
                                    firstParagraphText = firstParagraph.text();
                                }

                                System.out.println(TAG + firstParagraphText);
                                
                                // Insert text with osm_id to DB
                                firstParagraphText = firstParagraphText.replaceAll("'", "''");
                                String insert = "INSERT INTO public.wiki_data(osm_id, wiki_text) VALUES (" + osm_idStr + ", '" + firstParagraphText + "');";
                                System.out.println(TAG + insert);
                                Connection con = dbInterface.connectDB();
                                if(con == null)
                                {
                                    System.out.println(TAG + "connection null");
                                }
                                PreparedStatement ps = con.prepareStatement(insert);

                                ps.executeUpdate();
                                
                                con.close();
                                
                            } catch (Exception e) {
                                e.printStackTrace();
                                System.out.println(TAG + "2");
                            }
                }
            }

            

            conn.close();
        }
        catch(Exception e)
        {
            System.out.println(TAG + e.getMessage());
            System.out.println(TAG + "3");
        }
            }
        });
        
    }
}
