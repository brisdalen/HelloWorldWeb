package Servlets;

import Classes.*;
import sun.security.util.Password;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@WebServlet(name = "HelloServlet", urlPatterns = {"/HelloServlet"})
public class HelloServlet extends HttpServlet {

    ArrayList<String> users;
    PasswordHashAndCheck passwordHashAndCheck;

    Scanner scanner;

    public void init() {
        users = new ArrayList<>();
        passwordHashAndCheck = new PasswordHashAndCheck();

        scanner = new Scanner(System.in);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        request.setCharacterEncoding(StandardCharsets.ISO_8859_1.toString());

        String navn = request.getParameter("navn");
        String addresse = request.getParameter("adresse");
        //char[] password = request.getParameter("password").toCharArray();
        String action = request.getParameter("action");

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        openDocumentHeadAndTitle(out, "Hello World");
        /*
        Additional head-data can be inserted here.
         */
        closeHead(out);

        openBody(out);

        // CONNECTION TIL DATABASEN
        Connection connection;
        DbTools dbt = new DbTools();
        connection = dbt.logIn(out);

        // SJEKK VALG, OG UTFØR I HENHOLD TIL DET
        if (action.toLowerCase().contains("skriv")) {
            out.println(navn + " was entered.");
            out.println("<br>");

        // TODO: Skriv om alle under til hver sin metode
        } else if (action.toLowerCase().contains("lagre")) {
            try {
                PreparedStatement pst = connection.prepareStatement("USE db1");
                pst.execute();
                ResultSet numRows = pst.executeQuery("SELECT COUNT(*) FROM Customer");
                pst = connection.prepareStatement("INSERT INTO Customer(C_ID, C_NAME, C_ADDRESS) VALUES (?, ?, ?)");

                pst.setString(1, getID(numRows.getInt(1)));
                pst.setString(2, navn);
                pst.setString(3, addresse);
                pst.execute();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        // TODO: Sin egen metode
        } else if (action.toLowerCase().contains("list")) {
            try {

                PreparedStatement pst = connection.prepareStatement("USE db1");
                pst.execute();
                ResultSet resultSet = pst.executeQuery("SELECT C_ID AS 'Customer ID', C_NAME AS 'Customer name' FROM Customer");

                while (resultSet.next()) {
                    out.println("<p>" + resultSet.getString(1) + " : " + resultSet.getString(2) + "<br>");
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        // TODO: Sin egen metode
        } else if (action.toLowerCase().contains("ny bruker")) {
            out.println("<div class=\"register\">");
            out.println("<form action=\"./HelloServlet\" method=\"post\"");
            out.println("<label for=\"username\"> Username </label>");
            out.println("<input id=\"username\" type=\"text\" name=\"navn\"> <br>");
            out.println("<label for=\"password\"> Password </label>");
            out.println("<input id=\"password\" type=\"password\" name=\"password\"> <br>");
            out.println("<input type=\"submit\" name=\"action\" value=\"Registrer\"");
            out.println("</div>");
        // TODO: Sin egen metode
        } else if (action.toLowerCase().contains("registrer")) {
            try {
                // Save the password locally
                // TODO: Få det til å funke uten å lagre passordet som en String?
                String password = request.getParameter("password");
                System.out.println();

                PreparedStatement pst = connection.prepareStatement("USE users");
                pst.execute();
                ResultSet numRows = pst.executeQuery("SELECT COUNT(*) FROM registered_users");
                // Prepare to register new user
                String insert = "INSERT INTO registered_users(User_ID, Name, Password_Hash, Password_Salt) VALUES (?, ?, ?, ?)";
                pst = connection.prepareStatement(insert);

                if(numRows.next()) {
                    // If the table is empty, the first customer would get the id 'C00000', that why we add 1.
                    pst.setString(1, getID(numRows.getInt(1)+1));
                }

                pst.setString(2, navn);

                String hashedPassword = passwordHashAndCheck.stringToSaltedHash(password.toString());
                //Arrays.fill(password, (char) 0);

                String[] parts = hashedPassword.split(":");
                // The salt
                pst.setString(4, parts[0]);
                // The password
                pst.setString(3, parts[1]);
                // Insert the registered user
                pst.execute();

                // Try to validate the password, i.e. a log-in attempt
                password = scanner.next();

                System.out.println(passwordHashAndCheck.validatePassword(password.toString(), hashedPassword));

                //Arrays.fill(password, (char) 0);
            } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
                e.printStackTrace();
            }
        } else {
            out.println("<h1>¯\\_(ツ)_/¯</h1>");
        }

        out.println("<a href=\"http://localhost:8080\"> Go to index");

        closeBodyAndDocument(out);
    }

    public void openDocumentHeadAndTitle(PrintWriter out, String title) {
        out.println("<!DOCTYPE html>");
        out.println("<head>");
        out.println("<meta charset=\"utf-8\"/>");
        out.println("<title>" + title + "</title>");
        out.println("<link rel=\"stylesheet\" href=\"css.css\">");
    }

    public void closeHead(PrintWriter out) {
        out.println("</head>");
    }

    public void closeBodyAndDocument(PrintWriter out) {
        out.println("</body>");
        out.println("</html>");
    }

    public void openBody(PrintWriter out) {
        out.println("<body>");
    }

    public String getID(int id) {
        if(id > 9999) {
            return "C" + String.valueOf(id);
        }
        if(id > 999) {
            return "C0" + String.valueOf(id);
        }
        if(id > 99) {
            return "C00" + String.valueOf(id);
        }
        if(id > 9 && id < 100) {
            return "C000" + String.valueOf(id);
        }
        else {
            return "C0000" + String.valueOf(id);
        }
    }

}
