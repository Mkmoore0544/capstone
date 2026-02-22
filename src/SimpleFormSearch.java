import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/SimpleFormSearch")
public class SimpleFormSearch extends HttpServlet {
   private static final long serialVersionUID = 1L;

   public SimpleFormSearch() {
      super();
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {

      String keyword = request.getParameter("keyword");
      if (keyword == null) keyword = "";
      keyword = keyword.trim();

      search(keyword, response);
   }

   void search(String keyword, HttpServletResponse response) throws IOException {
      if (keyword == null) keyword = "";
      keyword = keyword.trim();

      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      String title = "Database Result";
      String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";

      out.println(docType +
            "<html>\n" +
            "<head><title>" + title + "</title></head>\n" +
            "<body bgcolor=\"#f0f0f0\">\n" +
            "<h1 align=\"center\">" + title + "</h1>\n");

      Connection connection = null;
      PreparedStatement preparedStatement = null;
      ResultSet rs = null;

      try {
         // Make sure DBConnection sets DBConnection.connection
         DBConnection.getDBConnection();
         connection = DBConnection.connection;

         if (connection == null) {
            out.println("<p><b>Error:</b> Database connection is null.</p>");
            out.println("<a href=/webproject/simpleFormSearch.html>Back</a>");
            out.println("</body></html>");
            return;
         }

         String selectSQL;
         if (keyword.isEmpty()) {
            selectSQL = "SELECT id, MYUSER, EMAIL, PHONE FROM myTable";
            preparedStatement = connection.prepareStatement(selectSQL);
         } else {
            selectSQL = "SELECT id, MYUSER, EMAIL, PHONE FROM myTable WHERE MYUSER LIKE ?";
            preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, keyword + "%"); // prefix match
            // If you want "contains" search instead, use:
            // preparedStatement.setString(1, "%" + keyword + "%");
         }

         rs = preparedStatement.executeQuery();

         boolean foundAny = false;

         while (rs.next()) {
            int id = rs.getInt("id");

            String userName = rs.getString("MYUSER");
            String email = rs.getString("EMAIL");
            String phone = rs.getString("PHONE");

            userName = (userName == null) ? "" : userName.trim();
            email    = (email    == null) ? "" : email.trim();
            phone    = (phone    == null) ? "" : phone.trim();

            // If keyword is empty, show all. If not empty, SQL already filtered
            // but this keeps it extra safe.
            if (keyword.isEmpty() || userName.contains(keyword)) {
               foundAny = true;
               out.println("ID: " + id + ", ");
               out.println("User: " + userName + ", ");
               out.println("Email: " + email + ", ");
               out.println("Phone: " + phone + "<br>");
            }
         }

         if (!foundAny) {
            out.println("<p>No results found.</p>");
         }

         out.println("<br><a href=/webproject/simpleFormSearch.html>Search Data</a><br>");
         out.println("</body></html>");

      } catch (SQLException se) {
         // Print the error both to the server logs and to the browser (helpful while debugging)
         se.printStackTrace();
         out.println("<p><b>SQL Error:</b> " + escapeHtml(se.getMessage()) + "</p>");
         out.println("<a href=/webproject/simpleFormSearch.html>Back</a>");
         out.println("</body></html>");
      } catch (Exception e) {
         e.printStackTrace();
         out.println("<p><b>Error:</b> " + escapeHtml(e.getMessage()) + "</p>");
         out.println("<a href=/webproject/simpleFormSearch.html>Back</a>");
         out.println("</body></html>");
      } finally {
         try { if (rs != null) rs.close(); } catch (SQLException ignore) {}
         try { if (preparedStatement != null) preparedStatement.close(); } catch (SQLException ignore) {}
         try { if (connection != null) connection.close(); } catch (SQLException ignore) {}
      }
   }

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
      doGet(request, response);
   }

   // Minimal HTML escaping for error messages
   private static String escapeHtml(String s) {
      if (s == null) return "";
      return s.replace("&", "&amp;")
              .replace("<", "&lt;")
              .replace(">", "&gt;")
              .replace("\"", "&quot;")
              .replace("'", "&#39;");
   }
}

