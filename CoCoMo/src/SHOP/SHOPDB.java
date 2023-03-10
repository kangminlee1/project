package SHOP;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.RequestDispatcher;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;


public class SHOPDB {
   static  Connection con         = null;
    static Statement stmt         = null;
    static PreparedStatement  prStmt = null;
    static  ResultSet rs           = null ;
    
   static String driver;
   public static String dbms;
   static String URL;
   static String database;
    
   static String driverMySQL = "com.mysql.jdbc.Driver";
   static String URLLocalMySQL = "jdbc:mysql://localhost:3306/";
   static String URLRemoteMySQL = "jdbc:mysql://203.252.21.54:3306/";
    
    static {
      driver = driverMySQL;
      dbms = "MySQL";
      URL = URLLocalMySQL;
      database = "shop";
   }
    
 // DEBUG
    static boolean DEBUG = false;
    
    static void outputForDebug(String msg) {
       if (DEBUG)
          System.out.println("  << for debug >> " + msg);      
    }
    
    public static void setDBMS(String dbmsTo) {
      outputForDebug("in setSBMS(): DBMS = " + dbmsTo); 
      
      if (dbmsTo.equals("MySQL")){
         driver = driverMySQL;
         dbms = "MySQL";
         URL = URLLocalMySQL;
      }
      else if (dbmsTo.equals("Remote MySQL")){
         driver = driverMySQL;
         dbms = "Remote MySQL";
         URL = URLRemoteMySQL;
      }

      loadConnectshop();
   }
    public static Connection getConnection() {
    	try {
    		Context initContext = new InitialContext();
    		Context envContext = (Context) initContext.lookup("java:comp/env");
    		DataSource ds = (DataSource) envContext.lookup("jdbc/shop");
    		Connection con = ds.getConnection();
    		return con;
    	}catch(Exception e) {
    		e.printStackTrace();
    		System.out.println("DB?????? ??????");
    	}
    	return null;
    }
    public static void close(Connection con, PreparedStatement prStmt) {
    	try {
    		con.close();
    		prStmt.close();

    	}catch (Exception e ) {
    		e.printStackTrace();
    	}
    }
    public static void close(Connection con, PreparedStatement prStmt, ResultSet rs) {
    	try {
    		con.close();
    		prStmt.close();
    		rs.close();
    	}catch (Exception e ) {
    		e.printStackTrace();
    	}
    }
 // JDBC
    public static boolean loadConnectshop()  {
       return loadConnect("SHOP");
    }
    
    
 //??????
    public static boolean loadConnect(String database)  {
       try {
          //     ??   ?? 
          Class.forName(driverMySQL);
       } catch ( java.lang.ClassNotFoundException e ) {
          System.err.println("\n  ??? Driver load error in loadConnect(): " + e.getMessage() );
          e.printStackTrace();
       }

       try {
          //      ??  - shop      ??  ??        
          con = DriverManager.getConnection(URL + database, "root", "onlyroot");
          outputForDebug("         : " + URL + database + "        ");
          
          return true;
       } catch( SQLException ex ) {
          System.err.println("\n  ??? Connection error in loadConnect(): " + ex.getMessage() );
          ex.printStackTrace();
       }
    
       return false;
    }
    // SQL
    public static void executeAnyQuery(String sql) {
       try {
          Statement stmt = con.createStatement();
          stmt.execute(sql);
       }
       catch(SQLException ex ) {
          System.err.println("\n  ??? SQL exec error in executeAnyQuery(): " + ex.getMessage() );
          ex.printStackTrace();
       }
    }
    public static ResultSet selectQuery(String sql) {
       try {
          //Statement     
          stmt = con.createStatement();
          rs = stmt.executeQuery(sql);
       } catch( SQLException ex) {
          System.err.println("** SQL exec error in selectQuery() : " + ex.getMessage() );
       }
       return rs;
    }
 // Customer??? ?????? ??????
    public static boolean insertCustomer(Customer customer) {
      try {
         customer.output();
         String sql = "insert into customer(ID, name, password, address, Email, number) values (?, ?, ?, ?, ?, ?);" ;
         prStmt= con.prepareStatement(sql);
         prStmt.setString(1, customer.getId());
          prStmt.setString(2, customer.getName());
          prStmt.setString(3, customer.getPassword());
          prStmt.setString(4, customer.getAddress());
          prStmt.setString(5, customer.getEmail());
          prStmt.setString(6, customer.getNumber());
         prStmt.executeUpdate();
         return true;
      }
      catch(SQLException ex ) {
         System.err.println("\n  ??? SQL exec error in executeAnyQuery(): " + ex.getMessage() );
         ex.printStackTrace();
         return false;
      }
      
    }
 //
    // ??????
    public static boolean idDuplication(String id) {

        String sql = "select id from Customer where id =? ";

        try {
           PreparedStatement pstmt = con.prepareStatement(sql);
           pstmt.setString(1, id);
           ResultSet rs = pstmt.executeQuery();

           while(rs.next()) {
              if(rs.getString("id").equals(id)) {
                 return false;
              }
           }
        }catch (SQLException e) {
           e.printStackTrace();
        }
        return true;
     }
    public static Customer getCustomerFromRS(ResultSet rs) { 
        Customer cu = new Customer();

        try {
           //if (rs.getRow() ==  0)
           //   return null;
           int num = rs.getInt("num");
           String id = rs.getString("id");  // ID 
           String name = rs.getString("name");
           String password = rs.getString("password");
           String address = rs.getString("address");
           String Email = rs.getString("Email");
           String number = rs.getString("number");
           
           cu.setNum(num);
           cu.setId(id);   // ResultSet          
           cu.setName(name);
           cu.setPassword(password);
           cu.setAddress(address);
           cu.setEmail(Email);
           cu.setNumber(number);

        } catch( SQLException ex )        {
           System.err.println("\n  ??? SQL exec error in getCustomerFromRS(): " + ex.getMessage() );
        }

        return cu;
     }
  //   ?????????
    public static Customer loginProcess(String id,  String password) {
       try {
         // SQL    ??         ?? .
         String sql = "select * from Customer c where id=? and password=?;" ;
         outputForDebug("In getCustomer() SQL : " + sql);
         PreparedStatement prStmt = con.prepareStatement(sql);
         prStmt.setString(1, id);
         prStmt.setString(2, password);

         ResultSet rs = prStmt.executeQuery();
         if (rs.next())  {
            Customer customer = new Customer();
            customer.setNum(rs.getInt("c.num"));
            customer.setId(rs.getString("c.id"));
            customer.setName(rs.getString("c.name"));
            customer.setPassword(rs.getString("c.password"));
            customer.setAddress(rs.getString("c.address"));
            customer.setEmail(rs.getString("c.Email"));
            customer.setNumber(rs.getString("c.number"));
            return customer;
         }
      } catch( SQLException ex ) {
         System.err.println("\n  ??? SQL exec error in getCustomer(): " + ex.getMessage() );
      }
       return null;
    }
    //???????????? ??????
    public static ResultSet getcustomerInfo(String id) {
        String sql = "select id as ?????????, name as ??????, address as ?????? , Email as ????????? , number as ????????????"
                + " from Customer where id = '"+ id +"';";
        System.out.println("   >> SQL : " + sql + "\n");

        return selectQuery(sql);
     }
    // ?????? ?????? ??????
    public static boolean cancelInfo(String id) {
      try {
         
         String sql = "delete from Customer where Id =? ";
         prStmt= con.prepareStatement(sql);
         prStmt.setString(1, id);
          
         prStmt.executeUpdate();
         return true;
      }
      catch(SQLException ex ) {
         System.err.println("\n  ??? SQL exec error in executeAnyQuery(): " + ex.getMessage() );
         ex.printStackTrace();
         return false;
      }
    }
    public static boolean deleteId(String id,String password) {
    	boolean result=false;
    	String dbpw="";
    	try {
    		String sql = "select password from Customer where Id =? ";
            prStmt= con.prepareStatement(sql);
            prStmt.setString(1, id);
            ResultSet rs = prStmt.executeQuery();
            if(rs.next()) {
            	dbpw=rs.getString("password");
            	if(dbpw.equals(password));{
            		String delsql="delete from Customer where id=?";
            		prStmt= con.prepareStatement(delsql);
                    prStmt.setString(1, id);
                    prStmt.executeUpdate();
                    result=true;
            	}
            }
    	}
    	catch(SQLException ex ) {
            System.err.println("\n  ??? SQL exec error in executeAnyQuery(): " + ex.getMessage() );
            ex.printStackTrace();
         }
    	return result;
    }
    //????????????
    public Customer getInform(String id) {
    	Customer inform=null;
    	try {
    		String sql="select * from Customer where id=?";
    		prStmt=con.prepareStatement(sql);
    		prStmt.setString(1, id);
    		ResultSet rs = prStmt.executeQuery();
    		if(rs.next()) {
    			inform=new Customer();
    			inform.setId(rs.getString("id"));
    			inform.setName(rs.getString("name"));
    			inform.setPassword(rs.getString("password"));
    			inform.setAddress(rs.getString("address"));
    			inform.setEmail(rs.getString("Email"));
    			inform.setNumber(rs.getString("number"));
    		}
    	}
    	catch( SQLException ex ){
            System.err.println("\n  ??? SQL exec error in getCustomerFromRS(): " + ex.getMessage() );
         }
    	return inform;
    }
    public static boolean editInform(Customer customer) {
    	boolean result=false;
    	try {
    		String id = customer.getId();
    		String sql="update Customer set id=?,name=?,password=?,address=?,Email=?,number=? where id= '"+id+"'; ";
    		prStmt=con.prepareStatement(sql);
    		prStmt.setString(1, customer.getId());
    		prStmt.setString(2, customer.getName());
    		prStmt.setString(3, customer.getPassword());
    		prStmt.setString(4, customer.getAddress());
    		prStmt.setString(5, customer.getEmail());
    		prStmt.setString(6, customer.getNumber());
    		prStmt.executeUpdate();
    		result=true;
    	}
    	catch(SQLException ex ) {
            System.err.println("\n  ??? SQL exec error in executeAnyQuery(): " + ex.getMessage() );
            ex.printStackTrace();
    }
    	return result;
   }
    //?????????

}
