package bflow.income;

import bflow.income.entity.Income;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RepositoryIncome extends JpaRepository<Income, UUID> {

}
