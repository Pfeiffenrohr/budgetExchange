package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.MapCategory;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MapCategoryRepository extends CrudRepository<MapCategory, Integer>, JpaSpecificationExecutor<MapCategory>  {

    MapCategory findByBudgetCategory (Integer budgetCategory );
    MapCategory findByBudgetCategoryAndProjectname (Integer budgetCategory,String projectname);
    @Query("select distinct m.projectname from MapCategory m")
    List<String> findDistinctProjectnames();

}
