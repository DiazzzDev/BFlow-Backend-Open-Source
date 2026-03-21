package bflow.category;

import bflow.category.entity.Category;
import bflow.category.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryCategory extends JpaRepository<Category, UUID> {
    /**
     * Finds all categories by their type.
     *
     * @param type the category type to filter by
     * @return a list of categories matching the specified type
     */
    List<Category> findByType(CategoryType type);

    /**
     * Finds all system-defined categories.
     *
     * @return a list of all system-defined categories
     */
    List<Category> findBySystemDefinedTrue();

    /**
     * Finds a category by its ID and type.
     *
     * @param id the unique identifier of the category
     * @param type the category type to match
     * @return an Optional containing the category if found
     */
    Optional<Category> findByIdAndType(UUID id, CategoryType type);
}
