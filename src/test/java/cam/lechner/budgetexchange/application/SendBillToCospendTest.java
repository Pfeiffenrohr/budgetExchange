package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.apicall.ApiCall;
import cam.lechner.budgetexchange.entity.*;
import cam.lechner.budgetexchange.service.KontoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendBillToCospendTest {

    @Mock
    private ApiCall apiCall;

    @Mock
    private CompareRepository compareRepository;

    @Mock
    private TransaktionRepository transaktionRepository;

    @Mock
    private MapCategoryRepository mapCategoryRepository;

    @Mock
    private MapMemberRepository mapMemberRepository;

    @Mock
    private KontoService kontoService;

    @InjectMocks
    private SendBillToCospend sendBillToCospend;

    private List<MapCategory> testMapCategories;
    private List<TransactionIds> testTransactionIds;
    private List<Konto> testKontos;
    private List<Transaktion> testTransaktions;

    @BeforeEach
    void setUp() {
        // Setup test data
        testMapCategories = createTestMapCategories();
        testTransactionIds = createTestTransactionIds();
        testKontos = createTestKontos();
        testTransaktions = createTestTransaktions();
    }

    @Test
    void testGetMissingTransactionsAndSendToCospend_NewTransaction() throws Exception {
        // Arrange
        when(mapCategoryRepository.findAll()).thenReturn(testMapCategories);
        when(compareRepository.findAll()).thenReturn(testTransactionIds);
        when(apiCall.getKontoWithAnlegeart("Geldkonto")).thenReturn(testKontos);
        when(apiCall.getTransactionWithCategoryAndDate(anyString(), anyString(), anyString()))
                .thenReturn(testTransaktions);
        when(compareRepository.findByBudgetTransIdAndProjectId(anyInt(), anyString()))
                .thenReturn(null); // Simulate new transaction
        when(mapMemberRepository.findByNameAndProject("Mieter", "test-project"))
                .thenReturn(createMapMember("Mieter", 1));
        when(mapMemberRepository.findByNameAndProject("Hausverwaltung", "test-project"))
                .thenReturn(createMapMember("Hausverwaltung", 2));
        when(kontoService.getMapKontoOrDefault(anyInt(), anyString()))
                .thenReturn(createMapKonto());
        when(apiCall.sendBill(any(MultiValueMap.class), anyString())).thenReturn(123);

        // Act
        sendBillToCospend.getMissingTransactionsAndSendToCospend();

        // Assert
        verify(apiCall).sendBill(any(MultiValueMap.class), eq("test-project"));
        verify(compareRepository).save(any(TransactionIds.class));
        verify(transaktionRepository).save(any(Transaktion.class));
    }

    @Test
    void testGetMissingTransactionsAndSendToCospend_UpdateExistingTransaction() throws Exception {
        // Arrange
        when(mapCategoryRepository.findAll()).thenReturn(testMapCategories);
        when(compareRepository.findAll()).thenReturn(testTransactionIds);
        when(apiCall.getKontoWithAnlegeart("Geldkonto")).thenReturn(testKontos);
        when(apiCall.getTransactionWithCategoryAndDate(anyString(), anyString(), anyString()))
                .thenReturn(testTransaktions);

        TransactionIds existingTransactionId = new TransactionIds();
        existingTransactionId.setBudgetTransId(1);
        existingTransactionId.setNextcloudBillId(123);
        when(compareRepository.findByBudgetTransIdAndProjectId(1, "test-project"))
                .thenReturn(existingTransactionId);

        Transaktion storedTrans = new Transaktion();
        storedTrans.setId(1);
        storedTrans.setName("Old Name");
        storedTrans.setWert(100.0);
        storedTrans.setBeschreibung("Old Description");
        when(transaktionRepository.findById(1)).thenReturn(Optional.of(storedTrans));

        when(mapMemberRepository.findByNameAndProject("Mieter", "test-project"))
                .thenReturn(createMapMember("Mieter", 1));
        when(mapMemberRepository.findByNameAndProject("Hausverwaltung", "test-project"))
                .thenReturn(createMapMember("Hausverwaltung", 2));
        when(kontoService.getMapKontoOrDefault(anyInt(), anyString()))
                .thenReturn(createMapKonto());
        when(compareRepository.findByBudgetTransIdAndIsChecked(anyInt(), eq(0)))
                .thenReturn(new ArrayList<>());

        // Act
        sendBillToCospend.getMissingTransactionsAndSendToCospend();

        // Assert
        verify(apiCall).updateBill(any(MultiValueMap.class), eq("/123"), eq("test-project"));
    }

    @Test
    void testGetPayer_MieteCategory() throws Exception {
        // Arrange
        MapCategory mapCategory = new MapCategory();
        mapCategory.setKind(0); // Miete
        mapCategory.setProjectname("test-project");

        when(mapMemberRepository.findByNameAndProject("Mieter", "test-project"))
                .thenReturn(createMapMember("Mieter", 59));

        // Act
        String result = sendBillToCospend.getPayer(mapCategory);

        // Assert
        assertEquals("59", result);
    }

    @Test
    void testGetPayer_AusgabenCategory() throws Exception {
        // Arrange
        MapCategory mapCategory = new MapCategory();
        mapCategory.setKind(2); // Ausgaben
        mapCategory.setProjectname("test-project");

        when(mapMemberRepository.findByNameAndProject("Hausverwaltung", "test-project"))
                .thenReturn(createMapMember("Hausverwaltung", 58));

        // Act
        String result = sendBillToCospend.getPayer(mapCategory);

        // Assert
        assertEquals("58", result);
    }

    @Test
    void testNotToCalculate_RentenversicherungKonto() {
        // Arrange
        Transaktion transaktion = new Transaktion();
        transaktion.setKonto_id(12); // Rentenversicherung

        // Act
        Boolean result = sendBillToCospend.notToCalculate(transaktion, "test-project");

        // Assert
        assertTrue(result);
    }

    @Test
    void testNotToCalculate_NormalTransaction() {
        // Arrange
        Transaktion transaktion = new Transaktion();
        transaktion.setKonto_id(1);
        transaktion.setKategorie(50); // Not in excluded categories

        // Act
        Boolean result = sendBillToCospend.notToCalculate(transaktion, "test-project");

        // Assert
        assertFalse(result);
    }

    @Test
    void testHatKontoMitId_KontoExists() {
        // Arrange
        List<Konto> kontos = Arrays.asList(
                createKonto(1, "Konto 1"),
                createKonto(2, "Konto 2")
        );

        // Act
        boolean result = sendBillToCospend.hatKontoMitId(kontos, 1);

        // Assert
        assertTrue(result);
    }

    @Test
    void testHatKontoMitId_KontoNotExists() {
        // Arrange
        List<Konto> kontos = Arrays.asList(
                createKonto(1, "Konto 1"),
                createKonto(2, "Konto 2")
        );

        // Act
        boolean result = sendBillToCospend.hatKontoMitId(kontos, 3);

        // Assert
        assertFalse(result);
    }

    @Test
    void testDeleteSaveTransaktion_CanDelete() {
        // Arrange
        when(compareRepository.findByBudgetTransId(1)).thenReturn(new ArrayList<>());

        // Act
        sendBillToCospend.deleteSaveTransaktion(1, transaktionRepository, compareRepository);

        // Assert
        verify(transaktionRepository).deleteById(1);
    }

    @Test
    void testDeleteSaveTransaktion_CannotDelete() {
        // Arrange
        List<TransactionIds> existingTransactions = Arrays.asList(new TransactionIds());
        when(compareRepository.findByBudgetTransId(1)).thenReturn(existingTransactions);

        // Act
        sendBillToCospend.deleteSaveTransaktion(1, transaktionRepository, compareRepository);

        // Assert
        verify(transaktionRepository, never()).deleteById(1);
    }

    @Test
    void testGetMissingTransactionsAndSendToCospend_ExceptionHandling() {
        // Arrange
        when(mapCategoryRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        sendBillToCospend.getMissingTransactionsAndSendToCospend();

        // Assert
        verify(apiCall).sendMessageToTalk(contains("Fehler"));
    }

    // Helper methods for creating test data
    private List<MapCategory> createTestMapCategories() {
        MapCategory category = new MapCategory();
        category.setBudgetCategory(1);
        category.setProjectname("test-project");
        category.setInout(1);
        category.setCospendCategory(10);
        category.setKind(0);
        return Arrays.asList(category);
    }

    private List<TransactionIds> createTestTransactionIds() {
        TransactionIds transId = new TransactionIds();
        transId.setBudgetTransId(1);
        transId.setNextcloudBillId(123);
        transId.setProjectId("test-project");
        return Arrays.asList(transId);
    }

    private List<Konto> createTestKontos() {
        return Arrays.asList(createKonto(1, "Test Konto"));
    }

    private Konto createKonto(Integer id, String name) {
        Konto konto = new Konto();
        konto.setId(id);
        konto.setKontoname(name);
        return konto;
    }

    private List<Transaktion> createTestTransaktions() {
        Transaktion trans = new Transaktion();
        trans.setId(1);
        trans.setKonto_id(1);
        trans.setName("Test Transaction");
        trans.setWert(200.0);
        trans.setBeschreibung("Test Description");
        trans.setDatum("2023-01-01");
        trans.setKategorie(1);
        return Arrays.asList(trans);
    }

    private MapMember createMapMember(String name, Integer id) {
        MapMember member = new MapMember();
        member.setName(name);
        member.setCospendMemberId(id);
        return member;
    }

    private MapKonto createMapKonto() {
        MapKonto mapKonto = new MapKonto();
        mapKonto.setCospendKonto(0);
        return mapKonto;
    }
}