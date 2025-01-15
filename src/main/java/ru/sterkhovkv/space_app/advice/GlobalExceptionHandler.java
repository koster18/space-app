package ru.sterkhovkv.space_app.advice;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.sterkhovkv.space_app.exception.WebRequestException;

import java.util.Objects;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    @ExceptionHandler(WebRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleWebRequestException(WebRequestException ex, Model model,
                                              HttpSession session) {
//        SkyMapForm skyMapForm = (SkyMapForm) session.getAttribute("skyMapForm");
//        model.addAttribute("skyMapForm", Objects.requireNonNullElseGet(skyMapForm, SkyMapForm::new));
//        model.addAttribute("errorMessage", ex.getMessage());
        return "skyMap";
    }
}
