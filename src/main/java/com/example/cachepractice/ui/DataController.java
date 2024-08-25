package com.example.cachepractice.ui;

import com.example.cachepractice.application.DataService;
import com.example.cachepractice.domain.MockData;
import com.example.cachepractice.ui.request.IdRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class DataController {

    private final DataService dataService;

    @PostMapping("/items")
    public List<MockData> getItems(@RequestBody IdRequest request) {
        // AOP 어드바이스에서 idRequest 객체의 ids 리스트가 이미 가공됨
        List<Long> ids = request.getIds();

        return dataService.getDataByIds(ids);
    }

}
