package dao;

import exception.DAO_Excep;
import exception.Write_SQL_DAO_Excep;
import exception.Read_SQL_DAO_Excep;
import model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Fran Perez
 */
public class DAOSQL implements IDAO {

    //Variables para la conexión segura contra el servidor (sin especificar DDBB)
    private final String JDBC_URL = "jdbc:mysql://localhost:3306";
    private final String JDBC_COMMU_OPT = "?useSSL=false&useTimezone=true&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private final String JDBC_USER = "root";
    private final String JDBC_PASSWORD = "";

    //Especificamos la base de Datos
    private final String JDBC_DDBB = "school";
    private final String JDBC_TABLE = "students";
    private final String JDBC_DDBB_TABLE = JDBC_DDBB + "." + JDBC_TABLE;

    //Variables para las consultas SQL
    //Consultas ejemplo, puede que necesiten modificación
    private final String SQL_INSERT = "INSERT INTO " + JDBC_DDBB_TABLE + " (name, age) VALUES (?, ?);";
    private final String SQL_SELECT = "SELECT * FROM " + JDBC_DDBB_TABLE + " WHERE (name = ";
    private final String SQL_UPDATE = "UPDATE " + JDBC_DDBB_TABLE + " SET age = ? WHERE (name = ?);";
    private final String SQL_DELETE_ALL = "DELETE FROM " + JDBC_DDBB_TABLE + ";";

    public Connection connect() throws DAO_Excep {
        Connection conn = null;
        try {
            //Esta línea no es necesaria, excepto en algunas aplicaciones WEB
            //En aplicaciones locales como esta no sería necesaria
            //Class.forName("com.mysql.cj.jdbc.Driver");
            //getConnection necesita la BBDD, el usuario y la contraseña
            conn = DriverManager.getConnection(JDBC_URL + JDBC_COMMU_OPT, JDBC_USER, JDBC_PASSWORD);
            createDB(conn);
            createTable(conn);
//        } catch (ClassNotFoundException ex) {
//           ex.printStackTrace(System.out);
        } catch (SQLException ex) {
            //ex.printStackTrace(System.out);
            throw new DAO_Excep("Can not connect or create database with tables: " + JDBC_DDBB);
        }
        return conn;
    }

    private void createDB(Connection conn) throws SQLException {
        //Sentencia SQL que crea la BBDD si no existe en el servidor
        String instruction = "create database if not exists " + JDBC_DDBB + ";";
        Statement stmt = null;
        stmt = conn.createStatement();
        //La clase Statemen nos permite ejecutar sentencias SQL
        stmt.executeUpdate(instruction);
        //Liberamos los recursos de la comunicación   
        stmt.close();
    }

    private void createTable(Connection conn) throws SQLException {
        String query = "create table if not exists " + JDBC_DDBB + "." + JDBC_TABLE + "("
                + "id Bigint primary key auto_increment, "
                + "name varchar(50), "
                + "age int);";
        Statement stmt = null;
        stmt = conn.createStatement();
        stmt.executeUpdate(query);
        //Liberamos los recursos de la comunicación   
        stmt.close();
    }

    public void disconnect(Connection conn) throws DAO_Excep {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                throw new DAO_Excep("Can not disconnect from database " + JDBC_DDBB);
            }
        }
    }
    
    @Override
    public int insert(Student student) throws DAO_Excep {
        Connection conn = null;
        //La clase PreparedStatement también permite ejecutar sentencias SQL
        //pero con mayor flexibilidad
        PreparedStatement instruction = null;
        int registers = 0;
        try {
            conn = connect();
            instruction = conn.prepareStatement(SQL_INSERT);
            instruction.setString(1, student.getName());
            instruction.setInt(2, student.getAge());
            registers = instruction.executeUpdate();
        } catch (SQLException ex) {
            throw new Write_SQL_DAO_Excep("Can not write to database (DAO_COntroller.DAOSQL.insert)");
        } finally {
            try {
                instruction.close();
                disconnect(conn);
            } catch (SQLException ex) {
                //ex.printStackTrace(System.out);
                throw new Write_SQL_DAO_Excep("Can not close database write process (DAO_COntroller.DAOSQL.insert)");
            }
        }
        //Devolvemos la cantidad de registros afectados, en nuestro caso siempre uno
        return registers;
    }    
    

    @Override
    public List<Student> read(Student s) throws DAO_Excep {
        ArrayList<Student> students = new ArrayList<>();
        Student student = null;
        Connection conn = null;
        Statement instruction = null;
        ResultSet rs = null;
        try {
            conn = connect();
            String query = SQL_SELECT + "'" + s.getName() + "'" + ");";
            instruction = conn.createStatement();
            rs = instruction.executeQuery(query);
            while (rs.next()) {
                int id = rs.getInt("id");
                String nam = rs.getString("name");
                int age = rs.getInt("age");
                student = new Student(id, nam, age);
                students.add(student);
            }
        } catch (SQLException ex) {
            //ex.printStackTrace(System.out);
            throw new Read_SQL_DAO_Excep("Can not read from database (DAO_COntroller.DAOSQL.read)");
        } finally {
            try {
                rs.close();
                instruction.close();
                disconnect(conn);
            } catch (SQLException ex) {
                //ex.printStackTrace(System.out);
                throw new Read_SQL_DAO_Excep("Can not close database read process (DAO_COntroller.DAOSQL.read)");
            }
        }
        return students;
    }
    
    

    @Override
    public int update(Student student) throws DAO_Excep {
        Connection conn = null;
        PreparedStatement instruction = null;
        int registers = 0;
        try {
            conn = connect();
            instruction = conn.prepareStatement(SQL_UPDATE);
            instruction.setInt(1, student.getAge());
            instruction.setString(2, student.getName());
            //cada vez que modificamos una base de datos llamamos a executeUpdate()
            registers = instruction.executeUpdate();
        } catch (SQLException ex) {
            //ex.printStackTrace(System.out);
            throw new Write_SQL_DAO_Excep("Can not write to database (DAO_COntroller.DAOSQL.update)");
        } finally {
            try {
                instruction.close();
                disconnect(conn);
            } catch (SQLException ex) {
                //ex.printStackTrace(System.out);
                throw new Write_SQL_DAO_Excep("Can not close database write process (DAO_COntroller.DAOSQL.update)");
            }
        }
        //Devolvemos la cantidad de registros afectados
        return registers;
    }    

    @Override
    public int deleteALL() throws DAO_Excep {
        Connection conn = null;
        PreparedStatement instruccion = null;
        int registers = 0;
        try {
            conn = connect();
            instruccion = conn.prepareStatement(SQL_DELETE_ALL);
            //cada vez que modificamos una base de datos llamamos a executeUpdate()
            registers = instruccion.executeUpdate();
        } catch (SQLException ex) {
            //ex.printStackTrace(System.out);
            throw new Write_SQL_DAO_Excep("Can not write to database (DAO_Controller.DAOSQL.deleteAll)");
        } finally {
            try {
                instruccion.close();
                disconnect(conn);
            } catch (SQLException ex) {
                ex.printStackTrace(System.out);
                throw new Write_SQL_DAO_Excep("Can not close database write process (DAO_COntroller.DAOSQL.deleteAll)");
            }
        }
        //Devolvemos la cantidad de registros afectados
        return registers;
    }

   
}
