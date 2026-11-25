package com.example.app.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/** 스프링 컨텍스트 없이(standalone) 전역 예외 처리 응답 형식을 검증한다. */
class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(new TestController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @Test
  @DisplayName("BusinessException은 ErrorCode의 상태와 코드로 응답한다")
  void businessException_respondsWithErrorCodeStatus() throws Exception {
    mockMvc
        .perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("C002"))
        .andExpect(jsonPath("$.message").value("요청한 리소스를 찾을 수 없습니다."));
  }

  @Test
  @DisplayName("검증 실패는 400과 필드 에러 목록으로 응답한다")
  void validationFailure_respondsWith400AndFieldErrors() throws Exception {
    String emptyName = "{\"name\":\"\"}";

    mockMvc
        .perform(post("/test/validate").contentType(MediaType.APPLICATION_JSON).content(emptyName))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("C001"))
        .andExpect(jsonPath("$.errors[0].field").value("name"));
  }

  @Test
  @DisplayName("예상치 못한 예외는 500으로 응답한다")
  void unexpectedException_respondsWith500() throws Exception {
    mockMvc
        .perform(get("/test/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("C999"));
  }

  @RestController
  static class TestController {

    @GetMapping("/test/not-found")
    void notFound() {
      throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
    }

    @PostMapping("/test/validate")
    void validate(@Valid @RequestBody NameRequest request) {}

    @GetMapping("/test/boom")
    void boom() {
      throw new IllegalStateException("boom");
    }
  }

  record NameRequest(@NotBlank String name) {}
}
