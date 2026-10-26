package com.pilates.booking.service.impl;

import com.pilates.booking.domain.dto.ConfirmationResponse;
import com.pilates.booking.domain.exceptions.BookingNotFoundException;
import com.pilates.booking.domain.models.Booking;
import com.pilates.booking.domain.models.PilatesClass;
import com.pilates.booking.repository.BookingRepository;
import com.pilates.booking.service.ConfirmationService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationServiceImpl.class);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final String INSTRUCTIONS = """
            You write booking confirmations for a small pilates studio.
            Write a short, warm confirmation of 2 sentences (max 40 words) addressed to the client by first name.
            Mention the class name, day and start time exactly as given. Plain text only: no markdown, no emojis,
            no subject line, no signature. Do not invent details such as the address, price or what to bring.
            """;

    private final BookingRepository bookings;
    private final RestClient gemini;
    private final String apiKey;
    private final String model;

    public ConfirmationServiceImpl(BookingRepository bookings, RestClient geminiRestClient,
                                   @Value("${gemini.api-key}") String apiKey,
                                   @Value("${gemini.model}") String model) {
        this.bookings = bookings;
        this.gemini = geminiRestClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    @Override
    public ConfirmationResponse confirmationFor(Long classId, Long bookingId) {
        Booking booking = bookings.findWithClass(bookingId, classId).orElseThrow(BookingNotFoundException::new);
        if (!apiKey.isBlank()) {
            try {
                String text = generate(prompt(booking));
                if (!text.isBlank()) {
                    return new ConfirmationResponse(text, true);
                }
                log.warn("Gemini returned an empty confirmation for booking {}", bookingId);
            } catch (RestClientException e) {
                log.warn("Gemini call failed for booking {}: {}", bookingId, e.getMessage());
            }
        }
        return new ConfirmationResponse(fallback(booking), false);
    }

    private String prompt(Booking b) {
        PilatesClass c = b.getPilatesClass();
        return "Client first name: %s%nClass: %s%nDay: %s%nStart time: %s%nDuration: %d minutes".formatted(
                firstName(b), c.getTitle(), c.getStartTime().format(DAY), c.getStartTime().format(TIME),
                c.getDurationMinutes());
    }

    private String generate(String prompt) {
        Map<String, Object> body = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", INSTRUCTIONS))),
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))));
        GeminiResponse res = gemini.post()
                .uri("/models/{model}:generateContent", model)
                .body(body)
                .retrieve()
                .body(GeminiResponse.class);
        if (res == null || res.candidates() == null || res.candidates().isEmpty()) {
            return "";
        }
        GeminiResponse.Content content = res.candidates().get(0).content();
        if (content == null || content.parts() == null) {
            return "";
        }
        return content.parts().stream()
                .filter(p -> !Boolean.TRUE.equals(p.thought()))
                .map(GeminiResponse.Part::text)
                .filter(Objects::nonNull)
                .collect(Collectors.joining())
                .trim();
    }

    private String fallback(Booking b) {
        PilatesClass c = b.getPilatesClass();
        return "You're booked for %s on %s at %s, %s. See you on the mat!".formatted(
                c.getTitle(), c.getStartTime().format(DAY), c.getStartTime().format(TIME), firstName(b));
    }

    private static String firstName(Booking b) {
        return b.getName().split("\\s+")[0];
    }

    record GeminiResponse(List<Candidate> candidates) {
        record Candidate(Content content) {}
        record Content(List<Part> parts) {}
        record Part(String text, Boolean thought) {}
    }
}
