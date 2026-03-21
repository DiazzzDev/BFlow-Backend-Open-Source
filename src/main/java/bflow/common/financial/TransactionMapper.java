package bflow.common.financial;

import bflow.category.DTO.CategoryResponse;
import bflow.category.entity.Category;

/**
 * Utility class for mapping financial transaction entities to DTOs.
 * Provides reusable mapping logic for category and transaction conversions.
 */
public final class TransactionMapper {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with static methods only.
     */
    private TransactionMapper() {
        throw new UnsupportedOperationException(
            "TransactionMapper is a utility class and cannot be instantiated"
        );
    }

    /**
     * Maps a Category entity to a CategoryResponse DTO.
     * Handles null values gracefully.
     *
     * @param category the category entity to map (can be null)
     * @return the mapped CategoryResponse or null if input is null
     */
    public static CategoryResponse mapCategoryToResponse(
            final Category category
    ) {
        if (category == null) {
            return null;
        }

        final CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setType(category.getType());
        response.setIcon(category.getIcon());
        response.setColor(category.getColor());

        return response;
    }
}
