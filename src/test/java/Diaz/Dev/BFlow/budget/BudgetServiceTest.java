package Diaz.Dev.BFlow.budget;

import bflow.auth.services.UserServiceImpl;
import bflow.budget.DTO.BudgetResponse;
import bflow.budget.DTO.BudgetSearchCriteria;
import bflow.budget.RepositoryBudget;
import bflow.budget.entity.Budget;
import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.PeriodType;
import bflow.budget.specifications.BudgetSpecifications;
import bflow.budget.services.BudgetAlertService;
import bflow.budget.services.BudgetCalculationService;
import bflow.budget.services.BudgetService;
import bflow.notifications.service.NotificationService;
import bflow.wallet.RepositoryWalletUser;
import bflow.wallet.entities.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

/** Unit tests for budget search behavior. */
@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private RepositoryBudget repositoryBudget;
    @Mock
    private BudgetCalculationService calculationService;
    @Mock
    private BudgetAlertService alertService;
    @Mock
    private RepositoryWalletUser repositoryWalletUser;
    @Mock
    private UserServiceImpl userService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BudgetService budgetService;

    @Test
    void searchBudgetsUsesOneSpecificationQueryAndPreservesPagination() {
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(1, 10);
        Budget budget = new Budget();
        budget.setWallet(new Wallet());
        BudgetResponse response = new BudgetResponse();
        response.setName("Holiday");
        Page<Budget> budgetPage = new PageImpl<>(List.of(budget), pageable, 11);

        doNothing().when(userService).validateUserActive(userId);
        when(repositoryBudget.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(budgetPage);
        when(calculationService.calculateAll(budgetPage.getContent()))
                .thenReturn(List.of(response));

        Page<BudgetResponse> result = budgetService.searchBudgets(
                userId, new BudgetSearchCriteria(), pageable);

        ArgumentCaptor<Specification<Budget>> specificationCaptor =
                ArgumentCaptor.forClass(Specification.class);
        verify(repositoryBudget).findAll(specificationCaptor.capture(),
                eq(pageable));
        assertEquals(11, result.getTotalElements());
        assertEquals("Holiday", result.getContent().getFirst().getName());
        assertEquals(pageable, result.getPageable());
    }

    /**
     * Verifies that the specification always scopes results to the owner and
     * applies a normalized, case-insensitive name predicate alongside optional
     * filters. This protects the API boundary from cross-user data exposure.
     */
    @SuppressWarnings("unchecked")
    @Test
    void searchSpecificationScopesOwnerAndNormalizesNameFilter() {
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        BudgetSearchCriteria criteria = new BudgetSearchCriteria();
        criteria.setName("  HoLiDaY  ");
        criteria.setWalletId(walletId);
        criteria.setPeriod(PeriodType.MONTHLY);
        criteria.setScope(BudgetScope.WALLET);

        Root<Budget> root = mock(Root.class);
        Path<Object> userPath = mock(Path.class);
        Path<Object> userIdPath = mock(Path.class);
        Path<String> namePath = mock(Path.class);
        Expression<String> lowerName = mock(Expression.class);
        Path<Object> walletPath = mock(Path.class);
        Path<Object> walletIdPath = mock(Path.class);
        Path<Object> periodPath = mock(Path.class);
        Path<Object> scopePath = mock(Path.class);
        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("id")).thenReturn(userIdPath);
        when(root.get("name")).thenReturn((Path) namePath);
        when(root.get("wallet")).thenReturn(walletPath);
        when(walletPath.get("id")).thenReturn(walletIdPath);
        when(root.get("period")).thenReturn(periodPath);
        when(root.get("scope")).thenReturn(scopePath);
        when(builder.lower(namePath)).thenReturn(lowerName);
        when(builder.equal(
        any(Expression.class), any(Object.class)))
        .thenReturn(predicate);
        when(builder.like(lowerName, "%holiday%")).thenReturn(predicate);
        when(builder.and(any(Predicate.class), any(Predicate.class)))
                .thenReturn(predicate);

        Specification<Budget> specification = BudgetSpecifications.from(
                criteria, userId);
        specification.toPredicate(root, query, builder);

        verify(builder).equal(userIdPath, userId);
        verify(builder).like(lowerName, "%holiday%");
        verify(builder).equal(walletIdPath, walletId);
        verify(builder).equal(periodPath, PeriodType.MONTHLY);
        verify(builder).equal(scopePath, BudgetScope.WALLET);
    }
}
