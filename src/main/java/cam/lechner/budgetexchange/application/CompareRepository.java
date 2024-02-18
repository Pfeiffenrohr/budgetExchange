package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.TransactionIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface CompareRepository  extends JpaRepository<TransactionIds, Integer>, JpaSpecificationExecutor<TransactionIds>  {
    TransactionIds findByBudgetTransId (Integer budgetTransId);
}
