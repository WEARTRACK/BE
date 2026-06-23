package com.weartrack.backend.domain.fashionReport.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.weartrack.backend.domain.clothes.entity.Clothes;
import com.weartrack.backend.domain.clothes.repository.ClothesRepository;
import com.weartrack.backend.domain.fashionReport.dto.response.WeeklyFashionConsumptionReportResDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FashionConsumptionReportServiceTest {

    @Mock
    private ClothesRepository clothesRepository;

    @InjectMocks
    private FashionConsumptionReportService fashionConsumptionReportService;

    @Test
    @DisplayName("주간 패션 소비 리포트는 해당 주 등록 옷 가격과 카테고리별 지출을 집계한다.")
    void getWeeklyReport() {
        Long memberId = 1L;
        LocalDate weekStartDate = LocalDate.of(2026, 6, 21);
        given(clothesRepository.findAllByMemberId(memberId)).willReturn(List.of(
                createClothes(1L, "T-Shirt", 89_000, LocalDateTime.of(2026, 6, 23, 12, 0)),
                createClothes(2L, "Shirt", 116_000, LocalDateTime.of(2026, 6, 14, 12, 0)),
                createClothes(3L, "Pants", 30_000, LocalDateTime.of(2026, 6, 28, 12, 0))
        ));

        WeeklyFashionConsumptionReportResDto response =
                fashionConsumptionReportService.getWeeklyReport(memberId, weekStartDate);

        assertThat(response.weekStartDate()).isEqualTo(weekStartDate);
        assertThat(response.weekEndDate()).isEqualTo(LocalDate.of(2026, 6, 27));
        assertThat(response.totalExpenseAmount()).isEqualTo(89_000);
        assertThat(response.expenseChangeRate()).isEqualTo(-23L);
        assertThat(response.categories()).extracting("category")
                .startsWith("T-SHIRT", "SHIRT", "KNIT", "HOODIE");
        assertThat(response.categories().get(0).expenseAmount()).isEqualTo(89_000);
        assertThat(response.categories().get(1).expenseAmount()).isZero();
    }

    @Test
    @DisplayName("전주 지출이 없고 이번 주 지출이 있으면 전주 대비 증감률은 null로 반환한다.")
    void getWeeklyReportReturnsNullChangeRateWhenPreviousExpenseIsZero() {
        Long memberId = 1L;
        LocalDate weekStartDate = LocalDate.of(2026, 6, 21);
        given(clothesRepository.findAllByMemberId(memberId)).willReturn(List.of(
                createClothes(1L, "Knit", 50_000, LocalDateTime.of(2026, 6, 21, 12, 0))
        ));

        WeeklyFashionConsumptionReportResDto response =
                fashionConsumptionReportService.getWeeklyReport(memberId, weekStartDate);

        assertThat(response.totalExpenseAmount()).isEqualTo(50_000);
        assertThat(response.expenseChangeRate()).isNull();
    }

    private Clothes createClothes(
            Long id,
            String category,
            Integer price,
            LocalDateTime createdAt
    ) {
        return Clothes.builder()
                .id(id)
                .clothesPhotoId(1L)
                .closetSectionId(1L)
                .imageUrl("https://example.com/clothes-" + id + ".jpg")
                .color("BLUE")
                .category(category)
                .price(price)
                .createdAt(createdAt)
                .build();
    }
}
