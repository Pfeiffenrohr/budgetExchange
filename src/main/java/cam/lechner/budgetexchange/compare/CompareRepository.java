package cam.lechner.budgetexchange.compare;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CompareRepository  extends CrudRepository<TransactionIds, Integer>, JpaSpecificationExecutor<TransactionIds>  {

    TransactionIds findByBudgetTransId (Integer budgetTransId);
}
