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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

// Diese Annotation aktiviert die Mockito-Erweiterung für JUnit 5
@ExtendWith(MockitoExtension.class)
class SendBillToCospendTest {

    // @Mock erstellt eine Schein-Implementierung (Mock) für die Abhängigkeit.
    // Wir kontrollieren ihr Verhalten im Test.
    @Mock
    private ApiCall apicall;
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

    // @InjectMocks erstellt eine Instanz der zu testenden Klasse
    // und injiziert automatisch alle mit @Mock erstellten Objekte.
    @InjectMocks
    private SendBillToCospend sendBillToCospend;

    // Hilfsvariablen für unsere Tests
    private MapCategory mockCategory;
    private Transaktion neueTransaktion;
    private Konto konto;
    private MapMember mockPayer;
    private MapMember mockPayedFor;
    private MapKonto mockMapKonto;

    @BeforeEach
    void setUp() {
        mockCategory = new MapCategory();
        mockCategory.setProjectname("test-project");
        mockCategory.setBudgetCategory(89); // Erlaubte Kategorie
        mockCategory.setCospendCategory(200);
        mockCategory.setInout(1);
        mockCategory.setKind(2);

        neueTransaktion = new Transaktion();
        neueTransaktion.setId(1);
        neueTransaktion.setName("Test-Einkauf");
        neueTransaktion.setWert(50.0);
        neueTransaktion.setBeschreibung("Wocheneinkauf");
        neueTransaktion.setDatum("2023-10-27");
        neueTransaktion.setKonto_id(32); // Erlaubtes Konto
        neueTransaktion.setKategorie(89); // Erlaubte Kategorie

        konto = new Konto();
        konto.setId(32); // <-- Wichtig: Muss mit neueTransaktion.getKonto_id() übereinstimmen!
        konto.setKontoname("Geldkonto");

        mockPayer = new MapMember();
        mockPayer.setCospendMemberId(58);

        mockPayedFor = new MapMember();
        mockPayedFor.setCospendMemberId(59);

        mockMapKonto = new MapKonto();
        mockMapKonto.setCospendKonto(4);
    }

    @Test
    void testGetMissingTransactions_sendsNewBill_whenTransactionIsNew() throws Exception {
        // Arrange
        when(mapCategoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(apicall.getKontoWithAnlegeart("Geldkonto")).thenReturn(List.of(konto));
        when(apicall.getTransactionWithCategoryAndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(neueTransaktion));
        when(compareRepository.findByBudgetTransIdAndProjectId(neueTransaktion.getId(), mockCategory.getProjectname()))
                .thenReturn(null);
        when(mapMemberRepository.findByNameAndProject("Hausverwaltung", mockCategory.getProjectname()))
                .thenReturn(mockPayer);
        when(mapMemberRepository.findByNameAndProject("Mieter", mockCategory.getProjectname()))
                .thenReturn(mockPayedFor);
        when(kontoService.getMapKontoOrDefault(anyInt(), anyString())).thenReturn(mockMapKonto);
        when(apicall.sendBill(any(MultiValueMap.class), anyString())).thenReturn(999);
        when(compareRepository.findByIsChecked(0)).thenReturn(Collections.emptyList());

        // Act
        sendBillToCospend.getMissingTransactionsAndSendToCospend();

        // Assert
        verify(apicall, times(1)).sendBill(any(MultiValueMap.class), eq("test-project"));
        verify(compareRepository, times(1)).save(any(TransactionIds.class));
        verify(transaktionRepository, times(1)).save(any(Transaktion.class));
        verify(apicall, never()).updateBill(any(), any(), any());
        verify(apicall, never()).deleteBill(any(), any());
    }

    @Test
    void testGetMissingTransactions_updatesExistingBill_whenTransactionHasChanged() throws Exception {
        // Arrange
        Transaktion gespeicherteTransaktion = new Transaktion();
        gespeicherteTransaktion.setId(1);
        gespeicherteTransaktion.setName("Test-Einkauf");
        gespeicherteTransaktion.setWert(45.0); // Anderer Wert
        gespeicherteTransaktion.setBeschreibung("Wocheneinkauf");
        gespeicherteTransaktion.setDatum("2023-10-27");
        gespeicherteTransaktion.setKonto_id(32);
        gespeicherteTransaktion.setKategorie(89); // Gleiche erlaubte Kategorie

        TransactionIds existingTransactionIds = new TransactionIds();
        existingTransactionIds.setBudgetTransId(1);
        existingTransactionIds.setNextcloudBillId(998);
        existingTransactionIds.setProjectId("test-project");

        when(mapCategoryRepository.findAll()).thenReturn(List.of(mockCategory));
        when(apicall.getKontoWithAnlegeart("Geldkonto")).thenReturn(List.of(konto));
        when(apicall.getTransactionWithCategoryAndDate(anyString(), anyString(), anyString()))
                .thenReturn(List.of(neueTransaktion));
        when(compareRepository.findByBudgetTransIdAndProjectId(neueTransaktion.getId(), mockCategory.getProjectname()))
                .thenReturn(existingTransactionIds);
        when(transaktionRepository.findById(existingTransactionIds.getBudgetTransId()))
                .thenReturn(Optional.of(gespeicherteTransaktion));
        when(mapMemberRepository.findByNameAndProject("Hausverwaltung", mockCategory.getProjectname()))
                .thenReturn(mockPayer);
        when(mapMemberRepository.findByNameAndProject("Mieter", mockCategory.getProjectname()))
                .thenReturn(mockPayedFor);
        when(kontoService.getMapKontoOrDefault(anyInt(), anyString())).thenReturn(mockMapKonto);
        when(compareRepository.findByIsChecked(0)).thenReturn(Collections.emptyList());
        when(compareRepository.findByBudgetTransIdAndIsChecked(anyInt(), eq(0)))
                .thenReturn(Collections.emptyList());

        // Act
        sendBillToCospend.getMissingTransactionsAndSendToCospend();

        // Assert
        verify(apicall, times(1)).updateBill(any(MultiValueMap.class),
                eq("/" + existingTransactionIds.getNextcloudBillId()),
                eq(mockCategory.getProjectname()));
        verify(apicall, never()).sendBill(any(MultiValueMap.class), anyString());
        verify(compareRepository, times(1)).save(existingTransactionIds);
        verify(transaktionRepository, times(1)).save(neueTransaktion);
    }
    @Test
    void testGetMissingTransactions_deletesBill_whenTransactionIsGone() {
        // ARRANGE
        // 1. mapCategoryRepository.findAll() gibt eine Kategorie zurück.
        // 2. apicall.getTransactionWithCategoryAndDate() gibt eine LEERE Liste zurück (die Transaktion existiert nicht mehr in der Quelle).
        // 3. compareRepository.findByIsChecked(0) gibt eine Liste mit dem zu löschenden TransactionIds-Objekt zurück.
        // 4. Für die deleteSaveTransaktion-Methode: compareRepository.findByBudgetTransId gibt eine leere Liste zurück.
        // ...

        // ACT
        // ...

        // ASSERT
        // Überprüfe, ob apicall.deleteBill() und compareRepository.delete() aufgerufen wurden.
        // verify(apicall, times(1)).deleteBill(anyString(), anyString());
        // verify(compareRepository, times(1)).delete(any(TransactionIds.class));
        // verify(transaktionRepository, times(1)).deleteById(anyInt());
    }
}

