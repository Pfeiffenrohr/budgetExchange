package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.MapMember;
import cam.lechner.budgetexchange.entity.TransactionIds;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompareRepository  extends JpaRepository<TransactionIds, Integer>, JpaSpecificationExecutor<TransactionIds>  {
    @Transactional
    @Modifying
    @Query("update TransactionIds ti set ti.isChecked = 0 ")
    void  setIscheckedTo0();
    List<TransactionIds> findByBudgetTransId (Integer budgetTransId);
    List<TransactionIds> findByNextcloudBillId (Integer nextcloudBillId);
    List<TransactionIds>  findByProjectId (String projectId);
    List<TransactionIds> findByIsChecked (Integer isChecked);
    TransactionIds findByBudgetTransIdAndProjectId (Integer budgetTransId, String projectId);
    List <TransactionIds> findByBudgetTransIdAndIsChecked (Integer budgetTransId, Integer IsChecked);
}
