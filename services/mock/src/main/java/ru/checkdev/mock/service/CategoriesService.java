package ru.checkdev.mock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.checkdev.mock.domain.Interview;
import ru.checkdev.mock.dto.TopicIdNameDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Service
public class CategoriesService {
    private final TopicService topicService;
    private final InterviewService interviewService;

    public Map<Integer, Long> getInterviewCount(List<Integer> catIds) throws JsonProcessingException {
        Map<Integer, Long> newCategories = new ConcurrentHashMap<>();
        for (var catId : catIds) {
            List<TopicIdNameDTO> topicIdNameDTOS = topicService.getByCategory(catId);
            List<Interview> interviews = interviewService.findByTopicsIds(
                    topicIdNameDTOS.stream()
                            .map(TopicIdNameDTO::getId)
                            .toList());
            long totalElements = interviews.size();
            newCategories.put(catId, totalElements);
        }
        return newCategories;
    }
}
