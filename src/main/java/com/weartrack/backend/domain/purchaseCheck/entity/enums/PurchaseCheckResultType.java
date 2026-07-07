package com.weartrack.backend.domain.purchaseCheck.entity.enums;

public enum PurchaseCheckResultType {

    NO_REGISTERED_CLOTHES("아직 등록된 옷이 없어요. 옷을 먼저 등록해주세요."),
    HAS_SIMILAR_CLOTHES("이미 비슷한 옷이 %d벌 있어요!"),
    NO_SIMILAR_CLOTHES("비슷한 옷이 없어요. 새 옷을 들여도 좋을 것 같아요!"),
    ANALYSIS_FAILED("옷의 색상과 카테고리를 확인하지 못했어요. 다른 사진이나 링크로 다시 시도해주세요.");

    private final String message;

    PurchaseCheckResultType(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }

    public String message(long count) {
        return message.formatted(count);
    }
}
