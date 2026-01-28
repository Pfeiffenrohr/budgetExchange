package cam.lechner.budgetexchange.service; // Beispielhafter Paketname

import cam.lechner.budgetexchange.application.MapKontoRepository;
import cam.lechner.budgetexchange.entity.MapKonto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KontoService {

    private final MapKontoRepository mapKontoRepository;

    @Autowired
    public KontoService(MapKontoRepository mapKontoRepository) {
        this.mapKontoRepository = mapKontoRepository;
    }

    /**
     * Sucht ein MapKonto anhand des budgetKonto.
     * Wenn kein Eintrag gefunden wird, wird ein neues MapKonto-Objekt
     * mit cospendKonto = 0 zurückgegeben.
     *
     * @param budgetKonto Die ID des zu suchenden Budget-Kontos.
     * @return Das gefundene MapKonto oder ein Standard-Objekt.
     */
    public MapKonto getMapKontoOrDefault(Integer budgetKonto, String projectname) {
        // Rufe die Repository-Methode auf, die ein Optional zurückgibt
        return mapKontoRepository.findByBudgetKontoAndProjectname(budgetKonto,projectname)
                .orElseGet(() -> {
                    // Dieser Code wird nur ausgeführt, wenn das Optional leer ist (also nichts gefunden wurde)
                    MapKonto defaultKonto = new MapKonto();
                    defaultKonto.setBudgetKonto(budgetKonto); // Sinnvoll, den Suchwert zu übernehmen
                    defaultKonto.setCospendKonto(0); // Hier setzen wir den Standardwert
                    // Die anderen Felder wie 'id' und 'projectname' bleiben null/default
                    return defaultKonto;
                });
    }
}