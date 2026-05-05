package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.request.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.request.ClothesUpdateRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import com.weartrack.backend.domain.clothes.dto.response.ClothesDetailResDto;
import com.weartrack.backend.domain.clothes.dto.response.ClothesFilterResDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClothesTransactionService {

    private final ClothesService clothesService;

    @Retryable(
            retryFor =
                    ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier
                    = 2)
    )
    public ClothesCreateResponse createClothes(Long memberId, ClothesCreateRequest request){
        return clothesService.createClothes(memberId,request);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    public ClothesDetailResDto updateClothes(
            Long memberId, Long clothesId, ClothesUpdateRequest request) {
        return clothesService.updateClothes(memberId, clothesId, request);
    }

    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 50, multiplier = 2)
    )
    public void deleteClothes(Long memberId, Long clothesId) {
        clothesService.deleteClothes(memberId, clothesId);
    }

    public ClothesDetailResDto getClothesDetail(Long memberId, Long clothesId) {
        return clothesService.getClothesDetail(memberId, clothesId);
    }

    public ClothesFilterResDto filterClothes(
            Long memberId, String color, String category, Pageable pageable) {
        return clothesService.filterClothes(memberId, color, category, pageable);
    }

}
