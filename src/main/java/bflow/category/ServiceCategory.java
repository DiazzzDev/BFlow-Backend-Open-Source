package bflow.category;

import bflow.category.DTO.CategoryRequest;
import bflow.category.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service class for managing category operations.
 * Provides business logic for creating and retrieving financial transaction
 * categories.
 */
@Service
@RequiredArgsConstructor
public class ServiceCategory {

    /**
     * Repository for category database operations.
     */
    private final RepositoryCategory repositoryCategory;

    /**
     * Creates a new category from the provided request.
     * Initializes system-defined flag to false for user-created categories.
     *
     * @param request the category request containing category details
     * @return the created category entity
     */
    public Category create(final CategoryRequest request) {

        Category category = new Category();
        category.setName(request.getName());
        category.setType(request.getType());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setSystemDefined(false);
        category.setCreatedAt(Instant.now());

        return repositoryCategory.save(category);
    }

    /**
     * Retrieves all categories from the database.
     *
     * @return a list of all categories
     */
    public List<Category> findAll() {
        return repositoryCategory.findAll();
    }

}
