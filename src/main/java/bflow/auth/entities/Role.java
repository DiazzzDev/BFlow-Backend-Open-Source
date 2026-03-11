package bflow.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.UUID;

/**
 * Entity representing a security role.
 */
@Entity
@Table(
        name = "roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name")
        }
)
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class Role {

    /** Maximum length for the role name. */
    private static final int MAX_NAME_LENGTH = 50;

    /** Unique identifier for the role. */
    @Id
    @GeneratedValue
    private UUID id;

    /** The name of the role. */
    @Column(nullable = false, length = MAX_NAME_LENGTH)
    private String name;
}
