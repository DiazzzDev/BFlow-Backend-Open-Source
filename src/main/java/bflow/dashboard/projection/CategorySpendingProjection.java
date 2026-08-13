package bflow.dashboard.projection;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection containing aggregated spending information by category.
 */
public interface CategorySpendingProjection {

    /**
     * Returns the category identifier.
     *
     * @return the category identifier
     */
    UUID getCategoryId();

    /**
     * Returns the category name.
     *
     * @return the category name
     */
    String getCategoryName();

    /**
     * Returns the total spending for the category.
     *
     * @return the total spending
     */
    BigDecimal getTotal();
}
