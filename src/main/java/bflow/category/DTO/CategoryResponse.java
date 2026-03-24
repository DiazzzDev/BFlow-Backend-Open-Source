package bflow.category.DTO;

import bflow.category.enums.CategoryType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CategoryResponse {
    /**
     * The unique identifier of the category.
     */
    private UUID id;

    /**
     * The name of the category.
     */
    private String name;

    /**
     * The type of category (INCOME, EXPENSE, or TRANSFER).
     */
    private CategoryType type;

    /**
     * The icon identifier for the category.
     */
    private String icon;

    /**
     * The color code for the category.
     */
    private String color;
}
