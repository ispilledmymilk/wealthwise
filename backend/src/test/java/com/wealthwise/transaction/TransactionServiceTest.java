package com.wealthwise.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.wealthwise.common.exception.ResourceNotFoundException;
import com.wealthwise.user.User;
import com.wealthwise.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void createTransaction_validRequest_returnsDTO() {
        User user = User.builder().id(1L).email("test@test.com").build();
        CreateTransactionRequest request =
                new CreateTransactionRequest(new BigDecimal("50.00"), CategoryType.DINING, "Lunch", LocalDate.now());

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TransactionDTO result = transactionService.create("test@test.com", request);

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getAmount());
        assertEquals(CategoryType.DINING, result.getCategory());
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void createTransaction_userNotFound_throwsException() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());
        CreateTransactionRequest request =
                new CreateTransactionRequest(new BigDecimal("10.00"), CategoryType.DINING, "x", LocalDate.now());

        assertThrows(
                ResourceNotFoundException.class, () -> transactionService.create("unknown@test.com", request));
    }
}
