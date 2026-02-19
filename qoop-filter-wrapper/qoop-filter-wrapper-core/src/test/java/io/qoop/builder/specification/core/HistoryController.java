package io.qoop.builder.specification.core;

import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.builder.specification.api.model.SortWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public ResponseEntity<?> getHistory(
            @RequestParam(name = "filter", required = false) FilterWrapper filter,
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "10") Integer limit) {

        return ResponseEntity.ok(historyService.findAll(filter, start, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) FilterWrapper filters,
            @RequestParam(required = false) SortWrapper sorts,
            @RequestParam(defaultValue = "0") Integer start,
            @RequestParam(defaultValue = "100") Integer limit) {

        return ResponseEntity.ok(historyService.findAll(filters, sorts, start, limit));
    }
}
