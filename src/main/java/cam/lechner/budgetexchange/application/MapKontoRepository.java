package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.MapKonto;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional; // Wichtig: Optional importieren

public interface MapKontoRepository extends CrudRepository<MapKonto, Integer>, JpaSpecificationExecutor<MapKonto> {

    // Gib ein Optional zurück, um den Fall "nicht gefunden" explizit zu machen
    Optional<MapKonto> findByBudgetKontoAndProject (Integer budgetKonto, String projectname);
}