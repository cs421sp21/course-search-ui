import kong.unirest.GenericType;
import kong.unirest.Unirest;
import model.Course;
import spark.ModelAndView;
import spark.template.handlebars.HandlebarsTemplateEngine;

import java.util.Map;
import java.util.Set;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.staticFiles;

public class WebServer {
  public static void main(String[] args) {
    final String KEY = System.getenv("SIS_API_KEY");
    Unirest.config().defaultBaseUrl("https://sis.jhu.edu/api");

    port(getHerokuAssignedPort());
    staticFiles.location("/public");

    get("/", (req, res) -> {
      return new ModelAndView(null, "index.hbs");
    }, new HandlebarsTemplateEngine());

    post("/search", (req, res) -> {
      String query = req.queryParams("query");
      res.redirect("/search?query=" + query);
      return null;
    }, new HandlebarsTemplateEngine());

    get("/search", (req, res) -> {
      String query = req.queryParams("query");
      Set<Course> courses = Unirest.get("/classes")
          .queryString("Key", KEY)
          .queryString("CourseTitle", query)
          .asObject(new GenericType<Set<Course>>() {})
          .getBody();
      Map<String, Object> model = Map.of("query", query, "courses", courses);
      return new ModelAndView(model, "search.hbs");
    }, new HandlebarsTemplateEngine());
  }

  private static int getHerokuAssignedPort() {
    // Heroku stores port number as an environment variable
    String herokuPort = System.getenv("PORT");
    if (herokuPort != null) {
      return Integer.parseInt(herokuPort);
    }
    //return default port if heroku-port isn't set (i.e. on localhost)
    return 4567;
  }
}