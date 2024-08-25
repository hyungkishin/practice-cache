package com.example.cachepractice.ui;

import com.example.cachepractice.application.DataService;
import com.example.cachepractice.domain.MockData;
import com.example.cachepractice.ui.request.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
public class DataController {

    private final DataService dataService;

    @GetMapping("/items")
    public List<MockData> getItems(@RequestParam("ids") String idString) {
        // AOP 어드바이스에서 idRequest 객체의 ids 리스트가 이미 가공됨
        List<Long> ids = Arrays.stream(idString.split(","))
                .map(Long::valueOf)
                .toList();

        return dataService.getDataByIds(ids);
    }

}
