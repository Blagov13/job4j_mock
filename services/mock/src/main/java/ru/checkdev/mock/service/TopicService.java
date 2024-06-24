package ru.checkdev.mock.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import ru.checkdev.mock.dto.TopicIdNameDTO;

import java.util.List;

@Service
public class TopicService {
    public List<TopicIdNameDTO> getByCategory(int id) throws JsonProcessingException {
        var text = new RestAuthCall("http://localhost:9902/topics/" + id).get();
        var mapper = new ObjectMapper();
        return mapper.readValue(text, new TypeReference<>() {
        });
    }
}
