package com.example.cachepractice.ui;

import com.example.cachepractice.annotation.Cached;
import com.example.cachepractice.application.DataService;
import com.example.cachepractice.domain.MockData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DataController {

    private final DataService dataService;

    @Cached(key = "data")
    @GetMapping("/items")
    public List<MockData> getItems(@RequestParam("ids") String idString) {
        return dataService.getDataByIds(idString);
    }

}
