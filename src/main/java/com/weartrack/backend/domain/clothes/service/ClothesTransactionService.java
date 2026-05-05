package com.weartrack.backend.domain.clothes.service;

import com.weartrack.backend.domain.clothes.dto.request.ClothesCreateRequest;
import com.weartrack.backend.domain.clothes.dto.response.ClothesCreateResponse;
import lombok.RequiredArgsConstructor;
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

}
