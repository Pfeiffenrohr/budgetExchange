package cam.lechner.budgetexchange.entity;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
public class Konto {
    private Integer id;
    private String kontoname;
    private String hidden;
    private Double upperlimit;
    private Double lowerlimit;
    private String description;
    private String mode;
    private Integer rule_id;
}
