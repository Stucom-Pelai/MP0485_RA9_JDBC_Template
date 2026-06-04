package dao;

import exception.DAO_Excep;
import model.Student;
import java.util.List;

/**
 *
 * @author fran
 * 
 */

public interface IDAO {     
        
    public abstract int insert(Student student) throws DAO_Excep;
    public abstract List<Student> read (Student student)throws DAO_Excep;
    public abstract int update (Student student) throws DAO_Excep;    
    public abstract int deleteALL () throws DAO_Excep;
    
}
