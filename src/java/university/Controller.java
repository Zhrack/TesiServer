package university;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author Davide
 */
@WebServlet("/Controller")
public class Controller extends HttpServlet{
    private static final long serialVersionUID = 1L;
    
    private DBInterface dBInterface;
    private WikiHandler wikiHandler;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();
        
        dBInterface = new DBInterface();
        wikiHandler = new WikiHandler(dBInterface);
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("servlet entered");
		String option = request.getParameter("option");
		System.out.println("chosen option: " + option);
		
		if(option == null) {
			request.getRequestDispatcher("/Home.html").forward(request, response);
		}
		 
		if("getPointList".equals(option))
		{
			List list = null;
			String latitude = request.getParameter("latitude");
                        String longitude = request.getParameter("longitude");
                        String radius = request.getParameter("radius");
                        
                        list = dBInterface.GetPointList(latitude, longitude, radius);
                        
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("list", list);
			} catch (Exception e) {
				e.printStackTrace();
			}

			response.setContentType("application/json");
		    response.getWriter().write(jsonObj.toString());
		}
                else if("updateWiki".equals(option))
                {
                    wikiHandler.updateWiki();
                }
		else{
			System.out.println("Error option: " + option + " not found\n");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}
