package cam.lechner.budgetexchange.compare;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface MapCategoryRepository extends CrudRepository<MapCategory, Integer>, JpaSpecificationExecutor<MapCategory>  {

    MapCategory findByBudgetCategory (Integer budgetCategory );

}
