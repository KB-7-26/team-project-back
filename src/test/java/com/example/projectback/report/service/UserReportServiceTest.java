package com.example.projectback.report.service;

import com.example.projectback.chat.repository.ChatRoomRepository;
import com.example.projectback.entity.Product;
import com.example.projectback.entity.User;
import com.example.projectback.entity.UserReport;
import com.example.projectback.product.repository.ProductRepository;
import com.example.projectback.report.repository.UserReportRepository;
import com.example.projectback.security.CurrentUserProvider;
import com.example.projectback.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserReportServiceTest {

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private UserReportRepository userReportRepository;

    @InjectMocks
    private UserReportService userReportService;

    @Test
    void reportProductSeller_savesReport() {
        User reporter = mockUser(1L);
        User seller = mockUser(2L);
        Product product = mockProduct(10L, seller);

        given(currentUserProvider.getCurrentUser()).willReturn(reporter);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));
        given(userReportRepository.existsByReporterIdAndReportedUserIdAndProductId(1L, 2L, 10L))
                .willReturn(false);

        userReportService.reportProductUser(10L, null);

        ArgumentCaptor<UserReport> reportCaptor = ArgumentCaptor.forClass(UserReport.class);
        verify(userReportRepository).save(reportCaptor.capture());

        UserReport savedReport = reportCaptor.getValue();
        assertThat(savedReport.getReporter()).isEqualTo(reporter);
        assertThat(savedReport.getReportedUser()).isEqualTo(seller);
        assertThat(savedReport.getProduct()).isEqualTo(product);
    }

    @Test
    void reportProductSeller_duplicateReportDoesNotSave() {
        User reporter = mockUser(1L);
        User seller = mockUser(2L);
        Product product = mockProduct(10L, seller);

        given(currentUserProvider.getCurrentUser()).willReturn(reporter);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));
        given(userReportRepository.existsByReporterIdAndReportedUserIdAndProductId(1L, 2L, 10L))
                .willReturn(true);

        userReportService.reportProductUser(10L, null);

        verify(userReportRepository, never()).save(any(UserReport.class));
    }

    @Test
    void reportProductSeller_selfReportThrowsException() {
        User reporter = mockUser(1L);
        Product product = mockProduct(10L, reporter);

        given(currentUserProvider.getCurrentUser()).willReturn(reporter);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));

        assertThatThrownBy(() -> userReportService.reportProductUser(10L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("본인은 신고할 수 없습니다.");

        verify(userReportRepository, never()).save(any(UserReport.class));
    }

    @Test
    void reportProductUser_sellerCanReportProductBuyer() {
        User seller = mockUser(1L);
        User buyer = mockUser(2L);
        Product product = mockProduct(10L, seller);

        given(currentUserProvider.getCurrentUser()).willReturn(seller);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));
        given(userRepository.findById(2L)).willReturn(Optional.of(buyer));
        given(chatRoomRepository.existsByProductIdAndBuyerId(10L, 2L)).willReturn(true);
        given(userReportRepository.existsByReporterIdAndReportedUserIdAndProductId(1L, 2L, 10L))
                .willReturn(false);

        userReportService.reportProductUser(10L, 2L);

        ArgumentCaptor<UserReport> reportCaptor = ArgumentCaptor.forClass(UserReport.class);
        verify(userReportRepository).save(reportCaptor.capture());

        UserReport savedReport = reportCaptor.getValue();
        assertThat(savedReport.getReporter()).isEqualTo(seller);
        assertThat(savedReport.getReportedUser()).isEqualTo(buyer);
        assertThat(savedReport.getProduct()).isEqualTo(product);
    }

    @Test
    void reportProductUser_rejectsUserOutsideProductChat() {
        User reporter = mockUser(1L);
        User seller = mockUser(2L);
        User unrelatedUser = mockUser(3L);
        Product product = mockProduct(10L, seller);

        given(currentUserProvider.getCurrentUser()).willReturn(reporter);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));
        given(userRepository.findById(3L)).willReturn(Optional.of(unrelatedUser));
        given(chatRoomRepository.existsByProductIdAndBuyerId(10L, 3L)).willReturn(false);

        assertThatThrownBy(() -> userReportService.reportProductUser(10L, 3L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 거래의 신고 대상이 아닙니다.");

        verify(userReportRepository, never()).save(any(UserReport.class));
    }

    private User mockUser(Long id) {
        User user = org.mockito.Mockito.mock(User.class);
        given(user.getId()).willReturn(id);
        return user;
    }

    private Product mockProduct(Long id, User seller) {
        Product product = org.mockito.Mockito.mock(Product.class);
        lenient().when(product.getId()).thenReturn(id);
        given(product.getSeller()).willReturn(seller);
        return product;
    }
}
