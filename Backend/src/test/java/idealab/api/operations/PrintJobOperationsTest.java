package idealab.api.operations;

import idealab.api.dto.response.GenericResponse;
import idealab.api.dto.request.PrintJobDeleteRequest;
import idealab.api.dto.request.PrintJobUpdateRequest;
import idealab.api.model.Employee;
import idealab.api.model.PrintJob;
import idealab.api.model.Status;
import idealab.api.repositories.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class PrintJobOperationsTest {

    @InjectMocks
    private PrintJobOperations operations;

    @Mock
    PrintJobRepo printJobRepo;

    @Mock
    private DropboxOperations dropboxOperations;

    @Mock
    private ColorTypeRepo colorTypeRepo;

    @Mock
    private EmailHashRepo emailHashRepo;

    @Mock
    private CustomerInfoRepo customerInfoRepo;

    @Mock
    private EmployeeRepo employeeRepo;

    @Before
    public void setup() {
        operations = new PrintJobOperations(dropboxOperations, printJobRepo,
                 colorTypeRepo, emailHashRepo, customerInfoRepo,
                 employeeRepo);
    }

    @Test
    public void updatePrintJobSuccess() {
        //Given
        PrintJobUpdateRequest request = new PrintJobUpdateRequest();
        request.setEmployeeId(1);
        request.setStatus("Completed");

        Employee employee = new Employee();
        employee.setId(1);

        PrintJob printJob = new PrintJob();
        printJob.setEmployeeId(employee);
        printJob.setId(2);
        printJob.setStatus(Status.COMPLETED);

        //When
        when(employeeRepo.findEmployeeById(anyInt())).thenReturn(employee);
        when(printJobRepo.findPrintJobById(anyInt())).thenReturn(printJob);
        when(printJobRepo.save(printJob)).thenReturn(printJob);

        GenericResponse response = operations.updatePrintJobStatus(2, request);

        //Then
        assertTrue("response is not true", response.isSuccess() == true);
        assertTrue("print job was not updated", response.getMessage().equalsIgnoreCase("Print Job Updated"));
    }

    @Test
    public void updatePrintJobFail() {
        //Given
        PrintJobUpdateRequest request = new PrintJobUpdateRequest();
        request.setEmployeeId(1);
        request.setStatus("adfasfasf");

        Employee employee = new Employee();
        employee.setId(1);

        PrintJob printJob = new PrintJob();
        printJob.setEmployeeId(employee);
        printJob.setId(2);
        printJob.setStatus(Status.COMPLETED);

        //When
        when(employeeRepo.findEmployeeById(anyInt())).thenReturn(employee);
        when(printJobRepo.findPrintJobById(anyInt())).thenReturn(printJob);

        GenericResponse response = operations.updatePrintJobStatus(2, request);

        //Then
        assertTrue("response is not false", response.isSuccess() == false);
        assertTrue("print job was updated", response.getMessage().equalsIgnoreCase("Print Job Update Failed - Invalid Status"));
    }

    @Test
    public void deletePrintJobSuccess() {
        PrintJobDeleteRequest request = new PrintJobDeleteRequest();
        request.setEmployeeId(1);
        request.setPrintJobId(2);

        Employee employee = new Employee();
        employee.setId(1);

        PrintJob printJob = new PrintJob();
        printJob.setEmployeeId(employee);
        printJob.setId(2);

        when(employeeRepo.findEmployeeById(anyInt())).thenReturn(employee);
        when(printJobRepo.findPrintJobById(anyInt())).thenReturn(printJob);
        doNothing().when(printJobRepo).delete(printJob);

        GenericResponse response = operations.deletePrintJobStatus(request);

        assertTrue("response is not true", response.isSuccess() == true);
        assertTrue("print job was not deleted", response.getMessage().equalsIgnoreCase("Print Job Deleted Successfully"));
    }

    @Test
    public void deletePrintJobFailEmployeeNull() {
        PrintJobDeleteRequest request = new PrintJobDeleteRequest();
        request.setEmployeeId(1);
        request.setPrintJobId(2);

        Employee employee = new Employee();
        employee.setId(1);

        PrintJob printJob = new PrintJob();
        printJob.setEmployeeId(employee);
        printJob.setId(2);

        when(employeeRepo.findEmployeeById(anyInt())).thenReturn(null);
        when(printJobRepo.findPrintJobById(anyInt())).thenReturn(printJob);

        GenericResponse response = operations.deletePrintJobStatus(request);

        assertTrue("response is not false", response.isSuccess() == false);
        assertTrue("print job was deleted", response.getMessage().equalsIgnoreCase("Print Job Delete Failed"));
    }

    @Test
    public void deletePrintJobFailPrintJobStatusNull() {
        PrintJobDeleteRequest request = new PrintJobDeleteRequest();
        request.setEmployeeId(1);
        request.setPrintJobId(2);

        Employee employee = new Employee();
        employee.setId(1);

        when(employeeRepo.findEmployeeById(anyInt())).thenReturn(employee);
        when(printJobRepo.findPrintJobById(anyInt())).thenReturn(null);

        GenericResponse response = operations.deletePrintJobStatus(request);

        assertTrue("response is not false", response.isSuccess() == false);
        assertTrue("print job was deleted", response.getMessage().equalsIgnoreCase("Print Job Delete Failed"));
    }

}
