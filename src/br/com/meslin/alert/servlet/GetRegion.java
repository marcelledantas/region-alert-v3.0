package br.com.meslin.alert.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.openstreetmap.gui.jmapviewer.Coordinate;

import br.com.meslin.alert.model.Region;
import br.com.meslin.alert.util.StaticLibrary;

/**
 * Servlet implementation class GetRegion
 */
@WebServlet(
		description = "Read the file with region numbers and filenames and returns a JSON structure",
		urlPatterns = { "/GetRegion" }, 
		initParams = {
				@WebInitParam(name = "workDir", value = "/media/meslin/643CA9553CA92352/Users/meslin/Google Drive/workspace-desktop-ubuntu/AlertCity/", description = "Working directory"),
				@WebInitParam(name = "groupDescriptionFilename", value = "Bairros/RioDeJaneiro.lista", description = "Group description filename")
		})
public class GetRegion extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private List<Region> regionList;

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String workdir = getServletConfig().getInitParameter("workDir");
		String filename = getServletConfig().getInitParameter("groupDescriptionFilename");
		this.regionList = StaticLibrary.readFilenamesFile(workdir + filename);
		JSONObject jsonObject = new JSONObject();
		for(Region region : regionList) {
			// computes the center of the region because InterSCity needs a coordinate to create a resource
			region.setPoints(StaticLibrary.readRegion(workdir + region.getFilename()));
			double lat =0, lon =0;
			for(Coordinate point : region.getPoints()) {
				lat += point.getLat();
				lon += point.getLon();
			}
			lat /= region.getPoints().size();
			lon /= region.getPoints().size();

			JSONObject jsonRegion = new JSONObject();
			jsonRegion.put("number", region.getNumber());
			jsonRegion.put("name", region.getFilename());
			jsonRegion.put("lat", lat);
			jsonRegion.put("lon", lon);
			jsonObject.accumulate("regions", jsonRegion);
		}
		
		PrintWriter out = response.getWriter();
		response.setContentType("text/plain");
		out.println(jsonObject.toString(4));
	}
}
