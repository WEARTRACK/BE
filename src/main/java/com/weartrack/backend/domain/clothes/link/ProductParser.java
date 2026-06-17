package com.weartrack.backend.domain.clothes.link;

import com.weartrack.backend.domain.clothes.entity.SourceShop;

public interface ProductParser {

    boolean supports(SourceShop sourceShop);

    ProductParseResult parse(String html, String pageUrl);
}
