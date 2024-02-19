package cam.lechner.budgetexchange.application;

import cam.lechner.budgetexchange.entity.MapCategory;
import cam.lechner.budgetexchange.entity.MapMember;
import cam.lechner.budgetexchange.entity.TransactionIds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MapMemberRepository extends JpaRepository<MapMember, Integer>, JpaSpecificationExecutor<MapMember>  {

    MapMember findByNameAndProject (String name,String project);

}
