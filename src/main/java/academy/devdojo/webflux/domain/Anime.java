package academy.devdojo.webflux.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("anime_tb")
public class Anime {
    @Id
    private Integer id;

    @NotNull
    @NotBlank(message = "name cant be empty")
    private String name;
}
