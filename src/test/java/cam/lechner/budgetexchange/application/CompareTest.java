package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.cospend.BillRespond;
import cam.lechner.budgetexchange.cospend.Ocs;
import cam.lechner.budgetexchange.cospend.Data;
import cam.lechner.budgetexchange.cospend.Bill;
import cam.lechner.budgetexchange.entity.TransactionIds;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompareTest {

    @Mock
    private ApiCall apicall;

    @Mock
    private CompareRepository compareRepository;

    @Mock
    private MapCategoryRepository mapCategoryRepository;

    @InjectMocks
    private Compare compare;

    @Test
    void testDoCompare_WithMultipleProjects() {
        // Arrange
        List<String> projects = Arrays.asList("project1", "project2");
        when(mapCategoryRepository.findDistinctProjectnames()).thenReturn(projects);

        // Setup für jeden getAllBills Aufruf - doCompare ruft es 4x auf (2 Projekte × 2 Methoden)
        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(anyString())).thenReturn(mockResponse);
        when(compareRepository.findNextCloudBillIdsByProject(anyString())).thenReturn(Arrays.asList(1, 2));
        when(compareRepository.findByProjectId(anyString())).thenReturn(Collections.emptyList());

        // Act
        compare.doCompare();

        // Assert
        verify(mapCategoryRepository).findDistinctProjectnames();
        verify(apicall, times(4)).getAllBills(anyString()); // 2 Projekte × 2 Methoden
        verify(compareRepository, times(2)).findNextCloudBillIdsByProject(anyString());
        verify(compareRepository, times(2)).findByProjectId(anyString());
    }

    @Test
    void testDoCompare_WithEmptyProjectList() {
        // Arrange
        when(mapCategoryRepository.findDistinctProjectnames()).thenReturn(Collections.emptyList());

        // Act
        compare.doCompare();

        // Assert
        verify(mapCategoryRepository).findDistinctProjectnames();
        verify(apicall, never()).getAllBills(anyString());
        verify(compareRepository, never()).findNextCloudBillIdsByProject(anyString());
    }

    @Test
    void testCompareCospendBudget_AllBillsExistInBudget() {
        // Arrange
        String project = "testProject";
        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(project)).thenReturn(mockResponse);
        when(compareRepository.findNextCloudBillIdsByProject(project)).thenReturn(Arrays.asList(1, 2));

        // Act
        compare.compareCospendBudget(project);

        // Assert
        verify(apicall).getAllBills(project);
        verify(compareRepository).findNextCloudBillIdsByProject(project);
        verify(apicall, never()).deleteBill(anyString(), anyString());
        verify(apicall, never()).sendMessageToTalk(anyString());
    }

     @Test
    void testCompareCospendBudget_BillNotInBudget_DeleteSuccessful() {
        // Arrange
        String project = "testProject";
        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(project)).thenReturn(mockResponse);
        when(compareRepository.findNextCloudBillIdsByProject(project)).thenReturn(Arrays.asList(2)); // Only bill 2 exists
        when(apicall.deleteBill("1", project)).thenReturn(true);

        // Act
        compare.compareCospendBudget(project);

        // Assert
        verify(apicall).deleteBill("1", project);
        verify(apicall).sendMessageToTalk("Deleted bill 1 in Cospend");
    }

    @Test
    void testCompareCospendBudget_BillNotInBudget_DeleteFailed() {
        // Arrange
        String project = "testProject";
        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(project)).thenReturn(mockResponse);
        when(compareRepository.findNextCloudBillIdsByProject(project)).thenReturn(Arrays.asList(2)); // Only bill 2 exists
        when(apicall.deleteBill("1", project)).thenReturn(false);

        // Act
        compare.compareCospendBudget(project);

        // Assert
        verify(apicall).deleteBill("1", project);
        verify(apicall).sendMessageToTalk("!!!Can not delete 1 in Cospend");
    }

    @Test
    void testCompareBudgetCospend_AllTransactionsExistInCospend() {
        // Arrange
        String project = "testProject";
        TransactionIds transaction1 = createTransactionIds(1, "123", 100L);
        TransactionIds transaction2 = createTransactionIds(2, "456", 101L);

        when(compareRepository.findByProjectId(project)).thenReturn(Arrays.asList(transaction1, transaction2));

        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(project)).thenReturn(mockResponse);

        // Act
        compare.compareBudgetCospend(project);

        // Assert
        verify(compareRepository).findByProjectId(project);
        verify(apicall).getAllBills(project);
        verify(compareRepository, never()).deleteById((int) anyLong());
        verify(apicall, never()).sendMessageToTalk(anyString());
    }

    @Test
    void testCompareBudgetCospend_TransactionNotInCospend() {
        // Arrange
        String project = "testProject";
        TransactionIds transaction1 = createTransactionIds(1, "123", 100L);
        TransactionIds transaction2 = createTransactionIds(3, "456", 101L); // ID 3 doesn't exist in Cospend

        when(compareRepository.findByProjectId(project)).thenReturn(Arrays.asList(transaction1, transaction2));

        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2}); // Only IDs 1 and 2 exist
        when(apicall.getAllBills(project)).thenReturn(mockResponse);

        // Act
        compare.compareBudgetCospend(project);

        // Assert
        verify(compareRepository).deleteById(101);
        verify(apicall).sendMessageToTalk("Deleted 456 in Budget, because it was not in Cospend");
        verify(compareRepository, never()).deleteById(100); // transaction1 should not be deleted
    }

    @Test
    void testCompareBudgetCospend_EmptyBudgetTransactions() {
        // Arrange
        String project = "testProject";
        when(compareRepository.findByProjectId(project)).thenReturn(Collections.emptyList());

        BillRespond mockResponse = createMockBillRespond(new Integer[]{1, 2});
        when(apicall.getAllBills(project)).thenReturn(mockResponse);

        // Act
        compare.compareBudgetCospend(project);

        // Assert
        verify(compareRepository).findByProjectId(project);
        verify(apicall).getAllBills(project);
        verify(compareRepository, never()).deleteById((int) anyLong());
        verify(apicall, never()).sendMessageToTalk(anyString());
    }

    // Helper-Methoden
    private BillRespond createMockBillRespond(Integer[] billIds) {
        BillRespond mockBillRespond = mock(BillRespond.class);
        Ocs mockOcs = mock(Ocs.class);
        Data mockData = mock(Data.class);

        // Setup der Mock-Kette
        when(mockBillRespond.getOcs()).thenReturn(mockOcs);
        when(mockOcs.getData()).thenReturn(mockData);

        // Erstelle echte Bill-Objekte
        Bill[] bills = new Bill[billIds.length];
        for (int i = 0; i < billIds.length; i++) {
            bills[i] = new Bill();
            bills[i].setId(billIds[i]);
        }

        // Konfiguriere nur die Methoden, die tatsächlich verwendet werden
        lenient().when(mockData.getBills()).thenReturn(bills);
        lenient().when(mockData.getAllBillIds()).thenReturn(billIds);

        return mockBillRespond;
    }

    private TransactionIds createTransactionIds(Integer nextcloudBillId, String budgetTransId, Long id) {
        // Erstelle echte TransactionIds - KEIN Mock!
        TransactionIds transaction = new TransactionIds();
        transaction.setId(Math.toIntExact(id));
        transaction.setNextcloudBillId(nextcloudBillId);
        transaction.setBudgetTransId(Integer.valueOf(budgetTransId));
        return transaction;
    }
}