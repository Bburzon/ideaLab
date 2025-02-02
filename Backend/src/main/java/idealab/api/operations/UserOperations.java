package idealab.api.operations;

import static idealab.api.exception.ErrorType.USER_NOT_FOUND;
import static idealab.api.exception.ErrorType.VALIDATION_ERROR;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import idealab.api.dto.request.EmployeeSignUpRequest;
import idealab.api.dto.request.UserChangePasswordRequest;
import idealab.api.dto.response.GenericResponse;
import idealab.api.dto.response.UserResponse;
import idealab.api.exception.ErrorType;
import idealab.api.exception.IdeaLabApiException;
import idealab.api.model.Employee;
import idealab.api.model.EmployeeRole;
import idealab.api.repositories.EmployeeRepo;

@Component
public class UserOperations {

    private EmployeeRepo employeeRepo;
    private BCryptPasswordEncoder encoder;

    public UserOperations(EmployeeRepo employeeRepo, BCryptPasswordEncoder encoder) {
        this.employeeRepo = employeeRepo;
        this.encoder = encoder;
    }

    /**
     * Gets all users but only returns specific information that can be displayed to all
     * admins.
     * This is to be used with the find all simple method in the employee repo.
     * @return
     */
    public UserResponse getAllUsers() {
        UserResponse response = new UserResponse("Could not get list of users");

        List<Object[]> users = employeeRepo.findAllSimple();

        if (users == null || users.isEmpty()){
            ErrorType.USER_NOT_FOUND.throwException();
        }

        response.setSuccess(true);
        response.setMessage("Successfully returned users");
        response.setSimpleData(users);
        response.setHttpStatus(HttpStatus.ACCEPTED);

        return response;
    }

    public GenericResponse userSignUp(EmployeeSignUpRequest request) {
        request.validate();
        Employee login = fromEmployeeSignUpRequest(request);
        GenericResponse response = new GenericResponse();

        try {
        	Employee userFound = employeeRepo.findEmployeeByUsername(login.getUsername());
        	
        	if (userFound != null) {
        		response.setSuccess(false);
                response.setMessage("User already exists");
                response.setHttpStatus(HttpStatus.BAD_REQUEST);
                return response;
        	}
        	
            login.setPassword(encoder.encode(login.getPassword()));
            employeeRepo.save(login);
        } catch (Exception e) {
            e.printStackTrace();
            response.setSuccess(false);
            response.setMessage("User Sign Up Failed");
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
            return response;
        }

        response.setSuccess(true);
        response.setMessage("User Sign Up Successful");
        response.setHttpStatus(HttpStatus.CREATED);
        return response;
    }

    public GenericResponse deleteUser(Integer id) {
        GenericResponse response = new GenericResponse();
        Employee e = employeeRepo.findEmployeeById(id);

        if(e != null) {
            try {
                employeeRepo.deleteById(id);
                response.setSuccess(true);
                response.setMessage("Employee Deleted Successfully");
                response.setHttpStatus(HttpStatus.ACCEPTED);
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setSuccess(false);
                response.setMessage("Employee Could Not Be Deleted");
                response.setHttpStatus(HttpStatus.BAD_REQUEST);
            }
        } else {
            response.setSuccess(false);
            response.setMessage("Employee ID is not valid");
            response.setHttpStatus(HttpStatus.BAD_REQUEST);
        }

        return response;
    }

    public GenericResponse changePassword(UserChangePasswordRequest request) {
        request.validate();
        GenericResponse response = new GenericResponse();
        Employee e = employeeRepo.findEmployeeByUsername(request.getUsername());

        if(e != null) {
            if(encoder.matches(request.getOldPassword(), e.getPassword())) {
                if(request.getNewPassword().equals(request.getConfirmNewPassword())) {
                    e.setPassword(encoder.encode(request.getNewPassword()));
                    e = employeeRepo.save(e);
                    response.setSuccess(true);
                    response.setMessage("Password Changed Successfully");
                    response.setHttpStatus(HttpStatus.ACCEPTED);
                    return response;
                } else {
                    throw new IdeaLabApiException(VALIDATION_ERROR, "New Passwords do not match");
                }
            } else {
                throw new IdeaLabApiException(VALIDATION_ERROR, "Previous Password does not match");
            }
        } else {
            throw new IdeaLabApiException(USER_NOT_FOUND);
        }
    }

    private Employee fromEmployeeSignUpRequest(EmployeeSignUpRequest request) {
        Employee e = new Employee();
        e.setUsername(request.getUsername());
        e.setPassword(request.getPassword());
        e.setFirstName(request.getFirstName());
        e.setLastName(request.getLastName());
        e.setRole(EmployeeRole.fromString(request.getRole()));
        return e;
    }
}
