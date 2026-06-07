package nutc.sot.farm_quest.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.qdrant")
public class QdrantProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String collectionName;

    @Min(1)
    private int vectorSize;

    private boolean initializeOnStartup;
}
