package cam.lechner.budgetexchange.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;


import java.util.Date;

@Getter
@Setter
@Configuration
public class Kategorie {

    private Integer id;
    private String name;
    private String parent;
    private String description;
    private Double limit_month;
    private Double limit_year;
    private String mode;
    private Integer active;
    private Integer forecast;
    private Integer inflation;
}
