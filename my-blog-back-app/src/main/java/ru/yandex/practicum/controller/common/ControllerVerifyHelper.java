package ru.yandex.practicum.controller.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.yandex.practicum.exceptions.base.ItemNotFoundException;

@Component
public class ControllerVerifyHelper {

    private static final String INT_MORE_ZERO_TEMPLATE = "`%s` cannot be less than 1";
    private static final String EMPTY_IMAGE_EXCEPTION = "Image cannot be null or empty";
    private static final String DIFFERENT_ID_IN_PARAM_AND_DTO_EXCEPTION = "Id in param and in dto must be the same";

    public void ensurePaginationParamsCorrect(int pageNumber, int pageSize) {
        ensureIntParamPositive("pageNumber", pageNumber);
        ensureIntParamPositive("pageSize", pageSize);
    }

    public void ensureIntParamPositive(String paramName, long paramValue) {
        if (paramValue < 1) {
            throw new IllegalArgumentException(INT_MORE_ZERO_TEMPLATE.formatted(paramName));
        }
    }

    public void ensureImageValid(MultipartFile image) {
        if (image == null || image.isEmpty() || image.getSize() == 0) {
            throw new IllegalArgumentException(EMPTY_IMAGE_EXCEPTION);
        }
    }

    public void ensureParamIdAndDtoIdEqual(long paramId, long dtoId) {
        if (paramId != dtoId) {
            throw new IllegalArgumentException(DIFFERENT_ID_IN_PARAM_AND_DTO_EXCEPTION);
        }
    }
}
