package university;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;
import java.util.List;
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
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Controller() {
        super();
        try{
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		}
		catch(Exception e){
			System.out.println(e.toString());
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		System.out.println("servlet entered");
		String option = request.getParameter("option");
		System.out.println("option scelta: " + option);
		
		if(option == null) {
			request.getRequestDispatcher("/Home.html").forward(request, response);
		}
		 
		if("bookList".equals(option))
		{
			List list = null;
			
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("list", list);
			} catch (Exception e) {
				e.printStackTrace();
			}

			response.setContentType("application/json");
		    response.getWriter().write(jsonObj.toString());
		}
		else{
			System.out.println("non trovato\n");
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
}
