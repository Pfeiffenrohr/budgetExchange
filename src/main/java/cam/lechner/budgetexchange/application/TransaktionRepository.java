package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.MapCategory;
import cam.lechner.budgetexchange.entity.Transaktion;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface TransaktionRepository extends CrudRepository<Transaktion, Integer>, JpaSpecificationExecutor<Transaktion>  {

}
